package com.example.cinelog.reviews

data class MovieReview(
    val id: String = "",
    val movieId: Long = 0L,
    val movieTitle: String = "",
    val posterPath: String? = null,
    val ratingOutOf10: Int = 0,
    val reviewText: String = "",
    val createdAtMillis: Long = 0L,
    val userId: String = "",
    val userDisplayName: String = "",
    val userUsername: String = "",
    val userPhotoBase64: String? = null,
    val likesCount: Int = 0,
    val dislikesCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val dislikedBy: List<String> = emptyList(),
    val commentCount: Int = 0,
)
