package com.rom.poa_firstapp.ui.screen.myRides

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.rom.poa_firstapp.data.model.Rating
import coil3.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rom.poa_firstapp.data.model.BookingWithProfile
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.NotificationRepositoryImpl
import com.rom.poa_firstapp.data.repository.ProfileRepositoryImpl
import com.rom.poa_firstapp.data.repository.RideRepositoryImpl
import com.rom.poa_firstapp.ui.navigation.ROUTES
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MyRidesViewModelFactory(
    private val rideRepository: com.rom.poa_firstapp.data.repository.RideRepository,
    private val notificationRepository: com.rom.poa_firstapp.data.repository.NotificationRepository,
    private val profileRepository: com.rom.poa_firstapp.data.repository.ProfileRepository,
    private val userId: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyRidesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyRidesViewModel(rideRepository, notificationRepository, profileRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

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
    val userId = remember { SupabaseModule.client.auth.currentSessionOrNull()?.user?.id }
    val viewModel: MyRidesViewModel = viewModel(
        factory = MyRidesViewModelFactory(
            RideRepositoryImpl(SupabaseModule.client),
            NotificationRepositoryImpl(SupabaseModule.client),
            ProfileRepositoryImpl(SupabaseModule.client),
            userId
        )
    )

    val selectedTab = viewModel.selectedTab
    val isLoading = viewModel.isLoading
    val unreadCount = viewModel.unreadCount
    val driverRides = viewModel.driverRides
    val passengerRides = viewModel.passengerRides
    val rideBookings = viewModel.rideBookings

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showCancelBookingDialog by remember { mutableStateOf<String?>(null) }
    var showCompleteRideDialog by remember { mutableStateOf<String?>(null) }
    var showRatingDialog by remember { mutableStateOf<Triple<String, String, String>?>(null) } // rideId, ratedUserId, ratedUserName

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

            PillTabRow(selectedTab = selectedTab, onTabChange = { viewModel.onTabChange(it) })

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
                                bookings = rideBookings[ride.id] ?: emptyList(),
                                onClick = { navController.navigate("${ROUTES.RideDetails.name}/${ride.id}") },
                                onEdit = { navController.navigate("${ROUTES.PostRide.name}?rideId=${ride.id}") },
                                onDelete = { showDeleteDialog = ride.id },
                                onCancel = { showCancelBookingDialog = ride.id },
                                onComplete = { showCompleteRideDialog = ride.id },
                                onRateDriver = { showRatingDialog = Triple(ride.id, ride.rider_id, ride.rider_name) },
                                onRatePassenger = { passenger ->
                                    showRatingDialog = Triple(ride.id, passenger.user_id, passenger.full_name)
                                },
                                onWhatsAppPassenger = { passenger ->
                                    val rawPhone = passenger.phone_number?.filter { it.isDigit() } ?: ""
                                    val phone = when {
                                        rawPhone.startsWith("254") -> rawPhone
                                        rawPhone.startsWith("0")   -> "254" + rawPhone.substring(1)
                                        rawPhone.length == 9       -> "254$rawPhone"
                                        else                       -> rawPhone
                                    }
                                    val text = "Hello ${passenger.full_name}, I'm the driver for your ride to ${ride.destination}."
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(text)}")))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onProfilePassenger = { passenger ->
                                    navController.navigate("${ROUTES.Profile.name}?profileId=${passenger.user_id}")
                                }
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
                        val result = viewModel.deleteRide(id)
                        Toast.makeText(context, if (result.isSuccess) "Ride deleted successfully" else "Failed to delete ride", Toast.LENGTH_SHORT).show()
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
                    scope.launch {
                        val result = viewModel.cancelBooking(id)
                        Toast.makeText(context, if (result.isSuccess) "Booking cancelled" else "Failed to cancel booking", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = { showCancelBookingDialog = null }
            )
        }

        showCompleteRideDialog?.let { id ->
            NightAlertDialog(
                title = "Complete Ride",
                body = "Mark this ride as completed? This will archive the ride.",
                confirmText = "Complete",
                confirmColor = MintPrimary,
                onConfirm = {
                    showCompleteRideDialog = null
                    scope.launch {
                        val result = viewModel.completeRide(id)
                        Toast.makeText(context, if (result.isSuccess) "Ride completed" else "Failed to complete ride", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = { showCompleteRideDialog = null }
            )
        }

        showRatingDialog?.let { (rideId, ratedId, ratedName) ->
            RatingDialog(
                targetName = ratedName,
                onDismiss = { showRatingDialog = null },
                onSubmit = { rating, comment ->
                    scope.launch {
                        val result = viewModel.submitRating(rideId, ratedId, rating, comment)
                        Toast.makeText(context, if (result.isSuccess) "Rating submitted for $ratedName" else "Failed to submit rating", Toast.LENGTH_SHORT).show()
                        showRatingDialog = null
                    }
                }
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
    bookings: List<BookingWithProfile> = emptyList(),
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    onRateDriver: () -> Unit = {},
    onRatePassenger: (BookingWithProfile) -> Unit = {},
    onWhatsAppPassenger: (BookingWithProfile) -> Unit = {},
    onProfilePassenger: (BookingWithProfile) -> Unit = {}
) {
    val statusColor = rideStatusColor(ride.status)
    var showMenu by remember { mutableStateOf(false) }
    var showPassengers by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (isPressed) 0.985f else 1f
                scaleY = if (isPressed) 0.985f else 1f
            }
            .background(Cavern, RoundedCornerShape(20.dp))
            .border(1.dp, GlassEdgeMid, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Left glowing accent
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(5.dp)
                    .height(140.dp) // Approximate height for the main info
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 0.dp))
                    .background(Brush.verticalGradient(listOf(statusColor, statusColor.copy(0.7f))))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header (same as before)
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
                                if (ride.status != "completed" && ride.status != "cancelled") {
                                    NightMenuItem(Icons.Default.CheckCircle, "Complete Ride", MintPrimary) { showMenu = false; onComplete() }
                                    NightMenuItem(Icons.Default.Edit, "Edit Ride", CyanPrimary) { showMenu = false; onEdit() }
                                }
                                NightMenuItem(Icons.Default.Delete, "Delete Ride", RedPrimary) { showMenu = false; onDelete() }
                            } else {
                                if (ride.status != "completed" && ride.status != "cancelled") {
                                    NightMenuItem(Icons.Default.Close, "Cancel Booking", RedPrimary) { showMenu = false; onCancel() }
                                } else if (ride.status == "completed") {
                                    NightMenuItem(Icons.Default.Star, "Rate Driver", GoldAccent) { showMenu = false; onRateDriver() }
                                }
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

        // Passenger Section for Drivers
        if (isDriver && bookings.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = GlassEdge, thickness = 1.dp)
            
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPassengers = !showPassengers },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Passengers (${bookings.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Icon(
                        if (showPassengers) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = CyanPrimary
                    )
                }

                if (showPassengers) {
                    Spacer(modifier = Modifier.height(12.dp))
                    bookings.forEach { booking ->
                        PassengerRow(
                            booking = booking,
                            showRateButton = ride.status == "completed",
                            onRate = { onRatePassenger(booking) },
                            onWhatsApp = { onWhatsAppPassenger(booking) },
                            onProfile = { onProfilePassenger(booking) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PassengerRow(
    booking: BookingWithProfile,
    showRateButton: Boolean = false,
    onRate: () -> Unit = {},
    onWhatsApp: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Crater, RoundedCornerShape(12.dp))
            .clickable { onProfile() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(PurpleAccent.copy(alpha = 0.1f), CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!booking.avatar_url.isNullOrBlank()) {
                AsyncImage(
                    model = booking.avatar_url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = booking.full_name.firstOrNull()?.uppercaseChar()?.toString() ?: "P",
                    color = PurpleAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(booking.full_name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(booking.phone_number ?: "No phone", fontSize = 12.sp, color = TextSecondary)
        }

        if (showRateButton) {
            IconButton(onClick = onRate, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Star, contentDescription = "Rate Passenger", tint = GoldAccent, modifier = Modifier.size(20.dp))
            }
        } else {
            IconButton(
                onClick = onWhatsApp,
                modifier = Modifier
                    .size(36.dp)
                    .background(MintPrimary.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Chat,
                    contentDescription = "WhatsApp",
                    tint = MintPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .background(MintPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    booking.status.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintPrimary
                )
            }
        }
    }
}

@Composable
fun RatingDialog(
    targetName: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Crater, RoundedCornerShape(20.dp))
                .border(1.dp, GlassEdge, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Rate $targetName", color = TextHero, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(5) { index ->
                    val starLevel = index + 1
                    IconButton(onClick = { rating = starLevel }) {
                        Icon(
                            if (starLevel <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (starLevel <= rating) GoldAccent else TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text("Add a comment (optional)", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = CyanPrimary,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = GlassEdge
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel", color = TextSecondary)
                }
                Button(
                    onClick = { onSubmit(rating, comment) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit", color = Abyss, fontWeight = FontWeight.Bold)
                }
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