package com.rom.poa_firstapp.ui.screen.postRide

import android.location.Geocoder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.repository.ProfileRepository
import com.rom.poa_firstapp.data.repository.RideRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

class PostRideViewModel(
    private val rideRepository: RideRepository,
    private val profileRepository: ProfileRepository,
    private val supabaseClient: SupabaseClient,
    private val geocoder: Geocoder
) : ViewModel() {

    var isPaidRide by mutableStateOf(false)
    var pickupLocation by mutableStateOf("")
    var destination by mutableStateOf("")
    var seatsCount by mutableIntStateOf(3)
    var departureTime by mutableStateOf("")
    var departureDate by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)

    var isEditing by mutableStateOf(false)
    private var currentRideId: String? = null

    fun incrementSeats() {
        if (seatsCount < 8) seatsCount++
    }

    fun decrementSeats() {
        if (seatsCount > 1) seatsCount--
    }

    fun setPaid(paid: Boolean) {
        isPaidRide = paid
    }

    fun updatePickupFromLocation(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val address = geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()?.getAddressLine(0)
                withContext(Dispatchers.Main) {
                    address?.let { pickupLocation = it }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Failed to get address from location"
                }
            }
        }
    }

    fun loadRide(rideId: String?) {
        if (rideId == null) return
        currentRideId = rideId
        isEditing = true
        viewModelScope.launch {
            isLoading = true
            try {
                val ride = rideRepository.getRideById(rideId)
                ride?.let {
                    pickupLocation = it.pickup_location ?: ""
                    destination = it.destination ?: ""
                    seatsCount = it.seats_left
                    departureTime = it.departure_time ?: ""
                    departureDate = it.departure_date ?: ""
                    isPaidRide = it.status.lowercase() == "paid"
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load ride"
            } finally {
                isLoading = false
            }
        }
    }

    fun postRide() {
        if (pickupLocation.isBlank() || destination.isBlank() ||
            departureTime.isBlank() || departureDate.isBlank()) {
            errorMessage = "Please fill in pickup, destination, time, and date."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                    ?: run {
                        errorMessage = "Session expired. Please login again."
                        return@launch
                    }

                val profile = profileRepository.getProfile(currentUser.id)
                    ?: run {
                        errorMessage = "Profile not found. Please complete your profile."
                        return@launch
                    }

                if (profile.user_type.lowercase() != "driver") {
                    errorMessage = "Only drivers can post rides. Please update your profile type."
                    return@launch
                }

                val pickupCoords = withContext(Dispatchers.IO) {
                    try {
                        geocoder.getFromLocationName(pickupLocation, 1)?.firstOrNull()
                    } catch (e: Exception) { null }
                }
                val destCoords = withContext(Dispatchers.IO) {
                    try {
                        geocoder.getFromLocationName(destination, 1)?.firstOrNull()
                    } catch (e: Exception) { null }
                }

                if (pickupCoords == null) {
                    errorMessage = "Could not find pickup location. Be more specific."
                    return@launch
                }
                if (destCoords == null) {
                    errorMessage = "Could not find destination. Be more specific."
                    return@launch
                }

                val rawPhone = profile.phone_number?.filter { it.isDigit() } ?: ""
                val formattedPhone = when {
                    rawPhone.startsWith("254") -> rawPhone
                    rawPhone.startsWith("0") -> "254" + rawPhone.substring(1)
                    rawPhone.length == 9 -> "254$rawPhone"
                    else -> rawPhone.ifBlank { "Contact via app" }
                }

                val ride = Ride(
                    id = currentRideId ?: UUID.randomUUID().toString(),
                    rider_id = currentUser.id,
                    rider_name = profile.full_name,
                    rider_avatar_url = profile.avatar_url,
                    seats_left = seatsCount,
                    rider_phone = formattedPhone,
                    start_lat = pickupCoords.latitude,
                    start_lng = pickupCoords.longitude,
                    status = if (isPaidRide) "Paid" else "Free",
                    pickup_location = pickupLocation,
                    destination = destination,
                    departure_time = departureTime,
                    departure_date = departureDate,
                    destination_lat = destCoords.latitude,
                    destination_lng = destCoords.longitude
                )

                val result = if (isEditing) {
                    rideRepository.updateRide(ride)
                } else {
                    rideRepository.postRide(ride)
                }

                result.fold(
                    onSuccess = {
                        isSuccess = true
                    },
                    onFailure = { e ->
                        errorMessage = "Failed to save ride: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                errorMessage = "An error occurred: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
