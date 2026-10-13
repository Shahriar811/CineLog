package com.example.cinelog.social

import com.example.cinelog.profile.UserProfile
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    fun isFollowing(targetUserId: String): Flow<Boolean>
    fun getFollowStats(userId: String): Flow<FollowStats>
    fun getFollowersList(userId: String): Flow<List<UserProfile>>
    fun getFollowingList(userId: String): Flow<List<UserProfile>>
    fun getFeedReviews(): Flow<List<com.example.cinelog.reviews.MovieReview>>
    fun discoverUsers(limit: Int = 20): Flow<List<UserProfile>>
    fun searchUsers(query: String): Flow<List<UserProfile>>
    suspend fun toggleFollow(targetUserId: String)
}
