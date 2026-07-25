package com.gym.model.activity

/**
 * Time range selector for calorie burn/consume historical graphs.
 */
enum class TimeRange(val displayName: String) {
    DAYS_7("7 Days"),
    DAYS_15("15 Days"),
    DAYS_30("30 Days"),
    MONTHS_3("3 Months"),
    MONTHS_6("6 Months")
}
