package com.rom.poa_firstapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: Int? = null,
    val ride_id: String,
    val user_id: String,
    val status: String,
    val created_at: String? = null
)
