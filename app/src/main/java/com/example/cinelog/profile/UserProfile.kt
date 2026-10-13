package com.example.cinelog.profile

data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val uniqueId: String = "",
    val displayName: String = "",
    val photoBase64: String? = null,
    val bio: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
)


data class UserStats(
    val moviesRatedCount: Int = 0,
    val averageScore: Double = 0.0,
    val totalLikesReceived: Int = 0,
)
