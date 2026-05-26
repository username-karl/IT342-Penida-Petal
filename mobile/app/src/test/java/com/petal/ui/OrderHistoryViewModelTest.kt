package com.petal.ui

import app.cash.turbine.test
import com.petal.data.checkout.BuyerOrderResponse
import com.petal.data.checkout.OrderRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class OrderHistoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: OrderRepository
    private lateinit var viewModel: OrderHistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = OrderHistoryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ────────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────────

    private fun makeOrder(id: Long, status: String = "PENDING") = BuyerOrderResponse(
        id = id,
        orderNumber = "PET-000$id",
        recipientName = "Test Recipient",
        recipientAddress = "123 Petal St",
        cardMessage = null,
        deliveryDate = "2026-06-01",
        timeSlot = "AM",
        paymentMethod = "CARD",
        status = status,
        totalAmount = BigDecimal("500.00"),
        itemSummary = "Rose Bouquet x1",
        fulfillmentImageUrl = null,
        proofImageUrl = null,
        shipping = null,
        items = emptyList()
    )

    // ────────────────────────────────────────────────────────────────
    // Initial state
    // ────────────────────────────────────────────────────────────────

    @Test
    fun `initial state is idle with empty orders and no error`() {
        val state = viewModel.state.value
        assertFalse(state.loading)
        assertFalse(state.detailLoading)
        assertNull(state.error)
        assertNull(state.detailError)
        assertTrue(state.orders.isEmpty())
        assertNull(state.selectedOrder)
    }

    // ────────────────────────────────────────────────────────────────
    // loadOrders
    // ────────────────────────────────────────────────────────────────

    @Test
    fun `loadOrders success populates orders list and clears loading`() = runTest {
        val orders = listOf(makeOrder(1L), makeOrder(2L))
        coEvery { repository.orders() } returns Result.success(orders)

        viewModel.loadOrders()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertEquals(orders, state.orders)
        assertNull(state.error)
    }

    @Test
    fun `loadOrders failure sets error message and clears loading`() = runTest {
        coEvery { repository.orders() } returns Result.failure(
            IllegalStateException("Connection failed. Check that the backend is running.")
        )

        viewModel.loadOrders()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertTrue(state.orders.isEmpty())
        assertEquals("Connection failed. Check that the backend is running.", state.error)
    }

    @Test
    fun `loadOrders failure with null message falls back to default error text`() = runTest {
        // IllegalStateException with null message
        coEvery { repository.orders() } returns Result.failure(
            IllegalStateException(null as String?)
        )

        viewModel.loadOrders()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertEquals("Unable to load orders.", state.error)
    }

    @Test
    fun `loadOrders clears previous error on retry`() = runTest {
        coEvery { repository.orders() } returns Result.failure(IllegalStateException("Network error"))
        viewModel.loadOrders()
        assertEquals("Network error", viewModel.state.value.error)

        val orders = listOf(makeOrder(1L))
        coEvery { repository.orders() } returns Result.success(orders)
        viewModel.loadOrders()

        val state = viewModel.state.value
        assertNull(state.error)
        assertEquals(orders, state.orders)
    }

    // ────────────────────────────────────────────────────────────────
    // loadOrder (detail)
    // ────────────────────────────────────────────────────────────────

    @Test
    fun `loadOrder success updates selectedOrder and clears detailLoading`() = runTest {
        val order = makeOrder(42L, "ARRANGING")
        coEvery { repository.order(42L) } returns Result.success(order)

        viewModel.loadOrder(42L)

        val state = viewModel.state.value
        assertFalse(state.detailLoading)
        assertEquals(order, state.selectedOrder)
        assertNull(state.detailError)
    }

    @Test
    fun `loadOrder pre-fills selectedOrder from cached list while detail loads`() = runTest {
        val cached = makeOrder(10L)
        // Pre-populate list cache
        coEvery { repository.orders() } returns Result.success(listOf(cached))
        viewModel.loadOrders()

        // Make detail call suspend long enough to observe pre-fill
        var capturedPreFill: BuyerOrderResponse? = null
        coEvery { repository.order(10L) } coAnswers {
            // Capture state while still loading
            capturedPreFill = viewModel.state.value.selectedOrder
            Result.success(cached)
        }

        viewModel.loadOrder(10L)

        // Pre-fill was populated from cache before the API call returned
        assertNotNull(capturedPreFill)
        assertEquals(cached.id, capturedPreFill!!.id)
    }

    @Test
    fun `loadOrder failure sets detailError and clears detailLoading`() = runTest {
        coEvery { repository.order(99L) } returns Result.failure(
            IllegalStateException("Order not found.")
        )

        viewModel.loadOrder(99L)

        val state = viewModel.state.value
        assertFalse(state.detailLoading)
        assertEquals("Order not found.", state.detailError)
    }

    @Test
    fun `loadOrder failure with null message falls back to default error text`() = runTest {
        coEvery { repository.order(5L) } returns Result.failure(
            IllegalStateException(null as String?)
        )

        viewModel.loadOrder(5L)

        val state = viewModel.state.value
        assertEquals("Unable to load order.", state.detailError)
    }

    @Test
    fun `loadOrder clears detailLoading flag after success`() = runTest {
        val order = makeOrder(1L)
        coEvery { repository.order(1L) } returns Result.success(order)

        viewModel.loadOrder(1L)

        val state = viewModel.state.value
        assertFalse(state.detailLoading)
        assertEquals(order, state.selectedOrder)
    }
}
