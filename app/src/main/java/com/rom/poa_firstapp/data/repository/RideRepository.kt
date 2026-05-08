package com.rom.poa_firstapp.data.repository

import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.model.RiderProfile
import com.rom.poa_firstapp.data.model.Booking
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface RideRepository {
    fun getRidesFlow(): Flow<PostgresAction>
    suspend fun subscribeToRides()
    suspend fun getAllRides(): List<Ride>
    suspend fun postRide(ride: Ride): Result<Unit>
    suspend fun getRideById(id: String): Ride?
    suspend fun bookRide(rideId: String, userId: String): Result<Unit>
    suspend fun searchRides(query: String): List<Ride>
    suspend fun getUserRides(userId: String): List<Ride>
    suspend fun getUserBookedRides(userId: String): List<Ride>
    suspend fun cancelBooking(rideId: String, userId: String): Result<Unit>
    suspend fun deleteRide(rideId: String): Result<Unit>
    suspend fun updateRide(ride: Ride): Result<Unit>
}

class RideRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : RideRepository {
    
    override fun getRidesFlow(): Flow<PostgresAction> {
        val channel = supabaseClient.realtime.channel("rides_channel")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "rides"
        }.onStart {
            channel.subscribe()
        }
    }

    override suspend fun subscribeToRides() {
        // No-op or just leave it if needed by other callers, 
        // but the flow should handle subscription now.
    }

    override suspend fun getAllRides(): List<Ride> = withContext(Dispatchers.IO) {
        try {
            // Fetch all rides
            val rides = supabaseClient.from("rides")
                .select()
                .decodeList<Ride>()

            if (rides.isEmpty()) {
                Log.d("RideRepo", "No rides found")
                return@withContext emptyList()
            }

            // Get unique rider IDs
            val riderIds = rides.map { it.rider_id }.distinct()

            // Fetch profiles for avatars
            val profiles = try {
                supabaseClient.from("profiles")
                    .select {
                        filter { isIn("id", riderIds) }
                    }
                    .decodeList<RiderProfile>()
            } catch (e: Exception) {
                Log.e("RideRepo", "Failed to fetch profiles", e)
                emptyList()
            }

            val avatarMap = profiles.associate { it.id to it.avatar_url }

            // Attach avatar and return
            return@withContext rides.map { ride ->
                ride.copy(
                    rider_avatar_url = avatarMap[ride.rider_id]
                )
            }

        } catch (e: Exception) {
            Log.e("RideRepo", "Error in getAllRides", e)
            emptyList()
        }
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

    override suspend fun getRideById(id: String): Ride? {
        return try {
            val ride = supabaseClient.from("rides").select {
                filter {
                    eq("id", id)
                }
            }.decodeSingle<Ride>()

            // Fetch rider profile to get the avatar URL
            val profile = try {
                supabaseClient.from("profiles").select {
                    filter { eq("id", ride.rider_id) }
                }.decodeSingleOrNull<RiderProfile>()
            } catch (e: Exception) {
                null
            }

            ride.copy(rider_avatar_url = profile?.avatar_url)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun bookRide(rideId: String, userId: String): Result<Unit> {
        return try {
            // 1. Check if seats available
            val ride = getRideById(rideId) ?: throw Exception("Ride not found")
            if (ride.seats_left <= 0) throw Exception("No seats available")

            // 2. Decrement seats
            supabaseClient.from("rides").update(
                mapOf("seats_left" to ride.seats_left - 1)
            ) {
                filter { eq("id", rideId) }
            }

            // 3. Create booking record (assuming a 'bookings' table exists)
            supabaseClient.from("bookings").insert(
                mapOf(
                    "ride_id" to rideId,
                    "user_id" to userId,
                    "status" to "confirmed"
                )
            )

            // 4. (Notification logic would go here, e.g., inserting into a notifications table)
            supabaseClient.from("notifications").insert(
                mapOf(
                    "user_id" to ride.rider_id,
                    "title" to "New Booking!",
                    "message" to "A seat has been booked for your ride to ${ride.destination}",
                    "type" to "booking"
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchRides(query: String): List<Ride> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.from("rides").select {
                filter {
                    or {
                        ilike("destination", "%$query%")
                        ilike("pickup_location", "%$query%")
                    }
                }
            }.decodeList<Ride>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getUserRides(userId: String): List<Ride> {
        return try {
            supabaseClient.from("rides").select {
                filter {
                    eq("rider_id", userId)
                }
            }.decodeList<Ride>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getUserBookedRides(userId: String): List<Ride> {
        return try {
            // This is a bit simplified; ideally we'd use a join or view
            val bookings = supabaseClient.from("bookings").select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<Booking>()
            
            val rideIds = bookings.map { it.ride_id }
            if (rideIds.isEmpty()) return emptyList()
            
            supabaseClient.from("rides").select {
                filter {
                    isIn("id", rideIds)
                }
            }.decodeList<Ride>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun cancelBooking(rideId: String, userId: String): Result<Unit> {
        return try {
            val ride = getRideById(rideId) ?: throw Exception("Ride not found")

            // 1. Delete the booking
            supabaseClient.from("bookings").delete {
                filter {
                    eq("ride_id", rideId)
                    eq("user_id", userId)
                }
            }

            // 2. Increment seats back
            supabaseClient.from("rides").update(
                mapOf("seats_left" to ride.seats_left + 1)
            ) {
                filter { eq("id", rideId) }
            }

            // 3. Notify the driver
            supabaseClient.from("notifications").insert(
                mapOf(
                    "user_id" to ride.rider_id,
                    "title" to "Booking Cancelled",
                    "message" to "A passenger has cancelled their booking for your ride to ${ride.destination}",
                    "type" to "cancellation"
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRide(rideId: String): Result<Unit> {
        return try {
            supabaseClient.from("rides").delete {
                filter { eq("id", rideId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRide(ride: Ride): Result<Unit> {
        return try {
            supabaseClient.from("rides").update(ride) {
                filter { eq("id", ride.id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
