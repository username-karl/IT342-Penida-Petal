package com.petal.core.network

import com.petal.data.auth.ApiResponse
import com.petal.data.auth.AuthResponse
import com.petal.data.auth.LoginRequest
import com.petal.data.auth.RegisterRequest
import com.petal.data.cart.AddCartItemRequest
import com.petal.data.cart.CartItemResponse
import com.petal.data.cart.CartResponse
import com.petal.data.cart.UpdateCartItemRequest
import com.petal.data.catalog.ProductResponse
import com.petal.data.checkout.CreateOrderRequest
import com.petal.data.checkout.DeliverySlotAvailabilityResponse
import com.petal.data.checkout.OrderResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @GET("/api/cart")
    suspend fun getCart(): Response<ApiResponse<CartResponse>>

    @POST("/api/cart/items")
    suspend fun addCartItem(@Body request: AddCartItemRequest): Response<ApiResponse<CartItemResponse>>

    @PUT("/api/cart/items/{id}")
    suspend fun updateCartItem(
        @Path("id") id: Long,
        @Body request: UpdateCartItemRequest
    ): Response<ApiResponse<CartItemResponse>>

    @DELETE("/api/cart/items/{id}")
    suspend fun removeCartItem(@Path("id") id: Long): Response<ApiResponse<Void>>

    @GET("/api/slots/availability")
    suspend fun slotAvailability(
        @Query("florist_id") floristId: Long,
        @Query("date") date: String
    ): Response<ApiResponse<DeliverySlotAvailabilityResponse>>

    @POST("/api/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResponse<OrderResponse>>
}
