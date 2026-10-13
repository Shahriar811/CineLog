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

private val MidnightVelvetScheme = darkColorScheme(
    primary = CinemaBlue,
    onPrimary = Color.White,
    primaryContainer = ObsidianSurfaceHighlight,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = CinemaCyan,
    onSecondary = Color(0xFF003548),
    secondaryContainer = ObsidianSurfaceElevated,
    onSecondaryContainer = Color(0xFFC7EFFF),
    tertiary = CinemaCrimson,
    onTertiary = Color.White,
    background = ObsidianBackground,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainer = ObsidianSurface,
    surfaceContainerHigh = ObsidianSurfaceElevated,
    surfaceContainerHighest = ObsidianSurfaceHighlight,
    surfaceContainerLow = ObsidianBackground,
    error = Danger,
    onError = Color.White,
    outline = ObsidianBorder,
    outlineVariant = Color(0x33FFFFFF),
)

private val EditorialStudioScheme = lightColorScheme(
    primary = Color(0xFF1D4ED8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = StudioSurfaceElevated,
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = Color(0xFFE11D48),
    onTertiary = Color.White,
    background = StudioBackground,
    onBackground = TextPrimaryLight,
    surface = StudioSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainer = StudioSurface,
    surfaceContainerHigh = StudioSurfaceElevated,
    surfaceContainerHighest = StudioSurfaceHighlight,
    error = Danger,
    onError = Color.White,
    outline = StudioBorder,
    outlineVariant = Color(0xFFCBD5E1),
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
        darkTheme -> MidnightVelvetScheme
        else -> EditorialStudioScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
