package com.petal.data.checkout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckoutCardMessageValidatorTest {
    @Test
    fun allowsEmptyCardMessage() {
        val error = CheckoutCardMessageValidator.validate("")

        assertNull(error)
    }

    @Test
    fun allowsCardMessageAtMaximumLength() {
        val error = CheckoutCardMessageValidator.validate("x".repeat(200))

        assertNull(error)
    }

    @Test
    fun returnsErrorWhenCardMessageExceedsMaximumLength() {
        val error = CheckoutCardMessageValidator.validate("x".repeat(201))

        assertEquals("Card message must be 200 characters or fewer.", error)
    }
}
