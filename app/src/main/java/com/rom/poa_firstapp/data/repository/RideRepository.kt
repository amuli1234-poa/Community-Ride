package com.rom.poa_firstapp.data.repository

import com.rom.poa_firstapp.data.model.Ride
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    fun getRidesFlow(): Flow<PostgresAction>
    suspend fun getAllRides(): List<Ride>
    suspend fun postRide(ride: Ride): Result<Unit>
}

class RideRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : RideRepository {
    
    override fun getRidesFlow(): Flow<PostgresAction> {
        val channel = supabaseClient.realtime.channel("rides_channel")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "rides"
        }
    }

    override suspend fun getAllRides(): List<Ride> {
        return supabaseClient.from("rides").select().decodeList<Ride>()
    }

    override suspend fun postRide(ride: Ride): Result<Unit> {
        return try {
            println("DEBUG: Posting ride to Supabase: $ride")
            supabaseClient.from("rides").insert(ride)
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Supabase Insert Error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
