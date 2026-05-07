package com.rom.poa_firstapp.ui.screen.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.rom.poa_firstapp.R
import com.rom.poa_firstapp.data.model.RiderProfile
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.ProfileRepositoryImpl
import com.rom.poa_firstapp.ui.common.ErrorState
import com.rom.poa_firstapp.ui.common.LoadingState
import io.github.jan.supabase.auth.auth
import java.util.Locale

// ---------------------------------------------------------------------------
// Colour palette
// ---------------------------------------------------------------------------
private val GreenDeep    = Color(0xFF1A3A2A)
private val GreenMid     = Color(0xFF2D6A4F)
private val GreenBright  = Color(0xFF40916C)
private val GreenLight   = Color(0xFF52B788)
private val GreenPale    = Color(0xFFD4E8D4)
private val GreenHint    = Color(0xFF74916C)
private val GreenSubtle  = Color(0xFFE8F5E9)
private val PageBg       = Color(0xFFF4F6F3)
private val CardWhite    = Color(0xFFFFFFFF)
private val WhatsApp     = Color(0xFF25D366)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderProfileScreen(
    profileId: String? = null,
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
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
    val isOwnProfile = profileId == null ||
            profileId == SupabaseModule.client.auth.currentUserOrNull()?.id

    LaunchedEffect(profileId) { viewModel.loadProfile(profileId) }
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            ProfileTopAppBar(
                onBack       = { onBack() },
                isOwnProfile = isOwnProfile,
                onEditClick  = { isEditing = !isEditing },
                onLogoutClick = {
                    viewModel.logout { onLogout() }
                }
            )
        },
        containerColor = PageBg,
        bottomBar = {
            if (currentProfile != null && profileId != null && !isOwnProfile) {
                MessageRiderButton(
                    riderName   = currentProfile.full_name,
                    phoneNumber = currentProfile.phone_number ?: "",
                    context     = context
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (currentProfile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Hero header (gradient)
                    ProfileHero(
                        profile         = currentProfile,
                        isEditing       = isEditing,
                        onProfileUpdate = { viewModel.updateProfile(it) },
                        onAvatarUpload  = { bytes ->
                            viewModel.uploadAvatar(currentProfile.id, bytes)
                        }
                    )
                    // Body sections
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PageBg)
                            .padding(horizontal = 16.dp)
                            .padding(top = 20.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        StatsSection(profile = currentProfile)
                        BadgesSection()
                        VerificationSection(profile = currentProfile)
                    }
                }
            }
            if (viewModel.isLoading) LoadingState()
            viewModel.errorMessage?.let { ErrorState(message = it) }
        }
    }
}

// ---------------------------------------------------------------------------
// Top app bar
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopAppBar(
    onBack: () -> Unit,
    isOwnProfile: Boolean,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                "User Profile",
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                fontSize   = 17.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        actions = {
            if (isOwnProfile) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (isOwnProfile) {
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showMenu = false
                                onLogoutClick()
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Share Profile") },
                        onClick = { showMenu = false }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenDeep)
    )
}

