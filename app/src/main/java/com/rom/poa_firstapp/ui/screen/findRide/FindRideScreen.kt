package com.rom.poa_firstapp.ui.screen.findRide

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rom.poa_firstapp.data.model.Ride
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.RideRepositoryImpl
import com.rom.poa_firstapp.ui.navigation.ROUTES
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FindRideViewModelFactory(private val repository: com.rom.poa_firstapp.data.repository.RideRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FindRideViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FindRideViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Design Tokens (same as RideDetailsScreen)
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

// ─────────────────────────────────────────────────────────────────────────────
// Status helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun statusColor(status: String) = when (status.lowercase()) {
    "free"    -> MintPrimary
    "paid"    -> GoldAccent
    "pending" -> CoralPrimary
    "full"    -> RedPrimary
    else      -> CyanPrimary
}
private fun statusBg(status: String)     = statusColor(status).copy(alpha = 0.10f)
private fun statusBorder(status: String) = statusColor(status).copy(alpha = 0.25f)

// ─────────────────────────────────────────────────────────────────────────────
// Filter chip model
// ─────────────────────────────────────────────────────────────────────────────
private data class FilterOption(val label: String, val color: Color)
private val filterOptions = listOf(
    FilterOption("All",     CyanPrimary),
    FilterOption("Free",    MintPrimary),
    FilterOption("Paid",    GoldAccent),
    FilterOption("Pending", CoralPrimary)
)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindRideScreen(navController: NavController, viewModel: FindRideViewModel = viewModel(
    factory = FindRideViewModelFactory(RideRepositoryImpl(SupabaseModule.client))
)) {
    val searchQuery     = viewModel.searchQuery
    val selectedFilter  = viewModel.selectedFilter
    val isLoading       = viewModel.isLoading
    val rides           = viewModel.filteredRides

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Find a Ride", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 17.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Box(
                            modifier         = Modifier
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cavern)
            )
        },
        containerColor = Abyss
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search bar ────────────────────────────────────────────
            SearchBar(
                query    = searchQuery,
                onChange = { viewModel.onSearchQueryChange(it) },
                onClear  = { viewModel.onSearchQueryChange("") }
            )

            // ── Filter chips ──────────────────────────────────────────
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.padding(bottom = 12.dp)
            ) {
                items(filterOptions) { opt ->
                    FilterChip(
                        option     = opt,
                        isSelected = selectedFilter == opt.label,
                        onClick    = { viewModel.onFilterChange(opt.label) }
                    )
                }
            }

            // ── Results label ─────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("AVAILABLE RIDES", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                Text("${rides.size} found", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CyanPrimary)
            }

            // ── List / Loading / Empty ────────────────────────────────
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CyanPrimary, strokeWidth = 2.5.dp)
                    }
                }
                rides.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    LazyColumn(
                        contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        verticalArrangement   = Arrangement.spacedBy(12.dp),
                        modifier              = Modifier.fillMaxSize()
                    ) {
                        items(rides) { ride ->
                            RideSearchCard(
                                ride    = ride,
                                onClick = { navController.navigate("${ROUTES.RideDetails.name}/${ride.id}") }
                            )
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SearchBar(query: String, onChange: (String) -> Unit, onClear: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Abyss)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        OutlinedTextField(
            value         = query,
            onValueChange = onChange,
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = { Text("Search by destination...", color = TextMuted, fontSize = 14.sp) },
            leadingIcon   = { Icon(Icons.Default.Search, null, tint = CyanPrimary, modifier = Modifier.size(20.dp)) },
            trailingIcon  = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine    = true,
            shape         = RoundedCornerShape(14.dp),
            textStyle     = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextPrimary),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = CyanPrimary.copy(alpha = 0.5f),
                unfocusedBorderColor    = GlassEdge,
                focusedContainerColor   = Cavern,
                unfocusedContainerColor = Cavern,
                cursorColor             = CyanPrimary
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter chip
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FilterChip(option: FilterOption, isSelected: Boolean, onClick: () -> Unit) {
    val bg     = if (isSelected) option.color.copy(alpha = 0.12f) else Cavern
    val border = if (isSelected) option.color.copy(alpha = 0.35f) else GlassEdge
    val text   = if (isSelected) option.color else TextSecondary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(option.color)
            )
        }
        Text(option.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = text, letterSpacing = 0.3.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ride card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RideSearchCard(ride: Ride, onClick: () -> Unit) {
    val avatarColor = when (ride.status.lowercase()) {
        "paid"    -> CoralPrimary
        "pending" -> GoldAccent
        else      -> CyanPrimary
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        color    = Cavern,
        border   = BorderStroke(1.dp, GlassEdge)
    ) {
        Column {
            // ── Header row ───────────────────────────────────────────
            Row(
                modifier          = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier         = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Crater)
                        .border(1.5.dp, avatarColor.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = avatarColor, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(ride.rider_name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    // Status pill
                    Row(
                        modifier          = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusBg(ride.status))
                            .border(1.dp, statusBorder(ride.status), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(statusColor(ride.status)))
                        Text(ride.status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor(ride.status), letterSpacing = 0.4.sp)
                    }
                }

                Spacer(Modifier.weight(1f))

                // Time / Date
                Column(horizontalAlignment = Alignment.End) {
                    Text(ride.departure_time ?: "--:--", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(ride.departure_date ?: "N/A", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                }
            }

            // Divider
            HorizontalDivider(color = GlassEdge, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 14.dp))

            // ── Route ────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                // Pickup
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(CyanPrimary.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocationOn, null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                    }
                    Column {
                        Text("PICKUP", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = CyanPrimary, letterSpacing = 0.4.sp)
                        Text(ride.pickup_location ?: "Unknown", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                }
                // Connector line
                Row(modifier = Modifier.padding(start = 13.dp, top = 3.dp, bottom = 3.dp)) {
                    Box(
                        modifier = Modifier.width(2.dp).height(14.dp).background(
                            Brush.linearGradient(listOf(CyanPrimary.copy(alpha = 0.4f), CoralPrimary.copy(alpha = 0.4f)), start = Offset(0f, 0f), end = Offset(0f, Float.POSITIVE_INFINITY))
                        )
                    )
                }
                // Destination
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(CoralPrimary.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Flag, null, tint = CoralPrimary, modifier = Modifier.size(14.dp))
                    }
                    Column {
                        Text("DESTINATION", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = CoralPrimary, letterSpacing = 0.4.sp)
                        Text(ride.destination ?: "Unknown", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                }
            }

            // Divider
            HorizontalDivider(color = GlassEdge, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 14.dp))

            // ── Footer ───────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Seats badge
                Row(
                    modifier          = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MintPrimary.copy(alpha = 0.10f))
                        .border(1.dp, MintPrimary.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.AirlineSeatReclineNormal, null, tint = MintPrimary, modifier = Modifier.size(13.dp))
                    Text("${ride.seats_left} seats left", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MintPrimary)
                }

                // View details
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("View Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    Icon(Icons.Default.ChevronRight, null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Cavern)
                    .border(1.dp, GlassEdge, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsCar, null, tint = TextMuted, modifier = Modifier.size(38.dp))
            }
            Text("No rides found", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Text("Try a different search or filter", fontSize = 13.sp, color = TextMuted, textAlign = TextAlign.Center)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewFindRideScreen() {
    MaterialTheme {
        FindRideScreen(navController = rememberNavController())
    }
}