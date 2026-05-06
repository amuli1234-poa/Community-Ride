package com.rom.poa_firstapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Ride(
    val id: String,
    val rider_id: String,
    val rider_name: String,
    val seats_left: Int,
    val rider_phone: String,
    val start_lat: Double,
    val start_lng: Double,
    val status: String,
    val pickup_location: String? = null,
    val destination: String? = null,
    val departure_time: String? = null,
    val departure_date: String? = null,
    val destination_lat: Double? = 0.0,
    val destination_lng: Double? = 0.0
)
