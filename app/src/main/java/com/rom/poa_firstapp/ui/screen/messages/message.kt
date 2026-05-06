package com.rom.poa_firstapp.ui.screen.messages

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.MessageRepositoryImpl
import com.rom.poa_firstapp.ui.common.LoadingState
import com.rom.poa_firstapp.ui.common.ErrorState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessagesScreen(
    onBack: () -> Unit,
    viewModel: MessageViewModel = viewModel {
        MessageViewModel(
            MessageRepositoryImpl(SupabaseModule.client),
            SupabaseModule.client
        )
    }
) {
    val context = LocalContext.current
    val conversations = viewModel.conversations

    Scaffold(
        topBar = { MessagesTopAppBar(onBack) },
        floatingActionButton = { NewMessageFab() }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF0F2F5))
            ) {
                if (conversations.isEmpty() && !viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No messages yet", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(conversations) { conversation ->
                            MessageItem(
                                recipientName = conversation.other_user_name,
                                recipientPhone = conversation.other_user_phone,
                                lastMessage = conversation.last_message,
                                timestamp = conversation.last_message_time,
                                unreadCount = conversation.unread_count,
                                context = context
                            )
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                LoadingState()
            }

            viewModel.errorMessage?.let {
                ErrorState(message = it)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesTopAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Messages", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Handle search */ }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun NewMessageFab() {
    FloatingActionButton(
        onClick = { /* TODO: Navigate to new message screen */ },
        containerColor = Color(0xFF673AB7), // Purple color from design
        shape = CircleShape,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "New Message", tint = Color.White)
    }
}

@Composable
fun MessageItem(
    recipientName: String,
    recipientPhone: String,
    lastMessage: String,
    timestamp: String,
    unreadCount: Int,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Redirect to WhatsApp
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$recipientPhone")
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com/send?phone=$recipientPhone"))
                    context.startActivity(browserIntent)
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for avatar
            Image(
                painter = rememberAsyncImagePainter("https://via.placeholder.com/150"),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = recipientName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = lastMessage, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = formatTimestamp(timestamp), fontSize = 12.sp, color = Color.Gray)
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Badge(
                        modifier = Modifier.align(Alignment.End),
                        containerColor = Color(0xFF673AB7)
                    ) {
                        Text(text = unreadCount.toString(), color = Color.White)
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(timestamp)
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: timestamp.substring(11, 16)
    } catch (e: Exception) {
        if (timestamp.length >= 16) timestamp.substring(11, 16) else timestamp
    }
}
