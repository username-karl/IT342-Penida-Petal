package com.petal.data.checkout

import java.math.BigDecimal

data class CreateOrderRequest(
    val recipientName: String,
    val recipientAddress: String,
    val cardMessage: String,
    val deliveryDate: String,
    val timeSlot: String,
    val paymentMethod: String
)

data class OrderResponse(
    val id: Long,
    val status: String,
    val deliveryDate: String?,
    val timeSlot: String?,
    val paymentMethod: String?,
    val totalAmount: BigDecimal?,
    val message: String?
)
