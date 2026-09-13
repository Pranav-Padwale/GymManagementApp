package com.gym.model.timeslot

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimeSlotRepositoryImplTest {

    @Test
    fun bookSlot_firstBookingForMember_succeedsAndIncrementsDisplayedCount() = runTest {
        val repo = TimeSlotRepositoryImpl()
        val before = repo.getAllSlots().getOrThrow().first { it.id == "TS-003" }

        val result = repo.bookSlot("TS-003", "M-999", "New Member")

        assertTrue(result.isSuccess)
        val after = repo.getAllSlots().getOrThrow().first { it.id == "TS-003" }
        assertEquals(before.currentBookings + 1, after.currentBookings)
    }

    @Test
    fun bookSlot_duplicateBookingBySameMember_isRejected() = runTest {
        val repo = TimeSlotRepositoryImpl()
        repo.bookSlot("TS-003", "M-999", "New Member")

        val duplicate = repo.bookSlot("TS-003", "M-999", "New Member")

        assertTrue(duplicate.isFailure)
        assertTrue(duplicate.exceptionOrNull()?.message?.contains("already has a booking") == true)
    }

    @Test
    fun bookSlot_unknownSlotId_fails() = runTest {
        val repo = TimeSlotRepositoryImpl()

        val result = repo.bookSlot("TS-DOES-NOT-EXIST", "M-999", "New Member")

        assertTrue(result.isFailure)
    }

    @Test
    fun currentBookings_onInit_matchesTheRealAttendanceLedgerNotAnIndependentSeedValue() = runTest {
        // Regression test for SLOT-1: TimeSlot.currentBookings used to be seeded independently
        // of the real attendance ledger - for TS-002 the seed data showed "24/30 booked" while
        // only 3 real AttendanceRecord rows (ATT-005/006/007) actually existed. Now it's derived
        // from the ledger at construction time, so the two can never disagree.
        val repo = TimeSlotRepositoryImpl()

        val slot = repo.getAllSlots().getOrThrow().first { it.id == "TS-002" }
        val realBookedCount = repo.getAttendanceForSlot("TS-002").getOrThrow().size

        assertEquals(3, realBookedCount)
        assertEquals(realBookedCount, slot.currentBookings)
    }

    @Test
    fun currentBookings_everySlot_matchesItsRealAttendanceCountOnInit() = runTest {
        val repo = TimeSlotRepositoryImpl()
        val allSlots = repo.getAllSlots().getOrThrow()

        allSlots.forEach { slot ->
            val realCount = repo.getAttendanceForSlot(slot.id).getOrThrow().size
            assertEquals(realCount, slot.currentBookings, "Slot ${slot.id} displayed count must match its real ledger count")
        }
    }

    @Test
    fun bookSlot_currentBookingsStaysInSyncWithRealLedger_untilTrueCapacityIsHit() = runTest {
        val repo = TimeSlotRepositoryImpl()
        val maxCapacity = repo.getAllSlots().getOrThrow().first { it.id == "TS-002" }.maxCapacity
        assertEquals(30, maxCapacity)

        var lastResult: Result<AttendanceRecord> = Result.failure(Exception("not run"))
        // 3 real bookings already exist for TS-002; fill the remaining 27 seats.
        repeat(27) { i ->
            lastResult = repo.bookSlot("TS-002", "M-TEST-$i", "Test Member $i")
        }
        assertTrue(lastResult.isSuccess)
        val fullSlot = repo.getAllSlots().getOrThrow().first { it.id == "TS-002" }
        assertEquals(30, fullSlot.currentBookings, "Displayed count must reflect the now-full real ledger")

        // The 28th booking correctly hits the true capacity ceiling.
        val overflow = repo.bookSlot("TS-002", "M-TEST-OVERFLOW", "Overflow Member")
        assertTrue(overflow.isFailure)
        assertTrue(overflow.exceptionOrNull()?.message?.contains("full capacity") == true)
    }

    @Test
    fun checkInMember_withoutAnExistingBookedRecord_fails() = runTest {
        val repo = TimeSlotRepositoryImpl()

        val result = repo.checkInMember("TS-001", "M-NEVER-BOOKED")

        assertTrue(result.isFailure)
    }

    @Test
    fun checkInMember_forExistingBookedRecord_transitionsToCheckedIn() = runTest {
        val repo = TimeSlotRepositoryImpl()
        // ATT-003 seeds M-102 as BOOKED on TS-001.
        val result = repo.checkInMember("TS-001", "M-102")

        assertTrue(result.isSuccess)
        assertEquals(AttendanceStatus.CHECKED_IN, result.getOrThrow().status)
    }

    @Test
    fun checkOutMember_recordNotCheckedIn_fails() = runTest {
        val repo = TimeSlotRepositoryImpl()
        // ATT-003 is BOOKED (not CHECKED_IN) for M-102 on TS-001.
        val result = repo.checkOutMember("ATT-003")

        assertTrue(result.isFailure)
    }

    @Test
    fun checkOutMember_forCheckedInRecord_transitionsToCheckedOut() = runTest {
        val repo = TimeSlotRepositoryImpl()
        // ATT-002 seeds M-104 as CHECKED_IN on TS-001.
        val result = repo.checkOutMember("ATT-002")

        assertTrue(result.isSuccess)
        assertEquals(AttendanceStatus.CHECKED_OUT, result.getOrThrow().status)
    }

    @Test
    fun deleteSlot_alsoRemovesItsAttendanceRecords() = runTest {
        val repo = TimeSlotRepositoryImpl()
        assertTrue(repo.getAttendanceForSlot("TS-001").getOrThrow().isNotEmpty())

        repo.deleteSlot("TS-001")

        assertTrue(repo.getAllSlots().getOrThrow().none { it.id == "TS-001" })
        assertTrue(repo.getAttendanceForSlot("TS-001").getOrThrow().isEmpty())
    }

    @Test
    fun toggleSlotActive_unknownSlotId_fails() = runTest {
        val repo = TimeSlotRepositoryImpl()

        val result = repo.toggleSlotActive("TS-DOES-NOT-EXIST")

        assertTrue(result.isFailure)
    }

    @Test
    fun toggleSlotActive_flipsBooleanEachCall() = runTest {
        val repo = TimeSlotRepositoryImpl()
        val initiallyActive = repo.getAllSlots().getOrThrow().first { it.id == "TS-001" }.isActive

        val toggledOnce = repo.toggleSlotActive("TS-001").getOrThrow()
        assertEquals(!initiallyActive, toggledOnce.isActive)

        val toggledTwice = repo.toggleSlotActive("TS-001").getOrThrow()
        assertEquals(initiallyActive, toggledTwice.isActive)
    }

    @Test
    fun getSlotsByDay_onlyReturnsSlotsForThatDay() = runTest {
        val repo = TimeSlotRepositoryImpl()

        val mondaySlots = repo.getSlotsByDay(DayOfWeek.MONDAY).getOrThrow()

        assertTrue(mondaySlots.isNotEmpty())
        assertFalse(mondaySlots.any { it.dayOfWeek != DayOfWeek.MONDAY })
    }
}
