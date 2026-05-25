package com.petal.data.account

data class DeliveryAddressRequest(
    val label: String,
    val recipientName: String,
    val phoneNumber: String?,
    val addressLine: String,
    val defaultAddress: Boolean
)

data class DeliveryAddressResponse(
    val id: Long,
    val label: String?,
    val recipientName: String?,
    val phoneNumber: String?,
    val addressLine: String?,
    val defaultAddress: Boolean
)

data class SavedDateRequest(
    val label: String,
    val eventDate: String,
    val recurring: Boolean
)

data class SavedDateResponse(
    val id: Long,
    val label: String?,
    val eventDate: String?,
    val recurring: Boolean,
    val notifiedYear: Int?,
    val nextOccurrenceDate: String?,
    val reminderDate: String?,
    val reminderDue: Boolean,
    val reminderSentForYear: Boolean
)
