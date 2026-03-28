package com.example.cinelog

import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import com.example.cinelog.settings.SettingsViewModel
import com.example.cinelog.ui.AppRoot
import com.example.cinelog.ui.theme.CineLogTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val preferHighRefreshRate by settingsViewModel.preferHighRefreshRate.collectAsState()

            LaunchedEffect(preferHighRefreshRate) {
                applyPreferredRefreshRate(preferHighRefreshRate)
            }

            CineLogTheme(themeMode = themeMode, dynamicColor = false) {
                AppRoot()
            }
        }
    }

    private fun applyPreferredRefreshRate(enabled: Boolean) {
        @Suppress("DEPRECATION")
        val display: Display = windowManager.defaultDisplay
        val modes = display.supportedModes
        val bestMode = modes.maxByOrNull { it.refreshRate }

        val attrs = window.attributes
        attrs.preferredDisplayModeId = if (enabled && bestMode != null) bestMode.modeId else 0
        window.attributes = attrs
    }
}