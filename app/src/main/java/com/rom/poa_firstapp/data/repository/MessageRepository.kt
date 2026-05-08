package com.rom.poa_firstapp.data.repository

import com.rom.poa_firstapp.data.model.Message
import com.rom.poa_firstapp.data.model.Conversation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

interface MessageRepository {
    suspend fun getConversations(userId: String): List<Conversation>
    suspend fun getMessages(userId: String, otherUserId: String): List<Message>
    suspend fun sendMessage(message: Message): Boolean
    suspend fun startWhatsAppConversation(senderId: String, recipientId: String, content: String): Boolean
    fun getMessagesFlow(userId: String): Flow<PostgresAction>
}

class MessageRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : MessageRepository {

    override suspend fun getConversations(userId: String): List<Conversation> {
        return try {
            val messages = supabaseClient.from("messages").select {
                filter {
                    or {
                        eq("sender_id", userId)
                        eq("recipient_id", userId)
                    }
                }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }.decodeList<Message>()

            val conversationsGrouped = messages.groupBy { 
                if (it.sender_id == userId) it.recipient_id else it.sender_id 
            }

            if (conversationsGrouped.isEmpty()) return emptyList()

            val otherUserIds = conversationsGrouped.keys.toList()
            val profiles = try {
                supabaseClient.from("profiles").select {
                    filter {
                        isIn("id", otherUserIds)
                    }
                }.decodeList<com.rom.poa_firstapp.data.model.RiderProfile>()
            } catch (e: Exception) {
                emptyList()
            }
            val profileMap = profiles.associateBy { it.id }

            conversationsGrouped.map { (otherUserId, userMessages) ->
                val lastMsg = userMessages.first()
                val profile = profileMap[otherUserId]

                Conversation(
                    id = otherUserId,
                    other_user_id = otherUserId,
                    other_user_name = profile?.full_name ?: "User ${otherUserId.take(5)}",
                    other_user_avatar_url = profile?.avatar_url,
                    other_user_phone = profile?.phone_number ?: "",
                    last_message = lastMsg.content,
                    last_message_time = lastMsg.created_at ?: "",
                    unread_count = userMessages.count { it.recipient_id == userId && !it.is_read }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getMessages(userId: String, otherUserId: String): List<Message> {
        return try {
            supabaseClient.from("messages").select {
                filter {
                    or {
                        and {
                            eq("sender_id", userId)
                            eq("recipient_id", otherUserId)
                        }
                        and {
                            eq("sender_id", otherUserId)
                            eq("recipient_id", userId)
                        }
                    }
                }
            }.decodeList<Message>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun sendMessage(message: Message): Boolean {
        return try {
            supabaseClient.from("messages").insert(message)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun startWhatsAppConversation(senderId: String, recipientId: String, content: String): Boolean {
        return try {
            val message = Message(
                sender_id = senderId,
                recipient_id = recipientId,
                content = "[WhatsApp] $content",
                is_read = true
            )
            supabaseClient.from("messages").insert(message)

            // Tuma pia notification kwa dereva/mpokeaji
            val senderProfile = try {
                supabaseClient.from("profiles").select {
                    filter { eq("id", senderId) }
                }.decodeSingleOrNull<com.rom.poa_firstapp.data.model.RiderProfile>()
            } catch (e: Exception) { null }

            supabaseClient.from("notifications").insert(
                mapOf(
                    "user_id" to recipientId,
                    "title" to "New Message Interest",
                    "message" to "${senderProfile?.full_name ?: "Someone"} is interested and texted you on WhatsApp",
                    "type" to "message"
                )
            )

            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getMessagesFlow(userId: String): Flow<PostgresAction> {
        val channel = supabaseClient.realtime.channel("messages_${userId}_${java.util.UUID.randomUUID()}")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
        }
        return flow.onStart {
            try {
                channel.subscribe()
                println("DEBUG: Successfully subscribed to messages_$userId")
            } catch (e: Exception) {
                println("DEBUG: Message realtime subscription error: ${e.message}")
            }
        }.onCompletion {
            channel.unsubscribe()
        }
    }
}
