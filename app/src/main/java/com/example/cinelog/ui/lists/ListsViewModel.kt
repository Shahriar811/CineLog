package com.example.cinelog.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.lists.MovieListRepository
import com.example.cinelog.lists.UserMovieList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListsUiState(
    val lists: List<UserMovieList> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val movieListRepository: MovieListRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ListsUiState())
    val state: StateFlow<ListsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                movieListRepository.userLists().collect { lists ->
                    _state.value = _state.value.copy(lists = lists, error = null)
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun createList(name: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                val id = movieListRepository.createList(name)
                onCreated(id)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun renameList(listId: String, newName: String) {
        viewModelScope.launch {
            runCatching {
                movieListRepository.renameList(listId, newName)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteList(listId: String) {
        viewModelScope.launch {
            runCatching {
                movieListRepository.deleteList(listId)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
