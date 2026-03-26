package com.example.cinelog.tmdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbMovieDetailsResponse(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("genres") val genres: List<TmdbGenreResponse> = emptyList(),
)

@Serializable
data class TmdbGenreResponse(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
)

