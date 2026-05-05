package com.rom.poa_firstapp.ui.screen.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rom.poa_firstapp.data.model.RiderProfile
import com.rom.poa_firstapp.data.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    var profile by mutableStateOf<RiderProfile?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadProfile(userId: String? = null) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val id = userId ?: supabaseClient.auth.currentUserOrNull()?.id
            if (id != null) {
                profile = profileRepository.getProfile(id)
                if (profile == null) {
                    errorMessage = "Failed to load profile"
                }
            } else {
                errorMessage = "User not logged in"
            }
            isLoading = false
        }
    }

    fun updateProfile(updatedProfile: RiderProfile) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = profileRepository.updateProfile(updatedProfile)
            result.onSuccess {
                profile = updatedProfile
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Failed to update profile"
            }
            isLoading = false
        }
    }
}
