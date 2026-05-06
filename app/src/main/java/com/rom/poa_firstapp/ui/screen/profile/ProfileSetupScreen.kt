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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rom.poa_firstapp.data.model.RiderProfile
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.ui.theme.Poa_firstappTheme
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlin.math.sin

// ---------------------------------------------------------------------------
// Colour palette (consistent with the mockup)
// ---------------------------------------------------------------------------
private val GreenDeep   = Color(0xFF1A3A2A)
private val GreenMid    = Color(0xFF2D6A4F)
private val GreenBright = Color(0xFF40916C)
private val GreenLight  = Color(0xFF52B788)
private val GreenPale   = Color(0xFFD4E8D4)
private val GreenHint   = Color(0xFF74916C)
private val SurfaceWhite = Color(0xFFF8F7F4)
private val FieldWhite  = Color(0xFFFFFFFF)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(userId: String, onProfileCreated: () -> Unit) {

    var fullName    by remember { mutableStateOf("") }
    var username    by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var bio         by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving    by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Pulsing animation for the avatar ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue  = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    fun saveProfile() {
        if (fullName.isBlank() || username.isBlank() || phoneNumber.isBlank()) {
            errorMessage = "Please fill in all required fields"
            return
        }
        coroutineScope.launch {
            isSaving = true
            try {
                val newProfile = RiderProfile(
                    id               = userId,
                    full_name        = fullName,
                    username         = username,
                    phone_number     = phoneNumber,
                    bio              = bio,
                    rides_given      = 0,
                    rides_taken      = 0,
                    community_rating = 5.0,
                    total_reviews    = 0
                )
                SupabaseModule.client.from("profiles").upsert(newProfile)
                onProfileCreated()
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred while saving your profile"
            } finally {
                isSaving = false
            }
        }
    }

    // Full-screen gradient background — no Scaffold so we own the status-bar area
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GreenDeep, GreenMid, GreenBright, GreenLight),
                    start  = Offset(0f, 0f),
                    end    = Offset(400f, 900f)
                )
            )
            // Decorative dot-grid overlay drawn directly on the canvas
            .drawBehind { drawDotGrid(this) }
    ) {

        // ── Decorative blobs ────────────────────────────────────────────
        BlobCircle(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 220.dp, y = (-40).dp),
            color = GreenLight.copy(alpha = 0.35f)
        )
        BlobCircle(
            modifier = Modifier
                .size(130.dp)
                .offset(x = (-20).dp, y = 60.dp),
            color = GreenBright.copy(alpha = 0.4f)
        )
        BlobCircle(
            modifier = Modifier
                .size(110.dp)
                .offset(x = 260.dp, y = 200.dp),
            color = GreenLight.copy(alpha = 0.25f)
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header (lives on the gradient) ───────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text       = "Complete Your Profile",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Avatar ring with pulse
                Box(contentAlignment = Alignment.Center) {
                    // Outer pulsing ring
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = pulseAlpha * 0.15f))
                            .border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = pulseAlpha * 0.5f),
                                shape = CircleShape
                            )
                    )
                    // Inner frosted circle
                    Box(
                        modifier            = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(2.dp, Color.White.copy(alpha = 0.45f), CircleShape),
                        contentAlignment    = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Person,
                            contentDescription = null,
                            tint               = Color.White.copy(alpha = 0.9f),
                            modifier           = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text      = "Just a few details to get you started",
                    fontSize  = 13.sp,
                    color     = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }

            // ── White bottom sheet ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(SurfaceWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Section label
                    Text(
                        text       = "YOUR DETAILS",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = GreenHint,
                        letterSpacing = 1.sp
                    )

                    // Error banner
                    errorMessage?.let { msg ->
                        Surface(
                            shape  = RoundedCornerShape(10.dp),
                            color  = Color(0xFFFFEBEB),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text     = msg,
                                color    = Color(0xFFC0392B),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Full Name
                    GreenTextField(
                        value       = fullName,
                        onValueChange = { fullName = it; errorMessage = null },
                        label       = "Full Name",
                        placeholder = "Your full name",
                        icon        = Icons.Default.Person,
                        enabled     = !isSaving
                    )

                    // Username
                    GreenTextField(
                        value       = username,
                        onValueChange = { username = it; errorMessage = null },
                        label       = "Username",
                        placeholder = "@yourhandle",
                        icon        = Icons.Rounded.AlternateEmail,
                        enabled     = !isSaving
                    )

                    // Phone
                    GreenTextField(
                        value         = phoneNumber,
                        onValueChange = { phoneNumber = it; errorMessage = null },
                        label         = "Phone Number",
                        placeholder   = "+254 700 000 000",
                        icon          = Icons.Default.Phone,
                        enabled       = !isSaving,
                        keyboardType  = KeyboardType.Phone
                    )

                    // Bio
                    Surface(
                        shape    = RoundedCornerShape(14.dp),
                        color    = FieldWhite,
                        border   = BorderStroke(1.5.dp, GreenPale),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value         = bio,
                            onValueChange = { bio = it },
                            label         = { Text("Bio", color = GreenHint, fontSize = 13.sp) },
                            placeholder   = { Text("Tell us a bit about yourself...", color = Color(0xFFB5C9B5), fontSize = 14.sp) },
                            modifier      = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            enabled       = !isSaving,
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

                    Spacer(modifier = Modifier.height(4.dp))

                    // CTA Button
                    Button(
                        onClick  = { saveProfile() },
                        enabled  = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(GreenMid, GreenBright, GreenLight),
                                        start  = Offset(0f, 0f),
                                        end    = Offset(Float.POSITIVE_INFINITY, 0f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color    = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint               = Color.White,
                                        modifier           = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text       = "Finish Setup",
                                        fontSize   = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = Color.White,
                                        letterSpacing = 0.2.sp
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text      = "All fields except Bio are required",
                        fontSize  = 11.sp,
                        color     = Color(0xFFAAC4AA),
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Reusable styled text field
// ---------------------------------------------------------------------------
@Composable
private fun GreenTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = FieldWhite,
        border   = BorderStroke(1.5.dp, GreenPale),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            label         = { Text(label, color = GreenHint, fontSize = 13.sp) },
            placeholder   = { Text(placeholder, color = Color(0xFFB5C9B5), fontSize = 14.sp) },
            leadingIcon   = {
                Icon(
                    imageVector        = icon,
                    contentDescription = label,
                    tint               = GreenBright,
                    modifier           = Modifier.size(20.dp)
                )
            },
            singleLine    = true,
            enabled       = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
// Decorative blob circle
// ---------------------------------------------------------------------------
@Composable
private fun BlobCircle(modifier: Modifier, color: Color) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent)
                )
            )
    )
}

// ---------------------------------------------------------------------------
// Dot-grid canvas helper
// ---------------------------------------------------------------------------
private fun drawDotGrid(scope: DrawScope) {
    val step = 18f
    val dotRadius = 1f
    val dotColor = Color.White.copy(alpha = 0.07f)
    var x = 0f
    while (x < scope.size.width) {
        var y = 0f
        while (y < scope.size.height) {
            scope.drawCircle(dotColor, dotRadius, Offset(x, y))
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
fun PreviewProfileSetupScreen() {
    Poa_firstappTheme {
        ProfileSetupScreen(userId = "test_user_id", onProfileCreated = {})
    }
}