package com.rom.poa_firstapp.data.repository

import com.rom.poa_firstapp.data.model.Notification
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

interface NotificationRepository {
    fun getNotificationsFlow(userId: String): Flow<PostgresAction>
    suspend fun getNotifications(userId: String): List<Notification>
    suspend fun markAsRead(notificationId: Int): Result<Unit>
    suspend fun getUnreadCount(userId: String): Int
}

class NotificationRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : NotificationRepository {

    override fun getNotificationsFlow(userId: String): Flow<PostgresAction> {
        val channel = supabaseClient.realtime.channel("notifications_${userId}_${java.util.UUID.randomUUID()}")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "notifications"
        }.onStart {
            channel.subscribe()
        }.onCompletion {
            channel.unsubscribe()
        }
    }

    override suspend fun getNotifications(userId: String): List<Notification> {
        return try {
            supabaseClient.from("notifications").select {
                filter {
                    eq("user_id", userId)
                }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }.decodeList<Notification>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun markAsRead(notificationId: Int): Result<Unit> {
        return try {
            supabaseClient.from("notifications").update(
                mapOf("is_read" to true)
            ) {
                filter { eq("id", notificationId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUnreadCount(userId: String): Int {
        return try {
            val response = supabaseClient.from("notifications").select {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
            response.decodeList<Notification>().size
        } catch (e: Exception) {
            0
        }
    }
}
