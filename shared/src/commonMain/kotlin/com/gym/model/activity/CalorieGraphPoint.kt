package com.gym.model.activity

/**
 * Historical graph point for Calorie Consume/Burn chart.
 */
data class CalorieGraphPoint(
    val dateLabel: String,   // e.g. "25-07-26"
    val caloriesTotal: Double,
    val caloriesEaten: Double,
    val caloriesBurned: Double
)
