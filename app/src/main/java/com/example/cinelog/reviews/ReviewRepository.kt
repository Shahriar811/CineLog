package com.example.cinelog.reviews

import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun reviewsForMovie(movieId: Long): Flow<List<MovieReview>>
    fun myReviews(): Flow<List<MovieReview>>
    fun userReviews(userId: String): Flow<List<MovieReview>>
    fun communityFeed(): Flow<List<MovieReview>>

    suspend fun upsertReview(
        movieId: Long,
        movieTitle: String,
        ratingOutOf10: Int,
        reviewText: String,
        posterPath: String? = null,
    )

    suspend fun toggleLike(reviewId: String)
    suspend fun toggleDislike(reviewId: String)

    fun commentsForReview(reviewId: String): Flow<List<ReviewComment>>
    suspend fun addComment(reviewId: String, text: String)
}
