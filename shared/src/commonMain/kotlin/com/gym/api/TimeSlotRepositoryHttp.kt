package com.gym.api

import com.gym.model.timeslot.AttendanceRecord
import com.gym.model.timeslot.DayOfWeek
import com.gym.model.timeslot.TimeSlot
import com.gym.model.timeslot.TimeSlotRepository
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TimeSlotRepositoryHttp : TimeSlotRepository {
    var authToken: String? = null

    override suspend fun getSlotsByDay(day: DayOfWeek): Result<List<TimeSlot>> = withContext(Dispatchers.Default) {
        try {
            val response = HttpClientProvider.client.get("${HttpClientProvider.BASE_URL}/api/timeslots")
            if (response.status.isSuccess()) {
                val allSlots: List<TimeSlot> = response.body()
                Result.success(allSlots.filter { it.dayOfWeek == day })
            } else {
                Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getAllSlots(): Result<List<TimeSlot>> = withContext(Dispatchers.Default) {
        try {
            val response = HttpClientProvider.client.get("${HttpClientProvider.BASE_URL}/api/timeslots") {
                authToken?.let { header("Authorization", "Bearer $it") }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun createSlot(slot: TimeSlot): Result<TimeSlot> = withContext(Dispatchers.Default) {
        try {
            val response = HttpClientProvider.client.post("${HttpClientProvider.BASE_URL}/api/timeslots") {
                contentType(ContentType.Application.Json)
                authToken?.let { header("Authorization", "Bearer $it") }
                setBody(slot)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // --- The below are mocked for now to keep the demo simple, but they would similarly call Ktor ---

    override suspend fun deleteSlot(slotId: String): Result<Unit> {
        return Result.success(Unit) // TODO: Implement DELETE /api/timeslots/{id}
    }

    override suspend fun toggleSlotActive(slotId: String): Result<TimeSlot> {
        return Result.failure(Exception("Not implemented on backend yet"))
    }

    override suspend fun getAttendanceForSlot(slotId: String): Result<List<AttendanceRecord>> {
        return Result.success(emptyList()) // TODO: Implement GET /api/timeslots/{id}/attendance
    }

    override suspend fun checkInMember(slotId: String, memberId: String): Result<AttendanceRecord> {
        return Result.failure(Exception("Not implemented on backend yet"))
    }

    override suspend fun checkOutMember(attendanceId: String): Result<AttendanceRecord> {
        return Result.failure(Exception("Not implemented on backend yet"))
    }

    override suspend fun bookSlot(slotId: String, memberId: String, memberName: String): Result<AttendanceRecord> {
        return Result.failure(Exception("Not implemented on backend yet"))
    }
}
