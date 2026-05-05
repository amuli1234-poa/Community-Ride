package com.rom.poa_firstapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String? = null,
    val sender_id: String,
    val recipient_id: String,
    val content: String,
    val created_at: String? = null,
    val is_read: Boolean = false
)

@Serializable
data class Conversation(
    val id: String,
    val other_user_id: String,
    val other_user_name: String,
    val other_user_phone: String,
    val last_message: String,
    val last_message_time: String,
    val unread_count: Int = 0
)
