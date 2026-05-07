package com.rom.poa_firstapp.ui.screen.myRides

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.NotificationRepositoryImpl
import com.rom.poa_firstapp.data.repository.RideRepositoryImpl
import com.rom.poa_firstapp.ui.navigation.ROUTES
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Design Tokens
// ─────────────────────────────────────────────────────────────────────────────
private val Abyss = Color(0xFF080C1C)
private val Cavern = Color(0xFF0E1325)
private val Crater = Color(0xFF141929)
private val GlassEdge = Color(0x18FFFFFF)
private val GlassEdgeMid = Color(0x30FFFFFF)

private val CyanPrimary = Color(0xFF00E5FF)
private val CyanGlow = Color(0x4400E5FF)
private val CoralPrimary = Color(0xFFFF4D7D)
private val MintPrimary = Color(0xFF00FFA3)
private val GoldAccent = Color(0xFFFFBB00)
private val RedPrimary = Color(0xFFFF3B47)
private val PurpleAccent = Color(0xFFAA55FF)

private val TextHero = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFFE8EEFF)
private val TextSecondary = Color(0xFF8896B8)
private val TextMuted = Color(0xFF4A5568)

// ─────────────────────────────────────────────────────────────────────────────
// Status Helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun rideStatusColor(status: String) = when (status.trim().lowercase()) {
    "free" -> MintPrimary
    "paid" -> RedPrimary
    "pending" -> GoldAccent
    "active" -> CyanPrimary
    "completed" -> MintPrimary
    "cancelled" -> RedPrimary
    else -> CyanPrimary
}

