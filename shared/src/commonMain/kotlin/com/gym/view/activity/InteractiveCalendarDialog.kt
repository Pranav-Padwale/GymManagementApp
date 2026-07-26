package com.gym.view.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth

private val CardBg = Color(0xFF202024)
private val DialogBg = Color(0xFF19191C)
private val OrangeAccent = Color(0xFFFF6D25)
private val TextMain = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFFA1A1AA)

/**
 * Interactive Real-Time Calendar Picker Modal Dialog.
 * Supports month/year navigation, day selection, real-time system today detection, and Jump to Today CTA.
 */
@Composable
fun InteractiveCalendarDialog(
    selectedDay: Int,
    selectedMonth: Int,
    selectedYear: Int,
    calendarViewMonth: Int,
    calendarViewYear: Int,
    onDismiss: () -> Unit,
    onDateSelected: (day: Int, month: Int, year: Int) -> Unit,
    onMonthNav: (deltaMonth: Int) -> Unit,
    onSelectToday: () -> Unit
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val realNow = LocalDate.now()
    val realTodayDay = realNow.dayOfMonth
    val realTodayMonth = realNow.monthValue
    val realTodayYear = realNow.year

    // Calculate total days & first day offset for the current viewed month
    val daysInMonth = try {
        YearMonth.of(calendarViewYear, calendarViewMonth).lengthOfMonth()
    } catch (e: Exception) {
        31
    }

    val firstDayOffset = try {
        val dayOfWeek = YearMonth.of(calendarViewYear, calendarViewMonth).atDay(1).dayOfWeek.value // 1 (Mon) to 7 (Sun)
        dayOfWeek % 7 // 0 (Sun) to 6 (Sat)
    } catch (e: Exception) {
        0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogBg,
        shape = RoundedCornerShape(20.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Modal Header Title & Dismiss Button ✕
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Date",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Month/Year Navigation Header (e.g. "< July 2026 >")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onMonthNav(-1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month", tint = TextMain, modifier = Modifier.size(18.dp))
                    }

                    Text(
                        text = "${monthNames.getOrNull(calendarViewMonth - 1) ?: "July"} $calendarViewYear",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    IconButton(onClick = { onMonthNav(1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", tint = TextMain, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day of Week Column Headers (S M T W T F S)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayHeader ->
                        Text(
                            text = dayHeader,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 7-Column Days Grid
                val totalSlots = firstDayOffset + daysInMonth
                val totalGridCells = if (totalSlots % 7 == 0) totalSlots else ((totalSlots / 7) + 1) * 7

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().height(230.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(totalGridCells) { index ->
                        val dayNumber = index - firstDayOffset + 1
                        if (dayNumber in 1..daysInMonth) {
                            val isSelected = (dayNumber == selectedDay && calendarViewMonth == selectedMonth && calendarViewYear == selectedYear)
                            val isRealToday = (dayNumber == realTodayDay && calendarViewMonth == realTodayMonth && calendarViewYear == realTodayYear)

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) OrangeAccent else if (isRealToday) Color(0xFF332015) else Color.Transparent)
                                    .border(
                                        width = if (isRealToday && !isSelected) 1.5.dp else 0.dp,
                                        color = if (isRealToday && !isSelected) OrangeAccent else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onDateSelected(dayNumber, calendarViewMonth, calendarViewYear)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dayNumber",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected || isRealToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextMain else if (isRealToday) OrangeAccent else TextMain
                                )
                            }
                        } else {
                            // Blank padding slot for offset
                            Box(modifier = Modifier.size(36.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // "Jump to Today" Action Button
                Surface(
                    onClick = onSelectToday,
                    shape = RoundedCornerShape(12.dp),
                    color = OrangeAccent,
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Jump to Today ($realTodayDay ${monthNames.getOrNull(realTodayMonth - 1) ?: "July"})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}
