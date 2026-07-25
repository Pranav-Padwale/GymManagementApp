package com.gym.controller.activity

import com.gym.model.activity.CalorieData
import com.gym.model.activity.CalorieGraphPoint
import com.gym.model.activity.NutritionMacro
import com.gym.model.activity.TimeRange

/**
 * Immutable UI State object passed from ActivityController to passive View.
 */
data class ActivityUiState(
    val selectedDay: Int = 25,
    val selectedMonth: Int = 7, // July
    val selectedYear: Int = 2026,
    val selectedDateDisplay: String = "Today, 25 July, 2026",
    val calendarViewMonth: Int = 7,
    val calendarViewYear: Int = 2026,
    val isCalendarOpen: Boolean = false,
    val calorieData: CalorieData = CalorieData(),
    val macros: List<NutritionMacro> = emptyList(),
    val isHealthPermissionGranted: Boolean = false,
    val selectedTimeRange: TimeRange = TimeRange.DAYS_7,
    val showCaloriesSeries: Boolean = true,
    val showEatenSeries: Boolean = true,
    val showBurnSeries: Boolean = true,
    val graphHistory: List<CalorieGraphPoint> = emptyList(),
    val activeBottomTab: String = "Activity",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
