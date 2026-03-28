package com.example.cinelog.lists

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieListRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore?,
    private val firebaseAuth: FirebaseAuth?,
) : MovieListRepository {

    override fun userLists(): Flow<List<UserMovieList>> = callbackFlow {
        val uid = firebaseAuth?.currentUser?.uid
        val db = firestore
        if (uid == null || db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listener = db.collection("users").document(uid).collection("movieLists")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lists = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    parseListDocument(doc)
                }.sortedByDescending { it.createdAtMillis }
                trySend(lists)
            }
        awaitClose { listener.remove() }
    }

    override fun list(listId: String): Flow<UserMovieList?> = callbackFlow {
        val uid = firebaseAuth?.currentUser?.uid
        val db = firestore
        if (uid == null || db == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val ref = db.collection("users").document(uid).collection("movieLists").document(listId)
        val listener = ref.addSnapshotListener { snap, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snap == null || !snap.exists()) {
                trySend(null)
                return@addSnapshotListener
            }
            trySend(parseListDocument(snap))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun createList(name: String): String {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "List name cannot be empty." }
        val uid = requireUserId()
        val db = requireFirestore()
        val ref = db.collection("users").document(uid).collection("movieLists").document()
        ref.set(
            mapOf(
                "name" to trimmed,
                "createdAtMillis" to System.currentTimeMillis(),
                "movieIds" to emptyList<Long>(),
            ),
        ).await()
        return ref.id
    }

    override suspend fun renameList(listId: String, newName: String) {
        val trimmed = newName.trim()
        require(trimmed.isNotEmpty()) { "List name cannot be empty." }
        val uid = requireUserId()
        val db = requireFirestore()
        db.collection("users").document(uid).collection("movieLists").document(listId)
            .update("name", trimmed).await()
    }

    override suspend fun deleteList(listId: String) {
        val uid = requireUserId()
        val db = requireFirestore()
        db.collection("users").document(uid).collection("movieLists").document(listId).delete().await()
    }

    override suspend fun addMovieToList(listId: String, movieId: Long) {
        val uid = requireUserId()
        val db = requireFirestore()
        db.collection("users").document(uid).collection("movieLists").document(listId)
            .update("movieIds", FieldValue.arrayUnion(movieId)).await()
    }

    override suspend fun removeMovieFromList(listId: String, movieId: Long) {
        val uid = requireUserId()
        val db = requireFirestore()
        db.collection("users").document(uid).collection("movieLists").document(listId)
            .update("movieIds", FieldValue.arrayRemove(movieId)).await()
    }

    private fun requireFirestore(): FirebaseFirestore {
        return firestore ?: error("Firebase is not initialized. Add app/google-services.json and rebuild the app.")
    }

    private fun requireUserId(): String {
        return firebaseAuth?.currentUser?.uid ?: error("Please sign in to manage lists.")
    }

    private fun parseListDocument(doc: DocumentSnapshot): UserMovieList? {
        val name = doc.getString("name")?.trim().orEmpty()
        if (name.isEmpty()) return null
        val idsRaw = doc.get("movieIds") as? List<*> ?: emptyList<Any>()
        val movieIds = idsRaw.mapNotNull { v ->
            when (v) {
                is Long -> v
                is Int -> v.toLong()
                is Double -> v.toLong()
                else -> null
            }
        }
        return UserMovieList(
            id = doc.id,
            name = name,
            createdAtMillis = doc.getLong("createdAtMillis") ?: 0L,
            movieIds = movieIds.distinct(),
        )
    }
}
