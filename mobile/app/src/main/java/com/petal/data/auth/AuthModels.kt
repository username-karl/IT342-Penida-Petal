package com.petal.data.auth

// Matches the live Spring Boot ApiResponse shape. The SDD still documents
// success/data/error/timestamp, but the current backend returns success/message/data.
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

data class AuthResponse(
    val token: String,
    val name: String,
    val email: String,
    val role: String
)

object AuthFormValidator {
    const val buyerRole = "BUYER"
    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private val numberPattern = Regex(".*\\d.*")
    private val specialPattern = Regex(".*[^A-Za-z0-9].*")

    fun loginError(email: String, password: String): String? {
        return when {
            email.isBlank() -> "Enter your email address."
            !emailPattern.matches(email.trim()) -> "Enter a valid email address."
            password.isBlank() -> "Enter your password."
            else -> null
        }
    }

    fun registerError(name: String, email: String, password: String): String? {
        return when {
            name.trim().isBlank() -> "Enter your full name."
            email.isBlank() -> "Enter your email address."
            !emailPattern.matches(email.trim()) -> "Enter a valid email address."
            password.isBlank() -> "Create a password."
            password.length < 8 -> "Use at least 8 characters."
            !numberPattern.matches(password) || !specialPattern.matches(password) ->
                "Add at least one number and one special character."
            else -> null
        }
    }
}
