package com.example.cinelog.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore?,
    private val firebaseAuth: FirebaseAuth?,
) : ProfileRepository {

    override val currentUserProfile: Flow<UserProfile?> = callbackFlow {
        val auth = firebaseAuth
        if (auth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val authListener = FirebaseAuth.AuthStateListener { currentAuth ->
            val uid = currentAuth.currentUser?.uid
            if (uid == null) {
                trySend(null)
            }
        }
        auth.addAuthStateListener(authListener)

        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            awaitClose { auth.removeAuthStateListener(authListener) }
            return@callbackFlow
        }

        val db = requireFirestore()
        val docListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                var uniqueId = snapshot.getString("uniqueId").orEmpty()
                if (uniqueId.isBlank()) {
                    uniqueId = UniqueIdGenerator.generate()
                    db.collection("users").document(uid).update("uniqueId", uniqueId)
                    db.collection("uniqueIds").document(uniqueId).set(
                        mapOf(
                            "userId" to uid,
                            "username" to snapshot.getString("username").orEmpty(),
                            "createdAtMillis" to System.currentTimeMillis(),
                        ),
                        SetOptions.merge(),
                    )
                }
                val profile = UserProfile(
                    userId = snapshot.getString("userId") ?: uid,
                    username = snapshot.getString("username").orEmpty(),
                    uniqueId = uniqueId,
                    displayName = snapshot.getString("displayName") ?: (auth.currentUser?.displayName.orEmpty()),
                    photoBase64 = snapshot.getString("photoBase64"),
                    bio = snapshot.getString("bio").orEmpty(),
                    createdAtMillis = snapshot.getLong("createdAtMillis") ?: System.currentTimeMillis(),
                )
                trySend(profile)
            }

        awaitClose {
            auth.removeAuthStateListener(authListener)
            docListener.remove()
        }
    }

    override fun getProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val db = requireFirestore()
        val listener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val profile = UserProfile(
                    userId = snapshot.getString("userId") ?: userId,
                    username = snapshot.getString("username").orEmpty(),
                    uniqueId = snapshot.getString("uniqueId").orEmpty(),
                    displayName = snapshot.getString("displayName").orEmpty(),
                    photoBase64 = snapshot.getString("photoBase64"),
                    bio = snapshot.getString("bio").orEmpty(),
                    createdAtMillis = snapshot.getLong("createdAtMillis") ?: 0L,
                )
                trySend(profile)
            }
        awaitClose { listener.remove() }
    }

    override fun getUserStats(userId: String): Flow<UserStats> = callbackFlow {
        val db = requireFirestore()
        val listener = db.collection("reviews")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(UserStats())
                    return@addSnapshotListener
                }
                val docs = snapshot.documents
                val count = docs.size
                val ratings = docs.mapNotNull { it.getLong("ratingOutOf10") }
                val avg = if (ratings.isNotEmpty()) ratings.average() else 0.0
                val totalLikes = docs.sumOf { (it.getLong("likesCount") ?: 0L).toInt() }
                trySend(
                    UserStats(
                        moviesRatedCount = count,
                        averageScore = avg,
                        totalLikesReceived = totalLikes,
                    )
                )
            }
        awaitClose { listener.remove() }
    }

    override suspend fun isUsernameAvailable(username: String): Boolean {
        val clean = username.trim().lowercase().removePrefix("@")
        if (!clean.matches(Regex("^[a-z0-9_]{1,20}$"))) return false
        val db = requireFirestore()
        val doc = db.collection("usernames").document(clean).get().await()
        if (!doc.exists()) return true
        val existingUid = doc.getString("userId")
        return existingUid == firebaseAuth?.currentUser?.uid
    }

    override suspend fun claimUsernameAndSetupProfile(
        username: String,
        displayName: String,
        photoBase64: String?,
        bio: String,
    ): Result<UserProfile> {
        val uid = requireUserId()
        val cleanUsername = username.trim().lowercase().removePrefix("@")
        if (!cleanUsername.matches(Regex("^[a-z0-9_]{1,20}$"))) {
            return Result.failure(
                IllegalArgumentException("Unique name must be between 1 and 20 characters using letters, numbers, and underscores only.")
            )
        }

        val db = requireFirestore()
        val generatedUniqueId = UniqueIdGenerator.generate()
        val usernameRef = db.collection("usernames").document(cleanUsername)
        val uniqueIdRef = db.collection("uniqueIds").document(generatedUniqueId)
        val userRef = db.collection("users").document(uid)

        return runCatching {
            db.runTransaction { tx ->
                val userDoc = tx.get(userRef)
                val existingUsername = userDoc.getString("username")
                if (!existingUsername.isNullOrBlank()) {
                    throw IllegalStateException("Your unique name is already permanently set to @$existingUsername and cannot be changed.")
                }

                val usernameDoc = tx.get(usernameRef)
                if (usernameDoc.exists() && usernameDoc.getString("userId") != uid) {
                    throw IllegalStateException("The unique name @$cleanUsername is already taken by another user. Please choose another.")
                }

                val now = System.currentTimeMillis()
                tx.set(
                    usernameRef,
                    mapOf(
                        "userId" to uid,
                        "uniqueId" to generatedUniqueId,
                        "createdAtMillis" to now,
                    )
                )
                tx.set(
                    uniqueIdRef,
                    mapOf(
                        "userId" to uid,
                        "username" to cleanUsername,
                        "createdAtMillis" to now,
                    )
                )

                val profileMap = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "username" to cleanUsername,
                    "uniqueId" to generatedUniqueId,
                    "displayName" to displayName.trim().ifBlank { cleanUsername },
                    "bio" to bio.trim(),
                    "createdAtMillis" to now,
                )
                if (photoBase64 != null) {
                    profileMap["photoBase64"] = photoBase64
                }
                tx.set(userRef, profileMap, SetOptions.merge())
            }.await()

            UserProfile(
                userId = uid,
                username = cleanUsername,
                uniqueId = generatedUniqueId,
                displayName = displayName.trim().ifBlank { cleanUsername },
                photoBase64 = photoBase64,
                bio = bio.trim(),
                createdAtMillis = System.currentTimeMillis(),
            )
        }
    }

    override suspend fun updateProfilePhoto(photoBase64: String) {
        val uid = requireUserId()
        val db = requireFirestore()
        db.collection("users").document(uid).update("photoBase64", photoBase64).await()

        // Also update existing reviews so user's avatar reflects everywhere
        val reviews = db.collection("reviews").whereEqualTo("userId", uid).get().await()
        for (doc in reviews.documents) {
            doc.reference.update("userPhotoBase64", photoBase64)
        }
    }

    override suspend fun updateBio(bio: String) {
        val uid = requireUserId()
        val db = requireFirestore()
        db.collection("users").document(uid).update("bio", bio.trim()).await()
    }

    private fun requireFirestore(): FirebaseFirestore {
        return firestore ?: error("Firebase is not initialized.")
    }

    private fun requireUserId(): String {
        return firebaseAuth?.currentUser?.uid
            ?: error("Please sign in to perform this action.")
    }
}
