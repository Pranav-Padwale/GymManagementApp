package com.gym.model.timeslot

import kotlinx.serialization.Serializable

/**
 * Model Layer: Represents a day of the week for gym scheduling.
 * Independent of UI and Controllers.
 */
@Serializable
enum class DayOfWeek(val short: String, val full: String) {
    MONDAY("Mon", "Monday"),
    TUESDAY("Tue", "Tuesday"),
    WEDNESDAY("Wed", "Wednesday"),
    THURSDAY("Thu", "Thursday"),
    FRIDAY("Fri", "Friday"),
    SATURDAY("Sat", "Saturday"),
    SUNDAY("Sun", "Sunday")
}

/**
 * Model Layer: Category classification for gym time slots.
 */
@Serializable
enum class SlotCategory(val displayName: String, val emoji: String) {
    YOGA("Yoga", "🧘"),
    CARDIO("Cardio", "🏃"),
    STRENGTH("Strength", "💪"),
    CROSSFIT("CrossFit", "🔥"),
    ZUMBA("Zumba", "💃"),
    OPEN_GYM("Open Gym", "🏋️")
}

/**
 * Model Layer: Attendance status for a member's slot booking.
 */
@Serializable
enum class AttendanceStatus(val displayName: String, val emoji: String) {
    BOOKED("Booked", "📋"),
    CHECKED_IN("Checked In", "✅"),
    CHECKED_OUT("Completed", "🏁"),
    NO_SHOW("No Show", "❌")
}

/**
 * Domain model representing a recurring gym time slot / class session.
 * Completely independent of UI and Controllers.
 */
@Serializable
data class TimeSlot(
    val id: String,
    val title: String,
    val trainerName: String,
    val dayOfWeek: DayOfWeek,
    val startTime: String,
    val endTime: String,
    val maxCapacity: Int,
    val currentBookings: Int,
    val category: SlotCategory,
    val isActive: Boolean = true
)

/**
 * Domain model representing an individual member's attendance record for a specific slot.
 * Completely independent of UI and Controllers.
 */
@Serializable
data class AttendanceRecord(
    val id: String,
    val slotId: String,
    val memberId: String,
    val memberName: String,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val status: AttendanceStatus = AttendanceStatus.BOOKED
)
