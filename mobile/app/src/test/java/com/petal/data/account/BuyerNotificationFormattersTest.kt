package com.petal.data.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuyerNotificationFormattersTest {

    @Test
    fun unreadImportantDateReminderIsVisible() {
        val notification = notification(read = false)

        assertTrue(BuyerNotificationFormatters.isVisibleReminder(notification))
    }

    @Test
    fun primaryUnreadReminderUsesNewestUnreadReminder() {
        val older = notification(id = 1, createdAt = "2026-05-20T00:00:00Z")
        val newer = notification(id = 2, createdAt = "2026-05-21T00:00:00Z")

        assertEquals(newer, BuyerNotificationFormatters.primaryUnreadReminder(listOf(older, newer)))
    }

    @Test
    fun readNotificationsAreNotPrimaryReminders() {
        val read = notification(read = true)

        assertNull(BuyerNotificationFormatters.primaryUnreadReminder(listOf(read)))
    }

    @Test
    fun cardCopyUsesNotificationMessage() {
        val notification = notification()

        assertEquals("Mom's Birthday is coming up", BuyerNotificationFormatters.cardTitle(notification))
        assertEquals(
            "Mom's Birthday is on May 23. Choose flowers now so the gift feels thoughtful, not rushed.",
            BuyerNotificationFormatters.cardBody(notification)
        )
    }

    private fun notification(
        id: Long = 22,
        read: Boolean = false,
        createdAt: String = "2026-05-20T00:00:00Z"
    ) = BuyerNotificationResponse(
        id = id,
        type = "IMPORTANT_DATE_REMINDER",
        title = "Mom's Birthday is coming up",
        message = "Mom's Birthday is on May 23. Choose flowers now so the gift feels thoughtful, not rushed.",
        savedDateId = 8,
        eventDate = "2026-05-23",
        notificationYear = 2026,
        read = read,
        createdAt = createdAt,
        readAt = if (read) "2026-05-20T01:00:00Z" else null
    )
}
