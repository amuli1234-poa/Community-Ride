package com.rom.poa_firstapp.ui.screen.authentication.resetpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.AuthRepositoryImpl
import com.rom.poa_firstapp.ui.common.ErrorState
import com.rom.poa_firstapp.ui.common.LoadingState
import com.rom.poa_firstapp.ui.navigation.ROUTES
import com.rom.poa_firstapp.ui.screen.authentication.AuthViewModel

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
    colors = listOf(NebulaViolet, StarPink, CometCyan)
)
private val ButtonGradient = Brush.horizontalGradient(
    colors = listOf(NebulaViolet, StarPink)
)

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel {
        AuthViewModel(AuthRepositoryImpl(SupabaseModule.client))
    }
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }

    LaunchedEffect(authViewModel.isSuccess) {
        if (authViewModel.isSuccess) {
            navController.navigate(ROUTES.Login.name) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RESET PASSWORD",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 4.sp
                )
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = "New Password",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush = HeroGradient
                )
            )

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Enter your new password below to regain access to your account.",
                        style = TextStyle(fontSize = 14.sp, color = TextMuted),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("New Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(
                                    if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CometCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = CometCyan,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(
                                    if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CometCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = CometCyan,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (password == confirmPassword && password.isNotEmpty()) {
                                authViewModel.updatePassword(password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(ButtonGradient),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("UPDATE PASSWORD", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    }
                }
            }
        }

        if (authViewModel.isLoading) LoadingState()
        authViewModel.errorMessage?.let { ErrorState(message = it) }
    }
}
