package com.rom.poa_firstapp.ui.screen.postRide
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import com.rom.poa_firstapp.R
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rom.poa_firstapp.ui.theme.GreenPrimary
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.repository.RideRepositoryImpl
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.ui.navigation.ROUTES
import com.rom.poa_firstapp.ui.common.LoadingState
import com.rom.poa_firstapp.ui.common.ErrorState
import kotlinx.coroutines.launch
import java.util.UUID
import io.github.jan.supabase.auth.auth
import com.rom.poa_firstapp.data.repository.ProfileRepositoryImpl

// --- THEME COLORS FROM IMAGE ---
private val PrimaryGreen = Color(0xFF4CAF50)
private val DarkGreen = Color(0xFF2E7D32)
private val LightGreenBG = Color(0xFFF1F8F1)
private val MutedText = Color(0xFF757575)

// ... (existing code)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostRideScreen(navController: NavHostController) {
    var isPaidRide by remember { mutableStateOf(false) }
    var pickupLocation by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    
    val rideRepository = remember { RideRepositoryImpl(SupabaseModule.client) }
    val profileRepository = remember { ProfileRepositoryImpl(SupabaseModule.client) }
    val scope = rememberCoroutineScope()
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

                    color = GreenPrimary
                )
                Text(
                    "Help someone reach their destination and make our community stronger.",
                    fontSize = 14.sp,
                    color = MutedText,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // 3. INPUT FIELDS (Simplified style)
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

                Spacer(modifier = Modifier.height(24.dp))

                // 4. RIDE TYPE SELECTOR
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

                // 5. INFO BANNER
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

                // 6. MAIN ACTION BUTTON
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val currentUser = SupabaseModule.client.auth.currentUserOrNull()
                                if (currentUser == null) {
                                    errorMessage = "Please login to post a ride"
                                    return@launch
                                }
                                
                                val profile = profileRepository.getProfile(currentUser.id)
                                if (profile == null) {
                                    errorMessage = "Profile not found"
                                    return@launch
                                }

                                val ride = Ride(
                                    id = UUID.randomUUID().toString(),
                                    rider_id = currentUser.id,
                                    rider_name = profile.full_name,
                                    seats_left = 3,
                                    rider_phone = profile.phone_number ?: "0000000000",
                                    start_lat = 0.0, // Placeholder
                                    start_lng = 0.0, // Placeholder
                                    status = if (isPaidRide) "Paid" else "Free"
                                )
                                val success = rideRepository.postRide(ride)
                                if (success) {
                                    navController.navigate(ROUTES.Home.name)
                                } else {
                                    errorMessage = "Failed to post ride. Please try again."
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error: ${e.localizedMessage}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Post Ride", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
        
        if (isLoading) {
            LoadingState()
        }
        
        errorMessage?.let {
            ErrorState(message = it)
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
        trailingIcon = { Icon(Icons.Default.MyLocation, null, tint = MutedText) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = PrimaryGreen
        )
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