// ---------------------------------------------------------------------------
// Hero header — gradient with avatar, name, bio
// ---------------------------------------------------------------------------
@Composable
fun ProfileHero(
    profile: RiderProfile,
    isEditing: Boolean,
    onProfileUpdate: (RiderProfile) -> Unit,
    onAvatarUpload: (ByteArray) -> Unit
) {
    val context     = LocalContext.current
    var fullName    by remember(profile.full_name)   { mutableStateOf(profile.full_name) }
    var bio         by remember(profile.bio)         { mutableStateOf(profile.bio ?: "") }
    var phoneNumber by remember(profile.phone_number){ mutableStateOf(profile.phone_number ?: "") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(GreenDeep, GreenMid, GreenBright),
                    start  = Offset(0f, 0f),
                    end    = Offset(600f, 600f)
                )
            )
            .drawBehind { drawDotGrid(this) }
    ) {
        // Decorative blobs
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 240.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(GreenLight.copy(alpha = 0.18f))
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .offset(x = 180.dp, y = 80.dp)
                .clip(CircleShape)
                .background(GreenLight.copy(alpha = 0.12f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Avatar + name row
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Avatar
                val painter = rememberAsyncImagePainter(
                    model    = profile.avatar_url,
                    error    = painterResource(R.drawable.ic_profile),
                    fallback = painterResource(R.drawable.ic_profile)
                )
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    uri?.let {
                        context.contentResolver.openInputStream(it)?.use { inputStream ->
                            val bytes = inputStream.readBytes()
                            onAvatarUpload(bytes)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clickable(enabled = isEditing) {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                ) {
                    Image(
                        painter            = painter,
                        contentDescription = "Profile Picture",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(2.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                    )
                    if (isEditing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Change Photo", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isEditing) {
                        OutlinedTextField(
                            value         = fullName,
                            onValueChange = { fullName = it },
                            label         = { Text("Full Name", color = GreenLight, fontSize = 12.sp) },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = GreenLight,
                                unfocusedBorderColor = GreenLight.copy(alpha = 0.5f),
                                focusedTextColor     = Color.White,
                                unfocusedTextColor   = Color.White,
                                cursorColor          = GreenLight
                            )
                        )
                    } else {
                        Text(
                            text       = profile.full_name,
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                    // Verified pill
                    Row(
                        modifier          = Modifier
                            .background(GreenLight.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                            .border(1.dp, GreenLight.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenLight, modifier = Modifier.size(13.dp))
                        Text("Verified", fontSize = 12.sp, color = GreenLight, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bio / edit fields
            if (isEditing) {
                OutlinedTextField(
                    value         = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label         = { Text("Phone Number", color = GreenLight, fontSize = 12.sp) },
                    modifier      = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors        = editFieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value         = bio,
                    onValueChange = { bio = it },
                    label         = { Text("Bio", color = GreenLight, fontSize = 12.sp) },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = editFieldColors()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val cleaned = phoneNumber.filter { it.isDigit() }
                        val formatted = when {
                            cleaned.startsWith("254") -> cleaned
                            cleaned.startsWith("0") -> "254" + cleaned.substring(1)
                            cleaned.length == 9 -> "254$cleaned"
                            else -> cleaned
                        }
                        onProfileUpdate(
                            profile.copy(full_name = fullName, bio = bio, phone_number = formatted)
                        )
                    },
                    modifier = Modifier.align(Alignment.End),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = GreenLight)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text    = profile.bio ?: "Community rider, always happy to help!",
                    color   = Color.White.copy(alpha = 0.65f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Wave bottom
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            // Handled by SVG-like clipping below
        }
    }

    // Wave transition into page bg
    Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, 0f)
            cubicTo(w * 0.25f, h * 1.4f, w * 0.75f, -h * 0.4f, w, h * 0.5f)
            lineTo(w, 0f)
            close()
        }
        // top strip is hero colour
        drawRect(color = GreenBright, size = androidx.compose.ui.geometry.Size(w, h / 2))
        // wave fill is page colour
        drawPath(path = path, color = PageBg)
        drawRect(color = PageBg, topLeft = Offset(0f, h / 2), size = androidx.compose.ui.geometry.Size(w, h / 2))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun editFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = GreenLight,
    unfocusedBorderColor = GreenLight.copy(alpha = 0.5f),
    focusedTextColor     = Color.White,
    unfocusedTextColor   = Color.White,
    cursorColor          = GreenLight
)

// ---------------------------------------------------------------------------
// Stats section
// ---------------------------------------------------------------------------
@Composable
fun StatsSection(profile: RiderProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(text = "STATS")
        // 2-col grid for first two stats
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                modifier    = Modifier.weight(1f),
                icon        = Icons.Default.DirectionsCar,
                value       = profile.rides_given.toString(),
                label       = "Rides Given",
                description = "Going the extra mile"
            )
            StatCard(
                modifier    = Modifier.weight(1f),
                icon        = Icons.Default.Person,
                value       = profile.rides_taken.toString(),
                label       = "Rides Taken",
                description = "Thanks, community!"
            )
        }
        // Full-width rating card
        RatingCard(
            rating      = profile.community_rating,
            totalReviews = profile.total_reviews
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    description: String
) {
    Surface(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        color     = CardWhite,
        tonalElevation = 0.dp,
        border    = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp).also { }
            .let { androidx.compose.foundation.BorderStroke(1.dp, GreenPale) }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = GreenBright, modifier = Modifier.size(22.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GreenDeep)
            Text(label, fontSize = 12.sp, color = GreenHint, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 11.sp, color = Color(0xFFAAC4AA))
        }
    }
}

@Composable
fun RatingCard(rating: Double, totalReviews: Int) {
    Surface(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        color     = CardWhite,
        border    = androidx.compose.foundation.BorderStroke(1.dp, GreenPale)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = GreenBright, modifier = Modifier.size(26.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    String.format(Locale.US, "%.1f", rating),
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GreenDeep
                )
                Text("Community Rating", fontSize = 12.sp, color = GreenHint, fontWeight = FontWeight.Medium)
                Text("Based on $totalReviews reviews", fontSize = 11.sp, color = Color(0xFFAAC4AA))
            }
            Spacer(modifier = Modifier.weight(1f))
            // Star row
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(5) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFEF9F27), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Badges section
// ---------------------------------------------------------------------------
@Composable
fun BadgesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            SectionLabel(text = "BADGES")
            TextButton(onClick = {}) {
                Text("View all", color = GreenBright, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BadgeCard(
                modifier     = Modifier.weight(1f),
                icon         = Icons.Default.Shield,
                title        = "Top Helper",
                description  = "Helped 50+ riders"
            )
            BadgeCard(
                modifier     = Modifier.weight(1f),
                icon         = Icons.Default.Eco,
                title        = "Eco-Friendly",
                description  = "Reduced 100+ kg CO₂"
            )
        }
        BadgeCard(
            modifier    = Modifier.fillMaxWidth(),
            icon        = Icons.Default.EmojiEvents,
            title       = "Community Star",
            description = "Rated 4.5+ by the community",
            horizontal  = true
        )
    }
}

@Composable
fun BadgeCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    horizontal: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(14.dp),
        color    = CardWhite,
        border   = androidx.compose.foundation.BorderStroke(1.dp, GreenPale)
    ) {
        if (horizontal) {
            Row(
                modifier          = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GreenSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = GreenMid, modifier = Modifier.size(22.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenDeep)
                    Text(description, fontSize = 12.sp, color = GreenHint)
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GreenSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = GreenMid, modifier = Modifier.size(22.dp))
                }
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenDeep)
                Text(description, fontSize = 11.sp, color = GreenHint, lineHeight = 15.sp)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Verification section
// ---------------------------------------------------------------------------
@Composable
fun VerificationSection(profile: RiderProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(text = "VERIFICATION")
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(14.dp),
            color    = CardWhite,
            border   = androidx.compose.foundation.BorderStroke(1.dp, GreenPale)
        ) {
            Column {
                VerificationRow(
                    icon  = Icons.Default.Person,
                    label = "Member since",
                    value = profile.member_since?.substringBefore("T") ?: "N/A"
                )
                HorizontalDivider(color = GreenPale, thickness = 0.8.dp)
                VerificationRow(
                    icon       = Icons.Default.Phone,
                    label      = "Phone verified",
                    isVerified = profile.phone_verified
                )
                HorizontalDivider(color = GreenPale, thickness = 0.8.dp)
                VerificationRow(
                    icon       = Icons.Default.Email,
                    label      = "Email verified",
                    isVerified = profile.email_verified
                )
            }
        }
    }
}

