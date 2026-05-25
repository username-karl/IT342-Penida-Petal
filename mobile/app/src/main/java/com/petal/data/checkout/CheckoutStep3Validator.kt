package com.petal.data.checkout

import com.petal.data.cart.CartResponse

object CheckoutStep3Validator {
    fun canConfirm(
        deliveryEpochDay: Long?,
        timeSlot: String,
        availability: DeliverySlotAvailabilityResponse?,
        cartHasItems: Boolean,
        recipientName: String,
        recipientAddress: String,
        todayEpochDay: Long = CheckoutScheduleValidator.currentEpochDay()
    ): Boolean {
        if (CheckoutScheduleValidator.validate(deliveryEpochDay, timeSlot, todayEpochDay) != null) {
            return false
        }
        if (!cartHasItems || recipientName.isBlank() || recipientAddress.isBlank()) {
            return false
        }
        return availability != null && selectedSlot(availability, timeSlot)?.available != false
    }

    fun singleFloristId(cart: CartResponse): Result<Long> {
        if (cart.items.isEmpty()) {
            return Result.failure(IllegalStateException("Add flowers before scheduling delivery."))
        }

        val floristIds = cart.items.map { it.floristId }.distinct()
        if (floristIds.any { it == null }) {
            return Result.failure(IllegalStateException("Cart items are missing florist details."))
        }
        if (floristIds.size > 1) {
            return Result.failure(IllegalStateException("Checkout supports one florist per order."))
        }

        return Result.success(requireNotNull(floristIds.first()))
    }

    fun selectedSlot(availability: DeliverySlotAvailabilityResponse?, timeSlot: String): DeliverySlotResponse? {
        return when (timeSlot) {
            CheckoutScheduleValidator.morningSlot -> availability?.am
            CheckoutScheduleValidator.afternoonSlot -> availability?.pm
            else -> null
        }
    }

    fun createOrderRequest(
        cart: CartResponse,
        recipientName: String,
        recipientAddress: String,
        cardMessage: String,
        deliveryDate: String,
        timeSlot: String,
        paymentMethod: String
    ): Result<CreateOrderRequest> {
        return singleFloristId(cart).map {
            CreateOrderRequest(
                recipientName = recipientName.trim(),
                recipientAddress = recipientAddress.trim(),
                cardMessage = cardMessage.trim(),
                deliveryDate = deliveryDate,
                timeSlot = timeSlot,
                paymentMethod = paymentMethod
            )
        }
    }
}
