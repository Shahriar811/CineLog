package com.example.cinelog.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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

    val currentUser: Flow<FirebaseUser?> = authRepository.currentUser

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

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    fun signInWithEmail(onSuccess: () -> Unit) {
        submit(onSuccess) { authRepository.signInWithEmail(state.value.email.trim(), state.value.password) }
    }

    fun signUpWithEmail(onSuccess: () -> Unit) {
        submit(onSuccess) { authRepository.signUpWithEmail(state.value.email.trim(), state.value.password) }
    }

    fun signUpWithGmail(gmail: String, password: String, onSuccess: () -> Unit) {
        val cleanEmail = gmail.trim().let {
            if (!it.contains("@")) "$it@gmail.com" else it
        }
        _state.value = _state.value.copy(email = cleanEmail, password = password)
        submit(onSuccess) {
            try {
                // First attempt account creation
                authRepository.signUpWithEmail(cleanEmail, password)
            } catch (e: Exception) {
                val msg = e.message.orEmpty()
                if (msg.contains("already in use", ignoreCase = true) ||
                    msg.contains("EMAIL_EXISTS", ignoreCase = true)
                ) {
                    // Account already exists with this Gmail address; sign in seamlessly
                    authRepository.signInWithEmail(cleanEmail, password)
                } else {
                    throw e
                }
            }
        }
    }

    fun sendPasswordReset(
        customEmail: String? = null,
        onComplete: ((Boolean, String) -> Unit)? = null,
    ) {
        val targetEmail = (customEmail ?: state.value.email).trim()
        if (targetEmail.isBlank()) {
            val err = "Please enter your email address to reset password."
            _state.value = _state.value.copy(
                isLoading = false,
                message = err,
                isSuccessMessage = false,
            )
            onComplete?.invoke(false, err)
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, message = null)
            runCatching {
                authRepository.sendPasswordReset(targetEmail)
            }.onSuccess {
                val msg = "Password reset link sent to $targetEmail. Please check your inbox."
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = msg,
                    isSuccessMessage = true,
                )
                onComplete?.invoke(true, msg)
            }.onFailure { e ->
                val errorMsg = when {
                    e.message?.contains("no user record", ignoreCase = true) == true ->
                        "No account found with this email address."
                    e.message?.contains("badly formatted", ignoreCase = true) == true ->
                        "Please enter a valid email address."
                    else -> e.message ?: "Failed to send reset email. Please try again."
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = errorMsg,
                    isSuccessMessage = false,
                )
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    fun sendPasswordResetForCurrentUser(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = authRepository.currentUser.firstOrNull()
            val email = user?.email?.trim()
            if (email.isNullOrBlank()) {
                onComplete(false, "No email found for current user session.")
                return@launch
            }
            sendPasswordReset(email, onComplete)
        }
    }

    fun signInWithGoogle(activity: Activity, onSuccess: () -> Unit) {
        submit(onSuccess) { authRepository.signInWithGoogle(activity) }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    private fun submit(onSuccess: () -> Unit, block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, message = null, isSuccessMessage = false)
            runCatching { block() }
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false, message = null)
                    onSuccess()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = e.message ?: "Something went wrong",
                        isSuccessMessage = false,
                    )
                }
        }
    }
}
