package com.example.cinelog.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.core.model.MovieDetails
import com.example.cinelog.reviews.MovieReview
import com.example.cinelog.reviews.ReviewRepository
import com.example.cinelog.tmdb.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieDetailsUiState(
    val isLoading: Boolean = false,
    val details: MovieDetails? = null,
    val ratingOutOf10: Int = 8,
    val reviewText: String = "",
    val saveOnline: Boolean = true,
    val movieReviews: List<MovieReview> = emptyList(),
    val saveMessage: String? = null,
    val error: String? = null,
)

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository,
    private val reviewRepository: ReviewRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MovieDetailsUiState(isLoading = true))
    val state: StateFlow<MovieDetailsUiState> = _state.asStateFlow()

    fun load(movieId: Long) {
        viewModelScope.launch {
            _state.value = MovieDetailsUiState(isLoading = true)
            runCatching { tmdbRepository.movieDetails(movieId) }
                .onSuccess { details ->
                    _state.value = _state.value.copy(isLoading = false, details = details)
                    observeReviews(movieId)
                }
                .onFailure { e -> _state.value = MovieDetailsUiState(isLoading = false, error = e.message) }
        }
    }

    fun setRating(value: Int) {
        _state.value = _state.value.copy(ratingOutOf10 = value.coerceIn(1, 10), saveMessage = null)
    }

    fun setReviewText(value: String) {
        _state.value = _state.value.copy(reviewText = value, saveMessage = null)
    }

    fun setSaveOnline(value: Boolean) {
        _state.value = _state.value.copy(saveOnline = value)
    }

    fun saveReview() {
        val details = state.value.details ?: return
        val ui = state.value
        viewModelScope.launch {
            runCatching {
                if (!ui.saveOnline) {
                    error("Enable 'Save online' to upload your review.")
                }
                reviewRepository.upsertReview(
                    movieId = details.id,
                    movieTitle = details.title,
                    ratingOutOf10 = ui.ratingOutOf10,
                    reviewText = ui.reviewText,
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    reviewText = "",
                    saveMessage = "Review saved online.",
                    error = null,
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(saveMessage = null, error = e.message)
            }
        }
    }

    private fun observeReviews(movieId: Long) {
        viewModelScope.launch {
            runCatching {
                reviewRepository.reviewsForMovie(movieId).collect { reviews ->
                    _state.value = _state.value.copy(movieReviews = reviews)
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}

