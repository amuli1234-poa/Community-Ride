package com.rom.poa_firstapp.data.remote

import com.rom.poa_firstapp.data.model.RiderProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileService(private val supabaseClient: SupabaseClient) {
    /**
     * Fetches a rider's profile from the 'profiles' table by their user ID.
     */
    suspend fun getProfile(userId: String): RiderProfile? = withContext(Dispatchers.IO) {
        try {
            supabaseClient.from("profiles").select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingleOrNull<RiderProfile>()
        } catch (e: Exception) {
            println("Error fetching profile for $userId: ${e.message}")
            null
        }
    }

    /**
     * Updates the current user's profile information.
     */
    suspend fun updateProfile(profile: RiderProfile): Boolean = withContext(Dispatchers.IO) {
        try {
            supabaseClient.from("profiles").update(profile) {
                filter {
                    eq("id", profile.id)
                }
            }
            true
        } catch (e: Exception) {
            println("Error updating profile: ${e.message}")
            false
        }
    }
}
