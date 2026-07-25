package com.gym.controller.goal

import com.gym.model.goal.ActivityLevel
import com.gym.model.goal.BmrCalculator
import com.gym.model.goal.Gender
import com.gym.model.goal.GoalRepository
import com.gym.model.goal.WeightGoal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Controller: Dedicated Kotlin class for Edit Goal feature.
 * Connects passive EditGoalScreen with BmrCalculator and GoalRepository.
 */
class GoalController(
    private val repository: GoalRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    init {
        loadGoalData()
    }

    fun onGenderSelected(gender: Gender) {
        _uiState.update { it.copy(gender = gender) }
        recalculateLiveBmi()
    }

    fun onAgeChanged(ageStr: String) {
        _uiState.update { it.copy(ageStr = ageStr) }
        recalculateLiveBmi()
    }

    fun onHeightChanged(heightStr: String) {
        _uiState.update { it.copy(heightStr = heightStr) }
        recalculateLiveBmi()
    }

    fun onWeightChanged(weightStr: String) {
        _uiState.update { it.copy(weightStr = weightStr) }
        recalculateLiveBmi()
    }

    fun onGoalWeightChanged(goalWeightStr: String) {
        _uiState.update { it.copy(goalWeightStr = goalWeightStr) }
    }

    fun onWeightGoalSelected(goal: WeightGoal) {
        _uiState.update { it.copy(weightGoal = goal) }
    }

    fun onActivityLevelSelected(activity: ActivityLevel) {
        _uiState.update { it.copy(activityLevel = activity) }
    }

    /**
     * User action: Click "View Result" button.
     * Computes BMR / TDEE formulas and updates goal metrics.
     */
    fun onViewResultClicked() {
        val currentState = _uiState.value
        val age = currentState.ageStr.toDoubleOrNull() ?: 26.0
        val height = currentState.heightStr.toDoubleOrNull() ?: 172.0
        val weight = currentState.weightStr.toDoubleOrNull() ?: 69.0
        val goalWeight = currentState.goalWeightStr.toDoubleOrNull() ?: 70.0

        val newResult = BmrCalculator.calculateGoalMetrics(
            gender = currentState.gender,
            age = age,
            heightCm = height,
            weightKg = weight,
            goalWeightKg = goalWeight,
            weightGoal = currentState.weightGoal,
            activityLevel = currentState.activityLevel
        )

        scope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.saveUserFitnessGoal(newResult)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    goalResult = newResult,
                    isResultCalculated = true
                )
            }
        }
    }

    private fun recalculateLiveBmi() {
        val currentState = _uiState.value
        val height = currentState.heightStr.toDoubleOrNull() ?: 172.0
        val weight = currentState.weightStr.toDoubleOrNull() ?: 69.0
        val bmi = BmrCalculator.calculateBmi(height, weight)
        val minW = BmrCalculator.calculateIdealWeightMin(height)
        val maxW = BmrCalculator.calculateIdealWeightMax(height)

        _uiState.update {
            it.copy(
                goalResult = it.goalResult.copy(
                    bmi = bmi,
                    idealWeightMinKg = minW,
                    idealWeightMaxKg = maxW
                )
            )
        }
    }

    private fun loadGoalData() {
        scope.launch {
            val result = repository.getUserFitnessGoal()
            result.onSuccess { goal ->
                _uiState.update {
                    it.copy(
                        gender = goal.gender,
                        ageStr = goal.age.toString(),
                        heightStr = goal.heightCm.toString(),
                        weightStr = goal.weightKg.toString(),
                        goalWeightStr = goal.goalWeightKg.toString(),
                        weightGoal = goal.weightGoal,
                        activityLevel = goal.activityLevel,
                        goalResult = goal
                    )
                }
            }
        }
    }
}
