package com.example.cinelog.profile

import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val currentUserProfile: Flow<UserProfile?>
    fun getProfile(userId: String): Flow<UserProfile?>
    fun getUserStats(userId: String): Flow<UserStats>
    suspend fun isUsernameAvailable(username: String): Boolean
    suspend fun claimUsernameAndSetupProfile(
        username: String,
        displayName: String,
        photoBase64: String?,
        bio: String,
    ): Result<UserProfile>
    suspend fun updateProfilePhoto(photoBase64: String)
    suspend fun updateBio(bio: String)
}
