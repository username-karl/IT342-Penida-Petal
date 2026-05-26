package com.petal.data.messaging

import com.petal.core.network.ApiService

class MessagingRepository(private val apiService: ApiService) {

    suspend fun getConversations(): Result<List<ConversationResponse>> {
        return try {
            val response = apiService.getConversations()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Unable to fetch conversations."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection failed. Check your network.", e))
        }
    }

    suspend fun startConversation(orderId: Long): Result<ConversationResponse> {
        return try {
            val response = apiService.startConversation(orderId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Unable to start conversation."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection failed. Check your network.", e))
        }
    }

    suspend fun getMessages(conversationId: Long): Result<List<MessageResponse>> {
        return try {
            val response = apiService.getMessages(conversationId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Unable to fetch messages."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection failed. Check your network.", e))
        }
    }

    suspend fun sendMessage(conversationId: Long, content: String): Result<MessageResponse> {
        return try {
            val request = CreateMessageRequest(content)
            val response = apiService.sendMessage(conversationId, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Unable to send message."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection failed. Check your network.", e))
        }
    }
}
