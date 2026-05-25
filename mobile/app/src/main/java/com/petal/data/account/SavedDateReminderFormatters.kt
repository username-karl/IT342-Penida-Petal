package com.petal.data.account

object SavedDateReminderFormatters {
    fun statusText(savedDate: SavedDateResponse): String {
        return when {
            savedDate.reminderDue -> "Reminder due today"
            savedDate.reminderSentForYear && savedDate.notifiedYear != null ->
                "Reminder logged for ${savedDate.notifiedYear}"
            !savedDate.reminderDate.isNullOrBlank() ->
                "Reminder scheduled for ${savedDate.reminderDate}"
            else -> "Reminder scheduled"
        }
    }
}
