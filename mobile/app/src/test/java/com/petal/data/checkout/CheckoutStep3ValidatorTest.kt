package com.petal.data.checkout

import com.petal.data.cart.CartItemResponse
import com.petal.data.cart.CartResponse
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutStep3ValidatorTest {
    private val today = 20_000L

    @Test
    fun disablesConfirmWhenSelectedSlotIsUnavailable() {
        val availability = DeliverySlotAvailabilityResponse(
            date = "2026-05-24",
            am = DeliverySlotResponse(available = false, remaining = 0),
            pm = DeliverySlotResponse(available = true, remaining = 2)
        )

        val canConfirm = CheckoutStep3Validator.canConfirm(
            deliveryEpochDay = today,
            timeSlot = "AM",
            availability = availability,
            cartHasItems = true,
            recipientName = "Maya",
            recipientAddress = "123 Rose Lane",
            todayEpochDay = today
        )

        assertFalse(canConfirm)
    }

    @Test
    fun enablesConfirmWhenDateSlotCartAndRecipientAreValid() {
        val availability = DeliverySlotAvailabilityResponse(
            date = "2026-05-24",
            am = DeliverySlotResponse(available = true, remaining = 3),
            pm = DeliverySlotResponse(available = false, remaining = 0)
        )

        val canConfirm = CheckoutStep3Validator.canConfirm(
            deliveryEpochDay = today,
            timeSlot = "AM",
            availability = availability,
            cartHasItems = true,
            recipientName = "Maya",
            recipientAddress = "123 Rose Lane",
            todayEpochDay = today
        )

        assertTrue(canConfirm)
    }

    @Test
    fun resolvesSingleFloristFromCart() {
        val cart = CartResponse(items = listOf(cartItem(9L), cartItem(9L)))

        assertEquals(9L, CheckoutStep3Validator.singleFloristId(cart).getOrThrow())
    }

    @Test
    fun rejectsMultipleFloristsInCart() {
        val cart = CartResponse(items = listOf(cartItem(9L), cartItem(10L)))

        val error = CheckoutStep3Validator.singleFloristId(cart).exceptionOrNull()?.message

        assertEquals("Checkout supports one florist per order.", error)
    }

    @Test
    fun buildsOrderRequestForSameFloristMultiItemCart() {
        val cart = CartResponse(items = listOf(cartItem(9L), cartItem(9L)))

        val request = CheckoutStep3Validator.createOrderRequest(
            cart = cart,
            recipientName = " Maya ",
            recipientAddress = " 123 Rose Lane ",
            cardMessage = "",
            deliveryDate = "2026-05-24",
            timeSlot = "AM",
            paymentMethod = "CARD"
        ).getOrThrow()

        assertEquals("Maya", request.recipientName)
        assertEquals("123 Rose Lane", request.recipientAddress)
        assertEquals("", request.cardMessage)
        assertEquals("2026-05-24", request.deliveryDate)
        assertEquals("AM", request.timeSlot)
        assertEquals("CARD", request.paymentMethod)
    }

    @Test
    fun blocksOrderRequestForMixedFloristCart() {
        val cart = CartResponse(items = listOf(cartItem(9L), cartItem(10L)))

        val error = CheckoutStep3Validator.createOrderRequest(
            cart = cart,
            recipientName = "Maya",
            recipientAddress = "123 Rose Lane",
            cardMessage = "Happy birthday",
            deliveryDate = "2026-05-24",
            timeSlot = "AM",
            paymentMethod = "CARD"
        ).exceptionOrNull()?.message

        assertEquals("Checkout supports one florist per order.", error)
    }

    private fun cartItem(floristId: Long) = CartItemResponse(
        id = floristId,
        productId = floristId * 10,
        floristId = floristId,
        productName = "Bouquet",
        productImageUrl = null,
        floristName = "Studio",
        unitPrice = BigDecimal.TEN,
        quantity = 1,
        lineTotal = BigDecimal.TEN
    )
}
