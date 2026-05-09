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
import java.lang.ref.WeakReference
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.ProfileRepositoryImpl
import com.rom.poa_firstapp.data.repository.RideRepositoryImpl
import com.rom.poa_firstapp.ui.common.ErrorState
import com.rom.poa_firstapp.ui.common.LoadingState
import com.rom.poa_firstapp.ui.navigation.ROUTES
import com.rom.poa_firstapp.ui.screen.profile.ProfileViewModel
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// DESIGN SYSTEM — Neon Noir Metropolis
// ─────────────────────────────────────────────────────────────────────────────
private val Void          = Color(0xFF050811)   // absolute dark bg
private val Abyss         = Color(0xFF080C1C)   // primary bg
private val Cavern        = Color(0xFF0E1325)   // card bg
private val Crater        = Color(0xFF141929)   // elevated card bg
private val GlassEdge     = Color(0x18FFFFFF)   // subtle border
private val GlassEdgeMid  = Color(0x30FFFFFF)   // medium border
private val GlassEdgeBold = Color(0x55FFFFFF)   // bold border

private val CyanPrimary   = Color(0xFF00E5FF)   // electric cyan
private val CyanSoft      = Color(0xFF00B4D8)   // softer cyan
private val CyanGlow      = Color(0x3300E5FF)   // cyan glow
private val CoralPrimary  = Color(0xFFFF4D7D)   // vivid coral
private val CoralGlow     = Color(0x33FF4D7D)   // coral glow
private val MintPrimary   = Color(0xFF00FFA3)   // mint green
private val MintGlow      = Color(0x2200FFA3)   // mint glow
private val GoldAccent    = Color(0xFFFFBB00)   // gold
private val GoldGlow      = Color(0x33FFBB00)   // gold glow
private val PurpleAccent  = Color(0xFFAA55FF)   // neon purple
private val RedPrimary    = Color(0xFFFF3B47)   // paid ride red
private val RedGlow       = Color(0x33FF3B47)   // red glow

private val TextHero      = Color(0xFFFFFFFF)

// Maps a ride status to its hex marker colour used in the WebView map
private fun statusMarkerColor(status: String) = when (status.trim().lowercase()) {
    "free"    -> "#00FFA3"   // green
    "paid"    -> "#FF3B47"   // red
    "pending" -> "#FFBB00"   // yellow
    else      -> "#00E5FF"   // default cyan
}
private val TextPrimary   = Color(0xFFE8EEFF)
private val TextSecondary = Color(0xFF8896B8)
private val TextMuted     = Color(0xFF4A5568)

// Sheet layout
private val SHEET_PEEK_HEIGHT = 260.dp
private val SHEET_FULL_HEIGHT = 620.dp
private val NAV_HEIGHT        = 72.dp   // fixed bottom nav height

// ─────────────────────────────────────────────────────────────────────────────
// Map Manager — Singleton to control WebView from other screens
// ─────────────────────────────────────────────────────────────────────────────
object MapManager {
    private var webViewRef: WeakReference<WebView>? = null

    fun setWebView(webView: WebView) {
        webViewRef = WeakReference(webView)
    }

    fun refreshMap() {
        webViewRef?.get()?.let { webView ->
            webView.post {
                webView.reload()
            }
        }
    }

