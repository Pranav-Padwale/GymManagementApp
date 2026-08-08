package com.gym.model.timeslot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Model Layer: TimeSlotRepository contract.
 * Defines operations for managing gym time slots and member attendance.
 */
interface TimeSlotRepository {
    suspend fun getSlotsByDay(day: DayOfWeek): Result<List<TimeSlot>>
    suspend fun getAllSlots(): Result<List<TimeSlot>>
    suspend fun createSlot(slot: TimeSlot): Result<TimeSlot>
    suspend fun deleteSlot(slotId: String): Result<Unit>
    suspend fun toggleSlotActive(slotId: String): Result<TimeSlot>
    suspend fun getAttendanceForSlot(slotId: String): Result<List<AttendanceRecord>>
    suspend fun checkInMember(slotId: String, memberId: String): Result<AttendanceRecord>
    suspend fun checkOutMember(attendanceId: String): Result<AttendanceRecord>
    suspend fun bookSlot(slotId: String, memberId: String, memberName: String): Result<AttendanceRecord>
}

/**
 * Model Layer: In-memory implementation of TimeSlotRepository.
 * Seeds realistic demo data across the week for demonstration and testing.
 */
class TimeSlotRepositoryImpl : TimeSlotRepository {

    // In-memory slot backing store with realistic weekly schedule
    private val slotsDb = mutableListOf(
        // Monday
        TimeSlot("TS-001", "Morning Power Yoga", "Coach Arjun", DayOfWeek.MONDAY, "06:00", "07:30", 25, 18, SlotCategory.YOGA),
        TimeSlot("TS-002", "Evening Cardio Blast", "Coach Priya", DayOfWeek.MONDAY, "18:00", "19:00", 30, 24, SlotCategory.CARDIO),
        TimeSlot("TS-003", "Strength Foundations", "Coach Vikram", DayOfWeek.MONDAY, "08:00", "09:30", 20, 12, SlotCategory.STRENGTH),

        // Tuesday
        TimeSlot("TS-004", "Strength Training", "Coach Vikram", DayOfWeek.TUESDAY, "07:00", "08:30", 20, 15, SlotCategory.STRENGTH),
        TimeSlot("TS-005", "Zumba Dance Fitness", "Coach Sneha", DayOfWeek.TUESDAY, "17:00", "18:00", 35, 28, SlotCategory.ZUMBA),
        TimeSlot("TS-006", "Open Gym Session", "Self-Guided", DayOfWeek.TUESDAY, "16:00", "20:00", 50, 22, SlotCategory.OPEN_GYM),

        // Wednesday
        TimeSlot("TS-007", "CrossFit HIIT", "Coach Arjun", DayOfWeek.WEDNESDAY, "06:00", "07:00", 20, 19, SlotCategory.CROSSFIT),
        TimeSlot("TS-008", "Open Gym Afternoon", "Self-Guided", DayOfWeek.WEDNESDAY, "16:00", "20:00", 50, 31, SlotCategory.OPEN_GYM),

        // Thursday
        TimeSlot("TS-009", "Morning Yoga Flow", "Coach Priya", DayOfWeek.THURSDAY, "06:00", "07:30", 25, 20, SlotCategory.YOGA),
        TimeSlot("TS-010", "Strength & Conditioning", "Coach Vikram", DayOfWeek.THURSDAY, "19:00", "20:00", 20, 14, SlotCategory.STRENGTH),

        // Friday
        TimeSlot("TS-011", "Cardio Kickboxing", "Coach Arjun", DayOfWeek.FRIDAY, "07:00", "08:00", 25, 21, SlotCategory.CARDIO),
        TimeSlot("TS-012", "Open Gym Evening", "Self-Guided", DayOfWeek.FRIDAY, "16:00", "20:00", 50, 35, SlotCategory.OPEN_GYM),

        // Saturday
        TimeSlot("TS-013", "Full Body CrossFit", "Coach Vikram", DayOfWeek.SATURDAY, "08:00", "09:30", 20, 17, SlotCategory.CROSSFIT),
        TimeSlot("TS-014", "Yoga Relax & Stretch", "Coach Sneha", DayOfWeek.SATURDAY, "10:00", "11:00", 30, 22, SlotCategory.YOGA),
        TimeSlot("TS-015", "Zumba Weekend Party", "Coach Sneha", DayOfWeek.SATURDAY, "17:00", "18:00", 35, 30, SlotCategory.ZUMBA),

        // Sunday
        TimeSlot("TS-016", "Open Gym (Rest Day)", "Self-Guided", DayOfWeek.SUNDAY, "09:00", "12:00", 40, 15, SlotCategory.OPEN_GYM),
        TimeSlot("TS-017", "Gentle Yoga Recovery", "Coach Priya", DayOfWeek.SUNDAY, "08:00", "09:00", 25, 10, SlotCategory.YOGA)
    )

