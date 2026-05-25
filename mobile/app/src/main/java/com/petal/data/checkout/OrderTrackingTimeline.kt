package com.petal.data.checkout

import java.util.Locale

enum class OrderTrackingStepState {
    COMPLETE,
    CURRENT,
    FUTURE,
    CANCELLED
}

data class OrderTrackingStep(
    val status: String,
    val label: String,
    val description: String,
    val state: OrderTrackingStepState,
    val eventDescription: String? = null,
    val eventTimestamp: String? = null
)

data class OrderTrackingTimelineState(
    val steps: List<OrderTrackingStep>,
    val isCancelled: Boolean,
    val hasKnownStatus: Boolean
)

object OrderTrackingTimeline {
    private val workflow = listOf(
        StepDefinition(
            status = "PENDING",
            label = "Order placed",
            description = "Your order has been received and is waiting for florist confirmation.",
            aliases = setOf("PENDING")
        ),
        StepDefinition(
            status = "ACCEPTED",
            label = "Florist accepted",
            description = "The florist has accepted your order.",
            aliases = setOf("ACCEPTED")
        ),
        StepDefinition(
            status = "ARRANGING",
            label = "Arranging bouquet",
            description = "The florist is preparing your bouquet.",
            aliases = setOf("ARRANGING", "PREPARING")
        ),
        StepDefinition(
            status = "READY_FOR_PICKUP",
            label = "Ready for pickup",
            description = "Your bouquet is ready for courier pickup.",
            aliases = setOf("READY_FOR_PICKUP")
        ),
        StepDefinition(
            status = "OUT_FOR_DELIVERY",
            label = "Out for delivery",
            description = "Your bouquet is on the way to the recipient.",
            aliases = setOf("OUT_FOR_DELIVERY", "SHIPPED")
        ),
        StepDefinition(
            status = "DELIVERED",
            label = "Delivered",
            description = "Your bouquet has been successfully delivered to the recipient.",
            aliases = setOf("DELIVERED", "COMPLETED")
        )
    )

    fun build(status: String?, events: List<TrackingEventResponse> = emptyList()): OrderTrackingTimelineState {
        val normalized = normalize(status)
        val isCancelled = normalized == "CANCELLED"
        val activeIndex = workflow.indexOfFirst { normalized in it.aliases }
        val hasKnownStatus = activeIndex >= 0 || isCancelled

        val steps = workflow.mapIndexed { index, definition ->
            val event = events.firstOrNull { event ->
                normalize(event.status) in definition.aliases
            }
            val state = when {
                isCancelled && index == 0 -> OrderTrackingStepState.CANCELLED
                isCancelled -> OrderTrackingStepState.FUTURE
                activeIndex < 0 -> OrderTrackingStepState.FUTURE
                index < activeIndex -> OrderTrackingStepState.COMPLETE
                index == activeIndex -> OrderTrackingStepState.CURRENT
                else -> OrderTrackingStepState.FUTURE
            }
            OrderTrackingStep(
                status = definition.status,
                label = definition.label,
                description = definition.description,
                state = state,
                eventDescription = event?.description?.trim()?.takeIf { it.isNotBlank() },
                eventTimestamp = event?.timestamp?.trim()?.takeIf { it.isNotBlank() }
            )
        }

        return OrderTrackingTimelineState(
            steps = steps,
            isCancelled = isCancelled,
            hasKnownStatus = hasKnownStatus
        )
    }

    fun isDelivered(status: String?): Boolean = normalize(status) == "DELIVERED"

    private fun normalize(status: String?): String {
        val normalized = status
            ?.trim()
            ?.uppercase(Locale.US)
            ?.replace(' ', '_')
            .orEmpty()

        return when (normalized) {
            "PREPARING" -> "ARRANGING"
            "SHIPPED" -> "OUT_FOR_DELIVERY"
            "COMPLETED" -> "DELIVERED"
            "" -> "PENDING"
            else -> normalized
        }
    }

    private data class StepDefinition(
        val status: String,
        val label: String,
        val description: String,
        val aliases: Set<String>
    )
}