@Composable
fun VerificationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String? = null,
    isVerified: Boolean = false
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = GreenHint, modifier = Modifier.size(20.dp))
            Text(label, fontSize = 14.sp, color = GreenDeep)
        }
        if (value != null) {
            Text(value, fontSize = 14.sp, color = GreenHint)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector        = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint               = if (isVerified) GreenBright else Color(0xFFE24B4A),
                    modifier           = Modifier.size(20.dp)
                )
                if (isVerified) {
                    Text("Verified", fontSize = 13.sp, color = GreenBright, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// WhatsApp message button
// ---------------------------------------------------------------------------
@Composable
fun MessageRiderButton(riderName: String, phoneNumber: String, context: Context) {
    Surface(
        color    = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = {
                val rawPhone = phoneNumber.filter { it.isDigit() }
                val formattedPhone = when {
                    rawPhone.startsWith("254") -> rawPhone
                    rawPhone.startsWith("0") -> "254" + rawPhone.substring(1)
                    else -> if (rawPhone.length == 9) "254$rawPhone" else rawPhone
                }
                val msg  = "Hi $riderName, I saw your profile on Community Ride app..."
                val uri  = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$msg")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(52.dp),
            shape  = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsApp)
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_message),
                contentDescription = null,
                modifier           = Modifier.size(20.dp),
                tint               = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Message $riderName",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Shared helpers
// ---------------------------------------------------------------------------
@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text,
        fontSize      = 11.sp,
        fontWeight    = FontWeight.Bold,
        color         = GreenHint,
        letterSpacing = 1.sp
    )
}

private fun drawDotGrid(scope: androidx.compose.ui.graphics.drawscope.DrawScope) {
    val step = 16f
    val dot  = Color.White.copy(alpha = 0.06f)
    var x = 0f
    while (x < scope.size.width) {
        var y = 0f
        while (y < scope.size.height) {
            scope.drawCircle(dot, 1f, Offset(x, y))
            y += step
        }
        x += step
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewRiderProfileScreen() {
    MaterialTheme {
        RiderProfileScreen(onBack = {})
    }
}
