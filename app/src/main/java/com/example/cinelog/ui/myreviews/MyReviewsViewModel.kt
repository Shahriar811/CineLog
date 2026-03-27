package com.example.cinelog.ui.myreviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.reviews.MovieReview
import com.example.cinelog.reviews.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyReviewsUiState(
    val isLoading: Boolean = false,
    val items: List<MovieReview> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class MyReviewsViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MyReviewsUiState(isLoading = true))
    val state: StateFlow<MyReviewsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                reviewRepository.myReviews().collect { reviews ->
                    _state.value = MyReviewsUiState(isLoading = false, items = reviews)
                }
            }.onFailure { e ->
                _state.value = MyReviewsUiState(isLoading = false, error = e.message)
            }
        }
    }
}

