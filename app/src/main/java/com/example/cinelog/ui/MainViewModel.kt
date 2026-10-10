package com.example.cinelog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.profile.ProfileRepository
import com.example.cinelog.profile.UserProfile
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val profileRepository: ProfileRepository,
    val firebaseAuth: FirebaseAuth?,
) : ViewModel() {

    private val _isCheckingProfile = MutableStateFlow(true)
    val isCheckingProfile: StateFlow<Boolean> = _isCheckingProfile.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.currentUserProfile.collect { profile ->
                _userProfile.value = profile
                _isCheckingProfile.value = false
            }
        }
    }

    fun onProfileSetupComplete(profile: UserProfile) {
        _userProfile.value = profile
    }
}
