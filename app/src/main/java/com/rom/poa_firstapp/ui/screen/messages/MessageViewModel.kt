package com.rom.poa_firstapp.ui.screen.messages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rom.poa_firstapp.data.model.Conversation
import com.rom.poa_firstapp.data.model.Message
import com.rom.poa_firstapp.data.repository.MessageRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.launch

class MessageViewModel(
    private val repository: MessageRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    var conversations by mutableStateOf<List<Conversation>>(emptyList())
        private set

    var messages by mutableStateOf<List<Message>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadConversations()
    }

    fun loadConversations() {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                conversations = repository.getConversations(userId)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
            } finally {
                isLoading = false
            }
        }
    }

    fun loadMessages(otherUserId: String) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                messages = repository.getMessages(userId, otherUserId)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
            } finally {
                isLoading = false
            }
        }
    }

    fun sendMessage(recipientId: String, content: String) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        val message = Message(
            sender_id = userId,
            recipient_id = recipientId,
            content = content
        )
        viewModelScope.launch {
            try {
                repository.sendMessage(message)
                // Optionally reload messages or wait for realtime update
                loadMessages(recipientId)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
            }
        }
    }
}
