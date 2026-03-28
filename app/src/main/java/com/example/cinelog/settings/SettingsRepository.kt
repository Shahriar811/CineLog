package com.example.cinelog.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val preferHighRefreshRate: Flow<Boolean>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setPreferHighRefreshRate(enabled: Boolean)
}

