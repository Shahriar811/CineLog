package com.example.cinelog.chat

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(otherUserId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(
        otherUserId: String,
        otherDisplayName: String,
        otherUsername: String,
        otherPhotoBase64: String?,
        text: String,
    )
    fun getConversations(): Flow<List<ChatConversation>>
    fun getRoomId(userA: String, userB: String): String {
        return if (userA < userB) "${userA}_$userB" else "${userB}_$userA"
    }
}
