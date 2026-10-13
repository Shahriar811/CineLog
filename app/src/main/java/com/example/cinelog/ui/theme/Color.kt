package com.example.cinelog.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// —— Primary Brand & Rating Tokens (Matte Blue / Navy Blue) ——
val CinemaBlue = Color(0xFF3B82F6)          // Primary Matte Royal/Navy Blue
val CinemaNavy = Color(0xFF1D4ED8)          // Deep Navy Blue
val CinemaDarkNavy = Color(0xFF1E3A8A)      // Dark Midnight Navy
val CinemaBlueGlow = Color(0xFF60A5FA)      // Matte Ice/Sky Blue Glow
val CinemaBlueLight = Color(0xFF93C5FD)     // Soft Blue Tint

// Backward compatibility alias so all existing references automatically adopt the Matte Navy Blue palette:
val CinemaAmber = CinemaBlue
val CinemaAmberGlow = CinemaBlueGlow
val CinemaGold = CinemaNavy
val CinemaGoldLight = CinemaBlueLight

val CinemaCrimson = Color(0xFFF43F5E)
val CinemaIndigo = Color(0xFF6366F1)
val CinemaCyan = Color(0xFF38BDF8)
val StarRating = Color(0xFFFBBF24)          // Film rating stars (cinematic warm star)
val Danger = Color(0xFFEF4444)
val Success = Color(0xFF10B981)
val CinemaOnAccent = Color.White

// —— Dark Theme ("Midnight Velvet" / Cinema Noir) ——
val ObsidianBackground = Color(0xFF080B12)
val ObsidianSurface = Color(0xFF0F1524)
val ObsidianSurfaceElevated = Color(0xFF161F34)
val ObsidianSurfaceHighlight = Color(0xFF1E2A44)
val ObsidianGlass = Color(0xCC0F1524)
val ObsidianBorder = Color(0x24FFFFFF)
val ObsidianBorderFocused = Color(0x803B82F6)

val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextTertiaryDark = Color(0xFF64748B)

// —— Light Theme ("Editorial Studio") ——
val StudioBackground = Color(0xFFF7F9FC)
val StudioSurface = Color(0xFFFFFFFF)
val StudioSurfaceElevated = Color(0xFFF0F4F8)
val StudioSurfaceHighlight = Color(0xFFE2E8F0)
val StudioBorder = Color(0xFFE2E8F0)
val StudioBorderFocused = Color(0x992563EB)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val TextTertiaryLight = Color(0xFF94A3B8)

// —— Gradients ——
val GoldAmberGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF60A5FA)),
)
val NavyBlueGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6)),
)

val CinemaGlowGradient = Brush.verticalGradient(
    colors = listOf(Color(0x333B82F6), Color.Transparent),
)

val CrimsonIndigoGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFF43F5E), Color(0xFF8B5CF6)),
)

val CyanIndigoGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF38BDF8), Color(0xFF6366F1)),
)

val DarkHeroOverlay = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0x80080B12),
        Color(0xEE080B12),
        ObsidianBackground,
    ),
)
