package com.example.cinelog.chat

import com.google.firebase.auth.FirebaseAuth
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
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore?,
    private val firebaseAuth: FirebaseAuth?,
) : ChatRepository {

    override fun getMessages(otherUserId: String): Flow<List<ChatMessage>> = callbackFlow {
        val uid = firebaseAuth?.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val db = requireFirestore()
        val roomId = getRoomId(uid, otherUserId)

        val listener = db.collection("chats").document(roomId)
            .collection("messages")
            .orderBy("timestampMillis")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val text = doc.getString("text").orEmpty()
                    if (text.isBlank()) null else {
                        ChatMessage(
                            id = doc.id,
                            senderId = doc.getString("senderId").orEmpty(),
                            senderName = doc.getString("senderName").orEmpty(),
                            receiverId = doc.getString("receiverId").orEmpty(),
                            text = text,
                            timestampMillis = doc.getLong("timestampMillis") ?: 0L,
                            read = doc.getBoolean("read") ?: false,
                        )
                    }
                }
                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(
        otherUserId: String,
        otherDisplayName: String,
        otherUsername: String,
        otherPhotoBase64: String?,
        text: String,
    ) {
        val cleanText = text.trim()
        require(cleanText.isNotBlank()) { "Message text cannot be empty." }
        val uid = requireUserId()
        require(uid != otherUserId) { "You cannot send messages to yourself." }
        val db = requireFirestore()

        val roomId = getRoomId(uid, otherUserId)
        val now = System.currentTimeMillis()

        // Fetch my current display name & photo
        val myDoc = runCatching { db.collection("users").document(uid).get().await() }.getOrNull()
        val myName = myDoc?.getString("displayName") ?: firebaseAuth?.currentUser?.displayName ?: "CineLog Member"
        val myUsername = myDoc?.getString("username").orEmpty()
        val myPhoto = myDoc?.getString("photoBase64")

        val messageData = mapOf(
            "senderId" to uid,
            "senderName" to myName,
            "receiverId" to otherUserId,
            "text" to cleanText,
            "timestampMillis" to now,
            "read" to false,
        )

        val chatRoomData = mapOf(
            "roomId" to roomId,
            "participantIds" to listOf(uid, otherUserId),
            "lastMessage" to cleanText,
            "lastMessageTimestamp" to now,
            "lastSenderId" to uid,
            "participant_${uid}_name" to myName,
            "participant_${uid}_username" to myUsername,
            "participant_${uid}_photo" to (myPhoto ?: ""),
            "participant_${otherUserId}_name" to otherDisplayName,
            "participant_${otherUserId}_username" to otherUsername,
            "participant_${otherUserId}_photo" to (otherPhotoBase64 ?: ""),
        )

        val roomRef = db.collection("chats").document(roomId)
        roomRef.set(chatRoomData, SetOptions.merge()).await()
        roomRef.collection("messages").document(UUID.randomUUID().toString()).set(messageData).await()
    }

    override fun getConversations(): Flow<List<ChatConversation>> = callbackFlow {
        val uid = firebaseAuth?.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val db = requireFirestore()

        val listener = db.collection("chats")
            .whereArrayContains("participantIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val conversations = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val participants = (doc.get("participantIds") as? List<String>).orEmpty()
                    val otherId = participants.firstOrNull { it != uid } ?: return@mapNotNull null
                    val lastMsg = doc.getString("lastMessage").orEmpty()
                    val lastTs = doc.getLong("lastMessageTimestamp") ?: 0L

                    val otherName = doc.getString("participant_${otherId}_name") ?: "Cinephile"
                    val otherUsername = doc.getString("participant_${otherId}_username").orEmpty()
                    val otherPhoto = doc.getString("participant_${otherId}_photo")?.takeIf { it.isNotBlank() }

                    ChatConversation(
                        roomId = doc.id,
                        otherUserId = otherId,
                        otherDisplayName = otherName,
                        otherUsername = otherUsername,
                        otherPhotoBase64 = otherPhoto,
                        lastMessage = lastMsg,
                        lastMessageTimestamp = lastTs,
                    )
                }.sortedByDescending { it.lastMessageTimestamp }

                trySend(conversations)
            }

        awaitClose { listener.remove() }
    }

    private fun requireFirestore(): FirebaseFirestore {
        return firestore ?: error("Firebase Firestore is not initialized.")
    }

    private fun requireUserId(): String {
        return firebaseAuth?.currentUser?.uid ?: error("Please sign in to send messages.")
    }
}
