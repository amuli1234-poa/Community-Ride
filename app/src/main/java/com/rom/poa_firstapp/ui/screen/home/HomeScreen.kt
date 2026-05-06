package com.rom.poa_firstapp.ui.screen.home

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.ProfileRepositoryImpl
import com.rom.poa_firstapp.data.repository.RideRepositoryImpl
import com.rom.poa_firstapp.ui.common.ErrorState
import com.rom.poa_firstapp.ui.common.LoadingState
import com.rom.poa_firstapp.ui.navigation.ROUTES
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Colour palette
// ---------------------------------------------------------------------------
private val GreenDeep    = Color(0xFF1A3A2A)
private val GreenMid     = Color(0xFF2D6A4F)
private val GreenBright  = Color(0xFF40916C)
private val GreenLight   = Color(0xFF52B788)
private val GreenPale    = Color(0xFFD4E8D4)
private val GreenHint    = Color(0xFF74916C)
private val GreenSurface = Color(0xFFEAF3DE)
private val GreenSubBorder = Color(0xFFC0DD97)
private val GreenPrimary = Color(0xFF2E7D32)
private val PageBg       = Color(0xFFF4F6F3)
private val CardBg       = Color(0xFFFFFFFF)
private val CardBorder   = Color(0xFFD4E8D4)
private val TextPrimary  = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF5F5E5A)
private val TextMuted    = Color(0xFF888780)
private val DividerColor = Color(0xFFE0E6D8)
private val NavBg        = Color(0xFFF5F5F5)

// Bottom sheet height constants (in dp)
private val SHEET_PEEK_HEIGHT = 260.dp   // visible when collapsed
private val SHEET_FULL_HEIGHT = 560.dp  // visible when fully expanded

