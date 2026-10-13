package com.example.cinelog.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.profile.UserProfile
import com.example.cinelog.reviews.MovieReview
import com.example.cinelog.reviews.ReviewComment
import com.example.cinelog.reviews.ReviewRepository
import com.example.cinelog.social.SocialRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeFeedUiState(
    val feedReviews: List<MovieReview> = emptyList(),
    val communityReviews: List<MovieReview> = emptyList(),
    val suggestedUsers: List<UserProfile> = emptyList(),
    val searchResults: List<UserProfile> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedTab: FeedTab = FeedTab.FOR_YOU,
)

enum class FeedTab { FOR_YOU, FOLLOWING, DISCOVER }

@HiltViewModel
class HomeFeedViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val reviewRepository: ReviewRepository,
    private val firebaseAuth: FirebaseAuth?,
) : ViewModel() {

    val currentUserId: String? = firebaseAuth?.currentUser?.uid

    private val _uiState = MutableStateFlow(HomeFeedUiState())
    val uiState: StateFlow<HomeFeedUiState> = _uiState.asStateFlow()

    init {
        loadCommunityFeed()
        loadFollowingFeed()
        loadSuggestedUsers()
    }

    private fun loadCommunityFeed() {
        viewModelScope.launch {
            reviewRepository.communityFeed().collect { reviews ->
                _uiState.value = _uiState.value.copy(
                    communityReviews = reviews,
                    isLoading = false,
                )
            }
        }
    }

    private fun loadFollowingFeed() {
        viewModelScope.launch {
            socialRepository.getFeedReviews().collect { reviews ->
                _uiState.value = _uiState.value.copy(feedReviews = reviews)
            }
        }
    }

    private fun loadSuggestedUsers() {
        viewModelScope.launch {
            socialRepository.discoverUsers(20).collect { users ->
                _uiState.value = _uiState.value.copy(suggestedUsers = users)
            }
        }
    }

    fun onTabSelected(tab: FeedTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            socialRepository.searchUsers(query).collect { users ->
                _uiState.value = _uiState.value.copy(searchResults = users)
            }
        }
    }

    fun setSearchActive(active: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchActive = active)
        if (!active) {
            _uiState.value = _uiState.value.copy(searchQuery = "", searchResults = emptyList())
        }
    }

    fun toggleFollow(targetUserId: String) {
        viewModelScope.launch {
            socialRepository.toggleFollow(targetUserId)
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

    fun isFollowing(userId: String): Flow<Boolean> {
        return socialRepository.isFollowing(userId)
    }
}
