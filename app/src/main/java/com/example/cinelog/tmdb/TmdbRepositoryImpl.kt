package com.example.cinelog.tmdb

import com.example.cinelog.BuildConfig
import com.example.cinelog.core.model.MovieDetails
import com.example.cinelog.core.model.MovieCategory
import com.example.cinelog.core.model.MovieSummary
import com.example.cinelog.tmdb.api.TmdbApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
) : TmdbRepository {
    private fun mapToSummaryList(results: List<com.example.cinelog.tmdb.model.TmdbMovieSummaryResponse>): List<MovieSummary> {
        return results.map {
            MovieSummary(
                id = it.id,
                title = it.resolvedTitle,
                posterPath = it.posterPath,
                releaseDate = it.releaseDate,
                voteAverage = it.voteAverage,
            )
        }
    }

    private fun requireApiKey(): String {
        val key = BuildConfig.TMDB_API_KEY
        require(key.isNotBlank()) {
            "TMDB_API_KEY is missing. Add it to local.properties as TMDB_API_KEY=..."
        }
        return key
    }

    override suspend fun trendingMovies(page: Int): List<MovieSummary> {
        val response = api.trendingMovies(apiKey = requireApiKey(), page = page)
        return mapToSummaryList(response.results)
    }

    override suspend fun searchMovies(query: String, page: Int): List<MovieSummary> {
        val response = api.searchMovies(apiKey = requireApiKey(), query = query, page = page)
        return mapToSummaryList(response.results)
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

    override suspend fun moviesByCategory(category: MovieCategory, page: Int): List<MovieSummary> {
        if (category == MovieCategory.TRENDING) {
            return trendingMovies(page)
        }

        val response = when (category) {
            MovieCategory.TRENDING -> api.trendingMovies(apiKey = requireApiKey(), page = page)
            MovieCategory.HOLLYWOOD -> api.discoverMovies(
                apiKey = requireApiKey(),
                region = "US",
                originalLanguage = "en",
                page = page,
            )
            MovieCategory.BOLLYWOOD -> api.discoverMovies(
                apiKey = requireApiKey(),
                region = "IN",
                originalLanguage = "hi",
                page = page,
            )
            MovieCategory.HORROR -> api.discoverMovies(
                apiKey = requireApiKey(),
                genres = "27",
                page = page,
            )
            MovieCategory.ACTION -> api.discoverMovies(
                apiKey = requireApiKey(),
                genres = "28",
                page = page,
            )
            MovieCategory.ROMANCE -> api.discoverMovies(
                apiKey = requireApiKey(),
                genres = "10749",
                page = page,
            )
        }

        return mapToSummaryList(response.results)
    }
}

