package com.rom.poa_firstapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Ride(
    val id: String,
    val rider_id: String,
    val rider_name: String,
    val rider_avatar_url: String? = null,
    val seats_left: Int,
    val rider_phone: String,
    val start_lat: Double? = null,
    val start_lng: Double? = null,
    val destination_lat: Double? = null,
    val destination_lng: Double? = null,
    val status: String,
    val pickup_location: String? = null,
    val destination: String? = null,
    val departure_time: String? = null,
    val departure_date: String? = null
)