// ---------------------------------------------------------------------------
// HomeScreen
// ---------------------------------------------------------------------------
@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val rideRepository    = remember { RideRepositoryImpl(SupabaseModule.client) }
    val profileRepository = remember { ProfileRepositoryImpl(SupabaseModule.client) }
    val scope             = rememberCoroutineScope()
    var isLoading   by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ── Draggable sheet state ──────────────────────────────────────
    // offsetY = 0 → fully expanded (sheet fills screen), offsetY = peekOffset → peeking
    val peekOffset  = with(density) { (SHEET_FULL_HEIGHT - SHEET_PEEK_HEIGHT).toPx() }
    val offsetY     = remember { Animatable(peekOffset) }

    val navigateToProfile: (String?) -> Unit = { uid ->
        navController.currentBackStackEntry?.savedStateHandle?.set("profileId", uid)
        navController.navigate(ROUTES.Profile.name)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(GreenDeep, GreenMid, GreenBright, GreenLight),
                    start  = Offset(0f, 0f),
                    end    = Offset(400f, 900f)
                )
            )
            .drawBehind { drawMapDots(this) }
    ) {
        var isMapReady by remember { mutableStateOf(false) }

        // ── Map (WebView) ──────────────────────────────────────────
        if (LocalInspectionMode.current) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .padding(top = 72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenMid),
                contentAlignment = Alignment.Center
            ) {
                Text("Interactive Map Preview", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
            }
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .padding(top = 72.dp),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isMapReady = true
                                scope.launch {
                                    try {
                                        val rides = rideRepository.getAllRides()
                                        Log.d("HomeScreen", "Fetched ${rides.size} rides from Supabase")
                                        (ctx as? androidx.activity.ComponentActivity)?.runOnUiThread {
                                            rides.forEach { ride ->
                                                val name = (ride.rider_name).replace("'", "\\'").replace("\"", "\\\"")
                                                val dest = (ride.destination ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
                                                val pick = (ride.pickup_location ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
                                                val dep = (ride.departure_time ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
                                                val date = (ride.departure_date ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")

                                                val js = "addOrUpdateRideMarker('${ride.id}', '${ride.rider_id}', \"$name\", ${ride.seats_left}, '${ride.rider_phone}', ${ride.start_lat}, ${ride.start_lng}, '${ride.status}', \"$dest\", \"$pick\", \"$dep\", \"$date\");"
                                                evaluateJavascript(js, null)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("HomeScreen", "Error loading initial rides", e)
                                    }
                                }
                            }
                        }
                        settings.javaScriptEnabled = true
                        settings.setGeolocationEnabled(true)
                        settings.domStorageEnabled  = true
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                Log.d("WebViewConsole", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                                return true
                            }
                            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                                val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                callback?.invoke(origin, granted, false)
                            }
                        }
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onMarkerClick(id: String, riderId: String, name: String, seats: Int, phone: String, lat: Double, lng: Double, status: String) {
                                (ctx as? androidx.activity.ComponentActivity)?.runOnUiThread {
                                    navigateToProfile(riderId)
                                }
                            }
                        }, "Android")
                        loadUrl("file:///android_asset/index.html")
                        scope.launch {
                            rideRepository.getRidesFlow().collect { action ->
                                if (isMapReady) {
                                    (ctx as? androidx.activity.ComponentActivity)?.runOnUiThread {
                                        try {
                                            when (action) {
                                                is PostgresAction.Insert -> action.decodeRecord<Ride>().let { ride ->
                                                    val name = (ride.rider_name).replace("'", "\\'").replace("\"", "\\\"")
                                                    val dest = (ride.destination ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
                                                    val pick = (ride.pickup_location ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
                                                    val dep = (ride.departure_time ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
                                                    val date = (ride.departure_date ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
                                                    evaluateJavascript("addOrUpdateRideMarker('${ride.id}', '${ride.rider_id}', \"$name\", ${ride.seats_left}, '${ride.rider_phone}', ${ride.start_lat}, ${ride.start_lng}, '${ride.status}', \"$dest\", \"$pick\", \"$dep\", \"$date\");", null)
                                                }
                                                is PostgresAction.Update -> action.decodeRecord<Ride>().let { ride ->
                                                    val name = (ride.rider_name).replace("'", "\\'").replace("\"", "\\\"")
                                                    val dest = (ride.destination ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
                                                    val pick = (ride.pickup_location ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
                                                    val dep = (ride.departure_time ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
                                                    val date = (ride.departure_date ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
                                                    evaluateJavascript("addOrUpdateRideMarker('${ride.id}', '${ride.rider_id}', \"$name\", ${ride.seats_left}, '${ride.rider_phone}', ${ride.start_lat}, ${ride.start_lng}, '${ride.status}', \"$dest\", \"$pick\", \"$dep\", \"$date\");", null)
                                                }
                                                is PostgresAction.Delete -> evaluateJavascript("removeRideMarker('${action.oldRecord["id"]}');", null)
                                                else -> {}
                                            }
                                        } catch (e: Exception) {
                                            Log.e("HomeScreen", "Realtime update error", e)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        // ── Top bar + filters overlay ──────────────────────────────
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBarSection(
                onMenuClick    = {},
                onProfileClick = { navigateToProfile(null) }
            )
            RideStatusFilters()
        }

        // ── Location FAB — moves up with the sheet ─────────────────
        val fabBottomDp = with(density) { offsetY.value.toDp() } + SHEET_PEEK_HEIGHT - 60.dp
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp)
                .offset(y = -fabBottomDp.coerceAtLeast(56.dp))
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, CardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "My Location", tint = GreenPrimary, modifier = Modifier.size(20.dp))
        }

        // ── Draggable bottom sheet ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = with(density) { offsetY.value.toDp() })
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                // Snap: if more than halfway expanded, go full; else peek
                                val target = if (offsetY.value < peekOffset / 2f) 0f else peekOffset
                                offsetY.animateTo(
                                    target,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                                )
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            scope.launch {
                                val newVal = (offsetY.value + dragAmount).coerceIn(0f, peekOffset)
                                offsetY.snapTo(newVal)
                            }
                        }
                    )
                }
        ) {
            BottomSheetContent(
                navController  = navController,
                onProfileClick = { navigateToProfile(null) }
            )
        }

        if (isLoading)       LoadingState()
        errorMessage?.let { ErrorState(message = it) }
    }
}

// ---------------------------------------------------------------------------
// Top app bar
// ---------------------------------------------------------------------------
@Composable
fun TopAppBarSection(onMenuClick: () -> Unit, onProfileClick: () -> Unit) {
    val menuSource = remember { MutableInteractionSource() }
    val menuPressed by menuSource.collectIsPressedAsState()
    val menuScale by animateFloatAsState(if (menuPressed) 0.92f else 1f, label = "menuScale")

    val profileSource = remember { MutableInteractionSource() }
    val profilePressed by profileSource.collectIsPressedAsState()
    val profileScale by animateFloatAsState(if (profilePressed) 0.92f else 1f, label = "profileScale")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Menu button
        IconButton(
            onClick = onMenuClick,
            interactionSource = menuSource,
            modifier = Modifier
                .size(38.dp)
                .graphicsLayer { scaleX = menuScale; scaleY = menuScale }
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(20.dp))
        }

        // Title
        Text(
            text       = "Community Ride",
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.White,
            letterSpacing = (-0.3).sp
        )

        // Profile avatar
        IconButton(
            onClick = onProfileClick,
            interactionSource = profileSource,
            modifier = Modifier
                .size(38.dp)
                .graphicsLayer { scaleX = profileScale; scaleY = profileScale }
                .clip(CircleShape)
                .background(GreenLight.copy(alpha = 0.3f))
                .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Filter chips
// ---------------------------------------------------------------------------
@Composable
fun RideStatusFilters() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        FilterChip("Free ride",  Color(0xFF2E7D32), Color(0xFFE8F5E9), Color(0xFFA5D6A7), Color(0xFF1B5E20))
        FilterChip("Paid ride",  Color(0xFFD85A30), Color(0xFFFAECE7), Color(0xFFF5C4B3), Color(0xFF993C1D))
        FilterChip("Pending",    Color(0xFFBA7517), Color(0xFFFAEEDA), Color(0xFFFAC775), Color(0xFF854F0B))
    }
}

@Composable
fun FilterChip(text: String, dotColor: Color, bgColor: Color, borderColor: Color, textColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .border(0.5.dp, borderColor, RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(dotColor))
        Spacer(modifier = Modifier.width(5.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

// ---------------------------------------------------------------------------
// Bottom sheet content (the scrollable white card)
// ---------------------------------------------------------------------------
@Composable
fun BottomSheetContent(navController: NavHostController, onProfileClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(PageBg)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GreenPale)
            )
        }

        // Scrollable body — allows content to scroll when sheet is fully open
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SearchBarSection()
            ActionCardsRow(navController)
            SafeSimpleTogetherCard()
        }

        BottomNavigationBar(navController = navController, onProfileClick = onProfileClick)
    }
}

// ---------------------------------------------------------------------------
// Search bar
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarSection() {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value         = text,
        onValueChange = { text = it },
        placeholder   = { Text("Where are you going?", color = TextMuted, fontSize = 14.sp) },
        leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(14.dp),
        singleLine    = true,
        textStyle     = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextPrimary),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = GreenBright,
            unfocusedBorderColor = CardBorder,
            focusedContainerColor = CardBg,
            unfocusedContainerColor = CardBg
        )
    )
}

