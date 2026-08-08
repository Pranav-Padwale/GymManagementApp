package com.gym.controller.timeslot

import com.gym.model.timeslot.*

/**
 * Immutable State Data Class for Time Slot & Attendance Management Screen.
 * Passed from Controller to View via StateFlow.
 */
data class TimeSlotUiState(
    val selectedDay: DayOfWeek = DayOfWeek.MONDAY,
    val allSlots: List<TimeSlot> = emptyList(),
    val daySlots: List<TimeSlot> = emptyList(),
    val selectedSlot: TimeSlot? = null,
    val attendanceRecords: List<AttendanceRecord> = emptyList(),
    val isLoading: Boolean = false,
    val isCreateDialogOpen: Boolean = false,
    val isAttendanceExpanded: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Create Slot Form Fields
    val newSlotTitle: String = "",
    val newSlotTrainer: String = "",
    val newSlotDay: DayOfWeek = DayOfWeek.MONDAY,
    val newSlotStartTime: String = "06:00",
    val newSlotEndTime: String = "07:00",
    val newSlotCapacity: String = "20",
    val newSlotCategory: SlotCategory = SlotCategory.OPEN_GYM,
    // Summary Counters
    val totalSlotsThisWeek: Int = 0,
    val totalBookingsToday: Int = 0,
    val checkedInToday: Int = 0,
    // Per-day slot counts for day tab badges
    val slotCountPerDay: Map<DayOfWeek, Int> = emptyMap()
)
