package com.petal.data.catalog

import com.petal.core.network.ApiService

class CatalogRepository(private val apiService: ApiService) {
    suspend fun products(mood: String): Result<List<ProductResponse>> {
        return try {
            val response = apiService.products(mood)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data.orEmpty())
            } else {
                Result.failure(IllegalStateException(body?.message ?: "Unable to load arrangements."))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    suspend fun product(id: Long): Result<ProductResponse> {
        return try {
            val response = apiService.product(id)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(IllegalStateException(body?.message ?: "This arrangement is unavailable."))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }
}
