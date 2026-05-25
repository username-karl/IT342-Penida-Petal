package com.petal.data.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BuyerAccountValidatorsTest {
    @Test
    fun addressRequiresLabelRecipientAndAddressLine() {
        val error = BuyerAccountValidators.validateAddress(
            label = " ",
            recipientName = "",
            addressLine = "   "
        )

        assertEquals("Add a label for this recipient.", error)
    }

    @Test
    fun addressAcceptsRequiredFields() {
        val error = BuyerAccountValidators.validateAddress(
            label = "Mom",
            recipientName = "Mika Santos",
            addressLine = "Cebu Business Park"
        )

        assertNull(error)
    }

    @Test
    fun savedDateRequiresLabelAndEventDate() {
        val error = BuyerAccountValidators.validateSavedDate(
            label = "",
            eventDate = ""
        )

        assertEquals("Add a label for this date.", error)
    }

    @Test
    fun recurringSavedDateAllowsPastEventDate() {
        val error = BuyerAccountValidators.validateSavedDate(
            label = "Mom's Birthday",
            eventDate = "2020-02-14",
            recurring = true,
            today = "2026-05-25"
        )

        assertNull(error)
    }

    @Test
    fun nonRecurringSavedDateRejectsPastEventDate() {
        val error = BuyerAccountValidators.validateSavedDate(
            label = "Graduation",
            eventDate = "2020-02-14",
            recurring = false,
            today = "2026-05-25"
        )

        assertEquals("Non-recurring dates cannot be in the past.", error)
    }
}