    fun evaluateJavascript(js: String) {
        webViewRef?.get()?.let { webView ->
            webView.post {
                webView.evaluateJavascript(js, null)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun buildRideJs(ride: Ride): String {
    val name = ride.rider_name.replace("'", "\\'").replace("\"", "\\\"")
    val dest = (ride.destination ?: "Unknown").replace("'", "\\'").replace("\"", "\\\"")
    val pick = (ride.pickup_location ?: "Unknown").replace("'", "\\'").replace("\"", "\\\"")
    val dep  = (ride.departure_time ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
    val date = (ride.departure_date ?: "N/A").replace("'", "\\'").replace("\"", "\\\"")
    val color = statusMarkerColor(ride.status ?: "free")
    val avatar = ride.rider_avatar_url ?: ""
    val lat = ride.start_lat ?: 0.0
    val lng = ride.start_lng ?: 0.0

    return "addOrUpdateRideMarker('${ride.id}','${ride.rider_id}',\"$name\",${ride.seats_left},'${ride.rider_phone}',$lat,$lng,'${ride.status}',\"$dest\",\"$pick\",\"$dep\",\"$date\",'$avatar','$color');"
}

// ─────────────────────────────────────────────────────────────────────────────
// HomeScreen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(
            ProfileRepositoryImpl(SupabaseModule.client),
            SupabaseModule.client
        )
    },
    homeViewModel: HomeViewModel = viewModel {
        HomeViewModel(RideRepositoryImpl(SupabaseModule.client))
    }
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { profileViewModel.loadProfile() }
    val profile = profileViewModel.profile

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Draggable sheet state
    val peekOffset = remember(density) { with(density) { (SHEET_FULL_HEIGHT - SHEET_PEEK_HEIGHT).toPx() } }
    val offsetY = remember { Animatable(peekOffset) }

    val navigateToProfile: (String?) -> Unit = { uid ->
        val route = if (uid != null) "${ROUTES.Profile.name}?profileId=$uid" else ROUTES.Profile.name
        navController.navigate(route)
    }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isMapReady by remember { mutableStateOf(false) }

    // Synchronize markers when rides change or map is ready
    LaunchedEffect(homeViewModel.rides, isMapReady) {
        if (isMapReady && webViewInstance != null) {
            Log.d("HomeScreen", "Syncing ${homeViewModel.rides.size} rides to map")
            
            // Clear existing markers
            webViewInstance?.evaluateJavascript("for(var id in rideMarkers) removeRideMarker(id);", null)

            homeViewModel.rides.forEach { ride ->
                if (ride.start_lat == null || ride.start_lng == null || ride.start_lat == 0.0) {
                    Log.w("HomeScreen", "Ride ${ride.id} has invalid coordinates: ${ride.start_lat}, ${ride.start_lng}")
                }
                val js = buildRideJs(ride)
                webViewInstance?.evaluateJavascript(js, null)
            }
            
            // Auto-fit to show all markers
            webViewInstance?.evaluateJavascript("fitAllMarkers();", null)
        }
    }

    // Handle Realtime Actions separately for efficiency
    LaunchedEffect(isMapReady) {
        if (isMapReady) {
            homeViewModel.rideActions.collect { action ->
                when (action) {
                    is PostgresAction.Insert -> {
                        val ride = action.decodeRecord<Ride>()
                        if (ride.seats_left > 0 && ride.status != "completed" && ride.status != "cancelled") {
                            webViewInstance?.evaluateJavascript(buildRideJs(ride), null)
                        }
                    }
                    is PostgresAction.Update -> {
                        val ride = action.decodeRecord<Ride>()
                        if (ride.seats_left <= 0 || ride.status == "completed" || ride.status == "cancelled") {
                            webViewInstance?.evaluateJavascript("removeRideMarker('${ride.id}');", null)
                        } else {
                            webViewInstance?.evaluateJavascript(buildRideJs(ride), null)
                        }
                    }
                    is PostgresAction.Delete -> {
                        val id = action.oldRecord["id"]?.toString()?.replace("\"", "")
                        if (id != null) {
                            webViewInstance?.evaluateJavascript("removeRideMarker('$id');", null)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Abyss)
    ) {
        // ── Map (WebView) ─────────────────────────────────────────
        if (LocalInspectionMode.current) {
            Box(
                modifier = Modifier.fillMaxSize().background(Cavern),
                contentAlignment = Alignment.Center
            ) {
                Text("Map Preview", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewInstance = this
                        MapManager.setWebView(this)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isMapReady = true
                            }
                        }
                        settings.apply {
                            javaScriptEnabled = true
                            setGeolocationEnabled(true)
                            domStorageEnabled = true
                            cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                                callback?.invoke(origin, true, false)
                            }
                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                Log.d("WebViewConsole", "${consoleMessage?.messageLevel()}: ${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                                return true
                            }
                        }
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onMarkerClick(id: String, riderId: String, name: String, seats: Int, phone: String, lat: Double, lng: Double, status: String) {
                                post {
                                    navController.navigate("${ROUTES.RideDetails.name}/$id")
                                }
                            }

                            @JavascriptInterface
                            fun onLocationUpdate(lat: Double, lng: Double) {
                                post {
                                    homeViewModel.updateLocation(lat, lng)
                                    // Refresh rides if it's the first time we get location or if we want to re-filter
                                    homeViewModel.refreshRides()
                                }
                            }
                        }, "Android")
                        loadUrl("file:///android_asset/index.html")
                    }
                },
                update = { /* Updates handled via LaunchedEffects for performance */ }
            )
        }

        // ── Top overlay ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Abyss, Abyss.copy(alpha = 0.92f), Color.Transparent),
                        startY = 0f,
                        endY   = Float.POSITIVE_INFINITY
                    )
                )
                .statusBarsPadding()
        ) {
            Column {
                TopAppBarSection(
                    onMenuClick    = {},
                    onProfileClick = { navigateToProfile(null) },
                    avatarUrl      = profile?.avatar_url
                )
                LiveFilterBar()
            }
        }

        // ── Location FAB ───────────────────────────────────────────
        val fabOffset = (SHEET_FULL_HEIGHT - with(density) { offsetY.value.toDp() }) + 16.dp
        GlowingFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp)
                .offset(y = -fabOffset),
            onClick  = { webViewInstance?.evaluateJavascript("mymap.locate({setView: true});", null) }
        )

        // ── Draggable bottom sheet ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, offsetY.value.toInt()) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                val target = if (offsetY.value < peekOffset / 2f) 0f else peekOffset
                                offsetY.animateTo(
                                    target,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness    = Spring.StiffnessMedium
                                    )
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
                onProfileClick = { navigateToProfile(null) },
                avatarUrl      = profile?.avatar_url,
                userType       = profile?.user_type ?: "passenger"
            )
        }

        // ── Fixed bottom nav — always visible above the sheet ─────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BottomNavigationBar(
                navController  = navController,
                onProfileClick = { navigateToProfile(null) },
                avatarUrl      = profile?.avatar_url
            )
        }

        if (homeViewModel.isLoading) LoadingState()
        homeViewModel.errorMessage?.let { ErrorState(message = it) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Glowing Location FAB
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GlowingFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(44.dp)
            .drawBehind {
                drawCircle(
                    color  = CyanGlow,
                    radius = size.minDimension / 2f + 8.dp.toPx()
                )
            }
            .background(Cavern, CircleShape)
            .border(1.dp, CyanPrimary.copy(alpha = 0.6f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.MyLocation,
            contentDescription = "My Location",
            tint     = CyanPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top App Bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TopAppBarSection(onMenuClick: () -> Unit, onProfileClick: () -> Unit, avatarUrl: String?) {
    Row(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Menu button
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Cavern, CircleShape)
                .border(0.5.dp, GlassEdgeMid, CircleShape)
                .clickable(onClick = onMenuClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(9.dp)
            ) {
                Box(modifier = Modifier.width(16.dp).height(1.5.dp).background(TextPrimary, RoundedCornerShape(1.dp)))
                Box(modifier = Modifier.width(11.dp).height(1.5.dp).background(CyanPrimary, RoundedCornerShape(1.dp)))
                Box(modifier = Modifier.width(16.dp).height(1.5.dp).background(TextPrimary, RoundedCornerShape(1.dp)))
            }
        }

        // Brand name with gradient removed as per user request
        Spacer(modifier = Modifier.weight(1f))

        // Avatar / profile button
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Cavern, CircleShape)
                .border(1.dp, CyanPrimary.copy(alpha = 0.5f), CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model              = avatarUrl,
                    contentDescription = "Profile",
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = CyanPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Live Filter Bar  (with pulsing "LIVE" badge)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LiveFilterBar() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue   = 0.4f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Live badge
        Row(
            modifier = Modifier
                .background(CoralGlow, RoundedCornerShape(8.dp))
                .border(1.dp, CoralPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(CoralPrimary.copy(alpha = pulseAlpha), CircleShape)
            )
            Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoralPrimary, letterSpacing = 1.sp)
        }

        FilterPill(label = "All Rides", accent = CyanPrimary,  selected = true)
        FilterPill(label = "Free",      accent = MintPrimary,  selected = false)
        FilterPill(label = "Paid",      accent = RedPrimary,   selected = false)
        FilterPill(label = "Pending",   accent = GoldAccent,   selected = false)
    }
}

