package com.petal.data.checkout

import com.google.gson.Gson
import com.petal.core.network.ApiService
import com.petal.data.auth.ApiResponse

class OrderRepository(private val apiService: ApiService) {
    private val gson = Gson()

    suspend fun orders(): Result<List<BuyerOrderResponse>> {
        return try {
            val response = apiService.orders()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data.orEmpty())
            } else {
                Result.failure(IllegalStateException(errorMessage(response.code(), body?.message, response.errorBody()?.string())))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    suspend fun order(id: Long): Result<BuyerOrderResponse> {
        return try {
            val response = apiService.order(id)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(IllegalStateException(errorMessage(response.code(), body?.message, response.errorBody()?.string())))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    private fun errorMessage(statusCode: Int, message: String?, errorBody: String?): String {
        val backendMessage = message ?: parseErrorMessage(errorBody)
        return backendMessage ?: when (statusCode) {
            401 -> "Sign in again to view your orders."
            403 -> "Buyer access is required to view orders."
            404 -> "Order not found."
            else -> "Unable to load orders."
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return runCatching {
            gson.fromJson(errorBody, ApiResponse::class.java)?.message
        }.getOrNull()
    }
}
