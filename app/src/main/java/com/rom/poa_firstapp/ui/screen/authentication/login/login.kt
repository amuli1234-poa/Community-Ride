package com.rom.poa_firstapp.ui.screen.authentication.login

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.rom.poa_firstapp.R
import com.rom.poa_firstapp.ui.navigation.ROUTES

// ─── Private Design Tokens ────────────────────────────────────────────────────

private val DeepSpace       = Color(0xFF080B1A)
private val NightSky        = Color(0xFF0D1230)
private val PurpleDark      = Color(0xFF160835)
private val NebulaViolet    = Color(0xFF6C2EFF)
private val CometCyan       = Color(0xFF00E5FF)
private val StarPink        = Color(0xFFFF3CAC)
private val AuroraGreen     = Color(0xFF00FFA3)
private val CardSurface     = Color(0x1AFFFFFF)
private val CardBorder      = Color(0x33FFFFFF)
private val FieldSurface    = Color(0x0DFFFFFF)
private val FieldBorder     = Color(0x33FFFFFF)
private val TextPrimary     = Color(0xFFF0F4FF)
private val TextMuted       = Color(0xFF8892B0)

private val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(DeepSpace, NightSky, PurpleDark)
)
private val HeroGradient = Brush.linearGradient(
    colors = listOf(NebulaViolet, StarPink, CometCyan),
    start  = Offset(0f, 0f),
    end    = Offset(600f, 200f)
)
private val ButtonGradient = Brush.horizontalGradient(
    colors = listOf(NebulaViolet, StarPink)
)
private val AccentGradient = Brush.horizontalGradient(
    colors = listOf(CometCyan, AuroraGreen)
)
private val OrbGradient1 = Brush.radialGradient(
    colors = listOf(NebulaViolet.copy(alpha = 0.40f), Color.Transparent)
)
private val OrbGradient2 = Brush.radialGradient(
    colors = listOf(StarPink.copy(alpha = 0.25f), Color.Transparent)
)

// ─── Login Screen ─────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {

        // Background glow orbs
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = 60.dp)
                .size(220.dp)
                .background(OrbGradient1, CircleShape)
                .blur(55.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = (-60).dp)
                .size(180.dp)
                .background(OrbGradient2, CircleShape)
                .blur(50.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Lottie ────────────────────────────────────────────────────
            CompactLottieWidget()

            Spacer(Modifier.height(6.dp))

            // ── Title block ───────────────────────────────────────────────
            Text(
                text  = "WELCOME BACK",
                style = TextStyle(
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = TextMuted,
                    letterSpacing = 4.sp
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = "Community Ride",
                style = TextStyle(
                    fontSize   = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush      = HeroGradient
                )
            )
            Spacer(Modifier.height(6.dp))
            // Accent rule
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AccentGradient)
            )

            Spacer(Modifier.height(20.dp))

            // ── Frosted Card ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation    = 24.dp,
                        shape        = RoundedCornerShape(24.dp),
                        ambientColor = NebulaViolet.copy(alpha = 0.35f),
                        spotColor    = StarPink.copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Email
                    FieldLabel(text = "Email Address")
                    Spacer(Modifier.height(5.dp))
                    CompactEmailField()

                    Spacer(Modifier.height(14.dp))

                    // Password label row
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stretches to fill all remaining space, pushing "Forgot password?" to the right edge
                        Box(modifier = Modifier.weight(1f)) {
                            FieldLabel("Password")
                        }
                        Text(
                            text     = "Forgot password?",
                            style    = TextStyle(
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                brush      = AccentGradient
                            ),
                            modifier = Modifier.clickable {
                                navController.navigate(ROUTES.ForgotPassword.name)
                            }
                        )
                    }

                    Spacer(Modifier.height(5.dp))
                    CompactPasswordField()

                    Spacer(Modifier.height(22.dp))

                    // Sign In button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(50))
                            .background(ButtonGradient)
                            .clickable { navController.navigate(ROUTES.Home.name) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = "SIGN IN",
                            style = TextStyle(
                                fontSize      = 15.sp,
                                fontWeight    = FontWeight.ExtraBold,
                                color         = TextPrimary,
                                letterSpacing = 2.sp
                            )
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Divider
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier  = Modifier.weight(1f),
                            color     = CardBorder,
                            thickness = 1.dp
                        )
                        Text(
                            text  = "  or  ",
                            style = TextStyle(fontSize = 11.sp, color = TextMuted)
                        )
                        HorizontalDivider(
                            modifier  = Modifier.weight(1f),
                            color     = CardBorder,
                            thickness = 1.dp
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Signup row
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "New here?  ",
                            style = TextStyle(fontSize = 13.sp, color = TextMuted)
                        )
                        Text(
                            text     = "Create Account",
                            style    = TextStyle(
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Bold,
                                brush      = AccentGradient
                            ),
                            modifier = Modifier.clickable {
                                navController.navigate(ROUTES.Signup.name)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── Field Label ──────────────────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(
        text     = text,
        modifier = Modifier.fillMaxWidth(),
        style    = TextStyle(
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = TextMuted,
            letterSpacing = 0.8.sp
        )
    )
}

// ─── Shared TextField Colors ──────────────────────────────────────────────────

@Composable
private fun sharedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor      = FieldSurface,
    unfocusedContainerColor    = FieldSurface,
    focusedBorderColor         = CometCyan,
    unfocusedBorderColor       = FieldBorder,
    focusedLabelColor          = CometCyan,
    unfocusedLabelColor        = TextMuted,
    cursorColor                = CometCyan,
    focusedTextColor           = TextPrimary,
    unfocusedTextColor         = TextPrimary,
    focusedLeadingIconColor    = CometCyan,
    unfocusedLeadingIconColor  = TextMuted,
    focusedTrailingIconColor   = CometCyan,
    unfocusedTrailingIconColor = TextMuted,
    focusedPlaceholderColor    = TextMuted,
    unfocusedPlaceholderColor  = TextMuted
)

