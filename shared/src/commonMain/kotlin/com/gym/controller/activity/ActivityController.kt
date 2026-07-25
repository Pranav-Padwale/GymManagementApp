package com.gym.controller.activity

import com.gym.model.activity.ActivityRepository
import com.gym.model.activity.CalorieData
import com.gym.model.activity.TimeRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Controller: Dedicated Kotlin class for Member Activity & Nutrition Dashboard.
 * Listens for View callbacks, updates Model, and publishes updated immutable ActivityUiState.
 */
class ActivityController(
    private val repository: ActivityRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    private val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    init {
        refreshActivityData()
    }

    /**
     * Action handler: Toggle interactive calendar picker dialog visibility.
     */
    fun onToggleCalendarDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isCalendarOpen = isOpen) }
    }

    /**
     * Action handler: User selects a date on the interactive calendar picker.
     */
    fun onCalendarDateSelected(day: Int, month: Int, year: Int) {
        val isToday = (day == 25 && month == 7 && year == 2026)
        val mName = monthNames.getOrNull(month - 1) ?: "July"
        val displayStr = if (isToday) "Today, $day $mName, $year" else "$day $mName, $year"

        _uiState.update {
            it.copy(
                selectedDay = day,
                selectedMonth = month,
                selectedYear = year,
                selectedDateDisplay = displayStr,
                isCalendarOpen = false,
                isLoading = true
            )
        }

        // Simulate fetching date-specific calorie & nutrition metrics from Model
        scope.launch {
            val dateIso = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
            val calResult = repository.getCalorieData(dateIso)
            val macroResult = repository.getNutritionMacros(dateIso)

            // Dynamic calorie data per date for interactive demonstration
            val dateCalorieData = if (isToday) {
                calResult.getOrDefault(CalorieData())
            } else {
                val mockEaten = (1400..2600).random().toDouble()
                val mockBurned = (200..650).random().toDouble()
                CalorieData(totalTargetCalories = 2557.5, caloriesBurned = mockBurned, caloriesEaten = mockEaten)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    calorieData = dateCalorieData,
                    macros = macroResult.getOrDefault(it.macros)
                )
            }
        }
    }

    /**
     * Action handler: Navigate month in calendar modal (`deltaMonth` = -1 for Prev, +1 for Next).
     */
    fun onCalendarMonthNav(deltaMonth: Int) {
        _uiState.update { currentState ->
            var newMonth = currentState.calendarViewMonth + deltaMonth
            var newYear = currentState.calendarViewYear

            if (newMonth < 1) {
                newMonth = 12
                newYear -= 1
            } else if (newMonth > 12) {
                newMonth = 1
                newYear += 1
            }

            currentState.copy(calendarViewMonth = newMonth, calendarViewYear = newYear)
        }
    }

    /**
     * Action handler: Quick selection for Today (25 July, 2026).
     */
    fun onSelectToday() {
        onCalendarDateSelected(25, 7, 2026)
    }

    /**
     * Action handler: Grant health sync permission.
     */
    fun onGrantHealthPermission() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.setHealthPermissionGranted(true)
            refreshActivityData()
        }
    }

    /**
     * Action handler: Change graph time range selector tab.
     */
    fun onGraphTimeRangeChanged(timeRange: TimeRange) {
        _uiState.update { it.copy(selectedTimeRange = timeRange) }
        scope.launch {
            val graphResult = repository.getCalorieGraphHistory(timeRange)
            graphResult.onSuccess { points ->
                _uiState.update { it.copy(graphHistory = points) }
            }
        }
    }

    /**
     * Action handler: Toggle series visibility on line chart.
     */
    fun onToggleSeries(showCalories: Boolean, showEaten: Boolean, showBurn: Boolean) {
        _uiState.update {
            it.copy(
                showCaloriesSeries = showCalories,
                showEatenSeries = showEaten,
                showBurnSeries = showBurn
            )
        }
    }

    /**
     * Action handler: Switch bottom bar tab.
     */
    fun onBottomTabSelected(tabName: String) {
        _uiState.update { it.copy(activeBottomTab = tabName) }
    }

    /**
     * Refresh all dashboard metrics from repository.
     */
    fun refreshActivityData() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val date = _uiState.value.selectedDateDisplay
            val range = _uiState.value.selectedTimeRange

            val calResult = repository.getCalorieData(date)
            val macroResult = repository.getNutritionMacros(date)
            val graphResult = repository.getCalorieGraphHistory(range)
            val permResult = repository.isHealthPermissionGranted()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    calorieData = calResult.getOrDefault(it.calorieData),
                    macros = macroResult.getOrDefault(it.macros),
                    graphHistory = graphResult.getOrDefault(it.graphHistory),
                    isHealthPermissionGranted = permResult.getOrDefault(false)
                )
            }
        }
    }
}
