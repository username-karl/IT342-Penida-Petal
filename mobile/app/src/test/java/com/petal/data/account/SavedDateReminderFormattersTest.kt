package com.petal.data.account

import org.junit.Assert.assertEquals
import org.junit.Test

class SavedDateReminderFormattersTest {
    @Test
    fun dueReminderShowsDueToday() {
        val savedDate = savedDate(reminderDue = true, reminderDate = "2026-05-20")

        assertEquals("Reminder due today", SavedDateReminderFormatters.statusText(savedDate))
    }

    @Test
    fun sentReminderShowsLoggedYear() {
        val savedDate = savedDate(notifiedYear = 2026, reminderSentForYear = true)

        assertEquals("Reminder logged for 2026", SavedDateReminderFormatters.statusText(savedDate))
    }

    @Test
    fun scheduledReminderShowsReminderDate() {
        val savedDate = savedDate(reminderDate = "2026-05-22")

        assertEquals("Reminder scheduled for 2026-05-22", SavedDateReminderFormatters.statusText(savedDate))
    }

    @Test
    fun missingReminderDateUsesFallback() {
        val savedDate = savedDate()

        assertEquals("Reminder scheduled", SavedDateReminderFormatters.statusText(savedDate))
    }

    private fun savedDate(
        notifiedYear: Int? = null,
        reminderDate: String? = null,
        reminderDue: Boolean = false,
        reminderSentForYear: Boolean = false
    ) = SavedDateResponse(
        id = 8L,
        label = "Mom's Birthday",
        eventDate = "1990-02-14",
        recurring = true,
        notifiedYear = notifiedYear,
        nextOccurrenceDate = null,
        reminderDate = reminderDate,
        reminderDue = reminderDue,
        reminderSentForYear = reminderSentForYear
    )
}
