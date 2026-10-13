package com.example.cinelog.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.chat.ChatMessage
import com.example.cinelog.chat.ChatRepository
import com.example.cinelog.profile.ProfileRepository
import com.example.cinelog.profile.UserProfile
import com.example.cinelog.ui.navigation.Route
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val targetUser: UserProfile? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val currentUserId: String = "",
    val error: String? = null,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val profileRepository: ProfileRepository,
    firebaseAuth: FirebaseAuth?,
) : ViewModel() {

    val targetUserId: String = savedStateHandle.get<String>(Route.Chat.argUserId).orEmpty()
    val currentUserId: String = firebaseAuth?.currentUser?.uid.orEmpty()

    private val _uiState = MutableStateFlow(
        ChatUiState(currentUserId = currentUserId)
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        if (targetUserId.isNotBlank()) {
            loadTargetUserProfile()
            observeMessages()
        }
    }

    private fun loadTargetUserProfile() {
        viewModelScope.launch {
            profileRepository.getProfile(targetUserId).collect { profile ->
                _uiState.update { it.copy(targetUser = profile) }
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.getMessages(targetUserId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.localizedMessage) }
                }
                .collect { messageList ->
                    _uiState.update { it.copy(messages = messageList) }
                }
        }
    }

    fun onInputChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isSending || targetUserId.isBlank()) return

        val target = _uiState.value.targetUser
        val displayName = target?.displayName?.ifBlank { null } ?: target?.username?.ifBlank { null } ?: "Cinephile"
        val username = target?.username.orEmpty()
        val photo = target?.photoBase64

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            try {
                chatRepository.sendMessage(
                    otherUserId = targetUserId,
                    otherDisplayName = displayName,
                    otherUsername = username,
                    otherPhotoBase64 = photo,
                    text = text,
                )
                _uiState.update { it.copy(inputText = "", isSending = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false, error = e.localizedMessage ?: "Failed to send message") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
