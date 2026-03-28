package com.example.cinelog.lists

import kotlinx.coroutines.flow.Flow

interface MovieListRepository {
    fun userLists(): Flow<List<UserMovieList>>
    fun list(listId: String): Flow<UserMovieList?>
    suspend fun createList(name: String): String
    suspend fun renameList(listId: String, newName: String)
    suspend fun deleteList(listId: String)
    suspend fun addMovieToList(listId: String, movieId: Long)
    suspend fun removeMovieFromList(listId: String, movieId: Long)
}
