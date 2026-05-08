package com.rom.poa_firstapp.ui.screen.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
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
import coil3.compose.rememberAsyncImagePainter
import com.rom.poa_firstapp.R
import com.rom.poa_firstapp.data.model.RiderProfile
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.ProfileRepositoryImpl
import com.rom.poa_firstapp.ui.common.ErrorState
import com.rom.poa_firstapp.ui.common.LoadingState
import io.github.jan.supabase.auth.auth
import java.util.Locale

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

private val WhatsApp = Color(0xFF25D366)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────
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
    val context        = LocalContext.current
    val currentProfile = viewModel.profile
    var isEditing      by remember { mutableStateOf(false) }
    val isOwnProfile   = profileId == null ||
            profileId == SupabaseModule.client.auth.currentUserOrNull()?.id

    LaunchedEffect(profileId) { viewModel.loadProfile(profileId) }
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            ProfileTopAppBar(
                onBack        = onBack,
                isOwnProfile  = isOwnProfile,
                onEditClick   = { isEditing = !isEditing },
                onLogoutClick = { viewModel.logout { onLogout() } }
            )
        },
        containerColor = Abyss,
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
                    ProfileHero(
                        profile         = currentProfile,
                        isEditing       = isEditing,
                        onProfileUpdate = { viewModel.updateProfile(it) },
                        onAvatarUpload  = { bytes -> viewModel.uploadAvatar(currentProfile.id, bytes) }
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Abyss)
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

