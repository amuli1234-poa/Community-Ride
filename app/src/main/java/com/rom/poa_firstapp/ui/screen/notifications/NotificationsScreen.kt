package com.rom.poa_firstapp.ui.screen.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rom.poa_firstapp.data.model.Notification
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.NotificationRepositoryImpl
import com.rom.poa_firstapp.ui.navigation.ROUTES
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val notificationRepository = remember { NotificationRepositoryImpl(SupabaseModule.client) }
    val userId = SupabaseModule.client.auth.currentSessionOrNull()?.user?.id
    val scope = rememberCoroutineScope()
    
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadNotifications() {
        if (userId != null) {
            scope.launch {
                isLoading = true
                notifications = notificationRepository.getNotifications(userId)
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId) {
        loadNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF4F6F3)
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2D6A4F))
            }
        } else if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No notifications yet", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(notification) {
                        if (!notification.is_read && notification.id != null) {
                            scope.launch {
                                notificationRepository.markAsRead(notification.id)
                                loadNotifications()
                            }
                        }

                        // Pelekwa sehemu husika kulingana na aina ya notification
                        when (notification.type) {
                            "message" -> navController.navigate(ROUTES.Messages.name)
                            "booking", "cancellation" -> navController.navigate(ROUTES.MyRides.name)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (notification.is_read) Color.White else Color(0xFFE8F5E9)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        when (notification.type) {
                            "cancellation" -> Color(0xFFFFEBEE)
                            "message" -> Color(0xFFE3F2FD)
                            else -> Color(0xFFE8F5E9)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (notification.type == "message") Icons.AutoMirrored.Filled.Chat else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = when (notification.type) {
                        "cancellation" -> Color.Red
                        "message" -> Color(0xFF1976D2)
                        else -> Color(0xFF2D6A4F)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column {
                Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(notification.message, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}
