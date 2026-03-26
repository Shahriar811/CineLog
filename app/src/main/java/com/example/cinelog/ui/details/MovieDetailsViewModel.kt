package com.example.cinelog.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.core.model.MovieDetails
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
    val error: String? = null,
)

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MovieDetailsUiState(isLoading = true))
    val state: StateFlow<MovieDetailsUiState> = _state.asStateFlow()

    fun load(movieId: Long) {
        viewModelScope.launch {
            _state.value = MovieDetailsUiState(isLoading = true)
            runCatching { tmdbRepository.movieDetails(movieId) }
                .onSuccess { details -> _state.value = MovieDetailsUiState(isLoading = false, details = details) }
                .onFailure { e -> _state.value = MovieDetailsUiState(isLoading = false, error = e.message) }
        }
    }
}

