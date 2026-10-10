package com.example.cinelog.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.profile.ProfileRepository
import com.example.cinelog.profile.UserProfile
import com.example.cinelog.profile.UserStats
import com.example.cinelog.reviews.MovieReview
import com.example.cinelog.reviews.ReviewComment
import com.example.cinelog.reviews.ReviewRepository
import com.example.cinelog.ui.navigation.Route
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileUiState(
    val profile: UserProfile? = null,
    val stats: UserStats = UserStats(),
    val reviews: List<MovieReview> = emptyList(),
    val isLoading: Boolean = true,
    val isCurrentUser: Boolean = false,
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository,
    private val firebaseAuth: FirebaseAuth?,
) : ViewModel() {

    val currentUserId: String? = firebaseAuth?.currentUser?.uid
    val targetUserId: String = savedStateHandle.get<String>(Route.UserProfile.argUserId)
        ?.takeIf { it.isNotBlank() } ?: currentUserId.orEmpty()

    private val _uiState = MutableStateFlow(
        UserProfileUiState(
            isCurrentUser = targetUserId == currentUserId && currentUserId != null,
            isLoading = targetUserId.isNotBlank(),
        )
    )
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        if (targetUserId.isBlank()) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            return
        }

        viewModelScope.launch {
            profileRepository.getProfile(targetUserId).collect { profile ->
                _uiState.value = _uiState.value.copy(
                    profile = profile,
                    isLoading = false,
                )
            }
        }

        viewModelScope.launch {
            profileRepository.getUserStats(targetUserId).collect { stats ->
                _uiState.value = _uiState.value.copy(stats = stats)
            }
        }

        viewModelScope.launch {
            reviewRepository.userReviews(targetUserId).collect { reviews ->
                _uiState.value = _uiState.value.copy(reviews = reviews)
            }
        }
    }

    fun updateProfilePhoto(photoBase64: String) {
        viewModelScope.launch {
            profileRepository.updateProfilePhoto(photoBase64)
        }
    }

    fun updateBio(bio: String) {
        viewModelScope.launch {
            profileRepository.updateBio(bio)
        }
    }

    fun toggleLike(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.toggleLike(reviewId)
        }
    }

    fun toggleDislike(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.toggleDislike(reviewId)
        }
    }

    fun commentsForReview(reviewId: String): Flow<List<ReviewComment>> {
        return reviewRepository.commentsForReview(reviewId)
    }

    fun addComment(reviewId: String, text: String) {
        viewModelScope.launch {
            reviewRepository.addComment(reviewId, text)
        }
    }
}
