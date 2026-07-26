package com.gym.model.diet

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

enum class MealCategory(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    SNACKS("Snacks"),
    DINNER("Dinner")
}

data class FoodItem(
    val id: String,
    val name: String,
    val quantityGrams: Double,
    val calories: Double,
    val category: MealCategory
)

data class DietPlan(
    val dateIso: String = "2026-07-26",
    val dateDisplay: String = "26/07/2026",
    val selectedTrainer: String = "No trainer assigned",
    val consumedCalories: Double = 0.0,
    val targetCalories: Double = 1968.0,
    val consumedCarbsGrams: Double = 0.0,
    val targetCarbsGrams: Double = 221.0,
    val consumedProteinGrams: Double = 0.0,
    val targetProteinGrams: Double = 98.0,
    val consumedFatGrams: Double = 0.0,
    val targetFatGrams: Double = 77.0,
    val foodItems: List<FoodItem> = emptyList()
)

/**
 * Model Layer: DietPlanRepository Contract
 */
interface DietPlanRepository {
    suspend fun getDietPlan(dateIso: String): Result<DietPlan>
    suspend fun updateTrainer(trainerName: String): Result<Unit>
    suspend fun addFoodItem(foodItem: FoodItem): Result<Unit>
    suspend fun removeFoodItem(foodItemId: String): Result<Unit>
    suspend fun getAvailableTrainers(): Result<List<String>>
}

/**
 * Model Layer: DietPlanRepository Implementation
 */
class DietPlanRepositoryImpl : DietPlanRepository {

    private var currentDietPlan = DietPlan()

    private val trainersList = listOf(
        "No trainer assigned",
        "Coach Dan (Strength & Conditioning)",
        "Coach Sarah (Nutrition Specialist)",
        "Coach Alex (Personal Fitness)"
    )

    override suspend fun getDietPlan(dateIso: String): Result<DietPlan> = withContext(Dispatchers.IO) {
        Result.success(currentDietPlan)
    }

    override suspend fun updateTrainer(trainerName: String): Result<Unit> = withContext(Dispatchers.IO) {
        currentDietPlan = currentDietPlan.copy(selectedTrainer = trainerName)
        Result.success(Unit)
    }

    override suspend fun addFoodItem(foodItem: FoodItem): Result<Unit> = withContext(Dispatchers.IO) {
        val updatedList = currentDietPlan.foodItems + foodItem
        val newCalories = updatedList.sumOf { it.calories }
        currentDietPlan = currentDietPlan.copy(
            foodItems = updatedList,
            consumedCalories = newCalories
        )
        Result.success(Unit)
    }

    override suspend fun removeFoodItem(foodItemId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val updatedList = currentDietPlan.foodItems.filter { it.id != foodItemId }
        val newCalories = updatedList.sumOf { it.calories }
        currentDietPlan = currentDietPlan.copy(
            foodItems = updatedList,
            consumedCalories = newCalories
        )
        Result.success(Unit)
    }

    override suspend fun getAvailableTrainers(): Result<List<String>> = withContext(Dispatchers.IO) {
        Result.success(trainersList)
    }
}
