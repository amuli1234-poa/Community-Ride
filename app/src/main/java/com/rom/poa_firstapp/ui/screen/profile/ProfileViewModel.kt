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
            try {
                val id = userId ?: supabaseClient.auth.currentUserOrNull()?.id
                if (id != null) {
                    profile = profileRepository.getProfile(id)
                    if (profile == null) {
                        errorMessage = "Failed to load profile"
                    }
                } else {
                    errorMessage = "User not logged in"
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "An error occurred"
            } finally {
                isLoading = false
            }
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

    fun uploadAvatar(userId: String, byteArray: ByteArray) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val uploadResult = profileRepository.uploadAvatar(userId, byteArray)
            uploadResult.onSuccess { publicUrl ->
                val currentProfile = profile
                if (currentProfile != null) {
                    val updatedProfile = currentProfile.copy(avatar_url = publicUrl)
                    val updateResult = profileRepository.updateProfile(updatedProfile)
                    updateResult.onSuccess {
                        profile = updatedProfile
                    }.onFailure {
                        errorMessage = it.localizedMessage ?: "Failed to update profile metadata"
                    }
                }
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Failed to upload avatar"
            }

            isLoading = false
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signOut()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Logout failed"
            }
        }
    }
}
