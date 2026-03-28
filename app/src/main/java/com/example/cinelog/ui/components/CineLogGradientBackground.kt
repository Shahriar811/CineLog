package com.example.cinelog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.example.cinelog.ui.theme.GlassGradientBottom
import com.example.cinelog.ui.theme.GlassGradientTop

@Composable
fun CineLogGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GlassGradientTop, GlassGradientBottom),
                ),
            ),
    ) {
        content()
    }
}
