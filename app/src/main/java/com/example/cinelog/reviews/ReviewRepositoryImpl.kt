package com.example.cinelog.reviews

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
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
                val reviews = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val rating = (doc.getLong("ratingOutOf10") ?: 0L).toInt()
                    val title = doc.getString("movieTitle").orEmpty()
                    val text = doc.getString("reviewText").orEmpty()
                    val userId = doc.getString("userId").orEmpty()
                    val created = doc.getLong("createdAtMillis") ?: 0L
                    if (title.isBlank() || userId.isBlank()) null else {
                        MovieReview(
                            id = doc.id,
                            movieId = doc.getLong("movieId") ?: movieId,
                            movieTitle = title,
                            ratingOutOf10 = rating.coerceIn(1, 10),
                            reviewText = text,
                            createdAtMillis = created,
                            userId = userId,
                        )
                    }
                }
                trySend(reviews.sortedByDescending { it.createdAtMillis })
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
                val reviews = snapshot?.documents.orEmpty().map { doc ->
                    MovieReview(
                        id = doc.id,
                        movieId = doc.getLong("movieId") ?: 0L,
                        movieTitle = doc.getString("movieTitle").orEmpty(),
                        ratingOutOf10 = (doc.getLong("ratingOutOf10") ?: 0L).toInt().coerceIn(1, 10),
                        reviewText = doc.getString("reviewText").orEmpty(),
                        createdAtMillis = doc.getLong("createdAtMillis") ?: 0L,
                        userId = doc.getString("userId").orEmpty(),
                    )
                }.filter { it.movieId > 0L && it.movieTitle.isNotBlank() }
                trySend(reviews.sortedByDescending { it.createdAtMillis })
            }
        awaitClose { listener.remove() }
    }

    override suspend fun upsertReview(movieId: Long, movieTitle: String, ratingOutOf10: Int, reviewText: String) {
        require(movieTitle.isNotBlank()) { "Movie title is missing." }
        require(reviewText.isNotBlank()) { "Please write a review." }
        require(ratingOutOf10 in 1..10) { "Rating must be between 1 and 10." }

        val uid = requireUserId()
        val db = requireFirestore()

        val docId = "${uid}_$movieId"
        db.collection("reviews").document(docId).set(
            mapOf(
                "movieId" to movieId,
                "movieTitle" to movieTitle,
                "ratingOutOf10" to ratingOutOf10,
                "reviewText" to reviewText.trim(),
                "createdAtMillis" to System.currentTimeMillis(),
                "userId" to uid,
            ),
        ).await()
    }

    private fun requireFirestore(): FirebaseFirestore {
        return firestore ?: error("Firebase is not initialized. Add app/google-services.json and rebuild the app.")
    }

    private fun requireUserId(): String {
        return firebaseAuth?.currentUser?.uid
            ?: error("Please sign in to save reviews.")
    }
}

