package com.example.cinelog.reviews

import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun reviewsForMovie(movieId: Long): Flow<List<MovieReview>>
    fun myReviews(): Flow<List<MovieReview>>
    suspend fun upsertReview(movieId: Long, movieTitle: String, ratingOutOf10: Int, reviewText: String)
}

