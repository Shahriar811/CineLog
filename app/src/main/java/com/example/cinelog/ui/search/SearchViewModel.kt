package com.example.cinelog.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.core.model.MovieSummary
import com.example.cinelog.tmdb.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val items: List<MovieSummary> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    fun setQuery(query: String) {
        _state.value = _state.value.copy(query = query, error = null)
    }

    fun search() {
        val q = state.value.query.trim()
        if (q.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
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

