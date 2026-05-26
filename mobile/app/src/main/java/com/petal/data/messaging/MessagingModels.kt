package com.petal.data.messaging

data class ConversationResponse(
    val id: Long,
    val orderId: Long?,
    val floristId: Long?,
    val floristName: String?,
    val unreadCount: Int?,
    val lastMessageAt: String?,
    val lastMessagePreview: String?
)

data class MessageResponse(
    val id: Long,
    val conversationId: Long,
    val senderId: Long?,
    val senderType: String?, // "buyer" or "florist"
    val content: String,
    val createdAt: String?
)

data class CreateMessageRequest(
    val content: String
)
