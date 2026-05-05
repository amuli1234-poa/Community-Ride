package com.rom.poa_firstapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Ride(
    val id: String,
    val rider_id: String, // Added this as it was used in code but missing from data class
    val rider_name: String,
    val seats_left: Int,
    val rider_phone: String,
    val start_lat: Double,
    val start_lng: Double,
    val status: String
)
