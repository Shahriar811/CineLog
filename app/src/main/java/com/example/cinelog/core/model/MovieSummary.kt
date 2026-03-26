package com.example.cinelog.core.model

data class MovieSummary(
    val id: Long,
    val title: String,
    val posterPath: String?,
    val releaseDate: String?,
    val voteAverage: Double?,
)

