package com.example.cinelog.tmdb.api

import com.example.cinelog.tmdb.model.TmdbMovieDetailsResponse
import com.example.cinelog.tmdb.model.TmdbMovieListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("trending/movie/day")
    suspend fun trendingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
    ): TmdbMovieListResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
    ): TmdbMovieListResponse

    @GET("movie/{movie_id}")
    suspend fun movieDetails(
        @Path("movie_id") movieId: Long,
        @Query("api_key") apiKey: String,
    ): TmdbMovieDetailsResponse
}

