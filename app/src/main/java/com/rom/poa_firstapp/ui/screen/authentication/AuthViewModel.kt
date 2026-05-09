package com.rom.poa_firstapp.ui.screen.authentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rom.poa_firstapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var isSuccess by mutableStateOf(false)
        private set

    fun signUp(email: String, password: String, fullName: String, username: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            isSuccess = false
            val result = authRepository.signUp(email, password, fullName, username)
            result.onSuccess {
                successMessage = "Registration successful!"
                isSuccess = true
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Registration failed"
            }
            isLoading = false
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            isSuccess = false
            val result = authRepository.signIn(email, password)
            result.onSuccess {
                isSuccess = true
            }.onFailure {
                val message = it.localizedMessage ?: ""
                errorMessage = when {
                    message.contains("Email not confirmed", ignoreCase = true) || 
                    message.contains("email_not_confirmed", ignoreCase = true) -> 
                        "Your email is not confirmed. Please check your inbox for the verification link."
                    else -> message.ifEmpty { "Login failed" }
                }
            }
            isLoading = false
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            isSuccess = false
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                isSuccess = true
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Failed to send reset email"
            }
            isLoading = false
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            isSuccess = false
            val result = authRepository.updatePassword(newPassword)
            result.onSuccess {
                isSuccess = true
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Failed to update password"
            }
            isLoading = false
        }
    }

    fun clearState() {
        errorMessage = null
        successMessage = null
        isSuccess = false
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}
