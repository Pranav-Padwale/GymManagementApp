package com.gym.model.activity

/**
 * Domain model for calorie metrics matching the design mockup.
 */
data class CalorieData(
    val totalTargetCalories: Double = 2557.5,
    val caloriesBurned: Double = 0.0,
    val caloriesEaten: Double = 0.0
) {
    val caloriesLeft: Double
        get() = (totalTargetCalories - caloriesEaten + caloriesBurned).coerceAtLeast(0.0)
}
