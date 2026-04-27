package com.petal.shared.data

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class AuthResponse(
    val token: String,
    val name: String,
    val email: String,
    val role: String
)

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: String
)
