package com.petal.data.review

data class CreateReviewRequest(
    val productRating: Int,
    val floristRating: Int,
    val comment: String? = null
)

data class ReviewResponse(
    val id: Long,
    val productRating: Int,
    val floristRating: Int,
    val comment: String?,
    val reviewerName: String,
    val createdAt: String
)

data class ReviewSummaryResponse(
    val averageRating: Double,
    val totalReviews: Long,
    val recentReviews: List<ReviewResponse>
)
