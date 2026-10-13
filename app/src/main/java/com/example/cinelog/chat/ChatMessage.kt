package com.example.cinelog.chat

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestampMillis: Long = 0L,
    val read: Boolean = false,
)

data class ChatConversation(
    val roomId: String = "",
    val otherUserId: String = "",
    val otherDisplayName: String = "",
    val otherUsername: String = "",
    val otherPhotoBase64: String? = null,
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
)
