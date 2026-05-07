package com.rom.poa_firstapp.ui.screen.rideDetails

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.MessageRepositoryImpl
import com.rom.poa_firstapp.data.repository.RideRepositoryImpl
import com.rom.poa_firstapp.ui.common.LoadingState
import com.rom.poa_firstapp.ui.navigation.ROUTES
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Design Tokens
// ─────────────────────────────────────────────────────────────────────────────
private val Abyss        = Color(0xFF080C1C)
private val Cavern       = Color(0xFF0E1325)
private val Crater       = Color(0xFF141929)
private val GlassEdge    = Color(0x18FFFFFF)
private val GlassEdgeMid = Color(0x30FFFFFF)

private val CyanPrimary  = Color(0xFF00E5FF)
private val CyanGlow     = Color(0x4400E5FF)
private val CoralPrimary = Color(0xFFFF4D7D)
private val MintPrimary  = Color(0xFF00FFA3)
private val GoldAccent   = Color(0xFFFFBB00)
private val RedPrimary   = Color(0xFFFF3B47)
private val PurpleAccent = Color(0xFFAA55FF)

private val TextHero      = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFFE8EEFF)
private val TextSecondary = Color(0xFF8896B8)
private val TextMuted     = Color(0xFF4A5568)

// ─────────────────────────────────────────────────────────────────────────────
// Status helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun statusColor(status: String) = when (status.lowercase()) {
    "free"    -> MintPrimary
    "paid"    -> GoldAccent
    "pending" -> CoralPrimary
    "full"    -> RedPrimary
    else      -> CyanPrimary
}
private fun statusBg(status: String) = statusColor(status).copy(alpha = 0.12f)
private fun statusBorder(status: String) = statusColor(status).copy(alpha = 0.30f)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDetailsScreen(
    navController: NavHostController,
    rideId: String
) {
    val context           = LocalContext.current
    val scope             = rememberCoroutineScope()
    val rideRepository    = remember { RideRepositoryImpl(SupabaseModule.client) }
    val messageRepository = remember { MessageRepositoryImpl(SupabaseModule.client) }

    var ride      by remember { mutableStateOf<Ride?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isBooking by remember { mutableStateOf(false) }

    LaunchedEffect(rideId) {
        ride      = rideRepository.getRideById(rideId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Ride Details", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 17.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cavern)
            )
        },
        containerColor = Abyss
    ) { padding ->
        if (isLoading) {
            LoadingState()
            return@Scaffold
        }

        if (ride == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ride not found", color = TextSecondary, fontSize = 15.sp)
            }
            return@Scaffold
        }

        val r = ride!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero ────────────────────────────────────────────────────
            RideHero(ride = r)

            // ── Body ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Abyss)
                    .padding(horizontal = 18.dp)
                    .padding(top = 18.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Trip info grid
                RDSectionLabel("TRIP INFO")
                TripInfoGrid(ride = r)

                // Rider card
                RDSectionLabel("RIDER")
                RiderCard(ride = r, onViewProfile = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("profileId", r.rider_id)
                    navController.navigate(ROUTES.Profile.name)
                })

                // Action buttons
                ActionButtons(
                    ride      = r,
                    isBooking = isBooking,
                    onMessage = {
                        val rawPhone = r.rider_phone.filter { it.isDigit() }
                        val phone = when {
                            rawPhone.startsWith("254") -> rawPhone
                            rawPhone.startsWith("0")   -> "254" + rawPhone.substring(1)
                            rawPhone.length == 9       -> "254$rawPhone"
                            else                       -> rawPhone
                        }
                        val text = "Hello ${r.rider_name}, I'm interested in your ride to ${r.destination}."
                        val currentUserId = SupabaseModule.client.auth.currentSessionOrNull()?.user?.id
                        if (currentUserId != null) {
                            scope.launch {
                                messageRepository.startWhatsAppConversation(
                                    senderId    = currentUserId,
                                    recipientId = r.rider_id,
                                    content     = text
                                )
                            }
                        }
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(text)}")))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onBook = {
                        scope.launch {
                            isBooking = true
                            val userId = SupabaseModule.client.auth.currentUserOrNull()?.id
                            if (userId != null) {
                                val result = rideRepository.bookRide(r.id, userId)
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Ride booked successfully! 🚗", Toast.LENGTH_LONG).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "Please login to book a ride", Toast.LENGTH_SHORT).show()
                            }
                            isBooking = false
                        }
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RideHero(ride: Ride) {
    val status = ride.status
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cavern)
            .drawBehind {
                // Grid lines
                val lineColor = Color.White.copy(alpha = 0.03f)
                val step = 28.dp.toPx()
                var x = 0f
                while (x < size.width) { drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), 1f); x += step }
                var y = 0f
                while (y < size.height) { drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 1f); y += step }
            }
    ) {
        // Ambient glow blobs
        Box(modifier = Modifier.size(180.dp).offset(x = 220.dp, y = (-40).dp).clip(CircleShape).background(Brush.radialGradient(listOf(CyanPrimary.copy(alpha = 0.18f), Color.Transparent))))
        Box(modifier = Modifier.size(130.dp).offset(x = (-30).dp, y = 60.dp).clip(CircleShape).background(Brush.radialGradient(listOf(PurpleAccent.copy(alpha = 0.14f), Color.Transparent))))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Status pill
            Row(
                modifier          = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(statusBg(status))
                    .border(1.dp, statusBorder(status), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(statusColor(status))
                )
                Text(
                    status.uppercase(),
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = statusColor(status),
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            // Pickup
            RouteRow(
                icon      = Icons.Default.RadioButtonChecked,
                iconColor = CyanPrimary,
                iconBg    = CyanPrimary.copy(alpha = 0.12f),
                label     = "PICKUP",
                labelColor = CyanPrimary,
                value     = ride.pickup_location ?: "Unknown Location"
            )

            // Connector
            Row(modifier = Modifier.padding(start = 17.dp, top = 4.dp, bottom = 4.dp)) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(22.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(CyanPrimary.copy(alpha = 0.5f), CoralPrimary.copy(alpha = 0.5f)),
                                start = Offset(0f, 0f),
                                end   = Offset(0f, Float.POSITIVE_INFINITY)
                            )
                        )
                )
            }

            // Destination
            RouteRow(
                icon       = Icons.Default.LocationOn,
                iconColor  = CoralPrimary,
                iconBg     = CoralPrimary.copy(alpha = 0.12f),
                label      = "DESTINATION",
                labelColor = CoralPrimary,
                value      = ride.destination ?: "Unknown Location"
            )
        }
    }

    // Wave
    Canvas(modifier = Modifier.fillMaxWidth().height(22.dp)) {
        val w = size.width; val h = size.height
        drawRect(Cavern, size = androidx.compose.ui.geometry.Size(w, h * .5f))
        val path = Path().apply {
            moveTo(0f, 0f)
            cubicTo(w * .25f, h * 2f, w * .75f, -h * 1f, w, h * .5f)
            lineTo(w, 0f); close()
        }
        drawPath(path, Abyss)
        drawRect(Abyss, Offset(0f, h * .5f), androidx.compose.ui.geometry.Size(w, h * .5f))
    }
}

