package com.example.cinelog.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.core.model.MovieDetails
import com.example.cinelog.lists.MovieListRepository
import com.example.cinelog.lists.UserMovieList
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
    val ratingOutOf10: Int? = null,
    val reviewText: String = "",
    val saveOnline: Boolean = true,
    val movieReviews: List<MovieReview> = emptyList(),
    val saveMessage: String? = null,
    val error: String? = null,
    val userLists: List<UserMovieList> = emptyList(),
    val listMessage: String? = null,
    val listMenuExpanded: Boolean = false,
)

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository,
    private val reviewRepository: ReviewRepository,
    private val movieListRepository: MovieListRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MovieDetailsUiState(isLoading = true))
    val state: StateFlow<MovieDetailsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                movieListRepository.userLists().collect { lists ->
                    _state.value = _state.value.copy(userLists = lists)
                }
            }
        }
    }

    fun load(movieId: Long) {
        viewModelScope.launch {
            val keepLists = _state.value.userLists
            _state.value = MovieDetailsUiState(isLoading = true, userLists = keepLists)
            runCatching { tmdbRepository.movieDetails(movieId) }
                .onSuccess { details ->
                    _state.value = _state.value.copy(isLoading = false, details = details, error = null)
                    observeReviews(movieId)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
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

    fun setListMenuExpanded(expanded: Boolean) {
        _state.value = _state.value.copy(listMenuExpanded = expanded)
    }

    fun addMovieToList(listId: String) {
        val movieId = state.value.details?.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(error = null)
            runCatching {
                movieListRepository.addMovieToList(listId, movieId)
            }.onSuccess {
                _state.value = _state.value.copy(
                    listMessage = "Saved to list.",
                    listMenuExpanded = false,
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message, listMenuExpanded = false)
            }
        }
    }

    fun createListAndAdd(name: String) {
        val movieId = state.value.details?.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(error = null)
            runCatching {
                val id = movieListRepository.createList(name)
                movieListRepository.addMovieToList(id, movieId)
            }.onSuccess {
                _state.value = _state.value.copy(
                    listMessage = "Created list and added movie.",
                    listMenuExpanded = false,
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message, listMenuExpanded = false)
            }
        }
    }

    fun clearListMessage() {
        _state.value = _state.value.copy(listMessage = null)
    }

    fun saveReview() {
        val details = state.value.details ?: return
        val ui = state.value
        viewModelScope.launch {
            runCatching {
                if (!ui.saveOnline) {
                    error("Enable 'Save online' to upload your review.")
                }
                val rating = ui.ratingOutOf10 ?: error("Please select a rating.")
                reviewRepository.upsertReview(
                    movieId = details.id,
                    movieTitle = details.title,
                    ratingOutOf10 = rating,
                    reviewText = ui.reviewText,
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    reviewText = "",
                    ratingOutOf10 = null,
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