// ─── Email Field ──────────────────────────────────────────────────────────────

@Composable
fun CompactEmailField() {
    var text by remember { mutableStateOf(TextFieldValue("")) }

    OutlinedTextField(
        value         = text,
        onValueChange = { text = it },
        singleLine    = true,
        colors        = sharedFieldColors(),
        leadingIcon   = {
            Icon(
                imageVector        = Icons.Filled.Email,
                contentDescription = "Email",
                modifier           = Modifier.size(18.dp)
            )
        },
        placeholder = {
            Text("you@example.com", fontSize = 13.sp)
        },
        shape           = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        textStyle       = TextStyle(fontSize = 14.sp),
        modifier        = Modifier
            .fillMaxWidth()
            .height(52.dp)
    )
}

// ─── Password Field ───────────────────────────────────────────────────────────

@Composable
fun CompactPasswordField() {
    var text      by remember { mutableStateOf(TextFieldValue("")) }
    var isVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value         = text,
        onValueChange = { text = it },
        singleLine    = true,
        colors        = sharedFieldColors(),
        leadingIcon   = {
            Icon(
                imageVector        = ImageVector.vectorResource(R.drawable.outline_password_24),
                contentDescription = "Password",
                modifier           = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            IconButton(
                onClick  = { isVisible = !isVisible },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if (isVisible) R.drawable.outline_visibility_off_24
                        else           R.drawable.outline_visibility_off_24
                    ),
                    contentDescription = if (isVisible) "Hide" else "Show",
                    modifier           = Modifier.size(18.dp)
                )
            }
        },
        visualTransformation = if (isVisible) VisualTransformation.None
        else           PasswordVisualTransformation(),
        shape     = RoundedCornerShape(14.dp),
        textStyle = TextStyle(fontSize = 14.sp),
        modifier  = Modifier
            .fillMaxWidth()
            .height(52.dp)
    )
}

// ─── Lottie Widget ────────────────────────────────────────────────────────────

@Composable
fun CompactLottieWidget() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.login)
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress    = { progress },
        modifier    = Modifier
            .size(160.dp)
    )
}
