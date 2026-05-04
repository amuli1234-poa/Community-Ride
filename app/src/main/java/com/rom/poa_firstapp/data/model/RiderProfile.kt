package com.rom.poa_firstapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RiderProfile(
    val id: String,
    val username: String,
    val full_name: String,
    val avatar_url: String? = null,
    val rides_given: Int = 0,
    val rides_taken: Int = 0,
    val community_rating: Double = 0.0,
    val total_reviews: Int = 0,
    val member_since: String? = null,
    val phone_verified: Boolean = false,
    val email_verified: Boolean = false,
    val bio: String? = null
)
