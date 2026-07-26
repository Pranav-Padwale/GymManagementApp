package com.gym.controller.diet

import com.gym.model.diet.DietPlanRepository
import com.gym.model.diet.FoodItem
import com.gym.model.diet.MealCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Controller: Dedicated Kotlin class for Diet Plan Module.
 * Connects View interactions with DietPlanRepository and emits updated DietPlanUiState.
 */
class DietPlanController(
    private val repository: DietPlanRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(DietPlanUiState())
    val uiState: StateFlow<DietPlanUiState> = _uiState.asStateFlow()

    init {
        loadDietPlanData()
    }

    fun onTrainerSelected(trainerName: String) {
        _uiState.update { it.copy(selectedTrainer = trainerName, isTrainerDropdownExpanded = false) }
        scope.launch {
            repository.updateTrainer(trainerName)
        }
    }

    fun onToggleTrainerDropdown(expanded: Boolean) {
        _uiState.update { it.copy(isTrainerDropdownExpanded = expanded) }
    }

    fun onCameraIconClicked(category: MealCategory) {
        _uiState.update {
            it.copy(activeSnackbarMessage = "Camera feature opened for ${category.displayName} photo log")
        }
    }

    fun onAddFoodItem(name: String, quantityGrams: Double, calories: Double, category: MealCategory) {
        val newFood = FoodItem(
            id = "FOOD-${System.currentTimeMillis()}",
            name = name,
            quantityGrams = quantityGrams,
            calories = calories,
            category = category
        )
        scope.launch {
            repository.addFoodItem(newFood)
            refreshDietPlan()
        }
    }

    fun onRemoveFoodItem(foodId: String) {
        scope.launch {
            repository.removeFoodItem(foodId)
            refreshDietPlan()
        }
    }

    fun onClearSnackbar() {
        _uiState.update { it.copy(activeSnackbarMessage = null) }
    }

    fun refreshDietPlan() {
        loadDietPlanData()
    }

    private fun loadDietPlanData() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val dateIso = _uiState.value.selectedDateIso
            val planResult = repository.getDietPlan(dateIso)
            val trainersResult = repository.getAvailableTrainers()

            val plan = planResult.getOrNull()
            val trainers = trainersResult.getOrDefault(listOf("No trainer assigned"))

            if (plan != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedDateDisplay = plan.dateDisplay,
                        selectedDateIso = plan.dateIso,
                        selectedTrainer = plan.selectedTrainer,
                        availableTrainers = trainers,
                        consumedCalories = plan.consumedCalories.toInt(),
                        targetCalories = plan.targetCalories.toInt(),
                        consumedCarbsGrams = plan.consumedCarbsGrams.toInt(),
                        targetCarbsGrams = plan.targetCarbsGrams.toInt(),
                        consumedProteinGrams = plan.consumedProteinGrams.toInt(),
                        targetProteinGrams = plan.targetProteinGrams.toInt(),
                        consumedFatGrams = plan.consumedFatGrams.toInt(),
                        targetFatGrams = plan.targetFatGrams.toInt(),
                        foodItems = plan.foodItems
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Unable to load diet plan data"
                    )
                }
            }
        }
    }
}
