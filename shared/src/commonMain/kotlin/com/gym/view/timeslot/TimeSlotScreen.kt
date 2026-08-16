package com.gym.view.timeslot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.timeslot.TimeSlotUiState
import com.gym.model.timeslot.*
import com.gym.view.theme.*

/**
 * View: Time Slot & Live Class Schedule matching Google Stitch ("Elite Gym Management Pro").
 * Supports weekly day navigation, real-time booking capacity meters, attendance rosters, and slot creation.
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(theme.cardBg)
                            .border(1.dp, theme.cardBorder, CircleShape)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = theme.textMain)
                    }

                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(theme.cardBg)
                            .border(1.dp, theme.cardBorder, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textMain)
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Class Schedule",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = theme.textMain,
                                maxLines = 1
                            )
                            Surface(
                                color = GymAccentPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${state.daySlots.size}",
                                    color = GymAccentPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Attendance & coaching schedule",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textMuted,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = { onToggleCreateDialog(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = GymAccentPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
            }

            // 2. Weekly Day Selector Tabs (Mon, Tue, Wed, Thu, Fri, Sat, Sun)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DayOfWeek.entries) { day ->
                    val isSelected = state.selectedDay == day
                    val slotCountForDay = state.allSlots.count { it.dayOfWeek == day }
                    Surface(
                        onClick = { onDaySelected(day) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) GymAccentPrimary else theme.cardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) GymAccentPrimary else theme.cardBorder
                        ),
                        modifier = Modifier.width(82.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day.short,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else theme.textMain
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$slotCountForDay slots",
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else theme.textMuted
                            )
                        }
                    }
                }
            }

            // 3. Telemetry KPI Strip (Horizontally scrollable for small screens)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val totalCap = state.daySlots.sumOf { it.maxCapacity }
                val totalBooked = state.daySlots.sumOf { it.currentBookings }
                val occupancyPct = if (totalCap > 0) (totalBooked.toFloat() / totalCap * 100).toInt() else 0

                KpiMiniCard("Total Capacity", "$totalCap spots", Icons.Default.Groups, theme)
                KpiMiniCard("Active Bookings", "$totalBooked booked", Icons.Default.EventAvailable, theme)
                KpiMiniCard("Occupancy Rate", "$occupancyPct%", Icons.AutoMirrored.Filled.TrendingUp, theme)
            }

            // 4. Notifications / Alerts
            AnimatedVisibility(
                visible = state.successMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.successMessage?.let { msg ->
                    Surface(
                        color = GymAccentSecondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GymAccentSecondary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().clickable { onClearMessages() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GymAccentSecondary, modifier = Modifier.size(16.dp))
                            Text(msg, color = GymAccentSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = GymAccentSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // 5. Scrollable Class Schedule Bento List
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GymAccentPrimary)
                }
            } else if (state.daySlots.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = theme.textMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No classes scheduled for ${state.selectedDay.full}", color = theme.textMuted, fontSize = 15.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.daySlots, key = { it.id }) { slot ->
                        val isExpanded = state.selectedSlot?.id == slot.id && state.isAttendanceExpanded
                        ClassSlotBentoCard(
                            slot = slot,
                            isExpanded = isExpanded,
                            attendanceRecords = if (state.selectedSlot?.id == slot.id) state.attendanceRecords else emptyList(),
                            theme = theme,
                            onSlotClicked = { onSlotSelected(slot) },
                            onBook = { onBookSlot(slot.id) },
                            onDelete = { onDeleteSlot(slot.id) },
                            onCheckIn = { memId -> onCheckInMember(slot.id, memId) },
                            onCheckOut = { attId -> onCheckOutMember(attId) }
                        )
                    }
                }
            }
        }

        // Create Slot Dialog Modal
        if (state.isCreateDialogOpen) {
            CreateSlotModalDialog(
                state = state,
                theme = theme,
                onDismiss = { onToggleCreateDialog(false) },
                onCreate = onCreateSlot,
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

@Composable
private fun KpiMiniCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: AppThemeColors,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GymAccentPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = GymAccentPrimary, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(label, fontSize = 11.sp, color = theme.textMuted)
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
            }
        }
    }
}

@Composable
private fun ClassSlotBentoCard(
    slot: TimeSlot,
    isExpanded: Boolean,
    attendanceRecords: List<AttendanceRecord>,
    theme: AppThemeColors,
    onSlotClicked: () -> Unit,
    onBook: () -> Unit,
    onDelete: () -> Unit,
    onCheckIn: (String) -> Unit,
    onCheckOut: (String) -> Unit
) {
    val occupancyRatio = if (slot.maxCapacity > 0) slot.currentBookings.toFloat() / slot.maxCapacity else 0f
    val isFull = slot.currentBookings >= slot.maxCapacity

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Category Icon + Title + Trainer + Time + Book Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GymAccentPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(slot.category.emoji, fontSize = 20.sp)
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = slot.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = theme.textMain,
                                maxLines = 1
                            )
                            Surface(
                                color = GymAccentPrimary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = slot.category.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymAccentPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${slot.trainerName} • ${slot.startTime} - ${slot.endTime}",
                            fontSize = 11.sp,
                            color = theme.textMuted,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = onBook,
                        enabled = !isFull,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFull) theme.cardElevated else GymAccentSecondary,
                            contentColor = if (isFull) theme.textMuted else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(if (isFull) "Full" else "Book", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    IconButton(onClick = onSlotClicked, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.Group,
                            contentDescription = "Roster",
                            tint = GymAccentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = GymAccentDanger, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Capacity Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Capacity", fontSize = 11.sp, color = theme.textMuted)
                Text(
                    text = "${slot.currentBookings} / ${slot.maxCapacity} Members Booked",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isFull) GymAccentDanger else theme.textMain
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { occupancyRatio.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (isFull) GymAccentDanger else GymAccentSecondary,
                trackColor = theme.cardElevated
            )

            // Attendance Roster Expanded Drawer
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = theme.dividerColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ACTIVE CLASS ROSTER (${attendanceRecords.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = theme.textMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (attendanceRecords.isEmpty()) {
                        Text("No members have checked in or booked yet.", fontSize = 12.sp, color = theme.textMuted)
                    } else {
                        attendanceRecords.forEach { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.cardElevated)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(GymAccentPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(record.memberName.take(1), fontWeight = FontWeight.Bold, color = GymAccentPrimary, fontSize = 12.sp)
                                    }
                                    Column {
                                        Text(record.memberName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = theme.textMain)
                                        Text(record.status.displayName, fontSize = 10.sp, color = GymAccentSecondary)
                                    }
                                }

                                if (record.status == AttendanceStatus.BOOKED) {
                                    Button(
                                        onClick = { onCheckIn(record.memberId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GymAccentSecondary),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Check In", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                } else if (record.status == AttendanceStatus.CHECKED_IN) {
                                    Button(
                                        onClick = { onCheckOut(record.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = theme.cardBorder),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Check Out", fontSize = 10.sp, color = theme.textMain)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateSlotModalDialog(
    state: TimeSlotUiState,
    theme: AppThemeColors,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onTrainerChanged: (String) -> Unit,
    onDayChanged: (DayOfWeek) -> Unit,
    onStartTimeChanged: (String) -> Unit,
    onEndTimeChanged: (String) -> Unit,
    onCapacityChanged: (String) -> Unit,
    onCategoryChanged: (SlotCategory) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Class / Time Slot", fontWeight = FontWeight.Bold, color = theme.textMain)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.newSlotTitle,
                    onValueChange = onTitleChanged,
                    label = { Text("Class Name (e.g. HIIT Morning Burn)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.newSlotTrainer,
                    onValueChange = onTrainerChanged,
                    label = { Text("Coach / Trainer (e.g. Coach Sarah Jenkins)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.newSlotStartTime,
                        onValueChange = onStartTimeChanged,
                        label = { Text("Start Time") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.newSlotEndTime,
                        onValueChange = onEndTimeChanged,
                        label = { Text("End Time") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = state.newSlotCapacity,
                    onValueChange = onCapacityChanged,
                    label = { Text("Max Capacity (e.g. 20)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onCreate,
                enabled = state.newSlotTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GymAccentPrimary)
            ) {
                Text("Create Slot", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = theme.textMuted)
            }
        },
        containerColor = theme.cardBg
    )
}
