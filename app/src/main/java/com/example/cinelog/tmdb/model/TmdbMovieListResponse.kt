package com.example.cinelog.tmdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbMovieListResponse(
    @SerialName("page") val page: Int = 1,
    @SerialName("results") val results: List<TmdbMovieSummaryResponse> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 1,
    @SerialName("total_results") val totalResults: Int = 0,
)

@Serializable
data class TmdbMovieSummaryResponse(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
) {
    val resolvedTitle: String get() = title ?: name ?: "Untitled"
}

