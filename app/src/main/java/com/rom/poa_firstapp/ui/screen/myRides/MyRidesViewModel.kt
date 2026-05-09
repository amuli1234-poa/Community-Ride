package com.rom.poa_firstapp.ui.screen.myRides

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rom.poa_firstapp.data.model.BookingWithProfile
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.repository.NotificationRepository
import com.rom.poa_firstapp.data.repository.ProfileRepository
import com.rom.poa_firstapp.data.repository.RideRepository
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MyRidesViewModel(
    private val rideRepository: RideRepository,
    private val notificationRepository: NotificationRepository,
    private val profileRepository: ProfileRepository,
    private val userId: String?
) : ViewModel() {

    var selectedTab by mutableIntStateOf(0)
        private set

    var driverRides by mutableStateOf<List<Ride>>(emptyList())
        private set

    var passengerRides by mutableStateOf<List<Ride>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var unreadCount by mutableIntStateOf(0)
        private set

    var rideBookings by mutableStateOf<Map<String, List<BookingWithProfile>>>(emptyMap())
        private set

    var userBookings by mutableStateOf<Map<String, com.rom.poa_firstapp.data.model.Booking>>(emptyMap())
        private set

    var userType by mutableStateOf("passenger")
        private set

    init {
        loadUserProfile()
        refreshRides()
        loadUnreadCount()
        observeBookings()
        observeNotifications()
    }

    private fun observeNotifications() {
        if (userId == null) return
        notificationRepository.getNotificationsFlow(userId)
            .onEach { 
                loadUnreadCount() 
            }
            .launchIn(viewModelScope)
    }

    private fun observeBookings() {
        if (userId == null) return
        rideRepository.getBookingsFlow(userId)
            .onEach { refreshRides() } // Refresh everything when a booking changes
            .launchIn(viewModelScope)
    }

    private fun loadUserProfile() {
        userId?.let {
            viewModelScope.launch {
                val profile = profileRepository.getProfile(it)
                profile?.let { p ->
                    userType = p.user_type
                    // Set default tab based on user type
                    selectedTab = if (p.user_type == "driver") 0 else 1
                }
            }
        }
    }

    fun onTabChange(index: Int) {
        selectedTab = index
    }

    fun refreshRides(showLoading: Boolean = true) {
        userId?.let {
            viewModelScope.launch {
                if (showLoading) isLoading = true
                driverRides = rideRepository.getUserRides(it)
                passengerRides = rideRepository.getUserBookedRides(it)
                
                // Fetch bookings for all driver rides
                driverRides.forEach { ride ->
                    loadBookingsForRide(ride.id)
                }

                // Fetch user's own bookings (as passenger)
                val bookings = rideRepository.getUserBookings(it)
                userBookings = bookings.associateBy { it.ride_id }
                
                if (showLoading) isLoading = false
            }
        }
    }

    private fun loadBookingsForRide(rideId: String) {
        viewModelScope.launch {
            val bookings = rideRepository.getRideBookings(rideId)
            rideBookings = rideBookings + (rideId to bookings)
        }
    }

    private fun loadUnreadCount() {
        userId?.let {
            viewModelScope.launch {
                unreadCount = notificationRepository.getUnreadCount(it)
            }
        }
    }

    suspend fun deleteRide(rideId: String): Result<Unit> {
        val result = rideRepository.deleteRide(rideId)
        if (result.isSuccess) {
            refreshRides()
        }
        return result
    }

    suspend fun cancelBooking(rideId: String): Result<Unit> {
        if (userId == null) return Result.failure(Exception("User not logged in"))
        
        // Optimistic Update: Remove it from the UI immediately
        val originalPassengerRides = passengerRides
        val originalUserBookings = userBookings
        
        passengerRides = passengerRides.filter { it.id != rideId }
        userBookings = userBookings - rideId
        
        val result = rideRepository.cancelBooking(rideId, userId)
        
        if (result.isSuccess) {
            // Silent refresh in the background to sync with server
            refreshRides(showLoading = false)
        } else {
            // Rollback UI if the network request fails
            passengerRides = originalPassengerRides
            userBookings = originalUserBookings
        }
        return result
    }

    suspend fun completeRide(rideId: String): Result<Unit> {
        val result = rideRepository.updateRideStatus(rideId, "completed")
        if (result.isSuccess) {
            refreshRides()
        }
        return result
    }

    suspend fun submitRating(rideId: String, ratedId: String, rating: Int, comment: String): Result<Unit> {
        return if (userId != null) {
            val ratingObj = com.rom.poa_firstapp.data.model.Rating(
                ride_id = rideId,
                rater_id = userId,
                rated_id = ratedId,
                rating = rating,
                comment = comment.takeIf { it.isNotBlank() }
            )
            rideRepository.submitRating(ratingObj)
        } else {
            Result.failure(Exception("User not logged in"))
        }
    }
}
