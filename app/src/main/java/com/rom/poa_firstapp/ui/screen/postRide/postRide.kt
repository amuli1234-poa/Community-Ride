package com.rom.poa_firstapp.ui.screen.postRide

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.ProfileRepositoryImpl
import com.rom.poa_firstapp.data.repository.RideRepositoryImpl
import com.rom.poa_firstapp.ui.common.ErrorState
import com.rom.poa_firstapp.ui.common.LoadingState
import com.rom.poa_firstapp.ui.navigation.ROUTES
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

// ---------------------------------------------------------------------------
// Colour palette
// ---------------------------------------------------------------------------
private val GreenDeep      = Color(0xFF1A3A2A)
private val GreenMid       = Color(0xFF2D6A4F)
private val GreenBright    = Color(0xFF40916C)
private val GreenLight     = Color(0xFF52B788)
private val GreenPale      = Color(0xFFD4E8D4)
private val GreenHint      = Color(0xFF74916C)
private val GreenSurface   = Color(0xFFEAF3DE)
private val GreenSubBorder = Color(0xFFC0DD97)
private val PageBg         = Color(0xFFF4F6F3)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextPrimary    = Color(0xFF1A1A1A)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostRideScreen(navController: NavController, rideId: String? = null) {
    var isPaidRide     by remember { mutableStateOf(false) }
    var pickupLocation by remember { mutableStateOf("") }
    var destination    by remember { mutableStateOf("") }
    var seatsCount     by remember { mutableIntStateOf(3) }
    var departureTime  by remember { mutableStateOf("") }
    var departureDate  by remember { mutableStateOf("") }
    var isLoading      by remember { mutableStateOf(false) }
    var errorMessage   by remember { mutableStateOf<String?>(null) }

    val rideRepository    = remember { RideRepositoryImpl(SupabaseModule.client) }
    val profileRepository = remember { ProfileRepositoryImpl(SupabaseModule.client) }
    val scope             = rememberCoroutineScope()
    val context           = LocalContext.current

    val isEditing = rideId != null

    // Load ride data if editing
    LaunchedEffect(rideId) {
        if (rideId != null) {
            isLoading = true
            try {
                val ride = rideRepository.getRideById(rideId)
                if (ride != null) {
                    pickupLocation = ride.pickup_location ?: ""
                    destination = ride.destination ?: ""
                    seatsCount = ride.seats_left
                    departureTime = ride.departure_time ?: ""
                    departureDate = ride.departure_date ?: ""
                    isPaidRide = ride.status == "Paid"
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load ride: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Date / time pickers
    val calendar = Calendar.getInstance()
    val timePicker = TimePickerDialog(context, { _, h, m ->
        departureTime = String.format("%02d:%02d", h, m)
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    val datePicker = DatePickerDialog(context, { _, y, mo, d ->
        departureDate = String.format("%04d-%02d-%02d", y, mo + 1, d)
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    fun postRide() {
        if (pickupLocation.isBlank() || destination.isBlank() ||
            departureTime.isBlank() || departureDate.isBlank()) {
            errorMessage = "Please fill in pickup, destination, time, and date."
            return
        }
        scope.launch {
            isLoading    = true
            errorMessage = null
            try {
                val currentUser = SupabaseModule.client.auth.currentSessionOrNull()?.user
                    ?: run { errorMessage = "Session expired. Please login again."; isLoading = false; return@launch }

                val profile = profileRepository.getProfile(currentUser.id)
                    ?: run { errorMessage = "Profile not found. Please complete setup."; isLoading = false; return@launch }

                val geocoder = Geocoder(context, Locale.getDefault())
                val pickupCoords = withContext(Dispatchers.IO) {
                    try { geocoder.getFromLocationName(pickupLocation, 1)?.firstOrNull() }
                    catch (e: Exception) { Log.e("PostRide", "Pickup geocode", e); null }
                }
                val destCoords = withContext(Dispatchers.IO) {
                    try { geocoder.getFromLocationName(destination, 1)?.firstOrNull() }
                    catch (e: Exception) { Log.e("PostRide", "Dest geocode", e); null }
                }

                if (pickupCoords == null) { errorMessage = "Could not find pickup location. Please be more specific."; isLoading = false; return@launch }
                if (destCoords   == null) { errorMessage = "Could not find destination. Please be more specific.";    isLoading = false; return@launch }

                val rawPhone = profile.phone_number?.filter { it.isDigit() } ?: ""
                val formattedPhone = when {
                    rawPhone.startsWith("254") -> rawPhone
                    rawPhone.startsWith("0") -> "254" + rawPhone.substring(1)
                    rawPhone.length == 9 -> "254$rawPhone"
                    else -> rawPhone.ifBlank { "Contact via app" }
                }

                val ride = Ride(
                    id                   = rideId ?: UUID.randomUUID().toString(),
                    rider_id             = currentUser.id,
                    rider_name           = profile.full_name,
                    seats_left           = seatsCount,
                    rider_phone          = formattedPhone,
                    start_lat            = pickupCoords.latitude,
                    start_lng            = pickupCoords.longitude,
                    status               = if (isPaidRide) "Paid" else "Free",
                    pickup_location      = pickupLocation,
                    destination          = destination,
                    departure_time       = departureTime,
                    departure_date       = departureDate,
                    destination_lat      = destCoords.latitude,
                    destination_lng      = destCoords.longitude
                )
                
                val result = if (isEditing) {
                    rideRepository.updateRide(ride)
                } else {
                    rideRepository.postRide(ride)
                }

                result.onSuccess {
                        navController.navigate(ROUTES.Home.name) {
                            popUpTo(ROUTES.PostRide.name) { inclusive = true }
                        }
                        Toast.makeText(context, if(isEditing) "Ride updated!" else "Ride posted!", Toast.LENGTH_SHORT).show()
                    }
                    .onFailure { e ->
                        errorMessage = "Failed: ${e.localizedMessage}"
                        Log.e("PostRide", "Action failed", e)
                    }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unexpected error"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Edit Ride" else "Post a Ride", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenDeep)
            )
        },
        containerColor = PageBg
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                // ── Hero ─────────────────────────────────────────────────
                PostRideHero()

                // ── Form ─────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PageBg)
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {

                    // Route
                    PRSectionLabel("ROUTE DETAILS")
                    Spacer(Modifier.height(10.dp))
                    RouteField(
                        value         = pickupLocation,
                        onValueChange = { pickupLocation = it },
                        placeholder   = "Pickup location",
                        leadIcon      = Icons.Default.LocationOn,
                        onTrailingClick = {
                            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                    .addOnSuccessListener { loc ->
                                        loc?.let {
                                            val geocoder = Geocoder(context, Locale.getDefault())
                                            scope.launch(Dispatchers.IO) {
                                                val address = try { geocoder.getFromLocation(it.latitude, it.longitude, 1)?.firstOrNull()?.getAddressLine(0) } catch (e: Exception) { null }
                                                withContext(Dispatchers.Main) { if (address != null) pickupLocation = address }
                                            }
                                        }
                                    }
                            } else {
                                errorMessage = "Location permission denied. Please enable it in settings."
                            }
                        }
                    )
                    // Swap connector
                    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.width(1.5.dp).height(20.dp).background(GreenPale))
                        IconButton(
                            onClick = {
                                val temp = pickupLocation
                                pickupLocation = destination
                                destination = temp
                            },
                            modifier = Modifier.size(26.dp).clip(CircleShape).background(GreenBright)
                        ) {
                            Icon(Icons.Default.SwapVert, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    RouteField(
                        value         = destination,
                        onValueChange = { destination = it },
                        placeholder   = "Destination",
                        leadIcon      = Icons.Default.Flag
                    )

                    Spacer(Modifier.height(20.dp))

                    // Schedule
                    PRSectionLabel("SCHEDULE")
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ScheduleCard(
                            label     = "TIME",
                            value     = departureTime.ifBlank { "Select time" },
                            icon      = Icons.Default.AccessTime,
                            isFilled  = departureTime.isNotBlank(),
                            modifier  = Modifier.weight(1f),
                            onClick   = { timePicker.show() }
                        )
                        ScheduleCard(
                            label     = "DATE",
                            value     = departureDate.ifBlank { "Select date" },
                            icon      = Icons.Default.DateRange,
                            isFilled  = departureDate.isNotBlank(),
                            modifier  = Modifier.weight(1f),
                            onClick   = { datePicker.show() }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Seats
                    PRSectionLabel("AVAILABLE SEATS")
                    Spacer(Modifier.height(10.dp))
                    SeatsSelector(
                        count     = seatsCount,
                        onDecrease = { if (seatsCount > 1) seatsCount-- },
                        onIncrease = { if (seatsCount < 8) seatsCount++ }
                    )

                    Spacer(Modifier.height(20.dp))

                    // Ride type
                    PRSectionLabel("RIDE TYPE")
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PRRideTypeCard(
                            title      = "Free",
                            subtitle   = "Offer a free ride",
                            icon       = Icons.Default.Favorite,
                            isSelected = !isPaidRide,
                            modifier   = Modifier.weight(1f),
                            onClick    = { isPaidRide = false }
                        )
                        PRRideTypeCard(
                            title      = "Paid",
                            subtitle   = "Request a fare",
                            icon       = Icons.Default.AccountBalanceWallet,
                            isSelected = isPaidRide,
                            modifier   = Modifier.weight(1f),
                            onClick    = { isPaidRide = true }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Trust banner
                    PRTrustBanner()

                    Spacer(Modifier.height(20.dp))

                    // CTA
                    Button(
                        onClick        = { postRide() },
                        enabled        = !isLoading,
                        modifier       = Modifier.fillMaxWidth().height(54.dp),
                        shape          = RoundedCornerShape(16.dp),
                        colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(GreenMid, GreenBright, GreenLight),
                                        start = Offset(0f, 0f),
                                        end   = Offset(Float.POSITIVE_INFINITY, 0f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                            } else {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.DirectionsCar, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Text(if (isEditing) "Update Ride" else "Post Ride", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) LoadingState()
            // Error dialog
            errorMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { errorMessage = null },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier  = Modifier.padding(32.dp),
                        shape     = RoundedCornerShape(20.dp),
                        color     = CardWhite,
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier            = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFE24B4A), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(msg, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = { errorMessage = null },
                                shape   = RoundedCornerShape(12.dp),
                                colors  = ButtonDefaults.buttonColors(containerColor = GreenMid)
                            ) { Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Hero
// ---------------------------------------------------------------------------
@Composable
private fun PostRideHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(GreenDeep, GreenMid, GreenBright),
                    start = Offset(0f, 0f),
                    end   = Offset(500f, 400f)
                )
            )
            .drawBehind {
                val dot  = Color.White.copy(alpha = 0.06f)
                val step = 16f
                var x = 0f
                while (x < size.width) {
                    var y = 0f
                    while (y < size.height) { drawCircle(dot, 1f, Offset(x, y)); y += step }
                    x += step
                }
            }
    ) {
        // Blobs
        Box(modifier = Modifier.size(160.dp).offset(x = 240.dp, y = (-30).dp).clip(CircleShape).background(Brush.radialGradient(listOf(GreenLight.copy(alpha = 0.28f), Color.Transparent))))
        Box(modifier = Modifier.size(90.dp).offset(x = (-20).dp, y = 60.dp).clip(CircleShape).background(Brush.radialGradient(listOf(GreenBright.copy(alpha = 0.22f), Color.Transparent))))

        Column(
            modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon ring
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsCar, null, tint = Color.White, modifier = Modifier.size(38.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Share a ride. Build community. ❤️", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, lineHeight = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text("Help someone reach their destination and make our community stronger.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.65f), textAlign = TextAlign.Center, lineHeight = 18.sp)
        }
    }
    // Wave
    Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
        val w = size.width; val h = size.height
        drawRect(color = GreenBright, size = androidx.compose.ui.geometry.Size(w, h * .5f))
        val path = Path().apply {
            moveTo(0f, 0f)
            cubicTo(w * .3f, h * 1.8f, w * .7f, -h * .8f, w, h * .5f)
            lineTo(w, 0f); close()
        }
        drawPath(path, PageBg)
        drawRect(PageBg, Offset(0f, h * .5f), androidx.compose.ui.geometry.Size(w, h * .5f))
    }
}

// ---------------------------------------------------------------------------
// Route field
// ---------------------------------------------------------------------------
@Composable
private fun RouteField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadIcon: ImageVector,
    onTrailingClick: (() -> Unit)? = null
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = CardWhite, border = BorderStroke(1.5.dp, GreenPale)) {
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text(placeholder, color = Color(0xFFAAC4AA), fontSize = 14.sp) },
            leadingIcon   = { Icon(leadIcon, null, tint = GreenBright, modifier = Modifier.size(20.dp)) },
            trailingIcon  = {
                if (onTrailingClick != null) {
                    IconButton(onClick = onTrailingClick) {
                        Icon(Icons.Default.MyLocation, null, tint = GreenHint, modifier = Modifier.size(18.dp))
                    }
                } else {
                    Icon(Icons.Default.MyLocation, null, tint = Color.Transparent, modifier = Modifier.size(18.dp))
                }
            },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor     = GreenDeep,
                unfocusedTextColor   = GreenDeep,
                cursorColor          = GreenBright
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
    }
}

// ---------------------------------------------------------------------------
// Schedule card (time / date)
// ---------------------------------------------------------------------------
@Composable
private fun ScheduleCard(
    label: String,
    value: String,
    icon: ImageVector,
    isFilled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier  = modifier,
        onClick   = onClick,
        shape     = RoundedCornerShape(14.dp),
        color     = CardWhite,
        border    = BorderStroke(1.5.dp, if (isFilled) GreenBright else GreenPale)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GreenHint, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, tint = if (isFilled) GreenBright else GreenHint, modifier = Modifier.size(16.dp))
                Text(value, fontSize = 13.sp, color = if (isFilled) GreenDeep else Color(0xFFAAC4AA), fontWeight = if (isFilled) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Seats selector
// ---------------------------------------------------------------------------
@Composable
private fun SeatsSelector(count: Int, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = CardWhite, border = BorderStroke(1.5.dp, GreenPale)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Minus
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (count > 1) GreenSurface else PageBg)
                        .border(1.dp, if (count > 1) GreenPale else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable(enabled = count > 1) { onDecrease() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, null, tint = if (count > 1) GreenMid else Color.LightGray, modifier = Modifier.size(20.dp))
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(count.toString(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = GreenDeep)
                    Text("seats", fontSize = 10.sp, color = GreenHint, fontWeight = FontWeight.Bold)
                }

                // Plus
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (count < 8) GreenSurface else PageBg)
                        .border(1.dp, if (count < 8) GreenPale else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable(enabled = count < 8) { onIncrease() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = if (count < 8) GreenMid else Color.LightGray, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Seat indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(8) { idx ->
                    val isSelected = idx < count
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GreenMid else PageBg)
                            .border(1.dp, if (isSelected) GreenBright else GreenPale, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AirlineSeatReclineNormal,
                            null,
                            tint = if (isSelected) Color.White else GreenPale,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Ride type card
// ---------------------------------------------------------------------------
@Composable
fun PRRideTypeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        onClick  = onClick,
        shape    = RoundedCornerShape(14.dp),
        color    = if (isSelected) GreenSurface else CardWhite,
        border   = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) GreenBright else GreenPale)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) GreenBright.copy(alpha = 0.18f) else PageBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = if (isSelected) GreenMid else GreenHint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isSelected) GreenDeep else TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = GreenHint)
        }
    }
}

// ---------------------------------------------------------------------------
// Trust banner
// ---------------------------------------------------------------------------
@Composable
private fun PRTrustBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GreenSurface)
            .border(0.5.dp, GreenSubBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier         = Modifier.size(32.dp).clip(CircleShape).background(GreenMid),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text("Community Ride is built on trust and kindness. Be respectful and keep everyone safe.", fontSize = 12.sp, color = Color(0xFF27500A), lineHeight = 18.sp)
    }
}

// ---------------------------------------------------------------------------
// Section label
// ---------------------------------------------------------------------------
@Composable
private fun PRSectionLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenHint, letterSpacing = 1.sp)
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewPostRideScreenV2() {
    MaterialTheme { PostRideScreen(navController = rememberNavController()) }
}