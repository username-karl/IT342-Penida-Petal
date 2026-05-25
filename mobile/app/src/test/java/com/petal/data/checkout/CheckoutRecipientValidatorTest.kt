package com.petal.data.checkout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckoutRecipientValidatorTest {
    @Test
    fun returnsNameErrorWhenRecipientNameIsBlank() {
        val error = CheckoutRecipientValidator.validate("", "123 Rose Street")

        assertEquals("Enter the recipient name.", error)
    }

    @Test
    fun returnsAddressErrorWhenRecipientAddressIsBlank() {
        val error = CheckoutRecipientValidator.validate("Mika Santos", " ")

        assertEquals("Enter the recipient address.", error)
    }

    @Test
    fun returnsNoErrorWhenRecipientDetailsArePresent() {
        val error = CheckoutRecipientValidator.validate("Mika Santos", "123 Rose Street")

        assertNull(error)
    }
}
