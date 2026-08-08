package com.gym.controller.timeslot

import com.gym.model.timeslot.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Controller: Dedicated Kotlin class for Time Slot & Attendance Management MVC.
 * Acts as glue between the View and Model layer without Android ViewModel or MVI pipelines.
 * Manages day filtering, slot CRUD, attendance check-in/out, and create-slot form state.
 */
class TimeSlotController(
    private val repository: TimeSlotRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(TimeSlotUiState())
    val uiState: StateFlow<TimeSlotUiState> = _uiState.asStateFlow()

    init {
        loadAllSlots()
    }

    // ──────────────────────────────────────────────
    // Day & Slot Selection Handlers
    // ──────────────────────────────────────────────

    /**
     * User taps a day tab (Mon–Sun). Filters slots for that day.
     */
    fun onDaySelected(day: DayOfWeek) {
        _uiState.update {
            it.copy(
                selectedDay = day,
                selectedSlot = null,
                isAttendanceExpanded = false,
                attendanceRecords = emptyList()
            )
        }
        fetchSlotsForDay(day)
    }

    /**
     * User taps a slot card to expand its attendance detail.
     */
    fun onSlotSelected(slot: TimeSlot) {
        val currentSelected = _uiState.value.selectedSlot
        if (currentSelected?.id == slot.id && _uiState.value.isAttendanceExpanded) {
            // Collapse if tapping the same slot
            _uiState.update { it.copy(selectedSlot = null, isAttendanceExpanded = false, attendanceRecords = emptyList()) }
        } else {
            _uiState.update { it.copy(selectedSlot = slot, isAttendanceExpanded = true) }
            loadAttendanceForSlot(slot.id)
        }
    }

    // ──────────────────────────────────────────────
    // Create Slot Dialog & Form Handlers
    // ──────────────────────────────────────────────

    fun onToggleCreateDialog(open: Boolean) {
        _uiState.update {
            if (open) {
                it.copy(isCreateDialogOpen = true, errorMessage = null, successMessage = null)
            } else {
                it.copy(
                    isCreateDialogOpen = false,
                    newSlotTitle = "", newSlotTrainer = "",
                    newSlotDay = it.selectedDay,
                    newSlotStartTime = "06:00", newSlotEndTime = "07:00",
                    newSlotCapacity = "20", newSlotCategory = SlotCategory.OPEN_GYM
                )
            }
        }
    }

    fun onNewSlotTitleChanged(value: String) {
        _uiState.update { it.copy(newSlotTitle = value) }
    }

    fun onNewSlotTrainerChanged(value: String) {
        _uiState.update { it.copy(newSlotTrainer = value) }
    }

    fun onNewSlotDayChanged(day: DayOfWeek) {
        _uiState.update { it.copy(newSlotDay = day) }
    }

    fun onNewSlotStartTimeChanged(value: String) {
        _uiState.update { it.copy(newSlotStartTime = value) }
    }

    fun onNewSlotEndTimeChanged(value: String) {
        _uiState.update { it.copy(newSlotEndTime = value) }
    }

    fun onNewSlotCapacityChanged(value: String) {
        _uiState.update { it.copy(newSlotCapacity = value) }
    }

    fun onNewSlotCategoryChanged(category: SlotCategory) {
        _uiState.update { it.copy(newSlotCategory = category) }
    }

    /**
     * Validates and creates a new slot from form fields.
     */
    fun onCreateSlot() {
        val state = _uiState.value
        // Validation
        if (state.newSlotTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Slot title is required") }
            return
        }
        if (state.newSlotTrainer.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Trainer name is required") }
            return
        }
        val capacity = state.newSlotCapacity.toIntOrNull()
        if (capacity == null || capacity <= 0) {
            _uiState.update { it.copy(errorMessage = "Capacity must be a positive number") }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val newSlot = TimeSlot(
                id = "", // Repository will assign ID
                title = state.newSlotTitle.trim(),
                trainerName = state.newSlotTrainer.trim(),
                dayOfWeek = state.newSlotDay,
                startTime = state.newSlotStartTime,
                endTime = state.newSlotEndTime,
                maxCapacity = capacity,
                currentBookings = 0,
                category = state.newSlotCategory,
                isActive = true
            )
            val result = repository.createSlot(newSlot)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCreateDialogOpen = false,
                            successMessage = "Slot \"${newSlot.title}\" created successfully!",
                            newSlotTitle = "", newSlotTrainer = "",
                            newSlotStartTime = "06:00", newSlotEndTime = "07:00",
                            newSlotCapacity = "20", newSlotCategory = SlotCategory.OPEN_GYM
                        )
                    }
                    loadAllSlots()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to create slot: ${error.message}") }
                }
            )
        }
    }

    // ──────────────────────────────────────────────
    // Slot Management Actions
    // ──────────────────────────────────────────────

    /**
     * Admin deletes a slot entirely.
     */
    fun onDeleteSlot(slotId: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.deleteSlot(slotId)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedSlot = null,
                            isAttendanceExpanded = false,
                            attendanceRecords = emptyList(),
                            successMessage = "Slot deleted successfully"
                        )
                    }
                    loadAllSlots()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to delete: ${error.message}") }
                }
            )
        }
    }

    /**
     * Admin enables/disables a slot.
     */
    fun onToggleSlotActive(slotId: String) {
        scope.launch {
            val result = repository.toggleSlotActive(slotId)
            result.fold(
                onSuccess = { updated ->
                    val statusText = if (updated.isActive) "enabled" else "disabled"
                    _uiState.update { it.copy(successMessage = "Slot ${statusText}: ${updated.title}") }
                    loadAllSlots()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = "Toggle failed: ${error.message}") }
                }
            )
        }
    }

    // ──────────────────────────────────────────────
    // Attendance Actions
    // ──────────────────────────────────────────────

    /**
     * Staff/Admin checks in a member to their booked slot.
     */
    fun onCheckInMember(slotId: String, memberId: String) {
        scope.launch {
            val result = repository.checkInMember(slotId, memberId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "Member checked in successfully!") }
                    loadAttendanceForSlot(slotId)
                    loadAllSlots()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = "Check-in failed: ${error.message}") }
                }
            )
        }
    }

    /**
     * Staff/Admin checks out a member from their slot.
     */
    fun onCheckOutMember(attendanceId: String) {
        scope.launch {
            val result = repository.checkOutMember(attendanceId)
            result.fold(
                onSuccess = { record ->
                    _uiState.update { it.copy(successMessage = "Member checked out successfully!") }
                    loadAttendanceForSlot(record.slotId)
                    loadAllSlots()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = "Check-out failed: ${error.message}") }
                }
            )
        }
    }

    /**
     * Member books themselves into a slot.
     */
    fun onBookSlot(slotId: String, memberId: String, memberName: String) {
        scope.launch {
            val result = repository.bookSlot(slotId, memberId, memberName)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "Slot booked successfully!") }
                    loadAttendanceForSlot(slotId)
                    loadAllSlots()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = "Booking failed: ${error.message}") }
                }
            )
        }
    }

    // ──────────────────────────────────────────────
    // Message Clearing
    // ──────────────────────────────────────────────

    fun onClearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    // ──────────────────────────────────────────────
    // Private Data Loading
    // ──────────────────────────────────────────────

    private fun loadAllSlots() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getAllSlots()
            result.fold(
                onSuccess = { allSlots ->
                    val selectedDay = _uiState.value.selectedDay
                    val daySlots = allSlots.filter { it.dayOfWeek == selectedDay }
                    val slotCountPerDay = DayOfWeek.entries.associateWith { day ->
                        allSlots.count { it.dayOfWeek == day }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allSlots = allSlots,
                            daySlots = daySlots,
                            totalSlotsThisWeek = allSlots.size,
                            totalBookingsToday = daySlots.sumOf { slot -> slot.currentBookings },
                            checkedInToday = daySlots.sumOf { slot -> slot.currentBookings / 2 },
                            slotCountPerDay = slotCountPerDay
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Load failed: ${error.message}") }
                }
            )
        }
    }

    private fun fetchSlotsForDay(day: DayOfWeek) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getSlotsByDay(day)
            result.fold(
                onSuccess = { slots ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            daySlots = slots,
                            totalBookingsToday = slots.sumOf { slot -> slot.currentBookings },
                            checkedInToday = slots.sumOf { slot -> slot.currentBookings / 2 }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load day slots: ${error.message}") }
                }
            )
        }
    }

    private fun loadAttendanceForSlot(slotId: String) {
        scope.launch {
            val result = repository.getAttendanceForSlot(slotId)
            result.onSuccess { records ->
                _uiState.update { it.copy(attendanceRecords = records) }
            }
        }
    }
}
