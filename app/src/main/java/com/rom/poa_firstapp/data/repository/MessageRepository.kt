package com.rom.poa_firstapp.data.repository

import com.rom.poa_firstapp.data.model.Message
import com.rom.poa_firstapp.data.model.Conversation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun getConversations(userId: String): List<Conversation>
    suspend fun getMessages(userId: String, otherUserId: String): List<Message>
    suspend fun sendMessage(message: Message): Boolean
    fun getMessagesFlow(userId: String): Flow<PostgresAction>
}

class MessageRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : MessageRepository {

    override suspend fun getConversations(userId: String): List<Conversation> {
        // In a real app, this might be a complex join or a RPC call in Supabase
        // For now, we'll try to fetch from a 'conversations' view or table if it exists
        // or derive it from messages. Let's assume a 'conversations' view for simplicity.
        return try {
            supabaseClient.from("conversations").select {
                filter {
                    or {
                        eq("user1_id", userId)
                        eq("user2_id", userId)
                    }
                }
            }.decodeList<Conversation>()
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

    override fun getMessagesFlow(userId: String): Flow<PostgresAction> {
        val channel = supabaseClient.realtime.channel("messages_$userId")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"

        }
    }
}
