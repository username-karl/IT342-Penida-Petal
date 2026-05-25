package com.petal.data.checkout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderTrackingTimelineTest {
    @Test
    fun mapsLiveBackendStatusesToCurrentTimelineStep() {
        assertCurrent("PENDING", "PENDING")
        assertCurrent("ACCEPTED", "ACCEPTED")
        assertCurrent("ARRANGING", "ARRANGING")
        assertCurrent("READY_FOR_PICKUP", "READY_FOR_PICKUP")
        assertCurrent("OUT_FOR_DELIVERY", "OUT_FOR_DELIVERY")
        assertCurrent("DELIVERED", "DELIVERED")
    }

    @Test
    fun treatsBackendAliasesAsTimelineStatuses() {
        assertCurrent("PREPARING", "ARRANGING")
        assertCurrent("SHIPPED", "OUT_FOR_DELIVERY")
        assertCurrent("COMPLETED", "DELIVERED")
    }

    @Test
    fun deliveredStatusCompletesEarlierStepsOnly() {
        val timeline = OrderTrackingTimeline.build("DELIVERED")

        assertEquals(OrderTrackingStepState.COMPLETE, timeline.steps.first { it.status == "PENDING" }.state)
        assertEquals(OrderTrackingStepState.CURRENT, timeline.steps.first { it.status == "DELIVERED" }.state)
        assertFalse(timeline.isCancelled)
    }

    @Test
    fun cancelledStatusDoesNotCompleteDelivery() {
        val timeline = OrderTrackingTimeline.build("CANCELLED")

        assertTrue(timeline.isCancelled)
        assertEquals(OrderTrackingStepState.CANCELLED, timeline.steps.first { it.status == "PENDING" }.state)
        assertEquals(OrderTrackingStepState.FUTURE, timeline.steps.first { it.status == "DELIVERED" }.state)
    }

    @Test
    fun attachesMatchingBackendEventToTimelineStep() {
        val timeline = OrderTrackingTimeline.build(
            status = "OUT_FOR_DELIVERY",
            events = listOf(
                TrackingEventResponse(
                    id = 7L,
                    status = "Out for delivery",
                    description = "Rider has the bouquet.",
                    timestamp = "2026-05-25T09:30:00"
                )
            )
        )

        val step = timeline.steps.first { it.status == "OUT_FOR_DELIVERY" }
        assertEquals("Rider has the bouquet.", step.eventDescription)
        assertEquals("2026-05-25T09:30:00", step.eventTimestamp)
    }

    private fun assertCurrent(rawStatus: String, normalizedStatus: String) {
        val timeline = OrderTrackingTimeline.build(rawStatus)
        assertEquals(OrderTrackingStepState.CURRENT, timeline.steps.first { it.status == normalizedStatus }.state)
    }
}
