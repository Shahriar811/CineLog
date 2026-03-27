package com.example.cinelog.ui.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.core.model.MovieCategory
import com.example.cinelog.core.model.MovieSummary
import com.example.cinelog.tmdb.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrendingUiState(
    val isLoading: Boolean = false,
    val selectedCategory: MovieCategory = MovieCategory.TRENDING,
    val items: List<MovieSummary> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TrendingUiState(isLoading = true))
    val state: StateFlow<TrendingUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun onCategorySelected(category: MovieCategory) {
        refresh(category)
    }

    fun refresh(category: MovieCategory = state.value.selectedCategory) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, selectedCategory = category)
            runCatching { tmdbRepository.moviesByCategory(category = category, page = 1) }
                .onSuccess { movies ->
                    _state.value = _state.value.copy(isLoading = false, items = movies)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, items = emptyList(), error = e.message)
                }
        }
    }
}

