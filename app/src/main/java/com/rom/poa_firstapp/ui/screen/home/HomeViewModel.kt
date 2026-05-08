package com.rom.poa_firstapp.ui.screen.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.repository.RideRepository
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: RideRepository) : ViewModel() {
    var rides by mutableStateOf<List<Ride>>(emptyList())
        private set

    var userLocation by mutableStateOf<Pair<Double, Double>?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _rideActions = MutableSharedFlow<PostgresAction>(extraBufferCapacity = 64)
    val rideActions = _rideActions.asSharedFlow()

    init {
        refreshRides()
        subscribeToRealtime()
    }

    fun updateLocation(lat: Double, lng: Double) {
        if (userLocation == null || calculateDistance(userLocation!!.first, userLocation!!.second, lat, lng) > 0.5) {
            userLocation = Pair(lat, lng)
            // Optional: Re-fetch or re-filter when location changes significantly
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    fun refreshRides() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                val allRides = repository.getAllRides()
                
                // Filter rides within 50km if location is available
                rides = userLocation?.let { loc ->
                    allRides.filter { ride ->
                        val rLat = ride.start_lat ?: 0.0
                        val rLng = ride.start_lng ?: 0.0
                        if (rLat == 0.0) true // Keep rides with no location for now? or hide them?
                        else calculateDistance(loc.first, loc.second, rLat, rLng) <= 50.0 // 50km radius
                    }
                } ?: allRides

                errorMessage = null
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching rides", e)
                errorMessage = "Failed to load rides"
            } finally {
                isLoading = false
            }
        }
    }

    private fun subscribeToRealtime() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getRidesFlow().collect { action ->
                    _rideActions.emit(action)
                    updateLocalList(action)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Realtime error", e)
            }
        }
    }

    private fun updateLocalList(action: PostgresAction) {
        when (action) {
            is PostgresAction.Insert -> {
                val newRide = action.decodeRecord<Ride>()
                if (newRide.seats_left > 0 && newRide.status != "completed" && newRide.status != "cancelled") {
                    rides = rides + newRide
                }
            }
            is PostgresAction.Update -> {
                val updatedRide = action.decodeRecord<Ride>()
                if (updatedRide.seats_left <= 0 || updatedRide.status == "completed" || updatedRide.status == "cancelled") {
                    rides = rides.filter { it.id != updatedRide.id }
                } else {
                    rides = rides.map { if (it.id == updatedRide.id) updatedRide else it }
                }
            }
            is PostgresAction.Delete -> {
                val id = action.oldRecord["id"]?.toString()?.replace("\"", "")
                if (id != null) {
                    rides = rides.filter { it.id != id }
                }
            }
            else -> {}
        }
    }
}
