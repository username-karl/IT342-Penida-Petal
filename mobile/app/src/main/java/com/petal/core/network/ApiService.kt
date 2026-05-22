package com.petal.core.network

import com.petal.data.auth.ApiResponse
import com.petal.data.auth.AuthResponse
import com.petal.data.auth.LoginRequest
import com.petal.data.auth.RegisterRequest
import com.petal.data.catalog.ProductResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<Void>>

    @GET("/api/products")
    suspend fun products(@Query("mood") mood: String? = null): Response<ApiResponse<List<ProductResponse>>>

    @GET("/api/products/{id}")
    suspend fun product(@Path("id") id: Long): Response<ApiResponse<ProductResponse>>
}
