package com.example.cinelog.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.core.model.MovieSummary
import com.example.cinelog.tmdb.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val suggestions: List<MovieSummary> = emptyList(),
    val items: List<MovieSummary> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()
    private var suggestionJob: Job? = null

    fun setQuery(query: String) {
        _state.value = _state.value.copy(query = query, error = null)
        suggestionJob?.cancel()
        if (query.trim().length < 1) {
            _state.value = _state.value.copy(suggestions = emptyList())
            return
        }
        suggestionJob = viewModelScope.launch {
            delay(220)
            runCatching { tmdbRepository.searchMovies(query = query.trim(), page = 1) }
                .onSuccess { movies ->
                    val q = query.trim()
                    val sorted = movies.sortedByDescending { it.title.startsWith(q, ignoreCase = true) }
                    _state.value = _state.value.copy(suggestions = sorted.take(8), error = null)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(suggestions = emptyList(), error = e.message)
                }
        }
    }

    fun onSuggestionClicked(movie: MovieSummary) {
        _state.value = _state.value.copy(query = movie.title, suggestions = emptyList())
        search(movie.title)
    }

    fun search() {
        search(state.value.query.trim())
    }

    private fun search(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, suggestions = emptyList())
            runCatching { tmdbRepository.searchMovies(query = q, page = 1) }
                .onSuccess { movies ->
                    _state.value = _state.value.copy(isLoading = false, items = movies, error = null)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, items = emptyList(), error = e.message)
                }
        }
    }
}

