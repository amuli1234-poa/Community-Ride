package com.rom.poa_firstapp.ui.screen.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
//import com.google.accompanist.pager.*
import com.rom.poa_firstapp.R
import com.rom.poa_firstapp.ui.navigation.ROUTES
import kotlinx.coroutines.launch

import androidx.lifecycle.viewmodel.compose.viewModel
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.AuthRepositoryImpl
import com.rom.poa_firstapp.ui.screen.authentication.AuthViewModel

// ─── Design Tokens ────────────────────────────────────────────────────────────

private val DeepSpace    = Color(0xFF080B1A)
private val NightSky     = Color(0xFF0D1230)
private val PurpleDark   = Color(0xFF160835)
private val NebulaViolet = Color(0xFF6C2EFF)
private val CometCyan    = Color(0xFF00E5FF)
private val StarPink     = Color(0xFFFF3CAC)
private val AuroraGreen  = Color(0xFF00FFA3)
private val TextPrimary  = Color(0xFFF0F4FF)
private val TextMuted    = Color(0xFF8892B0)

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
private val ActiveDotGradient = Brush.horizontalGradient(
    colors = listOf(NebulaViolet, StarPink)
)
private val ProgressGradient = Brush.horizontalGradient(
    colors = listOf(NebulaViolet, CometCyan)
)
private val OrbGradient1 = Brush.radialGradient(
    colors = listOf(NebulaViolet.copy(alpha = 0.45f), Color.Transparent)
)
private val OrbGradient2 = Brush.radialGradient(
    colors = listOf(StarPink.copy(alpha = 0.30f), Color.Transparent)
)
private val OrbGradient3 = Brush.radialGradient(
    colors = listOf(CometCyan.copy(alpha = 0.20f), Color.Transparent)
)

// ─── Data ─────────────────────────────────────────────────────────────────────

data class OnboardingPage(
    val tag:      String,
    val title:    String,
    val body:     String,
//    val lottieRes: Int
)

private val pages = listOf(
    OnboardingPage(
        tag       = "WELCOME TO",
        title     = "Community Ride",
        body      = "Join a community of riders who share rides. you can also get free rides in this community. This App is Build to Benefit both private car owners and pedestrians",
    ),
    OnboardingPage(
        tag       = "SMART MATCHING",
        title     = "Find Rides\nNear You",
        body      = "Our smart map shows nearby riders going your way. Match in seconds and hit the road together without any difficulties."
    ),
    OnboardingPage(
        tag       = "YOUR SAFETY",
        title     = "Ride with\nConfidence",
        body      = "Every rider is verified Before given a Chance to Share the Rides. This Reduces Chances of Insecurity.Making Community Ride A Better Platform to Share Rides.",
    ),
    OnboardingPage(
        tag       = "LET'S ROLL",
        title     = "Cut Costs,\nNot Comfort",
        body      = "Community Ride is Going To Help You Find RIdes That Are Near You , Connect with the drivers And Agree on Amounts that Favors Both Riders and Drivers .",

    )
)

// ─── Onboarding Screen ────────────────────────────────────────────────────────

