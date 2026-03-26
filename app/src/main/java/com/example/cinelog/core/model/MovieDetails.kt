package com.example.cinelog.core.model

data class MovieDetails(
    val id: Long,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double?,
    val runtimeMinutes: Int?,
    val genres: List<String>,
)

