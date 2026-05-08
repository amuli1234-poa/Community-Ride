package com.rom.poa_firstapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Rating(
    val id: String? = null,
    val ride_id: String,
    val rater_id: String,
    val rated_id: String,
    val rating: Int,
    val comment: String? = null,
    val created_at: String? = null
)
