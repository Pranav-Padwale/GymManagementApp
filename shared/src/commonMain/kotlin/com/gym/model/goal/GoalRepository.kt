package com.gym.model.goal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Model Layer: GoalRepository Contract
 */
interface GoalRepository {
    suspend fun getUserFitnessGoal(): Result<UserFitnessGoal>
    suspend fun saveUserFitnessGoal(goal: UserFitnessGoal): Result<Unit>
}

/**
 * Model Layer: GoalRepository Implementation
 */
class GoalRepositoryImpl : GoalRepository {

    private var currentGoal = UserFitnessGoal(
        gender = Gender.MALE,
        age = 26.0,
        heightCm = 172.0,
        weightKg = 69.0,
        goalWeightKg = 70.0,
        weightGoal = WeightGoal.GAIN,
        activityLevel = ActivityLevel.MODERATELY,
        bmi = 23.3,
        idealWeightMinKg = 55.0,
        idealWeightMaxKg = 74.0,
        calculatedCalories = 2557.5,
        calculatedProteinGrams = 159.84,
        calculatedCarbsGrams = 383.62,
        calculatedFatGrams = 42.62,
        lastUpdatedIso = "2026-07-25"
    )

    override suspend fun getUserFitnessGoal(): Result<UserFitnessGoal> = withContext(Dispatchers.IO) {
        Result.success(currentGoal)
    }

    override suspend fun saveUserFitnessGoal(goal: UserFitnessGoal): Result<Unit> = withContext(Dispatchers.IO) {
        currentGoal = goal
        Result.success(Unit)
    }
}
