package com.example.cinelog.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.chat.ChatConversation
import com.example.cinelog.chat.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationsUiState(
    val conversations: List<ChatConversation> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    firebaseAuth: FirebaseAuth?,
) : ViewModel() {

    val currentUserId: String? = firebaseAuth?.currentUser?.uid

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        if (currentUserId == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            chatRepository.getConversations()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
                .collect { list ->
                    _uiState.update { it.copy(conversations = list, isLoading = false) }
                }
        }
    }
}