// ─────────────────────────────────────────────────────────────────────────────
// Top App Bar
// ─────────────────────────────────────────────────────────────────────────────
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
            Text("User Profile", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 17.sp)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Box(
                    modifier = Modifier
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
            if (isOwnProfile) {
                IconButton(onClick = onEditClick) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GlassEdge)
                            .border(1.dp, GlassEdge, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextPrimary, modifier = Modifier.size(17.dp))
                    }
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GlassEdge)
                            .border(1.dp, GlassEdge, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextPrimary, modifier = Modifier.size(17.dp))
                    }
                }
                DropdownMenu(
                    expanded         = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier         = Modifier.background(Cavern)
                ) {
                    if (isOwnProfile) {
                        DropdownMenuItem(
                            text          = { Text("Logout", color = CoralPrimary, fontSize = 14.sp) },
                            onClick       = { showMenu = false; onLogoutClick() },
                            leadingIcon   = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = CoralPrimary, modifier = Modifier.size(18.dp)) }
                        )
                    }
                    DropdownMenuItem(
                        text        = { Text("Share Profile", color = TextPrimary, fontSize = 14.sp) },
                        onClick     = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Share, null, tint = CyanPrimary, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Cavern)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ProfileHero(
    profile: RiderProfile,
    isEditing: Boolean,
    onProfileUpdate: (RiderProfile) -> Unit,
    onAvatarUpload: (ByteArray) -> Unit
) {
    val context     = LocalContext.current
    var fullName    by remember(profile.full_name)    { mutableStateOf(profile.full_name) }
    var bio         by remember(profile.bio)          { mutableStateOf(profile.bio ?: "") }
    var phoneNumber by remember(profile.phone_number) { mutableStateOf(profile.phone_number ?: "") }
    var userType    by remember(profile.user_type)    { mutableStateOf(profile.user_type) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(Abyss, Cavern, Crater),
                    start  = Offset(0f, 0f),
                    end    = Offset(600f, 600f)
                )
            )
            .drawBehind {
                // dot grid
                val dot  = Color.White.copy(alpha = 0.04f)
                val step = 16f
                var x = 0f
                while (x < size.width) {
                    var y = 0f
                    while (y < size.height) { drawCircle(dot, 1f, Offset(x, y)); y += step }
                    x += step
                }
            }
    ) {
        // Ambient glows
        Box(modifier = Modifier.size(200.dp).offset(x = 220.dp, y = (-40).dp).clip(CircleShape).background(Brush.radialGradient(listOf(CyanPrimary.copy(alpha = 0.12f), Color.Transparent))))
        Box(modifier = Modifier.size(140.dp).offset(x = (-30).dp, y = 60.dp).clip(CircleShape).background(Brush.radialGradient(listOf(PurpleAccent.copy(alpha = 0.10f), Color.Transparent))))
        Box(modifier = Modifier.size(90.dp).offset(x = 180.dp, y = 80.dp).clip(CircleShape).background(Brush.radialGradient(listOf(MintPrimary.copy(alpha = 0.08f), Color.Transparent))))

        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        ) {
            // Avatar + name row
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    uri?.let {
                        context.contentResolver.openInputStream(it)?.use { stream ->
                            onAvatarUpload(stream.readBytes())
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
                    val painter = rememberAsyncImagePainter(
                        model    = profile.avatar_url,
                        error    = painterResource(R.drawable.ic_profile),
                        fallback = painterResource(R.drawable.ic_profile)
                    )
                    androidx.compose.foundation.Image(
                        painter            = painter,
                        contentDescription = "Avatar",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Crater)
                            .border(2.5.dp, CyanPrimary.copy(alpha = 0.28f), CircleShape)
                    )
                    if (isEditing) {
                        Box(
                            modifier         = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isEditing) {
                        OutlinedTextField(
                            value         = fullName,
                            onValueChange = { fullName = it },
                            label         = { Text("Full Name", color = CyanPrimary, fontSize = 12.sp) },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            colors        = darkFieldColors()
                        )
                    } else {
                        Text(profile.full_name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextHero)
                    }
                    // Verified pill + User Type
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CyanPrimary.copy(alpha = 0.12f))
                                .border(1.dp, CyanPrimary.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                "Verified",
                                fontSize = 11.sp,
                                color = CyanPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(PurpleAccent.copy(alpha = 0.12f))
                                .border(1.dp, PurpleAccent.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (profile.user_type == "driver") Icons.Default.DirectionsCar else Icons.Default.Person,
                                null,
                                tint = PurpleAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                profile.user_type.replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp,
                                color = PurpleAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Bio / edit fields
            if (isEditing) {
                OutlinedTextField(
                    value           = phoneNumber,
                    onValueChange   = { phoneNumber = it },
                    label           = { Text("Phone Number", color = CyanPrimary, fontSize = 12.sp) },
                    modifier        = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors          = darkFieldColors()
                )
                Spacer(Modifier.height(8.dp))
                
                // User Type Selection
                Text("I am a:", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("passenger", "driver").forEach { type ->
                        val isSelected = userType.lowercase() == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable { userType = type },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else Cavern,
                            border = BorderStroke(1.dp, if (isSelected) CyanPrimary else GlassEdge)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = type.replaceFirstChar { it.uppercase() },
                                    color = if (isSelected) CyanPrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value         = bio,
                    onValueChange = { bio = it },
                    label         = { Text("Bio", color = CyanPrimary, fontSize = 12.sp) },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = darkFieldColors()
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        val cleaned = phoneNumber.filter { it.isDigit() }
                        val formatted = when {
                            cleaned.startsWith("254") -> cleaned
                            cleaned.startsWith("0")   -> "254" + cleaned.substring(1)
                            cleaned.length == 9       -> "254$cleaned"
                            else                      -> cleaned
                        }
                        onProfileUpdate(profile.copy(
                            full_name = fullName, 
                            bio = bio, 
                            phone_number = formatted,
                            user_type = userType
                        ))
                    },
                    modifier = Modifier.align(Alignment.End),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Save", color = Abyss, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    profile.bio ?: "Community rider, always happy to help!",
                    color      = Color.White.copy(alpha = 0.55f),
                    fontSize   = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Wave
    Canvas(modifier = Modifier.fillMaxWidth().height(22.dp)) {
        val w = size.width; val h = size.height
        drawRect(Crater, size = androidx.compose.ui.geometry.Size(w, h * .5f))
        val path = Path().apply {
            moveTo(0f, 0f)
            cubicTo(w * .25f, h * 2f, w * .75f, -h * 1f, w, h * .5f)
            lineTo(w, 0f); close()
        }
        drawPath(path, Abyss)
        drawRect(Abyss, Offset(0f, h * .5f), androidx.compose.ui.geometry.Size(w, h * .5f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = CyanPrimary,
    unfocusedBorderColor = CyanPrimary.copy(alpha = 0.35f),
    focusedTextColor     = TextPrimary,
    unfocusedTextColor   = TextPrimary,
    cursorColor          = CyanPrimary,
    focusedContainerColor   = Cavern,
    unfocusedContainerColor = Cavern
)

// ─────────────────────────────────────────────────────────────────────────────
// Stats Section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StatsSection(profile: RiderProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        RPSectionLabel("STATS")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RPStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.DirectionsCar, iconColor = CyanPrimary,  value = profile.rides_given.toString(),  label = "Rides Given",  description = "Going the extra mile")
            RPStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Person,        iconColor = PurpleAccent, value = profile.rides_taken.toString(),  label = "Rides Taken",  description = "Thanks, community!")
        }
        RPRatingCard(rating = profile.community_rating, totalReviews = profile.total_reviews)
    }
}

@Composable
fun RPStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String,
    description: String
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Cavern, border = BorderStroke(1.dp, GlassEdge)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(iconColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 10.5.sp, color = TextMuted)
        }
    }
}

@Composable
fun RPRatingCard(rating: Double, totalReviews: Int) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = Cavern, border = BorderStroke(1.dp, GlassEdge)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(GoldAccent.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Star, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(String.format(Locale.US, "%.1f", rating), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("Community Rating", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Text("Based on $totalReviews reviews", fontSize = 10.5.sp, color = TextMuted)
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(5) { Icon(Icons.Default.Star, null, tint = GoldAccent, modifier = Modifier.size(15.dp)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Badges Section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BadgesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            RPSectionLabel("BADGES")
            TextButton(onClick = {}) {
                Text("View all", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RPBadgeCard(modifier = Modifier.weight(1f), icon = Icons.Default.Shield,      iconColor = MintPrimary,  title = "Top Helper",      description = "Helped 50+ riders")
            RPBadgeCard(modifier = Modifier.weight(1f), icon = Icons.Default.Eco,         iconColor = CyanPrimary,  title = "Eco-Friendly",    description = "Reduced 100+ kg CO₂")
        }
        RPBadgeCard(modifier = Modifier.fillMaxWidth(), icon = Icons.Default.EmojiEvents, iconColor = GoldAccent,   title = "Community Star",  description = "Rated 4.5+ by the community", horizontal = true)
    }
}

@Composable
fun RPBadgeCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    horizontal: Boolean = false
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Cavern, border = BorderStroke(1.dp, GlassEdge)) {
        if (horizontal) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(iconColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(description, fontSize = 12.sp, color = TextSecondary)
                }
            }
        } else {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(iconColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(description, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Verification Section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun VerificationSection(profile: RiderProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        RPSectionLabel("VERIFICATION")
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = Cavern, border = BorderStroke(1.dp, GlassEdge)) {
            Column {
                RPVerifRow(icon = Icons.Default.Person, label = "Member since", value = profile.member_since?.substringBefore("T") ?: "N/A")
                HorizontalDivider(color = GlassEdge, thickness = 0.8.dp)
                RPVerifRow(icon = Icons.Default.Phone, label = "Phone verified", isVerified = profile.phone_verified)
                HorizontalDivider(color = GlassEdge, thickness = 0.8.dp)
                RPVerifRow(icon = Icons.Default.Email, label = "Email verified",  isVerified = profile.email_verified)
            }
        }
    }
}

@Composable
fun RPVerifRow(
    icon: ImageVector,
    label: String,
    value: String? = null,
    isVerified: Boolean = false
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Text(label, fontSize = 14.sp, color = TextPrimary)
        }
        if (value != null) {
            Text(value, fontSize = 14.sp, color = TextSecondary)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    if (isVerified) Icons.Default.CheckCircle else Icons.Default.Close,
                    null,
                    tint     = if (isVerified) MintPrimary else RedPrimary,
                    modifier = Modifier.size(20.dp)
                )
                if (isVerified) Text("Verified", fontSize = 12.sp, color = MintPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WhatsApp button
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MessageRiderButton(riderName: String, phoneNumber: String, context: Context) {
    Surface(color = Abyss, modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = {
                val raw = phoneNumber.filter { it.isDigit() }
                val formatted = when {
                    raw.startsWith("254") -> raw
                    raw.startsWith("0")   -> "254" + raw.substring(1)
                    raw.length == 9       -> "254$raw"
                    else                  -> raw
                }
                val msg    = "Hi $riderName, I saw your profile on Community Ride app..."
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$formatted&text=$msg"))
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(52.dp),
            shape  = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsApp)
        ) {
            Icon(painterResource(R.drawable.ic_message), null, modifier = Modifier.size(20.dp), tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Message $riderName", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RPSectionLabel(text: String) {
    Text(text, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewRiderProfileScreenDark() {
    MaterialTheme { RiderProfileScreen(onBack = {}) }
}