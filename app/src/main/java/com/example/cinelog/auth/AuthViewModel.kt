package com.example.cinelog.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    val isSignedIn: StateFlow<Boolean> =
        authRepository.currentUser
            .map { it != null }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false,
            )

    fun setEmail(email: String) {
        _state.value = _state.value.copy(email = email, message = null)
    }

    fun setPassword(password: String) {
        _state.value = _state.value.copy(password = password, message = null)
    }

    fun signInWithEmail(onSuccess: () -> Unit) {
        submit(onSuccess) { authRepository.signInWithEmail(state.value.email.trim(), state.value.password) }
    }

    fun signUpWithEmail(onSuccess: () -> Unit) {
        submit(onSuccess) { authRepository.signUpWithEmail(state.value.email.trim(), state.value.password) }
    }

    fun sendPasswordReset() {
        submit(onSuccess = {}) { authRepository.sendPasswordReset(state.value.email.trim()) }
    }

    fun signInWithGoogle(activity: Activity, onSuccess: () -> Unit) {
        submit(onSuccess) { authRepository.signInWithGoogle(activity) }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    private fun submit(onSuccess: () -> Unit, block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, message = null)
            runCatching { block() }
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false, message = null)
                    onSuccess()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, message = e.message ?: "Something went wrong")
                }
        }
    }
}

