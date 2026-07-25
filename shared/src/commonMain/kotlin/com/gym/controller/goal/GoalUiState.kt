package com.gym.controller.goal

import com.gym.model.goal.ActivityLevel
import com.gym.model.goal.Gender
import com.gym.model.goal.UserFitnessGoal
import com.gym.model.goal.WeightGoal

/**
 * Immutable State object passed from GoalController to passive View.
 */
data class GoalUiState(
    val gender: Gender = Gender.MALE,
    val ageStr: String = "26.0",
    val heightStr: String = "172.0",
    val weightStr: String = "69.0",
    val goalWeightStr: String = "70.0",
    val weightGoal: WeightGoal = WeightGoal.GAIN,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY,
    val goalResult: UserFitnessGoal = UserFitnessGoal(),
    val isResultCalculated: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
