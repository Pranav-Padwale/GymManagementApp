package com.gym.controller.goal

import com.gym.model.goal.Gender
import com.gym.model.goal.GoalRepository
import com.gym.model.goal.UserFitnessGoal
import com.gym.model.goal.WeightGoal
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** In-memory fake with no dispatcher hops, so it completes synchronously under UnconfinedTestDispatcher. */
private class FakeGoalRepository(initial: UserFitnessGoal = UserFitnessGoal()) : GoalRepository {
    var saved: UserFitnessGoal? = null
    var current: UserFitnessGoal = initial

    override suspend fun getUserFitnessGoal(): Result<UserFitnessGoal> = Result.success(current)
    override suspend fun saveUserFitnessGoal(goal: UserFitnessGoal): Result<Unit> {
        saved = goal
        current = goal
        return Result.success(Unit)
    }
}

class GoalControllerTest {

    private fun newController(repo: GoalRepository): GoalController {
        val scope = TestScope(UnconfinedTestDispatcher())
        return GoalController(repository = repo, scope = scope)
    }

    @Test
    fun onViewResultClicked_recalculatesAndPersistsGoalMetrics() = runTest {
        val repo = FakeGoalRepository()
        val controller = newController(repo)

        controller.onGenderSelected(Gender.MALE)
        controller.onAgeChanged("26")
        controller.onHeightChanged("172")
        controller.onWeightChanged("69")
        controller.onWeightGoalSelected(WeightGoal.GAIN)
        controller.onViewResultClicked()

        val state = controller.uiState.value
        assertTrue(state.isResultCalculated)
        assertEquals(2842.0, state.goalResult.calculatedCalories, absoluteTolerance = 0.01)
        assertEquals(state.goalResult, repo.saved)
    }

    @Test
    fun onHeightOrWeightChanged_recalculatesBmiLiveWithoutWaitingForViewResultClick() = runTest {
        val controller = newController(FakeGoalRepository())

        controller.onHeightChanged("172")
        controller.onWeightChanged("69")

        assertEquals(23.3, controller.uiState.value.goalResult.bmi, absoluteTolerance = 0.01)
    }

    @Test
    fun onAgeChanged_nonNumericInput_fallsBackToDefaultInsteadOfCrashing() = runTest {
        val controller = newController(FakeGoalRepository())

        controller.onAgeChanged("not-a-number")
        controller.onViewResultClicked()

        // toDoubleOrNull() returning null falls back to the documented default age of 26.0.
        assertTrue(controller.uiState.value.isResultCalculated)
    }

    @Test
    fun loadGoalData_onInit_populatesStateFromRepository() = runTest {
        val seeded = UserFitnessGoal(gender = Gender.FEMALE, age = 40.0, heightCm = 165.0, weightKg = 60.0)
        val controller = newController(FakeGoalRepository(initial = seeded))

        assertEquals(Gender.FEMALE, controller.uiState.value.gender)
        assertEquals("40.0", controller.uiState.value.ageStr)
    }
}
