package com.example.cinelog.ui.myreviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.reviews.MovieReview
import com.example.cinelog.reviews.ReviewComment
import com.example.cinelog.reviews.ReviewRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ReviewsFeedTab {
    COMMUNITY,
    MY_REVIEWS,
}

data class ReviewsUiState(
    val selectedTab: ReviewsFeedTab = ReviewsFeedTab.COMMUNITY,
    val communityReviews: List<MovieReview> = emptyList(),
    val myReviews: List<MovieReview> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class MyReviewsViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val firebaseAuth: FirebaseAuth?,
) : ViewModel() {

    val currentUserId: String? = firebaseAuth?.currentUser?.uid

    private val _state = MutableStateFlow(ReviewsUiState())
    val state: StateFlow<ReviewsUiState> = _state.asStateFlow()

    init {
        loadFeeds()
    }

    private fun loadFeeds() {
        viewModelScope.launch {
            reviewRepository.communityFeed().collect { reviews ->
                _state.value = _state.value.copy(
                    communityReviews = reviews,
                    isLoading = false,
                )
            }
        }

        viewModelScope.launch {
            runCatching {
                reviewRepository.myReviews().collect { reviews ->
                    _state.value = _state.value.copy(
                        myReviews = reviews,
                        isLoading = false,
                    )
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun selectTab(tab: ReviewsFeedTab) {
        _state.value = _state.value.copy(selectedTab = tab)
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

    fun updateReview(reviewId: String, newRating: Int, newText: String) {
        viewModelScope.launch {
            runCatching {
                reviewRepository.updateReview(reviewId, newRating, newText)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.localizedMessage)
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            runCatching {
                reviewRepository.deleteReview(reviewId)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.localizedMessage)
            }
        }
    }
}
