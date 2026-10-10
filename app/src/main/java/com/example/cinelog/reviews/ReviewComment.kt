package com.example.cinelog.reviews

data class ReviewComment(
    val id: String = "",
    val reviewId: String = "",
    val userId: String = "",
    val userDisplayName: String = "",
    val userUsername: String = "",
    val userPhotoBase64: String? = null,
    val text: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
)
