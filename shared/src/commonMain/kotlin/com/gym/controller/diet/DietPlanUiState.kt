package com.gym.controller.diet

import com.gym.model.diet.DietPlan
import com.gym.model.diet.FoodItem
import com.gym.model.diet.MealCategory

/**
 * Immutable UI State for Diet Plan Screen.
 */
data class DietPlanUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedDateDisplay: String = "26/07/2026",
    val selectedDateIso: String = "2026-07-26",
    val selectedTrainer: String = "No trainer assigned",
    val availableTrainers: List<String> = listOf("No trainer assigned"),
    val consumedCalories: Int = 0,
    val targetCalories: Int = 1968,
    val consumedCarbsGrams: Int = 0,
    val targetCarbsGrams: Int = 221,
    val consumedProteinGrams: Int = 0,
    val targetProteinGrams: Int = 98,
    val consumedFatGrams: Int = 0,
    val targetFatGrams: Int = 77,
    val foodItems: List<FoodItem> = emptyList(),
    val activeSnackbarMessage: String? = null,
    val isTrainerDropdownExpanded: Boolean = false
) {
    val isDietPlanEmpty: Boolean
        get() = foodItems.isEmpty()

    fun getItemsForCategory(category: MealCategory): List<FoodItem> {
        return foodItems.filter { it.category == category }
    }
}
