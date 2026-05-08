package com.rom.poa_firstapp.ui.screen.rideDetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.repository.MessageRepository
import com.rom.poa_firstapp.data.repository.RideRepository
import kotlinx.coroutines.launch

class RideDetailsViewModel(
    private val rideId: String,
    private val rideRepository: RideRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    var ride by mutableStateOf<Ride?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isBooking by mutableStateOf(false)
        private set

    init {
        loadRide()
    }

    private fun loadRide() {
        viewModelScope.launch {
            ride = rideRepository.getRideById(rideId)
            isLoading = false
        }
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
        viewModelScope.launch {
            isBooking = true
            val result = rideRepository.bookRide(r.id, userId)
            onResult(result)
            if (result.isSuccess) {
                // Refresh local ride data to update seats_left
                ride = rideRepository.getRideById(rideId)
            }
            isBooking = false
        }
    }
}
