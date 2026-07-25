package com.gym.model.activity

/**
 * Domain model for macro-nutrient target vs consumed details.
 */
data class NutritionMacro(
    val name: String,             // "Carbs", "Protein", "Fat"
    val percentage: Double,       // e.g. 60.0, 25.0, 15.0
    val totalGrams: Double,       // e.g. 383.62, 159.84, 42.62
    val consumedGrams: Double = 0.0,
    val hexColor: String,          // "#FF5722", "#2196F3", "#8BC34A"
    val emojiIcon: String          // "🥖", "🥩", "🥑"
) {
    val remainingGrams: Double
        get() = (totalGrams - consumedGrams).coerceAtLeast(0.0)
}
