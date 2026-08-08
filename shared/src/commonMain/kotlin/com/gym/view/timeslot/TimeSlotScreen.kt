package com.gym.view.timeslot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.timeslot.TimeSlotUiState
import com.gym.model.timeslot.*
import com.gym.view.theme.*

// Slot Category Color Mapping
private fun getCategoryColor(category: SlotCategory): Color = when (category) {
    SlotCategory.YOGA -> Color(0xFF14B8A6)       // Teal
    SlotCategory.CARDIO -> Color(0xFFF97316)      // Orange
    SlotCategory.STRENGTH -> Color(0xFF3B82F6)    // Blue
    SlotCategory.CROSSFIT -> Color(0xFFEF4444)    // Red
    SlotCategory.ZUMBA -> Color(0xFFA855F7)       // Purple
    SlotCategory.OPEN_GYM -> Color(0xFF6366F1)    // Indigo
}

private fun getStatusColor(status: AttendanceStatus): Color = when (status) {
    AttendanceStatus.BOOKED -> Color(0xFFF59E0B)     // Amber
    AttendanceStatus.CHECKED_IN -> Color(0xFF10B981)  // Emerald
    AttendanceStatus.CHECKED_OUT -> Color(0xFF6366F1) // Indigo
    AttendanceStatus.NO_SHOW -> Color(0xFFEF4444)     // Red
}

/**
 * View (Passive Compose Multiplatform Screen): Time Slot & Attendance Management.
 * Renders weekly day tabs, slot cards with capacity progress, attendance panels,
 * and a create-slot modal dialog. Dynamically adapts between Dark / Light mode.
 */
@Composable
fun TimeSlotScreen(
    state: TimeSlotUiState,
    isDarkMode: Boolean = true,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    onDaySelected: (DayOfWeek) -> Unit,
    onSlotSelected: (TimeSlot) -> Unit,
    onToggleCreateDialog: (Boolean) -> Unit,
    onCreateSlot: () -> Unit,
    onDeleteSlot: (String) -> Unit,
    onToggleSlotActive: (String) -> Unit,
    onCheckInMember: (slotId: String, memberId: String) -> Unit,
    onCheckOutMember: (attendanceId: String) -> Unit,
    onBookSlot: (slotId: String) -> Unit,
    onNewSlotTitleChanged: (String) -> Unit,
    onNewSlotTrainerChanged: (String) -> Unit,
    onNewSlotDayChanged: (DayOfWeek) -> Unit,
    onNewSlotStartTimeChanged: (String) -> Unit,
    onNewSlotEndTimeChanged: (String) -> Unit,
    onNewSlotCapacityChanged: (String) -> Unit,
    onNewSlotCategoryChanged: (SlotCategory) -> Unit,
    onClearMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = getAppThemeColors(isDarkMode)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.bg)
                .padding(16.dp)
        ) {
            // ─── 1. Navigation Header ───
            NavigationHeader(theme, onOpenDrawer, onNavigateBack, state)

            Spacer(modifier = Modifier.height(16.dp))

            // ─── 2. Summary Stats Banner ───
            SummaryStatsBanner(theme, isDarkMode, state)

            Spacer(modifier = Modifier.height(16.dp))

            // ─── 3. Weekly Day Tabs ───
            WeeklyDayTabs(theme, isDarkMode, state, onDaySelected)

            Spacer(modifier = Modifier.height(16.dp))

            // ─── 4. Success/Error Messages ───
            AnimatedVisibility(
                visible = state.successMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                state.successMessage?.let { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = GymAccentSecondary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).clickable { onClearMessages() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GymAccentSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = msg, color = GymAccentSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = GymAccentSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                state.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = GymAccentDanger.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).clickable { onClearMessages() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = GymAccentDanger, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = error, color = GymAccentDanger, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = GymAccentDanger, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ─── 5. Slot Cards List ───
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GymAccentPrimary)
                }
            } else if (state.daySlots.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🏖️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No slots scheduled for ${state.selectedDay.full}",
                            color = theme.textMuted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to create a new time slot",
                            color = theme.textMuted.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.daySlots, key = { it.id }) { slot ->
                        SlotCard(
                            slot = slot,
                            theme = theme,
                            isDarkMode = isDarkMode,
                            isExpanded = state.selectedSlot?.id == slot.id && state.isAttendanceExpanded,
                            attendanceRecords = if (state.selectedSlot?.id == slot.id) state.attendanceRecords else emptyList(),
                            onSlotClicked = { onSlotSelected(slot) },
                            onDeleteSlot = { onDeleteSlot(slot.id) },
                            onToggleActive = { onToggleSlotActive(slot.id) },
                            onCheckIn = { memberId -> onCheckInMember(slot.id, memberId) },
                            onCheckOut = { attendanceId -> onCheckOutMember(attendanceId) },
                            onBook = { onBookSlot(slot.id) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }

        // ─── 6. Floating Action Button (Create Slot) ───
        FloatingActionButton(
            onClick = { onToggleCreateDialog(true) },
            containerColor = GymAccentPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create New Time Slot", modifier = Modifier.size(28.dp))
        }

        // ─── 7. Create Slot Modal Dialog ───
        if (state.isCreateDialogOpen) {
            CreateSlotDialog(
                state = state,
                theme = theme,
                isDarkMode = isDarkMode,
                onDismiss = { onToggleCreateDialog(false) },
                onCreateSlot = onCreateSlot,
                onTitleChanged = onNewSlotTitleChanged,
                onTrainerChanged = onNewSlotTrainerChanged,
                onDayChanged = onNewSlotDayChanged,
                onStartTimeChanged = onNewSlotStartTimeChanged,
                onEndTimeChanged = onNewSlotEndTimeChanged,
                onCapacityChanged = onNewSlotCapacityChanged,
                onCategoryChanged = onNewSlotCategoryChanged
            )
        }
    }
}

