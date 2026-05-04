package com.rom.poa_firstapp.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val primaryColor = Color(0xFF4F2B91)
val secondaryColor = Color(0xFF335AE7)
val BackgroundColor = Color(0xFF81A29D)

val tertiaryColor = Color(0xFF8F95D3)
val loginColor = Color(0xFFF194B4)
val ButtonColor = Color(0xFFB2273B)

// App Text & Background
val TextDark = Color(0xFF1A1A1A)
val TextGray = Color(0xFF5F5E5A)
val BackgroundGray = Color(0xFFF4F6F0)

// Green Theme Colors (Success & Community)
val SuccessGreen = Color(0xFF2E7D32)
val GreenPrimary = Color(0xFF2E7D32)
val GreenDark = Color(0xFF1B5E20)
val GreenLight = Color(0xFFE8F5E9)
val GreenBorder = Color(0xFFA5D6A7)
val GreenSurface = Color(0xFFEAF3DE)
val GreenSubBorder = Color(0xFFC0DD97)

// UI Element Colors

val BottomCardColor = Color(0xFF57A4A9)
val DividerColor = Color(0xFFE0E6D8)
val CardBg = Color(0xFFF4F6F0)

// Status Colors
val OrangePrimary = Color(0xFFD85A30)
val OrangeLight = Color(0xFFFAECE7)
val OrangeBorder = Color(0xFFF5C4B3)
val OrangeDark = Color(0xFF993C1D)

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

