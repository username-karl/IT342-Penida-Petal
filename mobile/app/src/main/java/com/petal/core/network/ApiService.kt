package com.petal.core.network

import com.petal.data.auth.ApiResponse
import com.petal.data.auth.AuthResponse
import com.petal.data.auth.LoginRequest
import com.petal.data.auth.RegisterRequest
import com.petal.data.account.DeliveryAddressRequest
import com.petal.data.account.DeliveryAddressResponse
import com.petal.data.account.BuyerNotificationResponse
import com.petal.data.account.SavedDateRequest
import com.petal.data.account.SavedDateResponse
import com.petal.data.cart.AddCartItemRequest
import com.petal.data.cart.CartItemResponse
import com.petal.data.cart.CartResponse
import com.petal.data.cart.UpdateCartItemRequest
import com.petal.data.catalog.ProductResponse
import com.petal.data.checkout.CreateOrderRequest
import com.petal.data.checkout.BuyerOrderResponse
import com.petal.data.checkout.DeliverySlotAvailabilityResponse
import com.petal.data.checkout.OrderResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
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

    @GET("/api/orders")
    suspend fun orders(): Response<ApiResponse<List<BuyerOrderResponse>>>

    @GET("/api/orders/{id}")
    suspend fun order(@Path("id") id: Long): Response<ApiResponse<BuyerOrderResponse>>

    @GET("/api/addresses")
    suspend fun addresses(): Response<ApiResponse<List<DeliveryAddressResponse>>>

    @POST("/api/addresses")
    suspend fun createAddress(@Body request: DeliveryAddressRequest): Response<ApiResponse<DeliveryAddressResponse>>

    @PUT("/api/addresses/{id}")
    suspend fun updateAddress(
        @Path("id") id: Long,
        @Body request: DeliveryAddressRequest
    ): Response<ApiResponse<DeliveryAddressResponse>>

    @DELETE("/api/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: Long): Response<ApiResponse<Void>>

    @GET("/api/users/dates")
    suspend fun savedDates(): Response<ApiResponse<List<SavedDateResponse>>>

    @POST("/api/users/dates")
    suspend fun createSavedDate(@Body request: SavedDateRequest): Response<ApiResponse<SavedDateResponse>>

    @GET("/api/users/notifications")
    suspend fun notifications(@Query("unreadOnly") unreadOnly: Boolean = false): Response<ApiResponse<List<BuyerNotificationResponse>>>

    @PATCH("/api/users/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Long): Response<ApiResponse<BuyerNotificationResponse>>
}
