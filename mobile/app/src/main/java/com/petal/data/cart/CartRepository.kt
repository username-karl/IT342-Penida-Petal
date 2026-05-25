package com.petal.data.cart

import com.petal.core.network.ApiService

class CartRepository(private val apiService: ApiService) {
    suspend fun getCart(): Result<CartResponse> {
        return try {
            val response = apiService.getCart()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data ?: CartResponse())
            } else {
                Result.failure(IllegalStateException(body?.message ?: "Unable to load cart."))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    suspend fun addItem(productId: Long, quantity: Int = 1): Result<CartItemResponse> {
        return try {
            val response = apiService.addCartItem(AddCartItemRequest(productId, quantity))
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(IllegalStateException(body?.message ?: "Unable to add item to cart."))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    suspend fun updateItem(itemId: Long, quantity: Int): Result<CartItemResponse> {
        return try {
            val response = apiService.updateCartItem(itemId, UpdateCartItemRequest(quantity))
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(IllegalStateException(body?.message ?: "Unable to update cart item."))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    suspend fun removeItem(itemId: Long): Result<Unit> {
        return try {
            val response = apiService.removeCartItem(itemId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException(body?.message ?: "Unable to remove cart item."))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }
}
