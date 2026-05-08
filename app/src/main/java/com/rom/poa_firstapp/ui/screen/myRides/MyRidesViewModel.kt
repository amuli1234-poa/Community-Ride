package com.rom.poa_firstapp.ui.screen.myRides

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.repository.NotificationRepository
import com.rom.poa_firstapp.data.repository.RideRepository
import kotlinx.coroutines.launch

class MyRidesViewModel(
    private val rideRepository: RideRepository,
    private val notificationRepository: NotificationRepository,
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

    init {
        refreshRides()
        loadUnreadCount()
    }

    fun onTabChange(index: Int) {
        selectedTab = index
    }

    fun refreshRides() {
        userId?.let {
            viewModelScope.launch {
                isLoading = true
                driverRides = rideRepository.getUserRides(it)
                passengerRides = rideRepository.getUserBookedRides(it)
                isLoading = false
            }
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
        return if (userId != null) {
            val result = rideRepository.cancelBooking(rideId, userId)
            if (result.isSuccess) {
                refreshRides()
            }
            result
        } else {
            Result.failure(Exception("User not logged in"))
        }
    }
}
