package com.example.cinelog.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val preferHighRefreshRateKey = booleanPreferencesKey("prefer_high_refresh_rate")

    override val themeMode: Flow<ThemeMode> =
        dataStore.data.map { prefs ->
            val raw = prefs[themeModeKey]
            raw?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.LIGHT
        }

    override val preferHighRefreshRate: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[preferHighRefreshRateKey] ?: false }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    override suspend fun setPreferHighRefreshRate(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[preferHighRefreshRateKey] = enabled
        }
    }
}

