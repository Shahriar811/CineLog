package com.example.cinelog.social

import com.example.cinelog.profile.UserProfile
import com.example.cinelog.reviews.MovieReview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data model for the follow system:
 *
 * /users/{userId}
 *   followersCount: Int
 *   followingCount: Int
 *
 * /users/{userId}/followers/{followerUserId}  → { followedAtMillis: Long }
 * /users/{userId}/following/{followedUserId}  → { followedAtMillis: Long }
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class SocialRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore?,
    private val firebaseAuth: FirebaseAuth?,
) : SocialRepository {

    // ── Follow / Unfollow ──────────────────────────────────────────────────────

    override fun isFollowing(targetUserId: String): Flow<Boolean> = callbackFlow {
        val uid = firebaseAuth?.currentUser?.uid
        if (uid == null || uid == targetUserId) {
            trySend(false)
            awaitClose { }
            return@callbackFlow
        }
        val db = requireFirestore()
        val listener = db.collection("users").document(uid)
            .collection("following").document(targetUserId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.exists() == true)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun toggleFollow(targetUserId: String) {
        val uid = requireUserId()
        require(uid != targetUserId) { "You cannot follow yourself." }
        val db = requireFirestore()

        val followingRef = db.collection("users").document(uid)
            .collection("following").document(targetUserId)
        val followersRef = db.collection("users").document(targetUserId)
            .collection("followers").document(uid)
        val myUserRef = db.collection("users").document(uid)
        val targetUserRef = db.collection("users").document(targetUserId)

        val doc = followingRef.get().await()
        if (doc.exists()) {
            // Unfollow
            db.runBatch { batch ->
                batch.delete(followingRef)
                batch.delete(followersRef)
                batch.update(myUserRef, "followingCount", FieldValue.increment(-1))
                batch.update(targetUserRef, "followersCount", FieldValue.increment(-1))
            }.await()
        } else {
            // Follow
            val now = System.currentTimeMillis()
            val followData = mapOf("followedAtMillis" to now)
            db.runBatch { batch ->
                batch.set(followingRef, followData)
                batch.set(followersRef, followData)
                batch.update(myUserRef, "followingCount", FieldValue.increment(1))
                batch.update(targetUserRef, "followersCount", FieldValue.increment(1))
            }.await()
        }
    }

    // ── Follow Stats ───────────────────────────────────────────────────────────

    override fun getFollowStats(userId: String): Flow<FollowStats> = callbackFlow {
        val db = requireFirestore()
        val listener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                val followers = (snapshot?.getLong("followersCount") ?: 0L).toInt().coerceAtLeast(0)
                val following = (snapshot?.getLong("followingCount") ?: 0L).toInt().coerceAtLeast(0)
                trySend(FollowStats(followersCount = followers, followingCount = following))
            }
        awaitClose { listener.remove() }
    }

    // ── Followers / Following Lists ────────────────────────────────────────────

    override fun getFollowersList(userId: String): Flow<List<UserProfile>> = callbackFlow {
        val db = requireFirestore()
        val listener = db.collection("users").document(userId)
            .collection("followers")
            .addSnapshotListener { snapshot, _ ->
                val ids = snapshot?.documents?.map { it.id }.orEmpty()
                trySend(ids)
            }
        awaitClose { listener.remove() }
    }.flatMapLatest { ids ->
        if (ids.isEmpty()) flowOf(emptyList())
        else fetchUserProfiles(ids)
    }

    override fun getFollowingList(userId: String): Flow<List<UserProfile>> = callbackFlow {
        val db = requireFirestore()
        val listener = db.collection("users").document(userId)
            .collection("following")
            .addSnapshotListener { snapshot, _ ->
                val ids = snapshot?.documents?.map { it.id }.orEmpty()
                trySend(ids)
            }
        awaitClose { listener.remove() }
    }.flatMapLatest { ids ->
        if (ids.isEmpty()) flowOf(emptyList())
        else fetchUserProfiles(ids)
    }

    private fun fetchUserProfiles(userIds: List<String>): Flow<List<UserProfile>> = callbackFlow {
        val db = requireFirestore()
        // Firestore 'in' query supports max 30 items; chunk if needed
        val profiles = mutableListOf<UserProfile>()
        for (chunk in userIds.chunked(30)) {
            val snapshot = db.collection("users")
                .whereIn("userId", chunk)
                .get().await()
            profiles.addAll(snapshot.documents.mapNotNull { mapToUserProfile(it) })
        }
        trySend(profiles)
        awaitClose { }
    }

    // ── Social Feed (reviews from people I follow) ─────────────────────────────

    override fun getFeedReviews(): Flow<List<MovieReview>> = callbackFlow {
        val uid = firebaseAuth?.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val db = requireFirestore()

        // First, get the list of users I follow
        val followingListener = db.collection("users").document(uid)
            .collection("following")
            .addSnapshotListener { followingSnapshot, _ ->
                val followingIds = followingSnapshot?.documents?.map { it.id }.orEmpty().toMutableList()
                // Also include my own reviews in the feed
                followingIds.add(uid)

                if (followingIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // Firestore 'in' supports up to 30 IDs; for larger sets chunk the query
                val allReviews = mutableListOf<MovieReview>()
                val chunks = followingIds.distinct().chunked(30)

                // We'll do a simple one-shot fetch per chunk; real-time will be driven
                // by the following-list listener re-firing.
                db.collection("reviews")
                    .whereIn("userId", chunks.first())
                    .limit(50)
                    .get()
                    .addOnSuccessListener { reviewsSnapshot ->
                        allReviews.addAll(
                            reviewsSnapshot.documents.map { mapToMovieReview(it) }
                                .filter { it.movieTitle.isNotBlank() && it.userId.isNotBlank() }
                        )

                        // If more chunks exist, fetch them too
                        if (chunks.size > 1) {
                            for (chunk in chunks.drop(1)) {
                                db.collection("reviews")
                                    .whereIn("userId", chunk)
                                    .limit(50)
                                    .get()
                                    .addOnSuccessListener { moreSnapshot ->
                                        allReviews.addAll(
                                            moreSnapshot.documents.map { mapToMovieReview(it) }
                                                .filter { it.movieTitle.isNotBlank() && it.userId.isNotBlank() }
                                        )
                                        trySend(allReviews.sortedByDescending { it.createdAtMillis })
                                    }
                            }
                        } else {
                            trySend(allReviews.sortedByDescending { it.createdAtMillis })
                        }
                    }
            }

        awaitClose { followingListener.remove() }
    }

    // ── Discover Users ─────────────────────────────────────────────────────────

    override fun discoverUsers(limit: Int): Flow<List<UserProfile>> = callbackFlow {
        val db = requireFirestore()
        val uid = firebaseAuth?.currentUser?.uid
        val listener = db.collection("users")
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, _ ->
                val users = snapshot?.documents.orEmpty()
                    .mapNotNull { mapToUserProfile(it) }
                    .filter { it.userId != uid && it.username.isNotBlank() }
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    override fun searchUsers(query: String): Flow<List<UserProfile>> = callbackFlow {
        val db = requireFirestore()
        val uid = firebaseAuth?.currentUser?.uid
        val cleanQuery = query.trim().lowercase()

        if (cleanQuery.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        // Search by username prefix (Firestore range query trick)
        val endStr = cleanQuery + "\uf8ff"
        val listener = db.collection("users")
            .whereGreaterThanOrEqualTo("username", cleanQuery)
            .whereLessThanOrEqualTo("username", endStr)
            .limit(20)
            .addSnapshotListener { snapshot, _ ->
                val users = snapshot?.documents.orEmpty()
                    .mapNotNull { mapToUserProfile(it) }
                    .filter { it.userId != uid }
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    // ── Mappers ────────────────────────────────────────────────────────────────

    private fun mapToUserProfile(doc: DocumentSnapshot): UserProfile? {
        val userId = doc.getString("userId") ?: doc.id
        val username = doc.getString("username").orEmpty()
        if (username.isBlank()) return null
        return UserProfile(
            userId = userId,
            username = username,
            displayName = doc.getString("displayName").orEmpty(),
            photoBase64 = doc.getString("photoBase64"),
            bio = doc.getString("bio").orEmpty(),
            createdAtMillis = doc.getLong("createdAtMillis") ?: 0L,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToMovieReview(doc: DocumentSnapshot): MovieReview {
        val likedBy = (doc.get("likedBy") as? List<String>).orEmpty()
        val dislikedBy = (doc.get("dislikedBy") as? List<String>).orEmpty()
        return MovieReview(
            id = doc.id,
            movieId = doc.getLong("movieId") ?: 0L,
            movieTitle = doc.getString("movieTitle").orEmpty(),
            posterPath = doc.getString("posterPath"),
            ratingOutOf10 = (doc.getLong("ratingOutOf10") ?: 0L).toInt().coerceIn(1, 10),
            reviewText = doc.getString("reviewText").orEmpty(),
            createdAtMillis = doc.getLong("createdAtMillis") ?: 0L,
            userId = doc.getString("userId").orEmpty(),
            userDisplayName = doc.getString("userDisplayName").orEmpty(),
            userUsername = doc.getString("userUsername").orEmpty(),
            userPhotoBase64 = doc.getString("userPhotoBase64"),
            likesCount = (doc.getLong("likesCount") ?: likedBy.size.toLong()).toInt(),
            dislikesCount = (doc.getLong("dislikesCount") ?: dislikedBy.size.toLong()).toInt(),
            likedBy = likedBy,
            dislikedBy = dislikedBy,
            commentCount = (doc.getLong("commentCount") ?: 0L).toInt(),
        )
    }

    private fun requireFirestore(): FirebaseFirestore {
        return firestore ?: error("Firebase is not initialized. Add app/google-services.json and rebuild.")
    }

    private fun requireUserId(): String {
        return firebaseAuth?.currentUser?.uid
            ?: error("Please sign in to perform this action.")
    }
}
