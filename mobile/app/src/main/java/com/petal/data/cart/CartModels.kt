package com.petal.data.cart

import java.math.BigDecimal

data class CartResponse(
    val items: List<CartItemResponse> = emptyList(),
    val subtotal: BigDecimal = BigDecimal.ZERO
)

data class CartItemResponse(
    val id: Long,
    val productId: Long,
    val floristId: Long?,
    val productName: String,
    val productImageUrl: String?,
    val floristName: String?,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal
)

data class AddCartItemRequest(
    val productId: Long,
    val quantity: Int
)

data class UpdateCartItemRequest(
    val quantity: Int
)
