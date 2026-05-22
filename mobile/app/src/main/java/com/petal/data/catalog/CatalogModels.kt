package com.petal.data.catalog

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val moodTags: List<String> = emptyList(),
    val imageUrl: String,
    val floristId: Long?,
    val floristName: String?,
    val floristLogoUrl: String?,
    val floristBio: String?,
    val inStock: Boolean
)

data class MoodOption(
    val label: String,
    val value: String,
    val note: String
)

val PetalMoods = listOf(
    MoodOption("Romance", "romance", "Soft gestures, anniversaries, and quiet devotion."),
    MoodOption("Apology", "apology", "Gentle arrangements for making things right."),
    MoodOption("Celebration", "celebration", "Bright pieces for milestones and big days."),
    MoodOption("Sympathy", "sympathy", "Calm, respectful florals for tender moments."),
    MoodOption("Friendship", "friendship", "Warm gifts for the people who stay close."),
    MoodOption("Just Because", "just because", "Everyday flowers with no occasion required.")
)

object CatalogFormatters {
    private val pesoFormat = NumberFormat.getNumberInstance(Locale("en", "PH")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun formatPeso(value: BigDecimal): String = "PHP ${pesoFormat.format(value)}"
}
