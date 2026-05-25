package com.petal.data.checkout

import java.util.Calendar

object CheckoutScheduleValidator {
    const val morningSlot = "AM"
    const val afternoonSlot = "PM"
    private const val millisPerDay = 86_400_000L

    fun validate(deliveryEpochDay: Long?, timeSlot: String, todayEpochDay: Long = currentEpochDay()): String? {
        return when {
            deliveryEpochDay == null -> "Choose a delivery date."
            deliveryEpochDay < todayEpochDay -> "Choose today or a future delivery date."
            timeSlot.isBlank() -> "Choose a delivery time slot."
            timeSlot !in setOf(morningSlot, afternoonSlot) -> "Choose a delivery time slot."
            else -> null
        }
    }

    fun currentEpochDay(): Long = epochDay(Calendar.getInstance())

    fun epochDay(calendar: Calendar): Long {
        val normalized = calendar.clone() as Calendar
        normalized.set(Calendar.HOUR_OF_DAY, 0)
        normalized.set(Calendar.MINUTE, 0)
        normalized.set(Calendar.SECOND, 0)
        normalized.set(Calendar.MILLISECOND, 0)
        return normalized.timeInMillis / millisPerDay
    }
}