@Composable
fun FilterPill(label: String, accent: Color, selected: Boolean) {
    Surface(
        shape  = RoundedCornerShape(20.dp),
        color  = if (selected) accent.copy(alpha = 0.22f) else Cavern,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) accent else accent.copy(alpha = 0.4f)
        ),
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier.padding(horizontal = 14.dp)
        ) {
            Text(
                text       = label,
                fontSize   = 12.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color      = if (selected) Color.White else accent
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Sheet Content
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BottomSheetContent(navController: NavHostController, onProfileClick: () -> Unit, avatarUrl: String?, userType: String = "passenger") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(SHEET_FULL_HEIGHT)
            .drawBehind {
                // Top glow line
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    brush       = Brush.horizontalGradient(
                        listOf(Color.Transparent, CyanPrimary.copy(alpha = 0.6f), PurpleAccent.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    start       = Offset(0f, strokeWidth / 2),
                    end         = Offset(size.width, strokeWidth / 2),
                    strokeWidth = strokeWidth
                )
            }
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Crater, Abyss),
                    startY = 0f,
                    endY   = Float.POSITIVE_INFINITY
                )
            )
    ) {
        // Drag handle
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(listOf(CyanPrimary.copy(alpha = 0.3f), PurpleAccent.copy(alpha = 0.5f))),
                        RoundedCornerShape(2.dp)
                    )
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            StatsRow()
            SearchBarSection(navController)
            ActionCardsRow(navController, userType)
            QuickInfoGrid()
            Spacer(modifier = Modifier.height(NAV_HEIGHT + 8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stats Row  (live numbers above search)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StatsRow() {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatChip(value = "12", label = "Active Rides", color = CyanPrimary)
        StatChip(value = "8",  label = "Nearby",       color = MintPrimary)
        StatChip(value = "3",  label = "Min Away",     color = GoldAccent)
    }
}

@Composable
fun StatChip(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = TextSecondary, letterSpacing = 0.3.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search Bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SearchBarSection(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .drawBehind {
                drawRoundRect(
                    brush       = Brush.horizontalGradient(
                        listOf(CyanPrimary.copy(alpha = 0.6f), PurpleAccent.copy(alpha = 0.5f))
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                    style        = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
            .background(Cavern, RoundedCornerShape(12.dp))
            .clickable { navController.navigate(ROUTES.FindRide.name) }
    ) {
        Row(
            modifier          = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Where are you headed?", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Normal)
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(CyanGlow, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("GO", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = CyanPrimary, letterSpacing = 1.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Action Cards Row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ActionCardsRow(navController: NavHostController, userType: String = "passenger") {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (userType.lowercase() == "driver") {
            ActionCard(
                icon      = Icons.Default.Add,
                title     = "Post Ride",
                subtitle  = "Share your seats",
                accent    = CyanPrimary,
                glowColor = CyanGlow,
                onClick   = { navController.navigate(ROUTES.PostRide.name) },
                modifier  = Modifier.weight(1f)
            )
        }
        ActionCard(
            icon      = Icons.Default.Search,
            title     = "Find Ride",
            subtitle  = "Join the journey",
            accent    = CoralPrimary,
            glowColor = CoralGlow,
            onClick   = { navController.navigate(ROUTES.FindRide.name) },
            modifier  = if (userType.lowercase() == "driver") Modifier.weight(1f) else Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ActionCard(
    icon      : ImageVector,
    title     : String,
    subtitle  : String,
    accent    : Color,
    glowColor : Color,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .drawBehind {
                drawRoundRect(
                    brush        = Brush.linearGradient(
                        listOf(accent.copy(alpha = 0.8f), accent.copy(alpha = 0.2f)),
                        start = Offset(0f, 0f),
                        end   = Offset(size.width, size.height)
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                    style        = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
            .background(Cavern, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon with glow bg
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(accent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title,    fontWeight = FontWeight.Bold,   fontSize = 14.sp, color = TextPrimary)
                Text(subtitle, fontWeight = FontWeight.Normal, fontSize = 11.sp, color = TextSecondary)
            }
        }

        // Decorative corner glow
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(60.dp)
                .drawBehind {
                    drawCircle(
                        color  = glowColor,
                        radius = size.minDimension / 1.5f,
                        center = Offset(size.width, 0f)
                    )
                }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick Info Grid (Trust, Schedule, Rating, Safety)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuickInfoGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section label
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Community Features", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSecondary, letterSpacing = 0.5.sp)
        }

        // Trust card (full width)
        InfoCardWide(
            icon     = Icons.Default.VerifiedUser,
            title    = "Verified Community",
            subtitle = "Every driver is ID-verified & reviewed",
            accent   = MintPrimary
        )
    }
}

@Composable
fun InfoCardWide(icon: ImageVector, title: String, subtitle: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cavern, RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(accent.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title,    fontWeight = FontWeight.Bold,   fontSize = 13.sp, color = TextPrimary)
            Text(subtitle, fontWeight = FontWeight.Normal, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Navigation Bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BottomNavigationBar(navController: NavHostController, onProfileClick: () -> Unit, avatarUrl: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    brush       = Brush.horizontalGradient(
                        listOf(Color.Transparent, CyanPrimary.copy(alpha = 0.3f), PurpleAccent.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    start       = Offset(0f, 0f),
                    end         = Offset(size.width, 0f),
                    strokeWidth = 0.5.dp.toPx()
                )
            }
            .background(Abyss)
            .padding(top = 10.dp, bottom = 24.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        BottomNavItem(icon = Icons.Default.Home,           label = "Home",  isSelected = true,  onClick = {})
        BottomNavItem(icon = Icons.Default.DirectionsCar,  label = "Rides", isSelected = false, onClick = { navController.navigate(ROUTES.MyRides.name) })
        BottomNavItem(icon = Icons.Default.ChatBubbleOutline, label = "Chat",  isSelected = false, onClick = { navController.navigate(ROUTES.Messages.name) })

        // Profile nav item
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier            = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onProfileClick)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model              = avatarUrl,
                    contentDescription = "Profile",
                    modifier           = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.dp, TextMuted, CircleShape),
                    contentScale       = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = TextSecondary, modifier = Modifier.size(24.dp))
            }
            Text("Profile", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val tint = if (isSelected) CyanPrimary else TextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier            = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CyanGlow else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Text(
            text       = label,
            fontSize   = 10.sp,
            color      = tint,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0xFF050811)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(navController = rememberNavController())
    }
}