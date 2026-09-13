package com.gym.controller.activity

import com.gym.model.activity.ActivityRepository
import com.gym.model.activity.CalorieData
import com.gym.model.activity.CalorieGraphPoint
import com.gym.model.activity.NutritionMacro
import com.gym.model.activity.TimeRange
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeActivityRepository : ActivityRepository {
    var shouldFail = false
    var permissionGranted = false

    override suspend fun getCalorieData(dateIso: String): Result<CalorieData> =
        if (shouldFail) Result.failure(Exception("simulated timeout")) else Result.success(CalorieData())

    override suspend fun getNutritionMacros(dateIso: String): Result<List<NutritionMacro>> =
        if (shouldFail) Result.failure(Exception("simulated timeout")) else Result.success(emptyList())

    override suspend fun getCalorieGraphHistory(timeRange: TimeRange): Result<List<CalorieGraphPoint>> =
        if (shouldFail) Result.failure(Exception("simulated timeout")) else Result.success(emptyList())

    override suspend fun isHealthPermissionGranted(): Result<Boolean> =
        if (shouldFail) Result.failure(Exception("simulated timeout")) else Result.success(permissionGranted)

    override suspend fun setHealthPermissionGranted(granted: Boolean): Result<Unit> {
        permissionGranted = granted
        return Result.success(Unit)
    }

    override suspend fun logMeal(calories: Double, carbsGrams: Double, proteinGrams: Double, fatGrams: Double): Result<Unit> =
        Result.success(Unit)

    override suspend fun logExercise(burnedCalories: Double): Result<Unit> = Result.success(Unit)
}

class ActivityControllerTest {

    private fun newController(repo: ActivityRepository): ActivityController {
        val scope = TestScope(UnconfinedTestDispatcher())
        return ActivityController(repository = repo, scope = scope)
    }

    @Test
    fun initRealTimeToday_setsFixedDemoDateAndLoadsData() = runTest {
        val controller = newController(FakeActivityRepository())

        val state = controller.uiState.value
        assertEquals(25, state.realTodayDay)
        assertEquals(7, state.realTodayMonth)
        assertEquals(2026, state.realTodayYear)
        assertEquals("Today, 25 July, 2026", state.selectedDateDisplay)
    }

    @Test
    fun refreshActivityData_repositoryFailure_doesNotSurfaceAnyErrorMessage_documentsSwallowedErrorGap() = runTest {
        // NOTE (see QA report "ACT-1"): unlike FinanceController/MemberManagementController,
        // ActivityController never sets errorMessage on failure - every repository call result
        // is unwrapped with getOrDefault(...) and failures are silently discarded. This test
        // documents that CURRENT behavior (isLoading still clears, but errorMessage stays null
        // and the UI has no way to tell the user their data may be stale/wrong).
        val repo = FakeActivityRepository().apply { shouldFail = true }
        val controller = newController(repo)

        controller.refreshActivityData()

        val state = controller.uiState.value
        assertTrue(!state.isLoading)
        assertNull(state.errorMessage, "Documents that ActivityController currently swallows repository failures silently")
    }

    @Test
    fun onGrantHealthPermission_success_updatesPermissionFlagInState() = runTest {
        val controller = newController(FakeActivityRepository())

        controller.onGrantHealthPermission()

        assertTrue(controller.uiState.value.isHealthPermissionGranted)
    }

    @Test
    fun onCalendarMonthNav_forward_wrapsFromDecemberIntoNextYear() = runTest {
        val controller = newController(FakeActivityRepository())
        // Walk the calendar to December of the current view year first.
        repeat(5) { controller.onCalendarMonthNav(+1) } // July -> December

        controller.onCalendarMonthNav(+1) // December -> January (next year)

        val state = controller.uiState.value
        assertEquals(1, state.calendarViewMonth)
        assertEquals(2027, state.calendarViewYear)
    }

    @Test
    fun onCalendarMonthNav_backward_wrapsFromJanuaryIntoPreviousYear() = runTest {
        val controller = newController(FakeActivityRepository())
        repeat(6) { controller.onCalendarMonthNav(-1) } // July -> January

        controller.onCalendarMonthNav(-1) // January -> December (previous year)

        val state = controller.uiState.value
        assertEquals(12, state.calendarViewMonth)
        assertEquals(2025, state.calendarViewYear)
    }
}
