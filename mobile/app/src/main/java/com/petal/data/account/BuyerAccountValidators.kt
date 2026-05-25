package com.petal.data.account

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BuyerAccountValidators {
    fun validateAddress(label: String, recipientName: String, addressLine: String): String? {
        return when {
            label.trim().isBlank() -> "Add a label for this recipient."
            recipientName.trim().isBlank() -> "Add the recipient name."
            addressLine.trim().isBlank() -> "Add the delivery address."
            else -> null
        }
    }

    fun validateSavedDate(
        label: String,
        eventDate: String,
        recurring: Boolean = true,
        today: String = dateFormat().format(Date())
    ): String? {
        if (label.trim().isBlank()) return "Add a label for this date."
        if (eventDate.trim().isBlank()) return "Choose the event date."

        val parsedEventDate = parseDate(eventDate) ?: return "Use date format YYYY-MM-DD."
        val parsedToday = parseDate(today) ?: Date()
        if (!recurring && parsedEventDate.before(parsedToday)) {
            return "Non-recurring dates cannot be in the past."
        }

        return null
    }

    private fun parseDate(value: String): Date? {
        return try {
            dateFormat().parse(value.trim())
        } catch (_: ParseException) {
            null
        }
    }

    private fun dateFormat(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
        }
    }
}
