package com.rom.poa_firstapp.ui.screen.rideDetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.remote.SupabaseModule
import io.github.jan.supabase.auth.auth
import com.rom.poa_firstapp.data.repository.MessageRepository
import com.rom.poa_firstapp.data.repository.RideRepository
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RideDetailsViewModel(
    private val rideId: String,
    private val rideRepository: RideRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    var ride by mutableStateOf<Ride?>(null)
        private set

    var bookings by mutableStateOf<List<com.rom.poa_firstapp.data.model.BookingWithProfile>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isBooking by mutableStateOf(false)
        private set

    var hasBooked by mutableStateOf(false)
        private set

    var userBooking by mutableStateOf<com.rom.poa_firstapp.data.model.Booking?>(null)
        private set

    init {
        loadRide()
        observeRideChanges()
        observeBookings()
    }

    private fun loadRide() {
        viewModelScope.launch {
            ride = rideRepository.getRideById(rideId)
            checkIfBooked()
            fetchBookingsIfOwner()
            isLoading = false
        }
    }

    private fun observeBookings() {
        val currentUserId = SupabaseModule.client.auth.currentSessionOrNull()?.user?.id ?: return
        rideRepository.getBookingsFlow(currentUserId)
            .onEach { action ->
                // Check if this booking belongs to the current ride
                val booking = when(action) {
                    is PostgresAction.Update -> action.decodeRecord<com.rom.poa_firstapp.data.model.Booking>()
                    is PostgresAction.Insert -> action.decodeRecord<com.rom.poa_firstapp.data.model.Booking>()
                    else -> null
                }
                
                if (booking?.ride_id == rideId) {
                    fetchBookingsIfOwner()
                    checkIfBooked()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun checkIfBooked() {
        val currentUserId = SupabaseModule.client.auth.currentSessionOrNull()?.user?.id ?: return
        viewModelScope.launch {
            val userBookings = rideRepository.getUserBookings(currentUserId)
            userBooking = userBookings.find { it.ride_id == rideId }
            hasBooked = userBooking != null
        }
    }

    private fun fetchBookingsIfOwner() {
        val r = ride ?: return
        val currentUserId = SupabaseModule.client.auth.currentSessionOrNull()?.user?.id ?: return
        if (r.rider_id == currentUserId) {
            viewModelScope.launch {
                bookings = rideRepository.getRideBookings(rideId)
            }
        }
    }

    private fun observeRideChanges() {
        rideRepository.getRidesFlow()
            .onEach { action ->
                when (action) {
                    is PostgresAction.Update -> {
                        val updatedRide = action.decodeRecord<Ride>()
                        if (updatedRide.id == rideId) {
                            ride = updatedRide
                        }
                    }
                    is PostgresAction.Delete -> {
                        val deletedId = action.oldRecord["id"]?.toString()
                        if (deletedId?.trim('"') == rideId) {
                            ride = null
                        }
                    }
                    else -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    fun startWhatsAppConversation(senderId: String, content: String, onComplete: () -> Unit) {
        val r = ride ?: return
        viewModelScope.launch {
            messageRepository.startWhatsAppConversation(
                senderId = senderId,
                recipientId = r.rider_id,
                content = content
            )
            onComplete()
        }
    }

    fun bookRide(userId: String, onResult: (Result<Unit>) -> Unit) {
        val r = ride ?: return
        if (r.rider_id == userId) {
            onResult(Result.failure(Exception("You cannot book your own ride")))
            return
        }
        viewModelScope.launch {
            isBooking = true
            // Initial status is PENDING for negotiation
            val result = rideRepository.bookRide(r.id, userId)
            onResult(result)
            if (result.isSuccess) {
                ride = rideRepository.getRideById(rideId)
            }
            isBooking = false
        }
    }

    fun confirmNegotiatedPrice(bookingId: String, price: Double, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = rideRepository.updateBookingStatus(bookingId, "CONFIRMED", price)
            if (result.isSuccess) {
                fetchBookingsIfOwner()
            }
            onResult(result)
        }
    }

    fun updateBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            val result = rideRepository.updateBookingStatus(bookingId, status)
            if (result.isSuccess) {
                fetchBookingsIfOwner()
            }
        }
    }
}
