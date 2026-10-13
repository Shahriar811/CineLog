package com.example.cinelog.reviews

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore?,
    private val firebaseAuth: FirebaseAuth?,
) : ReviewRepository {

    override fun reviewsForMovie(movieId: Long): Flow<List<MovieReview>> = callbackFlow {
        val db = requireFirestore()
        val listener = db.collection("reviews")
            .whereEqualTo("movieId", movieId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents.orEmpty()
                    .map { mapToMovieReview(it) }
                    .filter { it.movieTitle.isNotBlank() && it.userId.isNotBlank() }
                    .sortedByDescending { it.createdAtMillis }
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }

    override fun myReviews(): Flow<List<MovieReview>> = callbackFlow {
        val uid = requireUserId()
        val db = requireFirestore()
        val listener = db.collection("reviews")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents.orEmpty()
                    .map { mapToMovieReview(it) }
                    .filter { it.movieId > 0L && it.movieTitle.isNotBlank() }
                    .sortedByDescending { it.createdAtMillis }
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }

    override fun userReviews(userId: String): Flow<List<MovieReview>> = callbackFlow {
        val db = requireFirestore()
        val listener = db.collection("reviews")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents.orEmpty()
                    .map { mapToMovieReview(it) }
                    .filter { it.movieId > 0L && it.movieTitle.isNotBlank() }
                    .sortedByDescending { it.createdAtMillis }
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }

    override fun communityFeed(): Flow<List<MovieReview>> = callbackFlow {
        val db = requireFirestore()
        val listener = db.collection("reviews")
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents.orEmpty()
                    .map { mapToMovieReview(it) }
                    .filter { it.movieTitle.isNotBlank() && it.userId.isNotBlank() }
                    .sortedByDescending { it.createdAtMillis }
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun upsertReview(
        movieId: Long,
        movieTitle: String,
        ratingOutOf10: Int,
        reviewText: String,
        posterPath: String?,
    ) {
        require(movieTitle.isNotBlank()) { "Movie title is missing." }
        require(reviewText.isNotBlank()) { "Please write a review." }
        require(ratingOutOf10 in 1..10) { "Rating must be between 1 and 10." }

        val uid = requireUserId()
        val db = requireFirestore()

        // Fetch user's profile for denormalized display in community feed
        val userDoc = runCatching { db.collection("users").document(uid).get().await() }.getOrNull()
        val displayName = userDoc?.getString("displayName")
            ?: firebaseAuth?.currentUser?.displayName
            ?: "CineLog Fan"
        val username = userDoc?.getString("username").orEmpty()
        val photoBase64 = userDoc?.getString("photoBase64")

        val docId = "${uid}_$movieId"
        val reviewData = mutableMapOf<String, Any>(
            "movieId" to movieId,
            "movieTitle" to movieTitle,
            "ratingOutOf10" to ratingOutOf10,
            "reviewText" to reviewText.trim(),
            "createdAtMillis" to System.currentTimeMillis(),
            "userId" to uid,
            "userDisplayName" to displayName,
            "userUsername" to username,
        )
        if (posterPath != null) {
            reviewData["posterPath"] = posterPath
        }
        if (photoBase64 != null) {
            reviewData["userPhotoBase64"] = photoBase64
        }

        db.collection("reviews").document(docId).set(reviewData, SetOptions.merge()).await()
    }

    override suspend fun toggleLike(reviewId: String) {
        val uid = requireUserId()
        val db = requireFirestore()
        val docRef = db.collection("reviews").document(reviewId)

        db.runTransaction { tx ->
            val snapshot = tx.get(docRef)
            if (!snapshot.exists()) return@runTransaction

            @Suppress("UNCHECKED_CAST")
            val likedBy = (snapshot.get("likedBy") as? List<String>).orEmpty().toMutableList()
            @Suppress("UNCHECKED_CAST")
            val dislikedBy = (snapshot.get("dislikedBy") as? List<String>).orEmpty().toMutableList()

            if (likedBy.contains(uid)) {
                likedBy.remove(uid)
            } else {
                likedBy.add(uid)
                dislikedBy.remove(uid)
            }

            tx.update(
                docRef,
                mapOf(
                    "likedBy" to likedBy,
                    "dislikedBy" to dislikedBy,
                    "likesCount" to likedBy.size,
                    "dislikesCount" to dislikedBy.size,
                ),
            )
        }.await()
    }

    override suspend fun toggleDislike(reviewId: String) {
        val uid = requireUserId()
        val db = requireFirestore()
        val docRef = db.collection("reviews").document(reviewId)

        db.runTransaction { tx ->
            val snapshot = tx.get(docRef)
            if (!snapshot.exists()) return@runTransaction

            @Suppress("UNCHECKED_CAST")
            val likedBy = (snapshot.get("likedBy") as? List<String>).orEmpty().toMutableList()
            @Suppress("UNCHECKED_CAST")
            val dislikedBy = (snapshot.get("dislikedBy") as? List<String>).orEmpty().toMutableList()

            if (dislikedBy.contains(uid)) {
                dislikedBy.remove(uid)
            } else {
                dislikedBy.add(uid)
                likedBy.remove(uid)
            }

            tx.update(
                docRef,
                mapOf(
                    "likedBy" to likedBy,
                    "dislikedBy" to dislikedBy,
                    "likesCount" to likedBy.size,
                    "dislikesCount" to dislikedBy.size,
                ),
            )
        }.await()
    }

    override suspend fun updateReview(reviewId: String, ratingOutOf10: Int, reviewText: String) {
        require(reviewText.isNotBlank()) { "Review text cannot be empty." }
        require(ratingOutOf10 in 1..10) { "Rating must be between 1 and 10." }
        val uid = requireUserId()
        val db = requireFirestore()
        val docRef = db.collection("reviews").document(reviewId)

        val doc = docRef.get().await()
        require(doc.exists()) { "Review not found." }
        val ownerId = doc.getString("userId").orEmpty()
        require(ownerId == uid) { "You can only edit your own reviews." }

        docRef.update(
            mapOf(
                "ratingOutOf10" to ratingOutOf10,
                "reviewText" to reviewText.trim(),
                "updatedAtMillis" to System.currentTimeMillis(),
            )
        ).await()
    }

    override suspend fun deleteReview(reviewId: String) {
        val uid = requireUserId()
        val db = requireFirestore()
        val docRef = db.collection("reviews").document(reviewId)

        val doc = docRef.get().await()
        if (!doc.exists()) return
        val ownerId = doc.getString("userId").orEmpty()
        require(ownerId == uid) { "You can only delete your own reviews." }

        docRef.delete().await()
    }

    override fun commentsForReview(reviewId: String): Flow<List<ReviewComment>> = callbackFlow {
        val db = requireFirestore()
        val listener = db.collection("reviews").document(reviewId)
            .collection("comments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val text = doc.getString("text").orEmpty()
                    if (text.isBlank()) null else {
                        ReviewComment(
                            id = doc.id,
                            reviewId = doc.getString("reviewId") ?: reviewId,
                            userId = doc.getString("userId").orEmpty(),
                            userDisplayName = doc.getString("userDisplayName").orEmpty(),
                            userUsername = doc.getString("userUsername").orEmpty(),
                            userPhotoBase64 = doc.getString("userPhotoBase64"),
                            text = text,
                            createdAtMillis = doc.getLong("createdAtMillis") ?: 0L,
                        )
                    }
                }.sortedBy { it.createdAtMillis }
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addComment(reviewId: String, text: String) {
        val trimmed = text.trim()
        require(trimmed.isNotBlank()) { "Comment cannot be empty." }

        val uid = requireUserId()
        val db = requireFirestore()

        // Fetch user's profile for display
        val userDoc = runCatching { db.collection("users").document(uid).get().await() }.getOrNull()
        val displayName = userDoc?.getString("displayName")
            ?: firebaseAuth?.currentUser?.displayName
            ?: "CineLog Fan"
        val username = userDoc?.getString("username").orEmpty()
        val photoBase64 = userDoc?.getString("photoBase64")

        val commentId = UUID.randomUUID().toString()
        val commentData = mutableMapOf<String, Any>(
            "id" to commentId,
            "reviewId" to reviewId,
            "userId" to uid,
            "userDisplayName" to displayName,
            "userUsername" to username,
            "text" to trimmed,
            "createdAtMillis" to System.currentTimeMillis(),
        )
        if (photoBase64 != null) {
            commentData["userPhotoBase64"] = photoBase64
        }

        val reviewRef = db.collection("reviews").document(reviewId)
        val commentRef = reviewRef.collection("comments").document(commentId)

        db.runBatch { batch ->
            batch.set(commentRef, commentData)
            batch.update(reviewRef, "commentCount", FieldValue.increment(1))
        }.await()
    }

    private fun mapToMovieReview(doc: DocumentSnapshot): MovieReview {
        @Suppress("UNCHECKED_CAST")
        val likedBy = (doc.get("likedBy") as? List<String>).orEmpty()
        @Suppress("UNCHECKED_CAST")
        val dislikedBy = (doc.get("dislikedBy") as? List<String>).orEmpty()
        val likesCount = (doc.getLong("likesCount") ?: likedBy.size.toLong()).toInt()
        val dislikesCount = (doc.getLong("dislikesCount") ?: dislikedBy.size.toLong()).toInt()
        val commentCount = (doc.getLong("commentCount") ?: 0L).toInt()

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
            likesCount = likesCount,
            dislikesCount = dislikesCount,
            likedBy = likedBy,
            dislikedBy = dislikedBy,
            commentCount = commentCount,
        )
    }

    private fun requireFirestore(): FirebaseFirestore {
        return firestore ?: error("Firebase is not initialized. Add app/google-services.json and rebuild the app.")
    }

    private fun requireUserId(): String {
        return firebaseAuth?.currentUser?.uid
            ?: error("Please sign in to save reviews.")
    }
}
