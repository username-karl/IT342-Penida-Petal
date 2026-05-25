package com.petal.data.checkout

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

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

data class BuyerOrderResponse(
    val id: Long,
    val orderNumber: String?,
    val recipientName: String?,
    val recipientAddress: String?,
    val cardMessage: String?,
    val deliveryDate: String?,
    val timeSlot: String?,
    val paymentMethod: String?,
    val status: String?,
    val totalAmount: BigDecimal?,
    val itemSummary: String?,
    val fulfillmentImageUrl: String?,
    val proofImageUrl: String?,
    val shipping: ShippingInfoResponse?,
    val items: List<BuyerOrderItemResponse> = emptyList()
)

data class BuyerOrderItemResponse(
    val productId: Long?,
    val productName: String?,
    val imageUrl: String?,
    val floristName: String?,
    val quantity: Int,
    val unitPrice: BigDecimal?,
    val lineTotal: BigDecimal?
)

data class ShippingInfoResponse(
    val courierName: String?,
    val trackingNumber: String?,
    val estimatedDeliveryDate: String?,
    val latestStatus: String?,
    val fulfillmentImageUrl: String?,
    val proofImageUrl: String?,
    val events: List<TrackingEventResponse> = emptyList()
)

data class TrackingEventResponse(
    val id: Long?,
    val status: String?,
    val description: String?,
    val timestamp: String?
)

object OrderDisplayFormatters {
    private val pesoFormat = NumberFormat.getNumberInstance(Locale("en", "PH")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun statusLabel(status: String?): String {
        return when (status?.trim()?.uppercase(Locale.US)) {
            "PENDING" -> "Pending"
            "ACCEPTED" -> "Accepted"
            "ARRANGING", "PREPARING" -> "Arranging"
            "READY_FOR_PICKUP" -> "Ready for pickup"
            "OUT_FOR_DELIVERY", "SHIPPED" -> "Out for delivery"
            "DELIVERED", "COMPLETED" -> "Delivered"
            "CANCELLED" -> "Cancelled"
            null, "" -> "Pending"
            else -> status.trim().replace('_', ' ').lowercase(Locale.US)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        }
    }

    fun formatOptionalPeso(value: BigDecimal?): String {
        return value?.let { "PHP ${pesoFormat.format(it)}" } ?: "Not provided"
    }

    fun orFallback(value: String?, fallback: String = "Not provided"): String {
        return value?.trim()?.takeIf { it.isNotBlank() } ?: fallback
    }
}
