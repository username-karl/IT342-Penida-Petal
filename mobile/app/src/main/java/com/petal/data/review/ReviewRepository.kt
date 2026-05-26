package com.petal.data.review

import com.petal.core.network.ApiService

class ReviewRepository(private val apiService: ApiService) {

    suspend fun getProductReviews(productId: Long): Result<ReviewSummaryResponse> {
        return try {
            val response = apiService.getProductReviews(productId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Unable to fetch reviews."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection failed. Check your network.", e))
        }
    }

    suspend fun createReview(
        orderId: Long,
        productId: Long,
        productRating: Int,
        floristRating: Int,
        comment: String?
    ): Result<ReviewResponse> {
        return try {
            val request = CreateReviewRequest(productRating, floristRating, comment)
            val response = apiService.createReview(orderId, productId, request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Unable to submit review."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection failed. Check your network.", e))
        }
    }
}
