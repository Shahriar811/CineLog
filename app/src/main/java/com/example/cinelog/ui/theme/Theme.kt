package com.example.cinelog.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.cinelog.settings.ThemeMode

private val MidnightNeonScheme = darkColorScheme(
    primary = NeonAccent,
    onPrimary = Color.White,
    primaryContainer = MidnightSurfaceMuted,
    onPrimaryContainer = TextPrimaryDark,
    secondary = StarRating,
    onSecondary = MidnightBackground,
    secondaryContainer = MidnightSurface,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = StarRating,
    onTertiary = MidnightBackground,
    background = MidnightBackground,
    onBackground = TextPrimaryDark,
    surface = MidnightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = MidnightSurfaceMuted,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainer = MidnightSurface,
    surfaceContainerHigh = MidnightSurfaceMuted,
    surfaceContainerLow = MidnightBackground,
    error = Danger,
    onError = Color.White,
    outline = MidnightSurfaceMuted,
    outlineVariant = ToggleTrackOff,
)

private val GlassLightScheme = lightColorScheme(
    primary = GlassAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4EBFC),
    onPrimaryContainer = GlassTextPrimary,
    secondary = GlassAccentTeal,
    onSecondary = Color.White,
    secondaryContainer = GlassSurfaceMuted,
    onSecondaryContainer = GlassTextPrimary,
    tertiary = StarRating,
    onTertiary = GlassTextPrimary,
    background = Color(0xFFF4F8FD),
    onBackground = GlassTextPrimary,
    surface = GlassSurface,
    onSurface = GlassTextPrimary,
    surfaceVariant = GlassSurfaceMuted,
    onSurfaceVariant = GlassTextSecondary,
    surfaceContainer = Color(0xFFF8FAFD),
    surfaceContainerHigh = Color(0xFFE8EEF5),
    error = Danger,
    onError = Color.White,
    outline = Color(0xFFC5D4E3),
    outlineVariant = Color(0xFFD8E2EC),
)

@Composable
fun CineLogTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MidnightNeonScheme
        else -> GlassLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
