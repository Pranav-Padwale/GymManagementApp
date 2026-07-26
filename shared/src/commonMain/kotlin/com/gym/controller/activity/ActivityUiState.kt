package com.gym.controller.activity

import com.gym.model.activity.CalorieData
import com.gym.model.activity.CalorieGraphPoint
import com.gym.model.activity.NutritionMacro
import com.gym.model.activity.TimeRange

/**
 * Immutable State Data Class for Member Activity & Nutrition Dashboard.
 * Holds real-time system date properties and interactive calendar state.
 */
data class ActivityUiState(
    val realTodayDay: Int = 27,
    val realTodayMonth: Int = 7,
    val realTodayYear: Int = 2026,
    val selectedDay: Int = 27,
    val selectedMonth: Int = 7,
    val selectedYear: Int = 2026,
    val calendarViewMonth: Int = 7,
    val calendarViewYear: Int = 2026,
    val selectedDateDisplay: String = "Today, 27 July, 2026",
    val isCalendarOpen: Boolean = false,
    val isLoading: Boolean = false,
    val isHealthPermissionGranted: Boolean = false,
    val calorieData: CalorieData = CalorieData(),
    val macros: List<NutritionMacro> = listOf(
        NutritionMacro(name = "Carbs", percentage = 60.0, totalGrams = 383.62, consumedGrams = 0.0, hexColor = "#FF6D25", emojiIcon = "🥖"),
        NutritionMacro(name = "Protein", percentage = 25.0, totalGrams = 159.84, consumedGrams = 0.0, hexColor = "#3B82F6", emojiIcon = "🥩"),
        NutritionMacro(name = "Fat", percentage = 15.0, totalGrams = 42.62, consumedGrams = 0.0, hexColor = "#84CC16", emojiIcon = "🥑")
    ),
    val selectedTimeRange: TimeRange = TimeRange.DAYS_7,
    val showCaloriesSeries: Boolean = true,
    val showEatenSeries: Boolean = true,
    val showBurnSeries: Boolean = true,
    val graphHistory: List<CalorieGraphPoint> = listOf(
        CalorieGraphPoint("Mon", 2557.5, 1800.0, 450.0),
        CalorieGraphPoint("Tue", 2557.5, 2100.0, 520.0),
        CalorieGraphPoint("Wed", 2557.5, 1950.0, 380.0),
        CalorieGraphPoint("Thu", 2557.5, 2300.0, 610.0),
        CalorieGraphPoint("Fri", 2557.5, 1750.0, 400.0),
        CalorieGraphPoint("Sat", 2557.5, 2500.0, 700.0),
        CalorieGraphPoint("Sun", 2557.5, 1600.0, 350.0)
    ),
    val activeBottomTab: String = "Activity",
    val errorMessage: String? = null
)
