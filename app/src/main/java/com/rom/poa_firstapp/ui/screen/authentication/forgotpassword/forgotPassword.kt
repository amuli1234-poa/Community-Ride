package com.rom.poa_firstapp.ui.screen.authentication.forgotpassword

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
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
    colors = listOf(StarPink.copy(alpha = 0.30f), Color.Transparent)
)
private val OrbGradient2 = Brush.radialGradient(
    colors = listOf(CometCyan.copy(alpha = 0.20f), Color.Transparent)
)

// ─── Forgot Password Screen ───────────────────────────────────────────────────

@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Tracks whether the reset email was sent
    var emailSent by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {

        // Background glow orbs
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-60).dp, y = 100.dp)
                .size(200.dp)
                .background(OrbGradient1, CircleShape)
                .blur(55.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = (-100).dp)
                .size(160.dp)
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
            ForgotLottieWidget(emailSent = emailSent)

            Spacer(Modifier.height(6.dp))

            // ── Title block ───────────────────────────────────────────────
            Text(
                text  = if (emailSent) "CHECK YOUR INBOX" else "ACCOUNT RECOVERY",
                style = TextStyle(
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = TextMuted,
                    letterSpacing = 4.sp
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = if (emailSent) "Email Sent!" else "Forgot Password?",
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
                if (!emailSent) {
                    // ── Request reset state ────────────────────────────────
                    ForgotRequestContent(
                        onSend = { emailSent = true }
                    )
                } else {
                    // ── Confirmation state ─────────────────────────────────
                    ForgotConfirmationContent(
                        onBackToLogin = { navController.navigate(ROUTES.Login.name) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Back to login ─────────────────────────────────────────────
            if (!emailSent) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text  = "Remember your password?  ",
                        style = TextStyle(fontSize = 13.sp, color = TextMuted)
                    )
                    Text(
                        text     = "Sign In",
                        style    = TextStyle(
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold,
                            brush      = AccentGradient
                        ),
                        modifier = Modifier.clickable {
                            navController.navigate(ROUTES.Login.name)
                        }
                    )
                }
            }
        }
    }
}

// ─── Request Reset Content ────────────────────────────────────────────────────

@Composable
private fun ForgotRequestContent(onSend: () -> Unit) {
    var email by remember { mutableStateOf(TextFieldValue("")) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Description
        Text(
            text      = "Enter the email linked to your account and we'll send you a reset link.",
            style     = TextStyle(
                fontSize   = 13.sp,
                color      = TextMuted,
                lineHeight = 20.sp
            ),
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // Email label
        ForgotFieldLabel(text = "Email Address")
        Spacer(Modifier.height(5.dp))
        ForgotEmailField(
            value = email,
            onValueChange = { email = it }
        )

        Spacer(Modifier.height(22.dp))

        // Send Reset Link button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(50))
                .background(ButtonGradient)
                .clickable { onSend() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "SEND RESET LINK",
                style = TextStyle(
                    fontSize      = 15.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = Color(0xFFF0F4FF),
                    letterSpacing = 2.sp
                )
            )
        }
    }
}

// ─── Confirmation Content ─────────────────────────────────────────────────────

@Composable
private fun ForgotConfirmationContent(onBackToLogin: () -> Unit) {

    val accentGradientLocal = Brush.horizontalGradient(
        colors = listOf(Color(0xFF00E5FF), Color(0xFF00FFA3))
    )
    val buttonGradientLocal = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6C2EFF), Color(0xFFFF3CAC))
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(6.dp))

        // Success badge
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    )
                )
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.40f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "✓",
                style = TextStyle(
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    brush      = accentGradientLocal
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text      = "A password reset link has been sent to your email. Check your inbox and follow the instructions.",
            style     = TextStyle(
                fontSize   = 13.sp,
                color      = Color(0xFF8892B0),
                lineHeight = 20.sp
            ),
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(22.dp))

        // Back to Sign In button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(50))
                .background(buttonGradientLocal)
                .clickable { onBackToLogin() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "BACK TO SIGN IN",
                style = TextStyle(
                    fontSize      = 15.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = Color(0xFFF0F4FF),
                    letterSpacing = 2.sp
                )
            )
        }

        Spacer(Modifier.height(14.dp))

        // Resend row
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = "Didn't receive it?  ",
                style = TextStyle(fontSize = 13.sp, color = Color(0xFF8892B0))
            )
            Text(
                text     = "Resend Email",
                style    = TextStyle(
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    brush      = accentGradientLocal
                ),
                modifier = Modifier.clickable { /* handle resend */ }
            )
        }

        Spacer(Modifier.height(6.dp))
    }
}

// ─── Field Label ──────────────────────────────────────────────────────────────

@Composable
private fun ForgotFieldLabel(text: String) {
    Text(
        text     = text,
        modifier = Modifier.fillMaxWidth(),
        style    = TextStyle(
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = Color(0xFF8892B0),
            letterSpacing = 0.8.sp
        )
    )
}

// ─── Email Field ──────────────────────────────────────────────────────────────

@Composable
private fun ForgotEmailField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor      = Color(0x0DFFFFFF),
        unfocusedContainerColor    = Color(0x0DFFFFFF),
        focusedBorderColor         = Color(0xFF00E5FF),
        unfocusedBorderColor       = Color(0x33FFFFFF),
        cursorColor                = Color(0xFF00E5FF),
        focusedTextColor           = Color(0xFFF0F4FF),
        unfocusedTextColor         = Color(0xFFF0F4FF),
        focusedLeadingIconColor    = Color(0xFF00E5FF),
        unfocusedLeadingIconColor  = Color(0xFF8892B0),
        focusedPlaceholderColor    = Color(0xFF8892B0),
        unfocusedPlaceholderColor  = Color(0xFF8892B0)
    )

    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        singleLine    = true,
        colors        = fieldColors,
        leadingIcon   = {
            Icon(
                imageVector        = Icons.Filled.Email,
                contentDescription = "Email",
                modifier           = Modifier.size(18.dp)
            )
        },
        placeholder     = { Text("you@example.com", fontSize = 13.sp) },
        shape           = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        textStyle       = TextStyle(fontSize = 14.sp),
        modifier        = Modifier.fillMaxWidth().height(52.dp)
    )
}

// ─── Lottie Widget ────────────────────────────────────────────────────────────

@Composable
fun ForgotLottieWidget(emailSent: Boolean) {
    val composition by rememberLottieComposition(
        // Note: Using login/signup assets as placeholders for missing forgot_password/email_sent
        if (emailSent)
            LottieCompositionSpec.RawRes(R.raw.signup)
        else
            LottieCompositionSpec.RawRes(R.raw.login)
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = if (emailSent) 1 else LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress    = { progress },
        modifier    = Modifier.size(150.dp)
    )
}







