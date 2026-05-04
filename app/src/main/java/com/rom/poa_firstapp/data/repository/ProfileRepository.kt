package com.rom.poa_firstapp.data.repository

import com.rom.poa_firstapp.data.model.RiderProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

interface ProfileRepository {
    suspend fun getProfile(userId: String): RiderProfile?
}

class ProfileRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : ProfileRepository {
    override suspend fun getProfile(userId: String): RiderProfile? {
        return try {
            supabaseClient.from("profiles").select {
                filter { eq("id", userId) }
            }.decodeSingle<RiderProfile>()
        } catch (e: Exception) {
            null
        }
    }
}
