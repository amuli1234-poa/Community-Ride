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

    fun refreshRides() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                rides = repository.getAllRides()
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
                rides = rides + newRide
            }
            is PostgresAction.Update -> {
                val updatedRide = action.decodeRecord<Ride>()
                rides = rides.map { if (it.id == updatedRide.id) updatedRide else it }
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
