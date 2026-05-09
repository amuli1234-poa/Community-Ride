package com.rom.poa_firstapp.data.repository

import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.model.RiderProfile
import com.rom.poa_firstapp.data.model.Booking
import com.rom.poa_firstapp.data.model.BookingWithProfile
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
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
    suspend fun getUserBookings(userId: String): List<Booking>
    suspend fun cancelBooking(rideId: String, userId: String): Result<Unit>
    suspend fun deleteRide(rideId: String): Result<Unit>
    suspend fun updateRide(ride: Ride): Result<Unit>
    suspend fun updateRideStatus(rideId: String, status: String): Result<Unit>
    suspend fun updateBookingStatus(bookingId: String, status: String, agreedPrice: Double? = null): Result<Unit>
    suspend fun getRideBookings(rideId: String): List<BookingWithProfile>
    fun getBookingsFlow(userId: String): Flow<PostgresAction>
    suspend fun submitRating(rating: com.rom.poa_firstapp.data.model.Rating): Result<Unit>
}

class RideRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : RideRepository {
    
    override fun getRidesFlow(): Flow<PostgresAction> {
        val channel = supabaseClient.realtime.channel("rides_channel_${java.util.UUID.randomUUID()}")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "rides"
        }.onStart {
            channel.subscribe()
        }.onCompletion {
            channel.unsubscribe()
        }
    }

    override suspend fun subscribeToRides() {
        // No-op or just leave it if needed by other callers, 
        // but the flow should handle subscription now.
    }

    override suspend fun getAllRides(): List<Ride> = withContext(Dispatchers.IO) {
        try {
            // Fetch only active rides with seats available
            val rides = supabaseClient.from("rides")
                .select {
                    filter {
                        neq("status", "completed")
                        neq("status", "cancelled")
                        gt("seats_left", 0)
                    }
                }
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
            val booking = Booking(
                ride_id = rideId,
                user_id = userId,
                status = "PENDING"
            )
            supabaseClient.from("bookings").insert(booking)
            
            // Decrement seats_left in the rides table
            val ride = getRideById(rideId)
            if (ride != null && ride.seats_left > 0) {
                supabaseClient.from("rides").update({
                    set("seats_left", ride.seats_left - 1)
                }) {
                    filter { eq("id", rideId) }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RideRepo", "Booking failed", e)
            Result.failure(e)
        }
    }

    override suspend fun searchRides(query: String): List<Ride> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.from("rides").select {
                filter {
                    neq("status", "completed")
                    neq("status", "cancelled")
                    gt("seats_left", 0)
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
            val bookings = getUserBookings(userId)
            val rideIds = bookings.map { it.ride_id }.distinct()
            if (rideIds.isEmpty()) return emptyList()
            
            supabaseClient.from("rides").select {
                filter { isIn("id", rideIds) }
            }.decodeList<Ride>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getUserBookings(userId: String): List<Booking> {
        return try {
            supabaseClient.from("bookings").select {
                filter { eq("user_id", userId) }
            }.decodeList<Booking>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun cancelBooking(rideId: String, userId: String): Result<Unit> {
        return try {
            supabaseClient.from("bookings").delete {
                filter {
                    eq("ride_id", rideId)
                    eq("user_id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RideRepo", "Cancellation failed", e)
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

    override suspend fun updateRideStatus(rideId: String, status: String): Result<Unit> {
        return try {
            supabaseClient.from("rides").update({
                set("status", status)
            }) {
                filter { eq("id", rideId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBookingStatus(bookingId: String, status: String, agreedPrice: Double?): Result<Unit> {
        return try {
            supabaseClient.from("bookings").update({
                set("status", status)
                if (agreedPrice != null) {
                    set("agreed_price", agreedPrice)
                }
            }) {
                filter { eq("id", bookingId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRideBookings(rideId: String): List<BookingWithProfile> {
        return try {
            // Fetch bookings first
            val bookings = supabaseClient.from("bookings").select {
                filter { eq("ride_id", rideId) }
            }.decodeList<Booking>()

            if (bookings.isEmpty()) return emptyList()

            // Fetch profiles for these bookings
            val userIds = bookings.map { it.user_id }.distinct()
            val profiles = supabaseClient.from("profiles").select {
                filter { isIn("id", userIds) }
            }.decodeList<com.rom.poa_firstapp.data.model.RiderProfile>()

            // Map to BookingWithProfile
            bookings.mapNotNull { booking ->
                val profile = profiles.find { it.id == booking.user_id }
                if (profile != null) {
                    BookingWithProfile(
                        id = booking.id ?: "",
                        user_id = booking.user_id,
                        full_name = profile.full_name,
                        phone_number = profile.phone_number,
                        avatar_url = profile.avatar_url,
                        status = booking.status,
                        agreed_price = booking.agreed_price
                    )
                } else null
            }
        } catch (e: Exception) {
            Log.e("RideRepo", "Error fetching bookings", e)
            emptyList()
        }
    }

    override fun getBookingsFlow(userId: String): Flow<PostgresAction> {
        val channel = supabaseClient.realtime.channel("bookings_channel_${java.util.UUID.randomUUID()}")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "bookings"
            // We can't easily filter by user_id in postgresChangeFlow client-side without more complex setup,
            // but we can listen to all and filter in the ViewModel.
        }.onStart {
            channel.subscribe()
        }.onCompletion {
            channel.unsubscribe()
        }
    }

    override suspend fun submitRating(rating: com.rom.poa_firstapp.data.model.Rating): Result<Unit> {
        return try {
            supabaseClient.from("ratings").insert(rating)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RideRepo", "Error submitting rating", e)
            Result.failure(e)
        }
    }
}
