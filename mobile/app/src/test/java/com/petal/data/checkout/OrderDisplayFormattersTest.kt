package com.petal.data.checkout

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderDisplayFormattersTest {
    @Test
    fun mapsBackendStatusesToBuyerLabels() {
        assertEquals("Pending", OrderDisplayFormatters.statusLabel("PENDING"))
        assertEquals("Arranging", OrderDisplayFormatters.statusLabel("ARRANGING"))
        assertEquals("Delivered", OrderDisplayFormatters.statusLabel("DELIVERED"))
        assertEquals("Ready for pickup", OrderDisplayFormatters.statusLabel("READY_FOR_PICKUP"))
        assertEquals("Out for delivery", OrderDisplayFormatters.statusLabel("OUT_FOR_DELIVERY"))
    }

    @Test
    fun treatsBackendStatusAliasesAsBuyerLabels() {
        assertEquals("Arranging", OrderDisplayFormatters.statusLabel("PREPARING"))
        assertEquals("Out for delivery", OrderDisplayFormatters.statusLabel("SHIPPED"))
        assertEquals("Delivered", OrderDisplayFormatters.statusLabel("COMPLETED"))
    }

    @Test
    fun formatsOptionalOrderValues() {
        assertEquals("PHP 1,250.50", OrderDisplayFormatters.formatOptionalPeso(BigDecimal("1250.5")))
        assertEquals("Not provided", OrderDisplayFormatters.orFallback(null))
        assertEquals("AM", OrderDisplayFormatters.orFallback(" AM "))
    }

    @Test
    fun nullStatusFallsBackToPending() {
        assertEquals("Pending", OrderDisplayFormatters.statusLabel(null))
    }

    @Test
    fun emptyStatusFallsBackToPending() {
        assertEquals("Pending", OrderDisplayFormatters.statusLabel(""))
        assertEquals("Pending", OrderDisplayFormatters.statusLabel("   "))
    }

    @Test
    fun nullPesoValueFallsBackToNotProvided() {
        assertEquals("Not provided", OrderDisplayFormatters.formatOptionalPeso(null))
    }
}
