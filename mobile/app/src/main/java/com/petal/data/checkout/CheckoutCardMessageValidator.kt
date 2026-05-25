package com.petal.data.checkout

object CheckoutCardMessageValidator {
    const val maxLength = 200

    fun validate(message: String): String? {
        return if (message.length > maxLength) {
            "Card message must be 200 characters or fewer."
        } else {
            null
        }
    }
}
