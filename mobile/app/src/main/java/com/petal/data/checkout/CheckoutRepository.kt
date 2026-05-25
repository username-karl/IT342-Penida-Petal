package com.petal.data.checkout

import com.google.gson.Gson
import com.petal.core.network.ApiService
import com.petal.data.auth.ApiResponse

class CheckoutRepository(private val apiService: ApiService) {
    private val gson = Gson()

    suspend fun slotAvailability(
        floristId: Long,
        date: String
    ): Result<DeliverySlotAvailabilityResponse> {
        return try {
            val response = apiService.slotAvailability(floristId = floristId, date = date)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(IllegalStateException(body?.message ?: "Unable to load delivery slots."))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    suspend fun createOrder(request: CreateOrderRequest): Result<OrderResponse> {
        return try {
            val response = apiService.createOrder(request)
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
            400 -> "Order details could not be validated."
            401 -> "Sign in again to place your order."
            403 -> "Buyer access is required to place this order."
            else -> "Unable to place order."
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return runCatching {
            gson.fromJson(errorBody, ApiResponse::class.java)?.message
        }.getOrNull()
    }
}
