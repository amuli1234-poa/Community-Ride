package com.rom.poa_firstapp.ui.screen.home

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImagePainter
import com.rom.poa_firstapp.ui.navigation.ROUTES

// ─── Color tokens ───────────────────────────────────────────────
private val GreenPrimary   = Color(0xFF2E7D32)
private val GreenLight     = Color(0xFFE8F5E9)
private val GreenBorder    = Color(0xFFA5D6A7)
private val GreenDark      = Color(0xFF1B5E20)
private val GreenSurface   = Color(0xFFEAF3DE)
private val GreenSubBorder = Color(0xFFC0DD97)
private val PageBg         = Color(0xFFF4F6F0)
private val CardBg         = Color(0xFFF4F6F0)
private val CardBorder     = Color(0xFFC8DDB8)
private val TextPrimary    = Color(0xFF1A1A1A)
private val TextSecondary  = Color(0xFF5F5E5A)
private val TextMuted      = Color(0xFF888780)
private val DividerColor   = Color(0xFFE0E6D8)
private val BottomCard   = Color(0xFF245AB9F9)
private val HeaderColor   = Color(0xF9245AB9)

private val BottomNavigator    = Color(0xFF4F4A7EF4)

@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PageBg)
    ) {
        // ── Map layer ──────────────────────────────────────────
        if (LocalInspectionMode.current) {
            // Placeholder for WebView during Preview to avoid NoSuchMethodError: getSettings()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(top = 110.dp, bottom = 16.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Interactive Map (WebView) Preview")
            }
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(top = 110.dp, bottom = 16.dp),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        settings.setGeolocationEnabled(true)
                        settings.domStorageEnabled = true
                        webChromeClient = object : WebChromeClient() {
                            override fun onGeolocationPermissionsShowPrompt(
                                origin: String?,
                                callback: GeolocationPermissions.Callback?
                            ) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    callback?.invoke(origin, true, false)
                                } else {
                                    callback?.invoke(origin, false, false)
                                }
                            }
                        }
                        loadUrl("file:///android_asset/index.html")
                    }
                }
            )
        }

        // ── UI overlay ─────────────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBarSection(
                onMenuClick = { /* TODO: Open drawer or menu */ },
                onProfileClick = { /* TODO: Navigate to profile */ }
            )
            RideStatusFilters()
            Spacer(modifier = Modifier.weight(1f))
            BottomSheet(navController = navController)
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview( showBackground : Boolean = true) {
    HomeScreen(navController = rememberNavController())
}


// ── Top App Bar ────────────────────────────────────────────────────
@Composable
fun TopAppBarSection(onMenuClick: () -> Unit, onProfileClick: () -> Unit) {
    val menuInteractionSource = remember { MutableInteractionSource() }
    val menuIsPressed by menuInteractionSource.collectIsPressedAsState()
    val menuScale by animateFloatAsState(if (menuIsPressed) 0.92f else 1f, label = "menuScale")

    val profileInteractionSource = remember { MutableInteractionSource() }
    val profileIsPressed by profileInteractionSource.collectIsPressedAsState()
    val profileScale by animateFloatAsState(if (profileIsPressed) 0.92f else 1f, label = "profileScale")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = BottomCard)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onMenuClick,
            interactionSource = menuInteractionSource,
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer {
                    scaleX = menuScale
                    scaleY = menuScale
                }
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(0.5.dp, Color(0xFFD3D1C7), RoundedCornerShape(10.dp))
        ) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "Menu",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Community Ride",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = HeaderColor
            )
        }

        IconButton(
            onClick = onProfileClick,
            interactionSource = profileInteractionSource,
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer {
                    scaleX = profileScale
                    scaleY = profileScale
                }
                .clip(CircleShape)
                .background(GreenLight)
                .border(0.5.dp, GreenBorder, CircleShape)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                tint = GreenPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Filter Chips ───────────────────────────────────────────────────
@Composable
fun RideStatusFilters() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(color = BottomCard)
            .padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            text = "Free ride",
            dotColor = GreenPrimary,
            bgColor = GreenLight,
            borderColor = GreenBorder,
            textColor = GreenDark
        )
        FilterChip(
            text = "Paid ride",
            dotColor = Color(0xFFD85A30),
            bgColor = Color(0xFFFAECE7),
            borderColor = Color(0xFFF5C4B3),
            textColor = Color(0xFF993C1D)
        )
        FilterChip(
            text = "Pending",
            dotColor = Color(0xFFBA7517),
            bgColor = Color(0xFFFAEEDA),
            borderColor = Color(0xFFFAC775),
            textColor = Color(0xFF854F0B)
        )
    }
}

@Composable
fun FilterChip(
    text: String,
    dotColor: Color,
    bgColor: Color,
    borderColor: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(0.5.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

// ── Bottom Sheet ───────────────────────────────────────────────────
@Composable
fun BottomSheet(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(color =BottomCard)
            .padding(top = 12.dp)
    ) {
        // Handle
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color = White)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SearchBarSection()
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCard(
                    icon = Icons.Default.Add,
                    title = "Post a ride",
                    subtitle = "Offer a seat and ride together",
                    onClick = { navController.navigate(ROUTES.PostRide.name) },
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    icon = Icons.Default.Search,
                    title = "Find a ride",
                    subtitle = "Find shared rides near you",
                    onClick = { /* TODO: Navigate to Find a Ride */ },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            SafeSimpleTogetherCard()
            Spacer(modifier = Modifier.height(6.dp))
        }

        BottomNavigationBar(navController = navController)
    }
}

// ── Search Bar ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarSection() {
    var textInput by remember { mutableStateOf("") }
    OutlinedTextField(
        value = textInput,
        onValueChange = { textInput = it},
        placeholder = { Text("Where are you going?", color = TextMuted, fontSize = 14.sp) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenPrimary,
            unfocusedBorderColor = CardBorder,
            focusedContainerColor = PageBg,
            unfocusedContainerColor = PageBg
        ),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextPrimary)
    )
}

// ── Action Cards ───────────────────────────────────────────────────
@Composable
fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "cardScale"
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(0.5.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
    }
}

// ── Trust Card ─────────────────────────────────────────────────────
@Composable
fun SafeSimpleTogetherCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GreenSurface)
            .border(0.5.dp, GreenSubBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(GreenPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "Safe. Simple. Together.",
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color(0xFF27500A)
            )
            Text(
                text = "Verified users · Secure rides · Community",
                fontSize = 11.sp,
                color = Color(0xFF3B6D11),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ── Bottom Navigation ──────────────────────────────────────────────
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = DividerColor,
                shape = RoundedCornerShape(0.dp)
            )
            .background(color = BottomNavigator)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Default.Home,
            label = "Home",
            isSelected = true,
            onClick = { /* TODO: Navigate to Home */ }
        )
        BottomNavItem(
            icon = Icons.Default.LocationOn,
            label = "My rides",
            isSelected = false,
            onClick = { /* TODO: Navigate to My Rides */ }
        )
        BottomNavItem(
            icon = Icons.Default.Email,
            label = "Messages",
            isSelected = false,
            onClick = { /* TODO: Navigate to Messages */ }
        )
        BottomNavItem(
            icon = Icons.Default.AccountCircle,
            label = "Profile",
            isSelected = false,
            onClick = { /* TODO: Navigate to Profile */ }
        )
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "bottomNavScale")

    val tint = if (isSelected) GreenPrimary else TextMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Text(text = label, fontSize = 10.sp, color = tint, fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal)
    }
}
