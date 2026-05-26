package com.petal.data.checkout

import com.petal.core.network.ApiService
import com.petal.data.auth.ApiResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.math.BigDecimal

class OrderRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: OrderRepository

    @Before
    fun setUp() {
        apiService = mockk()
        repository = OrderRepository(apiService)
    }

    // ────────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────────

    private fun makeOrder(id: Long = 1L) = BuyerOrderResponse(
        id = id,
        orderNumber = "PET-000$id",
        recipientName = "Rosa Dela Cruz",
        recipientAddress = "456 Bloom Ave",
        cardMessage = "With love",
        deliveryDate = "2026-06-10",
        timeSlot = "PM",
        paymentMethod = "GCASH",
        status = "PENDING",
        totalAmount = BigDecimal("1250.00"),
        itemSummary = "Hydrangea Bunch x1",
        fulfillmentImageUrl = null,
        proofImageUrl = null,
        shipping = null,
        items = emptyList()
    )

    private fun errorBody(message: String?) =
        if (message == null) {
            "".toResponseBody("application/json".toMediaType())
        } else {
            """{"success":false,"message":"$message","data":null}"""
                .toResponseBody("application/json".toMediaType())
        }

    // ────────────────────────────────────────────────────────────────
    // orders() — list endpoint
    // ────────────────────────────────────────────────────────────────

    @Test
    fun `orders returns success list when API responds HTTP 200 with success body`() = runTest {
        val orders = listOf(makeOrder(1L), makeOrder(2L))
        coEvery { apiService.orders() } returns Response.success(
            ApiResponse(success = true, message = "OK", data = orders)
        )

        val result = repository.orders()

        assertTrue(result.isSuccess)
        assertEquals(orders, result.getOrThrow())
    }

    @Test
    fun `orders returns empty list when API body has null data but success true`() = runTest {
        coEvery { apiService.orders() } returns Response.success(
            ApiResponse(success = true, message = "No orders", data = null)
        )

        val result = repository.orders()

        // success=true + null data → orEmpty() → Result.success(emptyList())
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `orders returns failure when API body has success=false`() = runTest {
        coEvery { apiService.orders() } returns Response.success(
            ApiResponse(success = false, message = "Unauthorized", data = null)
        )

        val result = repository.orders()

        assertTrue(result.isFailure)
        assertEquals("Unauthorized", result.exceptionOrNull()?.message)
    }

    @Test
    fun `orders returns 401 user-friendly message`() = runTest {
        coEvery { apiService.orders() } returns Response.error(401, errorBody(null))

        val result = repository.orders()

        assertTrue(result.isFailure)
        assertEquals("Sign in again to view your orders.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `orders returns 403 user-friendly message`() = runTest {
        coEvery { apiService.orders() } returns Response.error(403, errorBody(null))

        val result = repository.orders()

        assertTrue(result.isFailure)
        assertEquals("Buyer access is required to view orders.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `orders returns fallback message for unexpected HTTP error`() = runTest {
        coEvery { apiService.orders() } returns Response.error(500, errorBody(null))

        val result = repository.orders()

        assertTrue(result.isFailure)
        assertEquals("Unable to load orders.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `orders returns connection failure message on IOException`() = runTest {
        coEvery { apiService.orders() } throws IOException("timeout")

        val result = repository.orders()

        assertTrue(result.isFailure)
        assertEquals(
            "Connection failed. Check that the backend is running.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `orders propagates backend error message from response body JSON`() = runTest {
        coEvery { apiService.orders() } returns Response.error(
            422,
            errorBody("Cart is empty.")
        )

        val result = repository.orders()

        assertTrue(result.isFailure)
        assertEquals("Cart is empty.", result.exceptionOrNull()?.message)
    }

    // ────────────────────────────────────────────────────────────────
    // order(id) — detail endpoint
    // ────────────────────────────────────────────────────────────────

    @Test
    fun `order(id) returns success when API responds HTTP 200 with non-null data`() = runTest {
        val order = makeOrder(42L)
        coEvery { apiService.order(42L) } returns Response.success(
            ApiResponse(success = true, message = "OK", data = order)
        )

        val result = repository.order(42L)

        assertTrue(result.isSuccess)
        assertEquals(order, result.getOrThrow())
    }

    @Test
    fun `order(id) returns failure when body data is null despite success=true`() = runTest {
        coEvery { apiService.order(99L) } returns Response.success(
            ApiResponse(success = true, message = "Not found", data = null)
        )

        val result = repository.order(99L)

        // null data → condition (body?.success == true && body.data != null) fails → failure
        assertTrue(result.isFailure)
    }

    @Test
    fun `order(id) returns 404 user-friendly message`() = runTest {
        coEvery { apiService.order(7L) } returns Response.error(404, errorBody(null))

        val result = repository.order(7L)

        assertTrue(result.isFailure)
        assertEquals("Order not found.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `order(id) returns connection failure message on IOException`() = runTest {
        coEvery { apiService.order(3L) } throws IOException("no route to host")

        val result = repository.order(3L)

        assertTrue(result.isFailure)
        assertEquals(
            "Connection failed. Check that the backend is running.",
            result.exceptionOrNull()?.message
        )
    }
}