private fun rideStatusLabel(status: String) = when (status.trim().lowercase()) {
    "free" -> "Free"
    "paid" -> "Paid"
    "pending" -> "Pending"
    "active" -> "Active"
    "completed" -> "Completed"
    "cancelled" -> "Cancelled"
    else -> status.replaceFirstChar { it.uppercaseChar() }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MyRidesScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var driverRides by remember { mutableStateOf<List<Ride>>(emptyList()) }
    var passengerRides by remember { mutableStateOf<List<Ride>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var unreadCount by remember { mutableIntStateOf(0) }

    val rideRepository = remember { RideRepositoryImpl(SupabaseModule.client) }
    val notificationRepository = remember { NotificationRepositoryImpl(SupabaseModule.client) }
    val userId = SupabaseModule.client.auth.currentSessionOrNull()?.user?.id
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showCancelBookingDialog by remember { mutableStateOf<String?>(null) }

    fun refreshRides() {
        userId?.let {
            scope.launch {
                isLoading = true
                driverRides = rideRepository.getUserRides(it)
                passengerRides = rideRepository.getUserBookedRides(it)
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId) {
        refreshRides()
        userId?.let { unreadCount = notificationRepository.getUnreadCount(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Abyss)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MyRidesTopBar(
                onBack = { navController.popBackStack() },
                onNotifications = { navController.navigate(ROUTES.Notifications.name) },
                unreadCount = unreadCount
            )

            PillTabRow(selectedTab = selectedTab, onTabChange = { selectedTab = it })

            val currentRides = if (selectedTab == 0) driverRides else passengerRides

            when {
                isLoading -> LoadingRides()
                currentRides.isEmpty() -> EmptyRides(isDriver = selectedTab == 0)
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(currentRides, key = { it.id }) { ride ->
                            MyRideCard(
                                ride = ride,
                                isDriver = selectedTab == 0,
                                onClick = { navController.navigate("${ROUTES.RideDetails.name}/${ride.id}") },
                                onEdit = { navController.navigate("${ROUTES.PostRide.name}?rideId=${ride.id}") },
                                onDelete = { showDeleteDialog = ride.id },
                                onCancel = { showCancelBookingDialog = ride.id }
                            )
                        }
                    }
                }
            }
        }

        // Dialogs
        showDeleteDialog?.let { id ->
            NightAlertDialog(
                title = "Delete Ride",
                body = "This will permanently remove the ride and notify all passengers. This cannot be undone.",
                confirmText = "Delete",
                confirmColor = RedPrimary,
                onConfirm = {
                    showDeleteDialog = null
                    scope.launch {
                        val result = rideRepository.deleteRide(id)
                        Toast.makeText(context, if (result.isSuccess) "Ride deleted successfully" else "Failed to delete ride", Toast.LENGTH_SHORT).show()
                        if (result.isSuccess) refreshRides()
                    }
                },
                onDismiss = { showDeleteDialog = null }
            )
        }

        showCancelBookingDialog?.let { id ->
            NightAlertDialog(
                title = "Cancel Booking",
                body = "Are you sure you want to cancel your booking for this ride?",
                confirmText = "Cancel Booking",
                confirmColor = RedPrimary,
                onConfirm = {
                    showCancelBookingDialog = null
                    userId?.let {
                        scope.launch {
                            val result = rideRepository.cancelBooking(id, it)
                            Toast.makeText(context, if (result.isSuccess) "Booking cancelled" else "Failed to cancel booking", Toast.LENGTH_SHORT).show()
                            if (result.isSuccess) refreshRides()
                        }
                    }
                },
                onDismiss = { showCancelBookingDialog = null }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MyRidesTopBar(
    onBack: () -> Unit,
    onNotifications: () -> Unit,
    unreadCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Abyss)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(brush = Brush.horizontalGradient(listOf(CyanPrimary, PurpleAccent)))) {
                    append("MY ")
                }
                withStyle(SpanStyle(color = TextHero)) {
                    append("RIDES")
                }
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Box(modifier = Modifier.size(48.dp)) {
            IconButton(onClick = onNotifications) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = if (unreadCount > 0) CyanPrimary else TextSecondary
                )
            }
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-4).dp)
                        .size(18.dp)
                        .background(RedPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab Row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PillTabRow(selectedTab: Int, onTabChange: (Int) -> Unit) {
    val tabs = listOf("As Driver" to Icons.Default.DirectionsCar, "As Passenger" to Icons.Default.Person)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .background(Cavern, RoundedCornerShape(16.dp))
            .border(1.dp, GlassEdge, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tabs.forEachIndexed { index, (label, icon) ->
            val isSelected = selectedTab == index

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) CyanGlow else Color.Transparent)
                    .then(
                        if (isSelected) Modifier.border(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(listOf(CyanPrimary, PurpleAccent)),
                            shape = RoundedCornerShape(12.dp)
                        ) else Modifier
                    )
                    .clickable { onTabChange(index) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) CyanPrimary else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = label,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) TextHero else TextSecondary
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Premium Ride Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MyRideCard(
    ride: Ride,
    isDriver: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    val statusColor = rideStatusColor(ride.status)
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (isPressed) 0.985f else 1f
                scaleY = if (isPressed) 0.985f else 1f
            }
            .drawBehind {
                drawRoundRect(
                    color = statusColor.copy(alpha = 0.25f),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    style = Stroke(width = 6f)
                )
            }
            .background(Cavern, RoundedCornerShape(20.dp))
            .border(1.dp, GlassEdgeMid, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        // Left glowing accent
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(5.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .background(Brush.verticalGradient(listOf(statusColor, statusColor.copy(0.7f))))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(statusColor.copy(alpha = 0.12f), CircleShape)
                            .border(1.5.dp, statusColor.copy(alpha = 0.45f), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!ride.rider_avatar_url.isNullOrBlank()) {
                            AsyncImage(
                                model = ride.rider_avatar_url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = ride.rider_name.firstOrNull()?.uppercaseChar()?.toString() ?: "R",
                                color = statusColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = ride.rider_name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = ride.rider_phone ?: "—",
                            fontSize = 12.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = rideStatusLabel(ride.status),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            letterSpacing = 0.4.sp
                        )
                    }

                    // Menu Button
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondary)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Crater)
                    ) {
                        if (isDriver) {
                            NightMenuItem(Icons.Default.Edit, "Edit Ride", CyanPrimary) { showMenu = false; onEdit() }
                            NightMenuItem(Icons.Default.Delete, "Delete Ride", RedPrimary) { showMenu = false; onDelete() }
                        } else {
                            NightMenuItem(Icons.Default.Close, "Cancel Booking", RedPrimary) { showMenu = false; onCancel() }
                        }
                    }
                }
            }

            // Route
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RideInfoRow(Icons.Default.MyLocation, ride.pickup_location ?: "—", CyanPrimary)
                RideInfoRow(Icons.Default.LocationOn, ride.destination ?: "—", CoralPrimary)
            }

            // Meta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ride.departure_date?.let {
                    MetaChip(Icons.Default.CalendarToday, it, PurpleAccent)
                }
                ride.departure_time?.let {
                    MetaChip(Icons.Default.Schedule, it, GoldAccent)
                }
                MetaChip(Icons.Default.AirlineSeatReclineNormal, "${ride.seats_left} seats left", MintPrimary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RideInfoRow(icon: ImageVector, label: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        Text(
            text = label,
            fontSize = 13.5.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MetaChip(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(13.5.dp))
        Text(text = text, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
fun NightMenuItem(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, fontSize = 14.sp, color = tint, fontWeight = FontWeight.Medium) },
        onClick = onClick,
        leadingIcon = { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.background(Crater)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty & Loading States
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun EmptyRides(isDriver: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Cavern, CircleShape)
                    .border(1.5.dp, GlassEdge, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isDriver) Icons.Default.DirectionsCar else Icons.Default.Person,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(42.dp)
                )
            }

            Text(
                text = if (isDriver) "No rides posted yet" else "No bookings yet",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )

            Text(
                text = if (isDriver) "Post your first ride from the home screen"
                else "Browse available rides and book your seat",
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}

@Composable
fun LoadingRides() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(138.dp)
                    .background(Cavern.copy(alpha = shimmerAlpha), RoundedCornerShape(20.dp))
                    .border(1.dp, GlassEdgeMid, RoundedCornerShape(20.dp))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Themed Dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NightAlertDialog(
    title: String,
    body: String,
    confirmText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Crater,
        titleContentColor = TextHero,
        textContentColor = TextSecondary,
        shape = RoundedCornerShape(20.dp),
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = { Text(body, fontSize = 14.sp, lineHeight = 20.sp) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = confirmColor)
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}