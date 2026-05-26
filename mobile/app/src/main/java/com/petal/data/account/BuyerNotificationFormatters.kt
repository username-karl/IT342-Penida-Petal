package com.petal.data.account

object BuyerNotificationFormatters {
    private const val ImportantDateReminder = "IMPORTANT_DATE_REMINDER"

    fun isVisibleReminder(notification: BuyerNotificationResponse): Boolean {
        return notification.type == ImportantDateReminder && !notification.read
    }

    fun primaryUnreadReminder(notifications: List<BuyerNotificationResponse>): BuyerNotificationResponse? {
        return notifications
            .filter(::isVisibleReminder)
            .maxWithOrNull(compareBy<BuyerNotificationResponse> { it.createdAt.orEmpty() }.thenBy { it.id })
    }

    fun cardTitle(notification: BuyerNotificationResponse): String {
        return notification.title.ifBlank { "Forget-Me-Not reminder" }
    }

    fun cardBody(notification: BuyerNotificationResponse): String {
        return notification.message.ifBlank { "An important date is coming up. Choose flowers while there is still time." }
    }
}
