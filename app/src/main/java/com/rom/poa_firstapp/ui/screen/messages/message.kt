package com.rom.poa_firstapp.ui.screen.messages

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.MessageRepositoryImpl
import com.rom.poa_firstapp.ui.common.ErrorState
import com.rom.poa_firstapp.ui.common.LoadingState
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
// Design Tokens
// ─────────────────────────────────────────────────────────────────────────────
private val Abyss        = Color(0xFF080C1C)
private val Cavern       = Color(0xFF0E1325)
private val Crater       = Color(0xFF141929)
private val GlassEdge    = Color(0x18FFFFFF)

private val CyanPrimary  = Color(0xFF00E5FF)
private val MintPrimary  = Color(0xFF00FFA3)
private val GoldAccent   = Color(0xFFFFBB00)
private val CoralPrimary = Color(0xFFFF4D7D)
private val PurpleAccent = Color(0xFFAA55FF)

private val TextPrimary   = Color(0xFFE8EEFF)
private val TextSecondary = Color(0xFF8896B8)
private val TextMuted     = Color(0xFF4A5568)

// Cycle avatar accent colours per conversation index
private val avatarAccents = listOf(CyanPrimary, GoldAccent, PurpleAccent, CoralPrimary, MintPrimary)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────
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
    val context       = LocalContext.current
    val conversations = viewModel.conversations

    Scaffold(
        topBar = { MessagesTopBar(onBack = onBack) },
        floatingActionButton = { NewMessageFab() },
        containerColor = Abyss
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Search bar ────────────────────────────────────────
                MessagesSearchBar()

                // ── Section label ─────────────────────────────────────
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("RECENT CONVERSATIONS", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                    Text("${conversations.size} chats", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CyanPrimary)
                }

                // ── Conversation list ─────────────────────────────────
                if (!viewModel.isLoading && conversations.isEmpty()) {
                    EmptyMessages()
                } else {
                    LazyColumn(
                        modifier              = Modifier.fillMaxSize(),
                        contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        verticalArrangement   = Arrangement.spacedBy(10.dp)
                    ) {
                        items(conversations.withIndex().toList()) { (idx, conv) ->
                            MessageCard(
                                recipientName      = conv.other_user_name,
                                recipientAvatarUrl = conv.other_user_avatar_url,
                                recipientPhone     = conv.other_user_phone,
                                lastMessage        = conv.last_message,
                                timestamp          = conv.last_message_time,
                                unreadCount        = conv.unread_count,
                                accentColor        = avatarAccents[idx % avatarAccents.size],
                                context            = context
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }

            if (viewModel.isLoading) LoadingState()
            viewModel.errorMessage?.let { ErrorState(message = it) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top bar
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text("Messages", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 17.sp)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Box(
                    modifier         = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GlassEdge)
                        .border(1.dp, GlassEdge, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Box(
                    modifier         = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GlassEdge)
                        .border(1.dp, GlassEdge, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Cavern)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Search bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MessagesSearchBar() {
    var query by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Abyss)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        OutlinedTextField(
            value         = query,
            onValueChange = { query = it },
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = { Text("Search conversations...", color = TextMuted, fontSize = 13.5.sp) },
            leadingIcon   = { Icon(Icons.Default.Search, null, tint = CyanPrimary, modifier = Modifier.size(20.dp)) },
            trailingIcon  = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine    = true,
            shape         = RoundedCornerShape(14.dp),
            textStyle     = LocalTextStyle.current.copy(fontSize = 13.5.sp, color = TextPrimary),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = CyanPrimary.copy(alpha = 0.45f),
                unfocusedBorderColor    = GlassEdge,
                focusedContainerColor   = Cavern,
                unfocusedContainerColor = Cavern,
                cursorColor             = CyanPrimary
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FAB
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NewMessageFab() {
    FloatingActionButton(
        onClick       = {},
        containerColor = Color.Transparent,
        shape          = CircleShape,
        modifier       = Modifier.padding(bottom = 8.dp, end = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(CyanPrimary, MintPrimary),
                        start = Offset(0f, 0f),
                        end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Edit, contentDescription = "New Message", tint = Abyss, modifier = Modifier.size(22.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Message card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MessageCard(
    recipientName: String,
    recipientAvatarUrl: String?,
    recipientPhone: String,
    lastMessage: String,
    timestamp: String,
    unreadCount: Int,
    accentColor: Color,
    context: Context
) {
    val hasUnread = unreadCount > 0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick  = {
            val url    = "https://api.whatsapp.com/send?phone=$recipientPhone"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com/send?phone=$recipientPhone")))
            }
        },
        shape  = RoundedCornerShape(16.dp),
        color  = Cavern,
        border = BorderStroke(1.dp, if (hasUnread) CyanPrimary.copy(alpha = 0.18f) else GlassEdge)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Avatar ───────────────────────────────────────────────
            Box(modifier = Modifier.size(46.dp)) {
                Box(
                    modifier         = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Crater)
                        .border(2.dp, accentColor.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!recipientAvatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model              = recipientAvatarUrl,
                            contentDescription = null,
                            modifier           = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale       = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, null, tint = accentColor, modifier = Modifier.size(22.dp))
                    }
                }
                // Online dot (shown for unread)
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Abyss)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(MintPrimary)
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(Modifier.width(13.dp))

            // ── Body ─────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = recipientName,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text       = lastMessage,
                    fontSize   = 12.5.sp,
                    color      = if (hasUnread) TextPrimary else TextSecondary,
                    fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                // WhatsApp tag
                Row(
                    modifier          = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MintPrimary.copy(alpha = 0.10f))
                        .border(1.dp, MintPrimary.copy(alpha = 0.20f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Default.Chat, null, tint = MintPrimary, modifier = Modifier.size(10.dp))
                    Text("WhatsApp", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = MintPrimary)
                }
            }

            Spacer(Modifier.width(10.dp))

            // ── Meta ─────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(formatTimestamp(timestamp), fontSize = 11.sp, color = TextSecondary)
                if (hasUnread) {
                    Box(
                        modifier         = Modifier
                            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = unreadCount.toString(),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Abyss,
                            modifier   = Modifier.padding(horizontal = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EmptyMessages() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Cavern)
                    .border(1.dp, GlassEdge, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MarkChatUnread, null, tint = TextMuted, modifier = Modifier.size(36.dp))
            }
            Text("No messages yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Text("Start a conversation by messaging\na rider from their profile.", fontSize = 13.sp, color = TextMuted, textAlign = TextAlign.Center, lineHeight = 18.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Timestamp formatter
// ─────────────────────────────────────────────────────────────────────────────
fun formatTimestamp(timestamp: String): String {
    return try {
        val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date     = inputFmt.parse(timestamp) ?: return timestamp.take(16).drop(11)
        val now      = Calendar.getInstance()
        val then     = Calendar.getInstance().apply { time = date }
        when {
            now.get(Calendar.DATE) == then.get(Calendar.DATE) ->
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            now.get(Calendar.DATE) - then.get(Calendar.DATE) == 1 -> "Yesterday"
            else -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        if (timestamp.length >= 16) timestamp.substring(11, 16) else timestamp
    }
}