package com.rom.poa_firstapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BookingWithProfile(
    val id: String,
    val user_id: String,
    val full_name: String,
    val phone_number: String? = null,
    val avatar_url: String? = null,
    val status: String,
    val agreed_price: Double? = null
)