// ───────────────────────────────────────────────────
// Sub-Components
// ───────────────────────────────────────────────────

@Composable
private fun NavigationHeader(
    theme: AppThemeColors,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    state: TimeSlotUiState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenDrawer, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = theme.textMain)
            }
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textMain)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "Time Slot Schedule",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMain
                )
                Text(
                    text = "${state.totalSlotsThisWeek} slots this week  •  ${state.selectedDay.full}",
                    fontSize = 12.sp,
                    color = theme.textMuted
                )
            }
        }

        // Schedule icon badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GymAccentPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = GymAccentPrimary, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun SummaryStatsBanner(theme: AppThemeColors, isDarkMode: Boolean, state: TimeSlotUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.cardBorder, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Weekly Slots", value = "${state.totalSlotsThisWeek}", color = GymAccentPrimary, theme = theme)
            VerticalDivider(modifier = Modifier.height(40.dp), color = theme.dividerColor)
            StatItem(label = "Today's Bookings", value = "${state.totalBookingsToday}", color = GymAccentSecondary, theme = theme)
            VerticalDivider(modifier = Modifier.height(40.dp), color = theme.dividerColor)
            StatItem(label = "Checked In", value = "${state.checkedInToday}", color = Color(0xFF14B8A6), theme = theme)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color, theme: AppThemeColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = theme.textMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun WeeklyDayTabs(
    theme: AppThemeColors,
    isDarkMode: Boolean,
    state: TimeSlotUiState,
    onDaySelected: (DayOfWeek) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(DayOfWeek.entries) { day ->
            val isSelected = state.selectedDay == day
            val slotCount = state.slotCountPerDay[day] ?: 0

            Surface(
                onClick = { onDaySelected(day) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) GymAccentPrimary else theme.cardBg,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = if (isSelected) GymAccentPrimary else theme.cardBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.short,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else theme.textMain
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Slot count badge
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.25f)
                                else theme.dividerColor
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$slotCount",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else theme.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotCard(
    slot: TimeSlot,
    theme: AppThemeColors,
    isDarkMode: Boolean,
    isExpanded: Boolean,
    attendanceRecords: List<AttendanceRecord>,
    onSlotClicked: () -> Unit,
    onDeleteSlot: () -> Unit,
    onToggleActive: () -> Unit,
    onCheckIn: (memberId: String) -> Unit,
    onCheckOut: (attendanceId: String) -> Unit,
    onBook: () -> Unit
) {
    val categoryColor = getCategoryColor(slot.category)
    val capacityFraction = if (slot.maxCapacity > 0) slot.currentBookings.toFloat() / slot.maxCapacity else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.cardBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row: Category + Title + Time
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onSlotClicked() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category emoji badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = slot.category.emoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = slot.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textMain,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (!slot.isActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GymAccentDanger.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("DISABLED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GymAccentDanger)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${slot.trainerName}  •  ${slot.category.displayName}",
                        fontSize = 12.sp,
                        color = theme.textMuted
                    )
                }

                // Time range badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = "${slot.startTime} – ${slot.endTime}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Capacity progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${slot.currentBookings}/${slot.maxCapacity} booked",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.textMuted
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.dividerColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = capacityFraction.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    capacityFraction >= 0.9f -> GymAccentDanger
                                    capacityFraction >= 0.7f -> GymAccentWarning
                                    else -> GymAccentSecondary
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = theme.dividerColor)
            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand/collapse attendance
                Surface(
                    onClick = onSlotClicked,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "View Attendance",
                            tint = GymAccentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isExpanded) "Hide Attendance" else "View Attendance",
                            fontSize = 12.sp,
                            color = GymAccentPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Book slot button
                    Surface(
                        onClick = onBook,
                        shape = RoundedCornerShape(8.dp),
                        color = GymAccentSecondary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = GymAccentSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Book", fontSize = 11.sp, color = GymAccentSecondary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Toggle active
                    IconButton(onClick = onToggleActive, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (slot.isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                            contentDescription = if (slot.isActive) "Disable Slot" else "Enable Slot",
                            tint = if (slot.isActive) GymAccentSecondary else theme.textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Delete slot
                    IconButton(onClick = onDeleteSlot, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Slot",
                            tint = GymAccentDanger.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ─── Expanded Attendance Panel ───
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = theme.dividerColor)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Attendance (${attendanceRecords.size} records)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMain
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (attendanceRecords.isEmpty()) {
                        Text(
                            text = "No attendance records yet",
                            fontSize = 12.sp,
                            color = theme.textMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        attendanceRecords.forEach { record ->
                            AttendanceRecordRow(
                                record = record,
                                theme = theme,
                                onCheckIn = { onCheckIn(record.memberId) },
                                onCheckOut = { onCheckOut(record.id) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceRecordRow(
    record: AttendanceRecord,
    theme: AppThemeColors,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit
) {
    val statusColor = getStatusColor(record.status)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(statusColor.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Member avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = record.memberName.take(1).uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.memberName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = theme.textMain
            )
            Row {
                Text(
                    text = "${record.status.emoji} ${record.status.displayName}",
                    fontSize = 11.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
                if (record.checkInTime != null) {
                    Text(
                        text = "  •  In: ${record.checkInTime}",
                        fontSize = 11.sp,
                        color = theme.textMuted
                    )
                }
                if (record.checkOutTime != null) {
                    Text(
                        text = "  Out: ${record.checkOutTime}",
                        fontSize = 11.sp,
                        color = theme.textMuted
                    )
                }
            }
        }

        // Action buttons based on status
        when (record.status) {
            AttendanceStatus.BOOKED -> {
                Surface(
                    onClick = onCheckIn,
                    shape = RoundedCornerShape(6.dp),
                    color = GymAccentSecondary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Check In",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymAccentSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            AttendanceStatus.CHECKED_IN -> {
                Surface(
                    onClick = onCheckOut,
                    shape = RoundedCornerShape(6.dp),
                    color = GymAccentPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Check Out",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymAccentPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            else -> { /* No action for CHECKED_OUT or NO_SHOW */ }
        }
    }
}

// ───────────────────────────────────────────────────
// Create Slot Modal Dialog
// ───────────────────────────────────────────────────

@Composable
private fun CreateSlotDialog(
    state: TimeSlotUiState,
    theme: AppThemeColors,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onCreateSlot: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onTrainerChanged: (String) -> Unit,
    onDayChanged: (DayOfWeek) -> Unit,
    onStartTimeChanged: (String) -> Unit,
    onEndTimeChanged: (String) -> Unit,
    onCapacityChanged: (String) -> Unit,
    onCategoryChanged: (SlotCategory) -> Unit
) {
    var isDayDropdownExpanded by remember { mutableStateOf(false) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.cardBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = GymAccentPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Slot", color = theme.textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Title field
                OutlinedTextField(
                    value = state.newSlotTitle,
                    onValueChange = onTitleChanged,
                    label = { Text("Slot Title", color = theme.textMuted) },
                    placeholder = { Text("e.g. Morning Power Yoga", color = theme.textMuted.copy(alpha = 0.5f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GymAccentPrimary,
                        unfocusedBorderColor = theme.cardBorder,
                        focusedTextColor = theme.textMain,
                        unfocusedTextColor = theme.textMain,
                        cursorColor = GymAccentPrimary
                    )
                )

                // Trainer field
                OutlinedTextField(
                    value = state.newSlotTrainer,
                    onValueChange = onTrainerChanged,
                    label = { Text("Trainer Name", color = theme.textMuted) },
                    placeholder = { Text("e.g. Coach Arjun", color = theme.textMuted.copy(alpha = 0.5f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GymAccentPrimary,
                        unfocusedBorderColor = theme.cardBorder,
                        focusedTextColor = theme.textMain,
                        unfocusedTextColor = theme.textMain,
                        cursorColor = GymAccentPrimary
                    )
                )

                // Day selector dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = "${state.newSlotDay.full}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day of Week", color = theme.textMuted) },
                        trailingIcon = {
                            IconButton(onClick = { isDayDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = theme.textMuted)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { isDayDropdownExpanded = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GymAccentPrimary,
                            unfocusedBorderColor = theme.cardBorder,
                            focusedTextColor = theme.textMain,
                            unfocusedTextColor = theme.textMain
                        )
                    )
                    DropdownMenu(
                        expanded = isDayDropdownExpanded,
                        onDismissRequest = { isDayDropdownExpanded = false },
                        modifier = Modifier.background(theme.cardBg)
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day.full, color = theme.textMain, fontSize = 14.sp) },
                                onClick = {
                                    onDayChanged(day)
                                    isDayDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Time range row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.newSlotStartTime,
                        onValueChange = onStartTimeChanged,
                        label = { Text("Start", color = theme.textMuted) },
                        placeholder = { Text("06:00", color = theme.textMuted.copy(alpha = 0.5f)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GymAccentPrimary,
                            unfocusedBorderColor = theme.cardBorder,
                            focusedTextColor = theme.textMain,
                            unfocusedTextColor = theme.textMain,
                            cursorColor = GymAccentPrimary
                        )
                    )
                    OutlinedTextField(
                        value = state.newSlotEndTime,
                        onValueChange = onEndTimeChanged,
                        label = { Text("End", color = theme.textMuted) },
                        placeholder = { Text("07:00", color = theme.textMuted.copy(alpha = 0.5f)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GymAccentPrimary,
                            unfocusedBorderColor = theme.cardBorder,
                            focusedTextColor = theme.textMain,
                            unfocusedTextColor = theme.textMain,
                            cursorColor = GymAccentPrimary
                        )
                    )
                }

                // Capacity field
                OutlinedTextField(
                    value = state.newSlotCapacity,
                    onValueChange = onCapacityChanged,
                    label = { Text("Max Capacity", color = theme.textMuted) },
                    placeholder = { Text("20", color = theme.textMuted.copy(alpha = 0.5f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GymAccentPrimary,
                        unfocusedBorderColor = theme.cardBorder,
                        focusedTextColor = theme.textMain,
                        unfocusedTextColor = theme.textMain,
                        cursorColor = GymAccentPrimary
                    )
                )

                // Category selector
                Text("Category:", fontSize = 12.sp, color = theme.textMuted, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SlotCategory.entries) { category ->
                        val isSelected = state.newSlotCategory == category
                        val catColor = getCategoryColor(category)
                        Surface(
                            onClick = { onCategoryChanged(category) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) catColor.copy(alpha = 0.2f) else Color.Transparent,
                            modifier = Modifier.border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) catColor else theme.cardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = category.emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = category.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) catColor else theme.textMain
                                )
                            }
                        }
                    }
                }

                // Error message in dialog
                state.errorMessage?.let { error ->
                    Text(
                        text = error,
                        fontSize = 12.sp,
                        color = GymAccentDanger,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCreateSlot,
                colors = ButtonDefaults.buttonColors(containerColor = GymAccentPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Slot", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = theme.textMuted, fontWeight = FontWeight.Medium)
            }
        }
    )
}