//@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel {
        AuthViewModel(AuthRepositoryImpl(SupabaseModule.client))
    }
) {
    println("DEBUG: OnboardingScreen composing")
    LaunchedEffect(Unit) {
        println("DEBUG: OnboardingScreen LaunchedEffect")
        if (authViewModel.isUserLoggedIn()) {
            println("DEBUG: User is logged in, navigating to Home from Onboarding")
            navController.navigate(ROUTES.Home.name) {
                popUpTo(ROUTES.Onboarding.name) { inclusive = true }
            }
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )
    val scope      = rememberCoroutineScope()
    val currentPage = pagerState.currentPage

    fun navigateNext() {
        if (currentPage < pages.lastIndex) {
            scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
        } else {
            println("DEBUG: Onboarding finished, navigating to Login")
            navController.navigate(ROUTES.Login.name) {
                popUpTo(ROUTES.Onboarding.name) { inclusive = true }
            }
        }
    }

    fun skipToEnd() {
        scope.launch { pagerState.animateScrollToPage(pages.lastIndex) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {

        // ── Glow orbs ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .offset(x = (-60).dp, y = 80.dp)
                .size(220.dp)
                .background(OrbGradient1, CircleShape)
                .blur(60.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = (-100).dp)
                .size(180.dp)
                .background(OrbGradient2, CircleShape)
                .blur(55.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = 200.dp)
                .size(140.dp)
                .background(OrbGradient3, CircleShape)
                .blur(50.dp)
        )

        // ── Progress bar (top) ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.White.copy(alpha = 0.08f))
                .align(Alignment.TopCenter)
                .zIndex(10f)
        ) {
            val progress = (currentPage + 1).toFloat() / pages.size.toFloat()
            val animatedProgress by animateFloatAsState(
                targetValue    = progress,
                animationSpec  = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                label          = "progress"
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(ProgressGradient)
            )
        }

        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Pager ─────────────────────────────────────────────────────
            HorizontalPager(
//                count    = pages.size,
                state    = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                OnboardingPage(page = pages[pageIndex])
            }

            // ── Bottom controls ───────────────────────────────────────────
            OnboardingControls(
                currentPage  = currentPage,
                pageCount    = pages.size,
                pagerState   = pagerState,
                onNext       = ::navigateNext,
                onSkip       = ::skipToEnd,
                onDotClick   = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                }
            )
        }
    }
}

// ─── Single Slide ─────────────────────────────────────────────────────────────

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Lottie animation
//
//        OnboardingLottie(lottieRes = page.lottieRes)

        Spacer(Modifier.height(32.dp))

        // Tag
        Text(
            text  = page.tag,
            style = TextStyle(
                fontSize      = 11.sp,
                fontWeight    = FontWeight.SemiBold,
                color         = TextMuted,
                letterSpacing = 4.sp
            )
        )

        Spacer(Modifier.height(10.dp))

        // Title
        Text(
            text      = page.title,
            textAlign = TextAlign.Center,
            style     = TextStyle(
                fontSize   = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                brush      = HeroGradient,
                lineHeight = 40.sp
            )
        )

        Spacer(Modifier.height(16.dp))

        // Accent divider
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(listOf(CometCyan, AuroraGreen))
                )
        )

        Spacer(Modifier.height(16.dp))

        // Body
        Text(
            text      = page.body,
            textAlign = TextAlign.Center,
            style     = TextStyle(
                fontSize   = 15.sp,
                color      = TextMuted,
                lineHeight = 24.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

// ─── Bottom Controls ──────────────────────────────────────────────────────────

//@OptIn(ExperimentalPagerApi::class)
@Composable
private fun OnboardingControls(
    currentPage : Int,
    pageCount   : Int,
    pagerState  : PagerState,
    onNext      : () -> Unit,
    onSkip      : () -> Unit,
    onDotClick  : (Int) -> Unit
) {
    val isLast = currentPage == pageCount - 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Dot indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val isActive = index == currentPage
                val width by animateDpAsState(
                    targetValue   = if (isActive) 22.dp else 6.dp,
                    animationSpec = tween(300),
                    label         = "dot_width_$index"
                )
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(width)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isActive) ActiveDotGradient
                            else          Brush.horizontalGradient(
                                listOf(TextMuted.copy(alpha = 0.4f), TextMuted.copy(alpha = 0.4f))
                            )
                        )
                        .clickable { onDotClick(index) }
                )
            }
        }

        // Primary CTA button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(50))
                .background(ButtonGradient)
                .clickable { onNext() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = if (isLast) "LET'S RIDE  →" else "NEXT  →",
                style = TextStyle(
                    fontSize      = 14.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = TextPrimary,
                    letterSpacing = 2.sp
                )
            )
        }

        // Skip button
        if (!isLast) {
            Text(
                text     = "Skip intro",
                style    = TextStyle(
                    fontSize = 13.sp,
                    color    = TextMuted
                ),
                modifier = Modifier
                    .clickable { onSkip() }
                    .padding(vertical = 4.dp)
            )
        }
    }
}

// ─── Lottie Widget ────────────────────────────────────────────────────────────

@Composable
private fun OnboardingLottie(lottieRes: Int) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(lottieRes)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations  = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress    = { progress },
        modifier    = Modifier.size(240.dp)
    )
}