package com.example.cinelog.lists

data class UserMovieList(
    val id: String,
    val name: String,
    val createdAtMillis: Long,
    val movieIds: List<Long>,
)
