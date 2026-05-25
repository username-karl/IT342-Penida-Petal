package com.petal.data.checkout

data class DeliverySlotAvailabilityResponse(
    val date: String?,
    val am: DeliverySlotResponse?,
    val pm: DeliverySlotResponse?
)

data class DeliverySlotResponse(
    val available: Boolean,
    val remaining: Int
)
