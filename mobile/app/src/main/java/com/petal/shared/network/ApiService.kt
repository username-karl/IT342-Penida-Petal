package com.petal.shared.network

import com.petal.shared.data.ApiResponse
import com.petal.shared.data.AuthResponse
import com.petal.shared.data.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("/api/auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<ApiResponse<AuthResponse>>

    @POST("/api/auth/register")
    suspend fun register(@Body request: Map<String, String>): Response<ApiResponse<Void>>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Void>>

    @GET("/api/user/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserResponse>>
}
