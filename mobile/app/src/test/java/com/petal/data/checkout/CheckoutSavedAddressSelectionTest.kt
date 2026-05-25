package com.petal.data.checkout

import com.petal.data.account.DeliveryAddressResponse
import com.petal.ui.CheckoutViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckoutSavedAddressSelectionTest {
    @Test
    fun selectingSavedAddressPopulatesRecipientFieldsOnly() {
        val viewModel = CheckoutViewModel()

        viewModel.selectSavedAddress(
            DeliveryAddressResponse(
                id = 7L,
                label = "Mom",
                recipientName = "Mika Santos",
                phoneNumber = "09171234567",
                addressLine = "Cebu Business Park, Cebu City",
                defaultAddress = true
            )
        )

        val state = viewModel.state.value
        assertEquals(7L, state.selectedAddressId)
        assertEquals("Mika Santos", state.recipientName)
        assertEquals("Cebu Business Park, Cebu City", state.recipientAddress)
        assertEquals("", state.cardMessage)
        assertEquals("", state.deliveryDateLabel)
        assertEquals("", state.timeSlot)
    }

    @Test
    fun manualRecipientEditClearsSavedAddressSelection() {
        val viewModel = CheckoutViewModel()
        viewModel.selectSavedAddress(
            DeliveryAddressResponse(
                id = 7L,
                label = "Mom",
                recipientName = "Mika Santos",
                phoneNumber = null,
                addressLine = "Cebu Business Park, Cebu City",
                defaultAddress = false
            )
        )

        viewModel.setRecipientName("Manual Recipient")

        val state = viewModel.state.value
        assertEquals(null, state.selectedAddressId)
        assertEquals("Manual Recipient", state.recipientName)
        assertEquals("Cebu Business Park, Cebu City", state.recipientAddress)
    }
}
