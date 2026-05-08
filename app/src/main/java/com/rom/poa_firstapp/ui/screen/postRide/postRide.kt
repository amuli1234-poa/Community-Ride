package com.rom.poa_firstapp.ui.screen.postRide

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.ProfileRepositoryImpl
import com.rom.poa_firstapp.data.repository.RideRepositoryImpl
import com.rom.poa_firstapp.ui.common.ErrorState
import com.rom.poa_firstapp.ui.common.LoadingState
import com.rom.poa_firstapp.ui.navigation.ROUTES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

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
// Main Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostRideScreen(
    navController: NavController,
    rideId: String? = null
) {
    val context = LocalContext.current
    val viewModel: PostRideViewModel = viewModel {
        PostRideViewModel(
            RideRepositoryImpl(SupabaseModule.client),
            ProfileRepositoryImpl(SupabaseModule.client),
            SupabaseModule.client,
            Geocoder(context)
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Load ride if editing
    LaunchedEffect(rideId) {
        viewModel.loadRide(rideId)
    }

    // Navigation on success
    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            Toast.makeText(context, if (viewModel.isEditing) "Ride updated!" else "Ride posted!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    val calendar = Calendar.getInstance()
    val timePicker = TimePickerDialog(context, { _, h, m ->
        viewModel.departureTime = String.format("%02d:%02d", h, m)
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    val datePicker = DatePickerDialog(context, { _, y, mo, d ->
        viewModel.departureDate = String.format("%04d-%02d-%02d", y, mo + 1, d)
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Ride" else "Post a Ride", fontWeight = FontWeight.Bold, color = TextHero) },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                PostRideHero()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    PRSectionLabel("ROUTE")
                    RouteField(
                        value = viewModel.pickupLocation,
                        onValueChange = { viewModel.pickupLocation = it },
                        placeholder = "Pickup Location",
                        leadIcon = Icons.Default.MyLocation,
                        accentColor = CyanPrimary,
                        onTrailingClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                val fused = LocationServices.getFusedLocationProviderClient(context)
                                fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                    .addOnSuccessListener { loc ->
                                        loc?.let {
                                            viewModel.updatePickupFromLocation(it.latitude, it.longitude)
                                        }
                                    }
                            } else {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = {
                                val temp = viewModel.pickupLocation
                                viewModel.pickupLocation = viewModel.destination
                                viewModel.destination = temp
                            },
                            modifier = Modifier.size(38.dp).background(CyanGlow, CircleShape)
                        ) {
                            Icon(Icons.Default.SwapVert, null, tint = CyanPrimary)
                        }
                    }

                    RouteField(viewModel.destination, { viewModel.destination = it }, "Destination", Icons.Default.LocationOn, CoralPrimary)

                    PRSectionLabel("SCHEDULE")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ScheduleCard("TIME", viewModel.departureTime.ifBlank { "Select Time" }, Icons.Default.Schedule, GoldAccent, Modifier.weight(1f)) { timePicker.show() }
                        ScheduleCard("DATE", viewModel.departureDate.ifBlank { "Select Date" }, Icons.Default.CalendarToday, PurpleAccent, Modifier.weight(1f)) { datePicker.show() }
                    }

                    PRSectionLabel("AVAILABLE SEATS")
                    SeatsSelector(
                        count = viewModel.seatsCount,
                        onMinus = { viewModel.decrementSeats() },
                        onPlus = { viewModel.incrementSeats() }
                    )

                    PRSectionLabel("RIDE TYPE")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PRRideTypeCard("Free", "Offer free ride", Icons.Default.Favorite, !viewModel.isPaidRide, MintPrimary, Modifier.weight(1f)) { viewModel.setPaid(false) }
                        PRRideTypeCard("Paid", "Charge a fare", Icons.Default.Payments, viewModel.isPaidRide, RedPrimary, Modifier.weight(1f)) { viewModel.setPaid(true) }
                    }

                    PRTrustBanner()

                    Button(
                        onClick = { viewModel.postRide() },
                        enabled = !viewModel.isLoading,
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(CyanPrimary, PurpleAccent)),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(color = TextHero, modifier = Modifier.size(26.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Default.DirectionsCar, null, tint = TextHero, modifier = Modifier.size(22.dp))
                                    Text(
                                        if (viewModel.isEditing) "Update Ride" else "Post Ride Now",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextHero
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (viewModel.isLoading) LoadingState()
            viewModel.errorMessage?.let { ErrorState(message = it, onDismiss = { viewModel.errorMessage = null }) }
        }
    }
}

@Composable
fun PostRideHero() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Crater, Abyss))
            )
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(CyanGlow, CircleShape)
                .border(2.dp, CyanPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ElectricCar, null, tint = CyanPrimary, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Share the Journey",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextHero,
            textAlign = TextAlign.Center
        )
        Text(
            "Reduce carbon footprint and meet new people",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun PRSectionLabel(label: String) {
    Text(
        label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = CyanPrimary,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun RouteField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadIcon: ImageVector,
    accentColor: Color,
    onTrailingClick: (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMuted) },
        leadingIcon = { Icon(leadIcon, null, tint = accentColor) },
        trailingIcon = onTrailingClick?.let {
            {
                IconButton(onClick = it) {
                    Icon(Icons.Default.MyLocation, null, tint = TextSecondary)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassEdge, RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Cavern,
            unfocusedContainerColor = Cavern,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun ScheduleCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(Cavern, RoundedCornerShape(12.dp))
            .border(1.dp, GlassEdge, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextHero)
    }
}

@Composable
fun SeatsSelector(
    count: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cavern, RoundedCornerShape(12.dp))
            .border(1.dp, GlassEdge, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(40.dp).background(CyanGlow, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Groups, null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
            }
            Text("Available Seats", color = TextPrimary, fontWeight = FontWeight.Medium)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            IconButton(onClick = onMinus, modifier = Modifier.size(32.dp).border(1.dp, GlassEdgeMid, CircleShape)) {
                Icon(Icons.Default.Remove, null, tint = TextPrimary, modifier = Modifier.size(16.dp))
            }
            Text(count.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
            IconButton(onClick = onPlus, modifier = Modifier.size(32.dp).border(1.dp, CyanPrimary.copy(alpha = 0.4f), CircleShape)) {
                Icon(Icons.Default.Add, null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun PRRideTypeCard(
    title: String,
    desc: String,
    icon: ImageVector,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) color.copy(alpha = 0.12f) else Cavern
    val border = if (selected) color.copy(alpha = 0.6f) else GlassEdge

    Column(
        modifier = modifier
            .background(bg, RoundedCornerShape(14.dp))
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = if (selected) color else TextSecondary, modifier = Modifier.size(20.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = if (selected) TextHero else TextPrimary)
            Text(desc, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
fun PRTrustBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GoldAccent.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .border(1.dp, GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.Verified, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
        Text(
            "Your profile is verified. Riders see your trust score and history.",
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}
