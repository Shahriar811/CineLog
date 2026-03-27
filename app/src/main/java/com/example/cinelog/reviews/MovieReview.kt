package com.example.cinelog.reviews

data class MovieReview(
    val id: String,
    val movieId: Long,
    val movieTitle: String,
    val ratingOutOf10: Int,
    val reviewText: String,
    val createdAtMillis: Long,
    val userId: String,
)

