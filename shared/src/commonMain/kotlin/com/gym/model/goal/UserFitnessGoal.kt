package com.gym.model.goal

/**
 * Domain model for User Fitness Goal inputs and calculated output results.
 */
data class UserFitnessGoal(
    val gender: Gender = Gender.MALE,
    val age: Double = 26.0,
    val heightCm: Double = 172.0,
    val weightKg: Double = 69.0,
    val goalWeightKg: Double = 70.0,
    val weightGoal: WeightGoal = WeightGoal.GAIN,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY,

    // Calculated fields
    val bmi: Double = 23.3,
    val idealWeightMinKg: Double = 55.0,
    val idealWeightMaxKg: Double = 74.0,
    val calculatedCalories: Double = 2557.5,
    val calculatedProteinGrams: Double = 159.84,
    val calculatedCarbsGrams: Double = 383.62,
    val calculatedFatGrams: Double = 42.62,
    val lastUpdatedIso: String = "2026-07-25"
)
