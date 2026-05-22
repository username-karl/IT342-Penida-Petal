package com.petal.data.auth

import com.petal.core.network.ApiService
import com.petal.core.session.SessionStore

class AuthRepository(
    private val apiService: ApiService,
    private val sessionStore: SessionStore
) {
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = apiService.login(LoginRequest(email.trim(), password))
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                val auth = body.data
                sessionStore.saveSession(auth.token, auth.name, auth.email, auth.role)
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException(body?.message ?: "Unable to sign in."))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return try {
            val response = apiService.register(
                RegisterRequest(
                    name = name.trim(),
                    email = email.trim(),
                    password = password,
                    role = AuthFormValidator.buyerRole
                )
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException(body?.message ?: "Unable to create account."))
            }
        } catch (exception: Exception) {
            Result.failure(IllegalStateException("Connection failed. Check that the backend is running.", exception))
        }
    }
}