    // In-memory attendance records
    private val attendanceDb = mutableListOf(
        // TS-001 Monday Morning Yoga attendees
        AttendanceRecord("ATT-001", "TS-001", "M-101", "Alex Rivers", "06:02", "07:28", AttendanceStatus.CHECKED_OUT),
        AttendanceRecord("ATT-002", "TS-001", "M-104", "Elena Rostova", "06:05", null, AttendanceStatus.CHECKED_IN),
        AttendanceRecord("ATT-003", "TS-001", "M-102", "Sarah Jenkins", null, null, AttendanceStatus.BOOKED),
        AttendanceRecord("ATT-004", "TS-001", "M-106", "Chloe Bennett", null, null, AttendanceStatus.NO_SHOW),

        // TS-002 Monday Evening Cardio attendees
        AttendanceRecord("ATT-005", "TS-002", "M-101", "Alex Rivers", null, null, AttendanceStatus.BOOKED),
        AttendanceRecord("ATT-006", "TS-002", "M-105", "David Kim", "18:03", "18:58", AttendanceStatus.CHECKED_OUT),
        AttendanceRecord("ATT-007", "TS-002", "M-107", "James Wilson", "18:01", null, AttendanceStatus.CHECKED_IN),

        // TS-004 Tuesday Strength Training attendees
        AttendanceRecord("ATT-008", "TS-004", "M-104", "Elena Rostova", "07:05", "08:25", AttendanceStatus.CHECKED_OUT),
        AttendanceRecord("ATT-009", "TS-004", "M-101", "Alex Rivers", "07:02", null, AttendanceStatus.CHECKED_IN),
        AttendanceRecord("ATT-010", "TS-004", "M-103", "Marcus Vance", null, null, AttendanceStatus.BOOKED),

        // TS-005 Tuesday Zumba attendees
        AttendanceRecord("ATT-011", "TS-005", "M-106", "Chloe Bennett", "17:00", "17:55", AttendanceStatus.CHECKED_OUT),
        AttendanceRecord("ATT-012", "TS-005", "M-102", "Sarah Jenkins", "17:02", null, AttendanceStatus.CHECKED_IN),

        // TS-007 Wednesday CrossFit attendees
        AttendanceRecord("ATT-013", "TS-007", "M-101", "Alex Rivers", "06:00", "06:58", AttendanceStatus.CHECKED_OUT),
        AttendanceRecord("ATT-014", "TS-007", "M-104", "Elena Rostova", "06:03", null, AttendanceStatus.CHECKED_IN),
        AttendanceRecord("ATT-015", "TS-007", "M-107", "James Wilson", null, null, AttendanceStatus.NO_SHOW),

        // TS-009 Thursday Morning Yoga attendees
        AttendanceRecord("ATT-016", "TS-009", "M-102", "Sarah Jenkins", "06:01", "07:28", AttendanceStatus.CHECKED_OUT),
        AttendanceRecord("ATT-017", "TS-009", "M-105", "David Kim", null, null, AttendanceStatus.BOOKED),

        // TS-013 Saturday CrossFit attendees
        AttendanceRecord("ATT-018", "TS-013", "M-101", "Alex Rivers", "08:00", "09:25", AttendanceStatus.CHECKED_OUT),
        AttendanceRecord("ATT-019", "TS-013", "M-104", "Elena Rostova", "08:05", null, AttendanceStatus.CHECKED_IN),
        AttendanceRecord("ATT-020", "TS-013", "M-103", "Marcus Vance", null, null, AttendanceStatus.BOOKED)
    )

    private var nextSlotId = 18
    private var nextAttendanceId = 21

