package com.rom.poa_firstapp.ui.screen.notifications

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rom.poa_firstapp.data.model.Notification
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.NotificationRepositoryImpl
import com.rom.poa_firstapp.ui.navigation.ROUTES
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Design Tokens (Consistent with MyRides)
// ─────────────────────────────────────────────────────────────────────────────
private val Abyss = Color(0xFF080C1C)
private val Cavern = Color(0xFF0E1325)
private val Crater = Color(0xFF141929)
private val GlassEdge = Color(0x18FFFFFF)
private val GlassEdgeMid = Color(0x30FFFFFF)

private val CyanPrimary = Color(0xFF00E5FF)
private val CyanGlow = Color(0x4400E5FF)
private val MintPrimary = Color(0xFF00FFA3)
private val RedPrimary = Color(0xFFFF3B47)
private val GoldAccent = Color(0xFFFFBB00)
private val PurpleAccent = Color(0xFFAA55FF)

private val TextHero = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFFE8EEFF)
private val TextSecondary = Color(0xFF8896B8)
private val TextMuted = Color(0xFF4A5568)

// ─────────────────────────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val notificationRepository = remember { NotificationRepositoryImpl(SupabaseModule.client) }
    val userId = SupabaseModule.client.auth.currentSessionOrNull()?.user?.id
    val scope = rememberCoroutineScope()

    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadNotifications() {
        userId?.let {
            scope.launch {
                isLoading = true
                notifications = notificationRepository.getNotifications(it)
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
                title = {
                    Text(
                        "Notifications",
                        fontWeight = FontWeight.Bold,
                        color = TextHero,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Abyss)
            )
        },
        containerColor = Abyss
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CyanPrimary)
                    }
                }
                notifications.isEmpty() -> {
                    EmptyNotifications()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications, key = { it.id ?: it.hashCode() }) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    if (!notification.is_read && notification.id != null) {
                                        scope.launch {
                                            notificationRepository.markAsRead(notification.id)
                                            loadNotifications()
                                        }
                                    }

                                    when (notification.type) {
                                        "message" -> navController.navigate(ROUTES.Messages.name)
                                        "booking", "cancellation", "ride" -> navController.navigate(ROUTES.MyRides.name)
                                        else -> {}
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Notification Item
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NotificationItem(notification: Notification, onClick: () -> Unit) {
    val accentColor = when (notification.type) {
        "message" -> CyanPrimary
        "cancellation" -> RedPrimary
        "booking" -> MintPrimary
        else -> GoldAccent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Cavern)
            .border(1.dp, GlassEdgeMid, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with glow
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape)
                    .border(1.5.dp, accentColor.copy(alpha = 0.4f), CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        "message" -> Icons.AutoMirrored.Filled.Chat
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                if (!notification.is_read) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "New",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                }
            }

            if (!notification.is_read) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(CyanPrimary, CircleShape)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun EmptyNotifications() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Cavern, CircleShape)
                    .border(1.5.dp, GlassEdge, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = "No notifications yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )

            Text(
                text = "We'll notify you when something important happens",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}