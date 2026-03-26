package com.example.cinelog.tmdb

import com.example.cinelog.BuildConfig
import com.example.cinelog.core.model.MovieDetails
import com.example.cinelog.core.model.MovieSummary
import com.example.cinelog.tmdb.api.TmdbApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
) : TmdbRepository {

    private fun requireApiKey(): String {
        val key = BuildConfig.TMDB_API_KEY
        require(key.isNotBlank()) {
            "TMDB_API_KEY is missing. Add it to local.properties as TMDB_API_KEY=..."
        }
        return key
    }

    override suspend fun trendingMovies(page: Int): List<MovieSummary> {
        val response = api.trendingMovies(apiKey = requireApiKey(), page = page)
        return response.results.map {
            MovieSummary(
                id = it.id,
                title = it.resolvedTitle,
                posterPath = it.posterPath,
                releaseDate = it.releaseDate,
                voteAverage = it.voteAverage,
            )
        }
    }

    override suspend fun searchMovies(query: String, page: Int): List<MovieSummary> {
        val response = api.searchMovies(apiKey = requireApiKey(), query = query, page = page)
        return response.results.map {
            MovieSummary(
                id = it.id,
                title = it.resolvedTitle,
                posterPath = it.posterPath,
                releaseDate = it.releaseDate,
                voteAverage = it.voteAverage,
            )
        }
    }

    override suspend fun movieDetails(movieId: Long): MovieDetails {
        val response = api.movieDetails(movieId = movieId, apiKey = requireApiKey())
        return MovieDetails(
            id = response.id,
            title = response.title ?: "Untitled",
            overview = response.overview.orEmpty(),
            posterPath = response.posterPath,
            backdropPath = response.backdropPath,
            releaseDate = response.releaseDate,
            voteAverage = response.voteAverage,
            runtimeMinutes = response.runtime,
            genres = response.genres.map { it.name },
        )
    }
}

