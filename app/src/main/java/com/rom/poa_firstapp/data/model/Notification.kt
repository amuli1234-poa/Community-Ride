package com.rom.poa_firstapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: Int? = null,
    val user_id: String,
    val title: String,
    val message: String,
    val type: String, // e.g., "booking", "cancellation"
    val is_read: Boolean = false,
    val created_at: String? = null
)
