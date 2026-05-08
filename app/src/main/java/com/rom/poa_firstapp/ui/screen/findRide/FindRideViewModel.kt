package com.rom.poa_firstapp.ui.screen.findRide

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.repository.RideRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FindRideViewModel(private val repository: RideRepository) : ViewModel() {
    var searchQuery by mutableStateOf("")
        private set

    var selectedFilter by mutableStateOf("All")
        private set

    var allRides by mutableStateOf<List<Ride>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    private var searchJob: Job? = null

    init {
        loadInitialRides()
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
        searchJob?.cancel()
        
        if (newQuery.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(500)
                performSearch(newQuery)
            }
        } else if (newQuery.isEmpty()) {
            loadInitialRides()
        }
    }

    fun onFilterChange(newFilter: String) {
        selectedFilter = newFilter
    }

    private fun loadInitialRides() {
        viewModelScope.launch {
            isLoading = true
            allRides = repository.getAllRides()
            isLoading = false
        }
    }

    private suspend fun performSearch(query: String) {
        isLoading = true
        allRides = repository.searchRides(query)
        isLoading = false
    }

    val filteredRides: List<Ride>
        get() = if (selectedFilter == "All") allRides
        else allRides.filter { it.status.equals(selectedFilter, ignoreCase = true) }
}
