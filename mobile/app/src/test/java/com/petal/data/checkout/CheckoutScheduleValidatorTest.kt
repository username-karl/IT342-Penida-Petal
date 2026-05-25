package com.petal.data.checkout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckoutScheduleValidatorTest {
    private val today = 20_000L

    @Test
    fun rejectsPastDeliveryDate() {
        val error = CheckoutScheduleValidator.validate(
            deliveryEpochDay = today - 1,
            timeSlot = "AM",
            todayEpochDay = today
        )

        assertEquals("Choose today or a future delivery date.", error)
    }

    @Test
    fun acceptsTodayDeliveryDate() {
        val error = CheckoutScheduleValidator.validate(
            deliveryEpochDay = today,
            timeSlot = "AM",
            todayEpochDay = today
        )

        assertNull(error)
    }

    @Test
    fun acceptsFutureDeliveryDate() {
        val error = CheckoutScheduleValidator.validate(
            deliveryEpochDay = today + 1,
            timeSlot = "PM",
            todayEpochDay = today
        )

        assertNull(error)
    }

    @Test
    fun rejectsMissingTimeSlot() {
        val error = CheckoutScheduleValidator.validate(
            deliveryEpochDay = today,
            timeSlot = "",
            todayEpochDay = today
        )

        assertEquals("Choose a delivery time slot.", error)
    }

    @Test
    fun acceptsValidDateAndSlot() {
        val error = CheckoutScheduleValidator.validate(
            deliveryEpochDay = today + 2,
            timeSlot = "AM",
            todayEpochDay = today
        )

        assertNull(error)
    }
}
