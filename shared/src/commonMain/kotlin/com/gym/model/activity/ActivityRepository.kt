package com.gym.model.activity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Model Layer: ActivityRepository contract.
 */
interface ActivityRepository {
    suspend fun getCalorieData(dateIso: String): Result<CalorieData>
    suspend fun getNutritionMacros(dateIso: String): Result<List<NutritionMacro>>
    suspend fun getCalorieGraphHistory(timeRange: TimeRange): Result<List<CalorieGraphPoint>>
    suspend fun isHealthPermissionGranted(): Result<Boolean>
    suspend fun setHealthPermissionGranted(granted: Boolean): Result<Unit>
    suspend fun logMeal(calories: Double, carbsGrams: Double, proteinGrams: Double, fatGrams: Double): Result<Unit>
    suspend fun logExercise(burnedCalories: Double): Result<Unit>
}

/**
 * Model Layer: ActivityRepository Implementation with mock/persisted dataset.
 */
class ActivityRepositoryImpl : ActivityRepository {

    private var hasHealthPermission = false

    private var currentCalorieData = CalorieData(
        totalTargetCalories = 2557.5,
        caloriesBurned = 0.0,
        caloriesEaten = 0.0
    )

    private val currentMacros = mutableListOf(
        NutritionMacro(name = "Carbs", percentage = 60.0, totalGrams = 383.62, consumedGrams = 0.0, hexColor = "#FF6D25", emojiIcon = "🥖"),
        NutritionMacro(name = "Protein", percentage = 25.0, totalGrams = 159.84, consumedGrams = 0.0, hexColor = "#3B82F6", emojiIcon = "🥩"),
        NutritionMacro(name = "Fat", percentage = 15.0, totalGrams = 42.62, consumedGrams = 0.0, hexColor = "#84CC16", emojiIcon = "🥑")
    )

    override suspend fun getCalorieData(dateIso: String): Result<CalorieData> = withContext(Dispatchers.IO) {
        Result.success(currentCalorieData)
    }

    override suspend fun getNutritionMacros(dateIso: String): Result<List<NutritionMacro>> = withContext(Dispatchers.IO) {
        Result.success(currentMacros.toList())
    }

    override suspend fun getCalorieGraphHistory(timeRange: TimeRange): Result<List<CalorieGraphPoint>> = withContext(Dispatchers.IO) {
        val count = when (timeRange) {
            TimeRange.DAYS_7 -> 7
            TimeRange.DAYS_15 -> 15
            TimeRange.DAYS_30 -> 30
            TimeRange.MONTHS_3 -> 90
            TimeRange.MONTHS_6 -> 180
        }

        val points = (0 until count).map { i ->
            val dateStr = "25-07-${26 - (count - 1 - i).coerceAtLeast(0)}"
            CalorieGraphPoint(
                dateLabel = dateStr,
                caloriesTotal = currentCalorieData.totalTargetCalories,
                caloriesEaten = if (i == count - 1) currentCalorieData.caloriesEaten else (1800..2400).random().toDouble(),
                caloriesBurned = if (i == count - 1) currentCalorieData.caloriesBurned else (300..700).random().toDouble()
            )
        }
        Result.success(points)
    }

    override suspend fun isHealthPermissionGranted(): Result<Boolean> = withContext(Dispatchers.IO) {
        Result.success(hasHealthPermission)
    }

    override suspend fun setHealthPermissionGranted(granted: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        hasHealthPermission = granted
        if (granted) {
            // Simulate health sync updates
            currentCalorieData = currentCalorieData.copy(caloriesBurned = 350.0)
        }
        Result.success(Unit)
    }

    override suspend fun logMeal(
        calories: Double,
        carbsGrams: Double,
        proteinGrams: Double,
        fatGrams: Double
    ): Result<Unit> = withContext(Dispatchers.IO) {
        currentCalorieData = currentCalorieData.copy(
            caloriesEaten = currentCalorieData.caloriesEaten + calories
        )
        // Update macros
        val carbsIdx = currentMacros.indexOfFirst { it.name == "Carbs" }
        if (carbsIdx != -1) {
            currentMacros[carbsIdx] = currentMacros[carbsIdx].copy(
                consumedGrams = currentMacros[carbsIdx].consumedGrams + carbsGrams
            )
        }
        val proteinIdx = currentMacros.indexOfFirst { it.name == "Protein" }
        if (proteinIdx != -1) {
            currentMacros[proteinIdx] = currentMacros[proteinIdx].copy(
                consumedGrams = currentMacros[proteinIdx].consumedGrams + proteinGrams
            )
        }
        val fatIdx = currentMacros.indexOfFirst { it.name == "Fat" }
        if (fatIdx != -1) {
            currentMacros[fatIdx] = currentMacros[fatIdx].copy(
                consumedGrams = currentMacros[fatIdx].consumedGrams + fatGrams
            )
        }
        Result.success(Unit)
    }

    override suspend fun logExercise(burnedCalories: Double): Result<Unit> = withContext(Dispatchers.IO) {
        currentCalorieData = currentCalorieData.copy(
            caloriesBurned = currentCalorieData.caloriesBurned + burnedCalories
        )
        Result.success(Unit)
    }
}
