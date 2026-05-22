package com.petal.data.catalog

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CatalogFormattersTest {
    @Test
    fun formatsPhilippinePesoPricesWithTwoDecimals() {
        assertEquals("PHP 1,250.00", CatalogFormatters.formatPeso(BigDecimal("1250")))
        assertEquals("PHP 799.50", CatalogFormatters.formatPeso(BigDecimal("799.5")))
    }

    @Test
    fun exposesPetalBuyerMoodsInWebOrder() {
        assertEquals(
            listOf("romance", "apology", "celebration", "sympathy", "friendship", "just because"),
            PetalMoods.map { it.value }
        )
    }
}