@Composable
private fun RouteRow(
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    label: String,
    labelColor: Color,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier         = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = labelColor, letterSpacing = 0.5.sp)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Trip info grid
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TripInfoGrid(ride: Ride) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RDInfoCard(icon = Icons.Default.Event,    iconColor = GoldAccent,   label = "Date",       value = ride.departure_date ?: "N/A",            modifier = Modifier.weight(1f))
            RDInfoCard(icon = Icons.Default.Schedule, iconColor = CyanPrimary,  label = "Time",       value = ride.departure_time ?: "N/A",            modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RDInfoCard(icon = Icons.Default.AirlineSeatReclineNormal, iconColor = MintPrimary,  label = "Seats Left", value = "${ride.seats_left} available", modifier = Modifier.weight(1f))
            RDInfoCard(icon = Icons.Default.Payments, iconColor = PurpleAccent, label = "Type",       value = ride.status,                             modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RDInfoCard(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(14.dp),
        color    = Cavern,
        border   = BorderStroke(1.dp, GlassEdge)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier         = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label.uppercase(), fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Rider card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RiderCard(ride: Ride, onViewProfile: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick  = onViewProfile,
        shape    = RoundedCornerShape(16.dp),
        color    = Cavern,
        border   = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier         = Modifier.size(50.dp).clip(CircleShape).background(Crater).border(2.dp, CyanPrimary.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (ride.rider_avatar_url != null) {
                    AsyncImage(model = ride.rider_avatar_url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, null, tint = CyanPrimary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ride.rider_name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text("View Rider Profile", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Action buttons
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ActionButtons(
    ride: Ride,
    isBooking: Boolean,
    onMessage: () -> Unit,
    onBook: () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Message button
        OutlinedButton(
            onClick  = onMessage,
            modifier = Modifier.weight(1f).height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            border   = BorderStroke(1.5.dp, CyanPrimary.copy(alpha = 0.4f)),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary)
        ) {
            Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Message", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        // Book button
        Button(
            onClick  = onBook,
            enabled  = !isBooking && ride.seats_left > 0,
            modifier = Modifier.weight(1.5f).height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (ride.seats_left > 0)
                            Brush.linearGradient(listOf(CyanPrimary, MintPrimary), Offset(0f, 0f), Offset(Float.POSITIVE_INFINITY, 0f))
                        else
                            Brush.linearGradient(listOf(TextMuted, TextMuted)),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isBooking) {
                    CircularProgressIndicator(color = Abyss, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            if (ride.seats_left > 0) Icons.Default.RocketLaunch else Icons.Default.Block,
                            null,
                            tint     = Abyss,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            if (ride.seats_left > 0) "Book Now" else "Full",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Abyss
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section label
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RDSectionLabel(text: String) {
    Text(text, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewRideDetailsScreen() {
    MaterialTheme {
        RideDetailsScreen(navController = rememberNavController(), rideId = "preview")
    }
}