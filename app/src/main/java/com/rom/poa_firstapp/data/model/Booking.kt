package com.rom.poa_firstapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: String? = null,
    val ride_id: String,
    val user_id: String,
    val status: String = "PENDING", // PENDING, NEGOTIATING, CONFIRMED, CANCELLED
    val agreed_price: Double? = null,
    val created_at: String? = null,
    val profiles: RiderProfile? = null
)
