package com.petal.data.account

import com.google.gson.Gson
import com.petal.core.network.ApiService
import com.petal.data.auth.ApiResponse

class BuyerAccountRepository(private val apiService: ApiService) {
    private val gson = Gson()

    suspend fun addresses(): Result<List<DeliveryAddressResponse>> {
        return try {
            val response = apiService.addresses()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data.orEmpty())
            } else {
                Result.failure(IllegalStateException(errorMessage(response.code(), body?.message, response.errorBody()?.string(), "Unable to load saved addresses.")))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    suspend fun createAddress(request: DeliveryAddressRequest): Result<DeliveryAddressResponse> {
        return try {
            val response = apiService.createAddress(request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(IllegalStateException(errorMessage(response.code(), body?.message, response.errorBody()?.string(), "Unable to save address.")))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    suspend fun updateAddress(id: Long, request: DeliveryAddressRequest): Result<DeliveryAddressResponse> {
        return try {
            val response = apiService.updateAddress(id, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(IllegalStateException(errorMessage(response.code(), body?.message, response.errorBody()?.string(), "Unable to update address.")))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    suspend fun deleteAddress(id: Long): Result<Unit> {
        return try {
            val response = apiService.deleteAddress(id)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException(errorMessage(response.code(), body?.message, response.errorBody()?.string(), "Unable to remove address.")))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    suspend fun savedDates(): Result<List<SavedDateResponse>> {
        return try {
            val response = apiService.savedDates()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data.orEmpty())
            } else {
                Result.failure(IllegalStateException(errorMessage(response.code(), body?.message, response.errorBody()?.string(), "Unable to load important dates.")))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    suspend fun createSavedDate(request: SavedDateRequest): Result<SavedDateResponse> {
        return try {
            val response = apiService.createSavedDate(request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(IllegalStateException(errorMessage(response.code(), body?.message, response.errorBody()?.string(), "Unable to save important date.")))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    private fun errorMessage(statusCode: Int, message: String?, errorBody: String?, fallback: String): String {
        val backendMessage = message ?: parseErrorMessage(errorBody)
        return backendMessage ?: when (statusCode) {
            401 -> "Sign in again to manage your account."
            403 -> "Buyer access is required."
            404 -> "This saved item was not found."
            else -> fallback
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return runCatching {
            gson.fromJson(errorBody, ApiResponse::class.java)?.message
        }.getOrNull()
    }
}
