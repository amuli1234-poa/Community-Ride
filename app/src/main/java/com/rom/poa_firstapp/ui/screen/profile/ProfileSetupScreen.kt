package com.rom.poa_firstapp.ui.screen.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rom.poa_firstapp.data.model.RiderProfile
import com.rom.poa_firstapp.data.remote.SupabaseModule
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Design Tokens (Same as MyRides & others)
// ─────────────────────────────────────────────────────────────────────────────
private val Abyss = Color(0xFF080C1C)
private val Cavern = Color(0xFF0E1325)
private val Crater = Color(0xFF141929)
private val GlassEdge = Color(0x18FFFFFF)
private val GlassEdgeMid = Color(0x30FFFFFF)

private val CyanPrimary = Color(0xFF00E5FF)
private val CyanGlow = Color(0x4400E5FF)
private val PurpleAccent = Color(0xFFAA55FF)
private val MintPrimary = Color(0xFF00FFA3)
private val RedPrimary = Color(0xFFFF3B47)
private val TextHero = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFFE8EEFF)
private val TextSecondary = Color(0xFF8896B8)
private val TextMuted = Color(0xFF4A5568)

// ─────────────────────────────────────────────────────────────────────────────
// Profile Setup Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    userId: String,
    onProfileCreated: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("passenger") }
    var bio by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Pulse animation for avatar ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    fun saveProfile() {
        println("DEBUG: saveProfile started")
        if (fullName.isBlank() || username.isBlank() || phoneNumber.isBlank()) {
            errorMessage = "Please fill in all required fields"
            println("DEBUG: Validation failed")
            return
        }

        coroutineScope.launch {
            isSaving = true
            errorMessage = null
            try {
                val newProfile = RiderProfile(
                    id = userId,
                    full_name = fullName,
                    username = username,
                    phone_number = phoneNumber,
                    bio = bio.ifBlank { null },
                    rides_given = 0,
                    rides_taken = 0,
                    community_rating = 5.0,
                    total_reviews = 0,
                    user_type = userType
                )

                println("DEBUG: Upserting profile for $userId: $newProfile")
                SupabaseModule.client.from("profiles").upsert(newProfile)
                println("DEBUG: Upsert successful, calling onProfileCreated")
                onProfileCreated()
            } catch (e: Exception) {
                println("DEBUG: Upsert failed: ${e.message}")
                errorMessage = e.message ?: "Failed to save profile"
            } finally {
                isSaving = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Abyss)
            .drawBehind { drawDotGrid(this) }
    ) {
        // Decorative glowing blobs
        BlobCircle(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 180.dp, y = (-60).dp),
            color = CyanPrimary.copy(alpha = 0.25f)
        )
        BlobCircle(
            modifier = Modifier
                .size(160.dp)
                .offset(x = (-80).dp, y = 140.dp),
            color = PurpleAccent.copy(alpha = 0.22f)
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Complete Your Profile",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHero
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Let's get you started on Community Ride",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Avatar with neon ring
                Box(contentAlignment = Alignment.Center) {
                    // Pulsing outer ring
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary.copy(alpha = pulseAlpha * 0.15f))
                            .border(2.5.dp, CyanPrimary.copy(alpha = pulseAlpha * 0.6f), CircleShape)
                    )

                    // Inner avatar
                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .clip(CircleShape)
                            .background(Cavern)
                            .border(2.dp, CyanPrimary.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            // Form Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Cavern)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "YOUR INFORMATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.5.sp
                    )

                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = RedPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RedPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                    }

                    NeonTextField(
                        value = fullName,
                        onValueChange = { fullName = it; errorMessage = null },
                        label = "Full Name",
                        placeholder = "Enter your full name",
                        icon = Icons.Default.Person
                    )

                    NeonTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = "Username",
                        placeholder = "@yourusername",
                        icon = Icons.Rounded.AlternateEmail
                    )

                    NeonTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it; errorMessage = null },
                        label = "Phone Number",
                        placeholder = "+254 700 123 456",
                        icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone
                    )

                    // User Type Selection
                    Text(
                        text = "I AM A...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.5.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("passenger", "driver").forEach { type ->
                            val isSelected = userType == type
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clickable { userType = type },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else Crater,
                                border = BorderStroke(1.dp, if (isSelected) CyanPrimary else GlassEdgeMid)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (type == "driver") Icons.Rounded.DirectionsCar else Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = if (isSelected) CyanPrimary else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = type.replaceFirstChar { it.uppercase() },
                                        color = if (isSelected) CyanPrimary else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    NeonTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = "Bio (Optional)",
                        placeholder = "Tell riders a little about yourself...",
                        icon = Icons.Rounded.Description,
                        singleLine = false,
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save Button
                    Button(
                        onClick = { 
                            println("DEBUG: Save Button clicked")
                            if (!isSaving) saveProfile() 
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = TextMuted.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        if (isSaving) 
                                            listOf(TextMuted, TextMuted) 
                                        else 
                                            listOf(CyanPrimary, PurpleAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = TextHero, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, null, tint = TextHero)
                                    Text(
                                        "Complete Setup",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextHero
                                    )
                                }
                            }
                        }
                    }
                    
                    // Extra spacer at bottom to ensure scrollability
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Neon TextField
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Crater,
        border = BorderStroke(1.dp, GlassEdgeMid)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = TextSecondary, fontSize = 13.sp) },
            placeholder = { Text(placeholder, color = TextMuted) },
            leadingIcon = {
                Icon(icon, null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
            },
            singleLine = singleLine,
            minLines = minLines,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = GlassEdgeMid,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = CyanPrimary
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Decorative Blob
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BlobCircle(modifier: Modifier, color: Color) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    center = Offset(0.5f, 0.3f)
                )
            )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Dot Grid Background
// ─────────────────────────────────────────────────────────────────────────────
private fun drawDotGrid(scope: DrawScope) {
    val step = 22f
    val dotColor = Color.White.copy(alpha = 0.06f)
    var x = 0f
    while (x < scope.size.width) {
        var y = 0f
        while (y < scope.size.height) {
            scope.drawCircle(dotColor, 1f, Offset(x, y))
            y += step
        }
        x += step
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewProfileSetupScreen() {
    ProfileSetupScreen(userId = "test-user", onProfileCreated = {})
}