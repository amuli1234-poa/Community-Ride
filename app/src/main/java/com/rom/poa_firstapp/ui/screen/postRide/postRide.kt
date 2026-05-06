package com.rom.poa_firstapp.ui.screen.postRide

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import com.rom.poa_firstapp.R
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
import java.util.UUID

// --- THEME COLORS ---
private val PrimaryGreen = Color(0xFF4CAF50)
private val DarkGreen = Color(0xFF2E7D32)
private val LightGreenBG = Color(0xFFF1F8F1)
private val MutedText = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostRideScreen(navController: NavController) {
    var isPaidRide by remember { mutableStateOf(false) }
    var pickupLocation by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var seatsCount by remember { mutableIntStateOf(3) }
    var departureTime by remember { mutableStateOf("") }
    var departureDate by remember { mutableStateOf("") }
    
    val rideRepository = remember { RideRepositoryImpl(SupabaseModule.client) }
    val profileRepository = remember { ProfileRepositoryImpl(SupabaseModule.client) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post a Ride", fontWeight = FontWeight.Bold, color = DarkGreen) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. ILLUSTRATION
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.postrideimage),
                        contentDescription = "Ride illustration",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    // 2. HEADER TEXT
                    Text(
                        "Share a ride. Build community. ❤️",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = PrimaryGreen,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Help someone reach their destination and make our community stronger.",
                        fontSize = 14.sp,
                        color = MutedText,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    // 3. INPUT FIELDS
                    LocationInputField(
                        label = "Pickup Location",
                        placeholder = "Enter pickup location",
                        icon = Icons.Default.LocationOn,
                        value = pickupLocation,
                        onValueChange = { pickupLocation = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LocationInputField(
                        label = "Destination",
                        placeholder = "Enter destination",
                        icon = Icons.Default.Place,
                        value = destination,
                        onValueChange = { destination = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Departure Time Field
                    OutlinedTextField(
                        value = departureTime,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth().clickable {
                            val calendar = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    departureTime = String.format("%02d:%02d", hour, minute)
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        label = { Text("Departure Time", color = PrimaryGreen, fontSize = 12.sp) },
                        placeholder = { Text("Select time") },
                        leadingIcon = { Icon(Icons.Default.AccessTime, null, tint = PrimaryGreen) },
                        readOnly = true,
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Color.LightGray,
                            disabledTextColor = Color.Black,
                            disabledLabelColor = PrimaryGreen,
                            disabledLeadingIconColor = PrimaryGreen,
                            disabledPlaceholderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Departure Date Field
                    OutlinedTextField(
                        value = departureDate,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth().clickable {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    departureDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        label = { Text("Departure Date", color = PrimaryGreen, fontSize = 12.sp) },
                        placeholder = { Text("Select date") },
                        leadingIcon = { Icon(Icons.Default.DateRange, null, tint = PrimaryGreen) },
                        readOnly = true,
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Color.LightGray,
                            disabledTextColor = Color.Black,
                            disabledLabelColor = PrimaryGreen,
                            disabledLeadingIconColor = PrimaryGreen,
                            disabledPlaceholderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 4. SEATS SELECTOR
                    Text("Available Seats", fontWeight = FontWeight.Bold, color = Color.Black)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = { if (seatsCount > 1) seatsCount-- },
                            modifier = Modifier.background(LightGreenBG, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Remove, null, tint = DarkGreen)
                        }
                        Text(
                            text = seatsCount.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                        IconButton(
                            onClick = { if (seatsCount < 8) seatsCount++ },
                            modifier = Modifier.background(LightGreenBG, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Add, null, tint = DarkGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. RIDE TYPE SELECTOR
                    Text("Ride Type", fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RideTypeCard(
                            title = "Free",
                            subtitle = "Offer a free ride",
                            icon = Icons.Default.Favorite,
                            isSelected = !isPaidRide,
                            modifier = Modifier.weight(1f),
                            onClick = { isPaidRide = false }
                        )
                        RideTypeCard(
                            title = "Paid",
                            subtitle = "Request a fare",
                            icon = Icons.Default.AccountBalanceWallet,
                            isSelected = isPaidRide,
                            modifier = Modifier.weight(1f),
                            onClick = { isPaidRide = true }
                        )
                    }

                    // 6. INFO BANNER
                    Card(
                        modifier = Modifier.padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = LightGreenBG),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Community Ride is built on trust and kindness. Be respectful and keep everyone safe.",
                                fontSize = 12.sp,
                                color = DarkGreen
                            )
                        }
                    }

                    // 7. MAIN ACTION BUTTON
                    Button(
                        onClick = {
                            scope.launch {
                                if (pickupLocation.isBlank() || destination.isBlank() || departureTime.isBlank() || departureDate.isBlank()) {
                                    errorMessage = "Please enter pickup, destination, time and date"
                                    return@launch
                                }

                                isLoading = true
                                errorMessage = null
                                try {
                                    val auth = SupabaseModule.client.auth
                                    val currentUser = auth.currentSessionOrNull()?.user

                                    if (currentUser == null) {
                                        errorMessage = "Session expired. Please login again."
                                        isLoading = false
                                        return@launch
                                    }
                                    
                                    val profile = profileRepository.getProfile(currentUser.id)
                                    if (profile == null) {
                                        errorMessage = "Profile not found. Please complete your profile setup."
                                        isLoading = false
                                        return@launch
                                    }

                                    // Get coordinates from address names using Geocoder
                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    
                                    val pickupCoords = withContext(Dispatchers.IO) {
                                        try {
                                            geocoder.getFromLocationName(pickupLocation, 1)?.firstOrNull()
                                        } catch (e: Exception) {
                                            Log.e("PostRide", "Geocoding pickup failed", e)
                                            null
                                        }
                                    }

                                    val destCoords = withContext(Dispatchers.IO) {
                                        try {
                                            geocoder.getFromLocationName(destination, 1)?.firstOrNull()
                                        } catch (e: Exception) {
                                            Log.e("PostRide", "Geocoding destination failed", e)
                                            null
                                        }
                                    }

                                    if (pickupCoords == null) {
                                        errorMessage = "Could not find pickup location. Please be more specific."
                                        isLoading = false
                                        return@launch
                                    }

                                    if (destCoords == null) {
                                        errorMessage = "Could not find destination. Please be more specific."
                                        isLoading = false
                                        return@launch
                                    }

                                    val ride = Ride(
                                        id = UUID.randomUUID().toString(),
                                        rider_id = currentUser.id,
                                        rider_name = profile.full_name,
                                        seats_left = seatsCount,
                                        rider_phone = profile.phone_number ?: "Contact via app",
                                        start_lat = pickupCoords.latitude,
                                        start_lng = pickupCoords.longitude,
                                        status = if (isPaidRide) "Paid" else "Free",
                                        pickup_location = pickupLocation,
                                        destination = destination,
                                        departure_time = departureTime,
                                        departure_date = departureDate,
                                        destination_lat = destCoords.latitude,
                                        destination_lng = destCoords.longitude
                                    )
                                    
                                    val result = rideRepository.postRide(ride)
                                    result.onSuccess {
                                        navController.navigate(ROUTES.Home.name) {
                                            popUpTo(ROUTES.PostRide.name) { inclusive = true }
                                        }
                                    }.onFailure { e ->
                                        errorMessage = "Database Error: ${e.localizedMessage ?: "Check RLS policies or connection"}"
                                        Log.e("PostRide", "Post ride failed", e)
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.localizedMessage ?: "An unexpected error occurred"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsCar, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Post Ride", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                LoadingState()
            }

            errorMessage?.let {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)).clickable { errorMessage = null }) {
                    Card(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(it, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { errorMessage = null },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationInputField(label: String, placeholder: String, icon: ImageVector, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = PrimaryGreen, fontSize = 12.sp) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, null, tint = PrimaryGreen) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = PrimaryGreen
        ),
        singleLine = true
    )
}

@Composable
fun RideTypeCard(title: String, subtitle: String, icon: ImageVector, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val borderColor = if (isSelected) PrimaryGreen else Color.LightGray
    val bgColor = if (isSelected) LightGreenBG else Color.Transparent

    Card(
        modifier = modifier.clickable { onClick() },
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isSelected) PrimaryGreen else MutedText, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = if (isSelected) DarkGreen else Color.Black)
                Text(subtitle, fontSize = 10.sp, color = MutedText)
            }
        }
    }
}
