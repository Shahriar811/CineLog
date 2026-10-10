package com.example.cinelog.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.profile.ProfileRepository
import com.example.cinelog.profile.UserProfile
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    val profileRepository: ProfileRepository,
    val firebaseAuth: FirebaseAuth?,
) : ViewModel() {

    val currentUserProfile: StateFlow<UserProfile?> =
        profileRepository.currentUserProfile
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

    val themeMode: StateFlow<ThemeMode> =
        settingsRepository.themeMode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM,
        )

    val preferHighRefreshRate: StateFlow<Boolean> =
        settingsRepository.preferHighRefreshRate.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setPreferHighRefreshRate(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPreferHighRefreshRate(enabled)
        }
    }
}

