package com.example.cinelog.ui.lists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.core.model.MovieSummary
import com.example.cinelog.lists.MovieListRepository
import com.example.cinelog.tmdb.TmdbRepository
import com.example.cinelog.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListDetailUiState(
    val listName: String = "",
    val movieIds: List<Long> = emptyList(),
    val movies: List<MovieSummary> = emptyList(),
    val isLoadingMovies: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val movieListRepository: MovieListRepository,
    private val tmdbRepository: TmdbRepository,
) : ViewModel() {

    private val listId: String = savedStateHandle.get<String>(Route.ListDetail.argListId)
        ?: error("Missing list id")

    private val _state = MutableStateFlow(ListDetailUiState())
    val state: StateFlow<ListDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                movieListRepository.list(listId).collect { list ->
                    if (list == null) {
                        _state.value = _state.value.copy(
                            listName = "",
                            movieIds = emptyList(),
                            movies = emptyList(),
                            error = "List not found.",
                        )
                        return@collect
                    }
                    val ids = list.movieIds
                    _state.value = _state.value.copy(
                        listName = list.name,
                        movieIds = ids,
                        error = null,
                    )
                    loadSummaries(ids)
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    private fun loadSummaries(ids: List<Long>) {
        if (ids.isEmpty()) {
            _state.value = _state.value.copy(movies = emptyList(), isLoadingMovies = false)
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingMovies = true)
            val summaries = ids.mapNotNull { id ->
                runCatching { tmdbRepository.movieDetails(id) }
                    .getOrNull()
                    ?.let { d ->
                        MovieSummary(
                            id = d.id,
                            title = d.title,
                            posterPath = d.posterPath,
                            releaseDate = d.releaseDate,
                            voteAverage = d.voteAverage,
                        )
                    }
            }
            _state.value = _state.value.copy(movies = summaries, isLoadingMovies = false)
        }
    }

    fun removeMovie(movieId: Long) {
        viewModelScope.launch {
            runCatching {
                movieListRepository.removeMovieFromList(listId, movieId)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
