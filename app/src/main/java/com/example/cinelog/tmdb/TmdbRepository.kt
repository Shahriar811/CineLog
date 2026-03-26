package com.example.cinelog.tmdb

import com.example.cinelog.core.model.MovieDetails
import com.example.cinelog.core.model.MovieSummary

interface TmdbRepository {
    suspend fun trendingMovies(page: Int = 1): List<MovieSummary>
    suspend fun searchMovies(query: String, page: Int = 1): List<MovieSummary>
    suspend fun movieDetails(movieId: Long): MovieDetails
}

