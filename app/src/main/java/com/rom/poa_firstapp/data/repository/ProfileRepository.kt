package com.rom.poa_firstapp.data.repository

import com.rom.poa_firstapp.data.model.RiderProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

interface ProfileRepository {
    suspend fun getProfile(userId: String): RiderProfile?
    suspend fun updateProfile(profile: RiderProfile): Result<Unit>
    suspend fun uploadAvatar(userId: String, byteArray: ByteArray): Result<String>
}

class ProfileRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : ProfileRepository {
    override suspend fun getProfile(userId: String): RiderProfile? {
        return try {
            val response = supabaseClient.from("profiles").select {
                filter { eq("id", userId) }
            }
            if (response.data == "null" || response.data == "[]") {
                null
            } else {
                response.decodeSingle<RiderProfile>()
            }
        } catch (e: Exception) {
            println("DEBUG: ProfileRepository Error: ${e.message}")
            null
        }
    }

    override suspend fun updateProfile(profile: RiderProfile): Result<Unit> {
        return try {
            supabaseClient.from("profiles").update(profile) {
                filter { eq("id", profile.id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAvatar(userId: String, byteArray: ByteArray): Result<String> {
        return try {
            val fileName = "$userId-avatar.png"
            val bucket = supabaseClient.storage.from("avatars")
            bucket.upload(fileName, byteArray) {
                upsert = true
            }
            val publicUrl = bucket.publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