// ---------------------------------------------------------------------------
// Action cards
// ---------------------------------------------------------------------------
@Composable
fun ActionCardsRow(navController: NavHostController) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionCard(
            icon     = Icons.Default.Add,
            title    = "Post a ride",
            subtitle = "Offer a seat and ride together",
            onClick  = { navController.navigate(ROUTES.PostRide.name) },
            modifier = Modifier.weight(1f)
        )
        ActionCard(
            icon     = Icons.Default.Search,
            title    = "Find a ride",
            subtitle = "Find shared rides near you",
            onClick  = {},
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "cardScale")

    Card(
        onClick           = onClick,
        interactionSource = interactionSource,
        modifier          = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        shape             = RoundedCornerShape(16.dp),
        colors            = CardDefaults.cardColors(containerColor = CardBg),
        border            = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier         = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GreenMid),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// Trust card
// ---------------------------------------------------------------------------
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
            modifier         = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(GreenMid),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text("Safe. Simple. Together.", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF27500A))
            Text("Verified users · Secure rides · Community", fontSize = 11.sp, color = Color(0xFF3B6D11), modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom navigation bar
// ---------------------------------------------------------------------------
@Composable
fun BottomNavigationBar(navController: NavHostController, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = DividerColor, shape = RoundedCornerShape(0.dp))
            .background(NavBg)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        BottomNavItem(icon = Icons.Default.Home,          label = "Home",     isSelected = true,  onClick = {})
        BottomNavItem(icon = Icons.Default.LocationOn,    label = "My rides", isSelected = false, onClick = {})
        BottomNavItem(icon = Icons.Default.Email,         label = "Messages", isSelected = false, onClick = { navController.navigate(ROUTES.Messages.name) })
        BottomNavItem(icon = Icons.Default.AccountCircle, label = "Profile",  isSelected = false, onClick = onProfileClick)
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, label = "navScale")
    val tint = if (isSelected) GreenPrimary else TextMuted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier            = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(interactionSource = source, indication = LocalIndication.current, onClick = onClick)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Text(label, fontSize = 10.sp, color = tint, fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal)
    }
}

// ---------------------------------------------------------------------------
// Canvas helpers
// ---------------------------------------------------------------------------
private fun drawMapDots(scope: DrawScope) {
    val dot  = Color.White.copy(alpha = 0.05f)
    val step = 20f
    var x = 0f
    while (x < scope.size.width) {
        var y = 0f
        while (y < scope.size.height) {
            scope.drawCircle(dot, 1.2f, Offset(x, y))
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
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(navController = rememberNavController())
    }
}