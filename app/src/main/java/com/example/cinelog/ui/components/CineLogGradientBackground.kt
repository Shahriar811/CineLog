package com.example.cinelog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun CineLogGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val bg = MaterialTheme.colorScheme.background
    val isDark = bg.luminance() < 0.35f

    val backgroundBrush = if (isDark) {
        Brush.radialGradient(
            colors = listOf(
                Color(0x282563EB), // Ambient matte navy / royal blue glow
                Color(0x181E3A8A), // Deep navy accent
                bg,
            ),
            center = Offset(800f, 100f),
            radius = 1400f,
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF1F5F9),
                Color(0xFFF8FAFD),
                Color(0xFFFFFFFF),
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .background(backgroundBrush),
    ) {
        content()
    }
}
