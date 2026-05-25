package com.petal.data.checkout

object CheckoutRecipientValidator {
    fun validate(recipientName: String, recipientAddress: String): String? {
        return when {
            recipientName.trim().isBlank() -> "Enter the recipient name."
            recipientAddress.trim().isBlank() -> "Enter the recipient address."
            else -> null
        }
    }
}