    override suspend fun getAllSlots(): Result<List<TimeSlot>> = withContext(Dispatchers.IO) {
        Result.success(slotsDb.toList())
    }

    override suspend fun getSlotsByDay(day: DayOfWeek): Result<List<TimeSlot>> = withContext(Dispatchers.IO) {
        val filtered = slotsDb.filter { it.dayOfWeek == day }
        Result.success(filtered)
    }

    override suspend fun createSlot(slot: TimeSlot): Result<TimeSlot> = withContext(Dispatchers.IO) {
        try {
            val newSlot = slot.copy(id = "TS-${String.format("%03d", nextSlotId++)}")
            slotsDb.add(newSlot)
            Result.success(newSlot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSlot(slotId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            slotsDb.removeAll { it.id == slotId }
            attendanceDb.removeAll { it.slotId == slotId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleSlotActive(slotId: String): Result<TimeSlot> = withContext(Dispatchers.IO) {
        try {
            val index = slotsDb.indexOfFirst { it.id == slotId }
            if (index != -1) {
                val updated = slotsDb[index].copy(isActive = !slotsDb[index].isActive)
                slotsDb[index] = updated
                Result.success(updated)
            } else {
                Result.failure(Exception("Slot not found: $slotId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAttendanceForSlot(slotId: String): Result<List<AttendanceRecord>> = withContext(Dispatchers.IO) {
        val records = attendanceDb.filter { it.slotId == slotId }
        Result.success(records)
    }

    override suspend fun checkInMember(slotId: String, memberId: String): Result<AttendanceRecord> = withContext(Dispatchers.IO) {
        try {
            val index = attendanceDb.indexOfFirst { it.slotId == slotId && it.memberId == memberId && it.status == AttendanceStatus.BOOKED }
            if (index != -1) {
                val updated = attendanceDb[index].copy(
                    checkInTime = getCurrentTimeString(),
                    status = AttendanceStatus.CHECKED_IN
                )
                attendanceDb[index] = updated
                Result.success(updated)
            } else {
                Result.failure(Exception("No booking found for member $memberId in slot $slotId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkOutMember(attendanceId: String): Result<AttendanceRecord> = withContext(Dispatchers.IO) {
        try {
            val index = attendanceDb.indexOfFirst { it.id == attendanceId && it.status == AttendanceStatus.CHECKED_IN }
            if (index != -1) {
                val updated = attendanceDb[index].copy(
                    checkOutTime = getCurrentTimeString(),
                    status = AttendanceStatus.CHECKED_OUT
                )
                attendanceDb[index] = updated
                Result.success(updated)
            } else {
                Result.failure(Exception("Attendance record not found or not checked in: $attendanceId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun bookSlot(slotId: String, memberId: String, memberName: String): Result<AttendanceRecord> = withContext(Dispatchers.IO) {
        try {
            // Check if member already has a booking for this slot
            val existingBooking = attendanceDb.find { it.slotId == slotId && it.memberId == memberId }
            if (existingBooking != null) {
                return@withContext Result.failure(Exception("Member already has a booking for this slot"))
            }

            // Check slot capacity
            val slot = slotsDb.find { it.id == slotId }
                ?: return@withContext Result.failure(Exception("Slot not found: $slotId"))
            val currentCount = attendanceDb.count { it.slotId == slotId }
            if (currentCount >= slot.maxCapacity) {
                return@withContext Result.failure(Exception("Slot is at full capacity (${slot.maxCapacity})"))
            }

            val record = AttendanceRecord(
                id = "ATT-${String.format("%03d", nextAttendanceId++)}",
                slotId = slotId,
                memberId = memberId,
                memberName = memberName,
                status = AttendanceStatus.BOOKED
            )
            attendanceDb.add(record)

            // Update slot booking count
            val slotIndex = slotsDb.indexOfFirst { it.id == slotId }
            if (slotIndex != -1) {
                slotsDb[slotIndex] = slotsDb[slotIndex].copy(currentBookings = slotsDb[slotIndex].currentBookings + 1)
            }

            Result.success(record)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns a simple time string for demo purposes.
     */
    private fun getCurrentTimeString(): String {
        // Using a fixed demo time since kotlinx-datetime may not be available
        return "07:15"
    }
}
