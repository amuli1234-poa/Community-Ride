package com.rom.poa_firstapp.ui.screen.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import java.util.Locale
import com.rom.poa_firstapp.R
import com.rom.poa_firstapp.data.model.RiderProfile

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import io.github.jan.supabase.auth.auth
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.ProfileRepositoryImpl
import com.rom.poa_firstapp.ui.navigation.ROUTES
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.rom.poa_firstapp.ui.common.LoadingState
import com.rom.poa_firstapp.ui.common.ErrorState

// Data class for Rider Profile (matches Supabase 'profiles' table)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderProfileScreen(
    profileId: String? = null,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel {
        ProfileViewModel(
            ProfileRepositoryImpl(SupabaseModule.client),
            SupabaseModule.client
        )
    }
) {
    val context = LocalContext.current
    val currentProfile = viewModel.profile
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(profileId) {
        viewModel.loadProfile(profileId)
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            ProfileTopAppBar(
                onBack = onBack,
                isOwnProfile = profileId == null || profileId == SupabaseModule.client.auth.currentUserOrNull()?.id,
                onEditClick = { isEditing = !isEditing }
            )
        },
        bottomBar = {
            if (currentProfile != null && profileId != null && profileId != SupabaseModule.client.auth.currentUserOrNull()?.id) {
                MessageRiderButton(currentProfile.full_name, currentProfile.phone_number ?: "", context)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (currentProfile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF0F2F5))
                        .verticalScroll(rememberScrollState())
                ) {
                    ProfileHeader(profile = currentProfile, isEditing = isEditing, onProfileUpdate = {
                        viewModel.updateProfile(it)
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileStats(profile = currentProfile)
                    Spacer(modifier = Modifier.height(16.dp))
                    BadgesSection()
                    Spacer(modifier = Modifier.height(16.dp))
                    VerificationSection(profile = currentProfile)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            if (viewModel.isLoading) {
                LoadingState()
            }

            viewModel.errorMessage?.let {
                ErrorState(message = it)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopAppBar(onBack: () -> Unit, isOwnProfile: Boolean, onEditClick: () -> Unit) {
    TopAppBar(
        title = { Text("User Profile", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (isOwnProfile) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                }
            }
            IconButton(onClick = { /* TODO: Handle more options */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun ProfileHeader(
    profile: RiderProfile,
    isEditing: Boolean = false,
    onProfileUpdate: (RiderProfile) -> Unit = {}
) {
    var fullName by remember(profile.full_name) { mutableStateOf(profile.full_name) }
    var bio by remember(profile.bio) { mutableStateOf(profile.bio ?: "") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color(0xFFE8F5E9)) // Light green background
    ) {
        // Background car image
        Image(
            painter = painterResource(id = R.drawable.ic_car), // Use a car icon as placeholder
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(150.dp)
                .offset(x = 30.dp, y = 30.dp),
            contentScale = ContentScale.Fit,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0x444CAF50)) // Semi-transparent green
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Picture
                val painter = rememberAsyncImagePainter(profile.avatar_url ?: R.drawable.ic_profile) // Fallback to default icon
                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    if (isEditing) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(text = profile.full_name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Verified", color = Color(0xFF4CAF50), fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isEditing) {
                var phoneNumber by remember(profile.phone_number) { mutableStateOf(profile.phone_number ?: "") }
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onProfileUpdate(profile.copy(full_name = fullName, bio = bio, phone_number = phoneNumber)) },
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                ) {
                    Text("Save")
                }
            } else {
                Text(text = profile.bio ?: "Community rider, always happy to help!", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ProfileStats(profile: RiderProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatCard(icon = painterResource(id = R.drawable.ic_car), value = profile.rides_given.toString(), label = "Rides Given", description = "Going the extra mile")
        StatCard(icon = painterResource(id = R.drawable.ic_profile), value = profile.rides_taken.toString(), label = "Rides Taken", description = "Thanks, community!")
        StatCard(icon = Icons.Default.Star, value = String.format(Locale.US, "%.1f", profile.community_rating), label = "Community Rating", description = "Based on ${profile.total_reviews} reviews")
    }
}

@Composable
fun StatCard(icon: Any, value: String, label: String, description: String) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (icon) {
                is androidx.compose.ui.graphics.painter.Painter -> {
                    Icon(painter = icon, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                }
                is androidx.compose.ui.graphics.vector.ImageVector -> {
                    Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                }
            }
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = label, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            Text(text = description, fontSize = 10.sp, color = Color.LightGray, maxLines = 1)
        }
    }
}

@Composable
fun BadgesSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Badges", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { /* TODO: Navigate to all badges */ }) {
                Text("View all", color = Color(0xFF4CAF50))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            BadgeCard(title = "Top Helper", description = "Helped 50+ riders", icon = painterResource(id = R.drawable.ic_shield_check), color = Color(0xFFE8F5E9))
            BadgeCard(title = "Eco-Friendly", description = "Reduced 100+ kg CO₂", icon = painterResource(id = R.drawable.ic_leaf), color = Color(0xFFE8F5E9))
            BadgeCard(title = "Community Star", description = "Rated 4.5+ by the community", icon = painterResource(id = R.drawable.ic_group), color = Color(0xFFE3F2FD))
        }
    }
}

@Composable
fun BadgeCard(title: String, description: String, icon: androidx.compose.ui.graphics.painter.Painter, color: Color) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(36.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1)
            Text(text = description, fontSize = 10.sp, color = Color.Gray, maxLines = 2)
        }
    }
}

@Composable
fun VerificationSection(profile: RiderProfile) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Verification", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        VerificationItem(icon = Icons.Default.Person, label = "Member since", value = profile.member_since?.substringBefore("T") ?: "N/A")
        VerificationItem(icon = Icons.Default.Phone, label = "Phone verified", isVerified = profile.phone_verified)
        VerificationItem(icon = Icons.Default.Email, label = "Email verified", isVerified = profile.email_verified)
    }
}

@Composable
fun VerificationItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String? = null, isVerified: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, fontSize = 16.sp, color = Color.Black)
        }
        if (value != null) {
            Text(text = value, fontSize = 16.sp, color = Color.Gray)
        } else {
            Icon(
                if (isVerified) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = if (isVerified) "Verified" else "Not Verified",
                tint = if (isVerified) Color(0xFF4CAF50) else Color.Red,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun MessageRiderButton(riderName: String, phoneNumber: String, context: Context) {
    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=Hi $riderName, I saw your ride on Community Ride app...")
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback for when WhatsApp is not installed
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com/send?phone=$phoneNumber&text=Hi $riderName, I saw your ride on Community Ride app..."))
                context.startActivity(browserIntent)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(painterResource(id = R.drawable.ic_message), contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Message $riderName", fontSize = 18.sp, color = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRiderProfileScreen() {
    CommunityRideTheme {
        RiderProfileScreen(
            onBack = {}
        )
    }
}

@Composable
fun CommunityRideTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}