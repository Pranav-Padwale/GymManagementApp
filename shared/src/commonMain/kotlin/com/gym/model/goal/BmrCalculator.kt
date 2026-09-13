package com.gym.model.goal

import kotlin.math.roundToInt

/**
 * Standard WHO BMI weight-status classification bands.
 */
enum class BmiCategory {
    UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE
}

/**
 * Formula engine for Mifflin-St Jeor Equation (1990) & Harris-Benedict revised calculations.
 */
object BmrCalculator {

    fun bmiCategory(bmi: Double): BmiCategory = when {
        bmi < 18.5 -> BmiCategory.UNDERWEIGHT
        bmi < 25.0 -> BmiCategory.NORMAL
        bmi < 30.0 -> BmiCategory.OVERWEIGHT
        else -> BmiCategory.OBESE
    }

    fun calculateBmi(heightCm: Double, weightKg: Double): Double {
        if (heightCm <= 0.0) return 0.0
        val heightMeters = heightCm / 100.0
        val rawBmi = weightKg / (heightMeters * heightMeters)
        return (rawBmi * 10.0).roundToInt() / 10.0
    }

    fun calculateIdealWeightMin(heightCm: Double): Double {
        val hM = heightCm / 100.0
        return (18.5 * hM * hM).roundToInt().toDouble()
    }

    fun calculateIdealWeightMax(heightCm: Double): Double {
        val hM = heightCm / 100.0
        return (24.9 * hM * hM).roundToInt().toDouble()
    }

    fun calculateGoalMetrics(
        gender: Gender,
        age: Double,
        heightCm: Double,
        weightKg: Double,
        goalWeightKg: Double,
        weightGoal: WeightGoal,
        activityLevel: ActivityLevel
    ): UserFitnessGoal {
        val bmi = calculateBmi(heightCm, weightKg)
        val minW = calculateIdealWeightMin(heightCm)
        val maxW = calculateIdealWeightMax(heightCm)

        // Mifflin-St Jeor BMR
        val bmr = if (gender == Gender.MALE) {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) + 5.0
        } else {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) - 161.0
        }

        // TDEE Maintenance
        val tdee = bmr * activityLevel.multiplier

        // Calorie Goal Adjustment
        val finalCalories = when (weightGoal) {
            WeightGoal.GAIN -> tdee + 300.0
            WeightGoal.LOSE -> (tdee - 400.0).coerceAtLeast(1200.0)
            WeightGoal.LOSE_KETO -> (tdee - 450.0).coerceAtLeast(1200.0)
            WeightGoal.MAINTAIN -> tdee
        }

        // Macro Distribution
        val (proteinPct, carbsPct, fatPct) = if (weightGoal == WeightGoal.LOSE_KETO) {
            Triple(0.25, 0.05, 0.70)
        } else {
            Triple(0.25, 0.60, 0.15)
        }

        val proteinGrams = (finalCalories * proteinPct) / 4.0
        val carbsGrams = (finalCalories * carbsPct) / 4.0
        val fatGrams = (finalCalories * fatPct) / 9.0

        return UserFitnessGoal(
            gender = gender,
            age = age,
            heightCm = heightCm,
            weightKg = weightKg,
            goalWeightKg = goalWeightKg,
            weightGoal = weightGoal,
            activityLevel = activityLevel,
            bmi = bmi,
            idealWeightMinKg = minW,
            idealWeightMaxKg = maxW,
            calculatedCalories = (finalCalories * 10.0).roundToInt() / 10.0,
            calculatedProteinGrams = (proteinGrams * 100.0).roundToInt() / 100.0,
            calculatedCarbsGrams = (carbsGrams * 100.0).roundToInt() / 100.0,
            calculatedFatGrams = (fatGrams * 100.0).roundToInt() / 100.0,
            lastUpdatedIso = "2026-07-25"
        )
    }
}
