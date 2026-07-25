package com.gym.view.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CardBg = Color(0xFF202024)
private val OrangeAccent = Color(0xFFFF6D25)
private val TextMain = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFFA1A1AA)

/**
 * Interactive Calendar Modal Dialog.
 * Allows user to pick any date, navigate months, or jump to Today.
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
    val daysInMonth = getDaysInMonth(calendarViewMonth, calendarViewYear)
    val firstDayOfWeekOffset = getFirstDayOfWeekOffset(calendarViewMonth, calendarViewYear)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthNav(-1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = TextMain)
                }

                Text(
                    text = "${monthNames.getOrNull(calendarViewMonth - 1)} $calendarViewYear",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )

                IconButton(onClick = { onMonthNav(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = TextMain)
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Day of week headers
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayLabel ->
                        Text(
                            text = dayLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Days Grid
                val totalCells = firstDayOfWeekOffset + daysInMonth
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().height(210.dp)
                ) {
                    items((0 until totalCells).toList()) { index ->
                        if (index < firstDayOfWeekOffset) {
                            Box(modifier = Modifier.size(32.dp))
                        } else {
                            val dayNum = index - firstDayOfWeekOffset + 1
                            val isSelected = (dayNum == selectedDay && calendarViewMonth == selectedMonth && calendarViewYear == selectedYear)
                            val isToday = (dayNum == 25 && calendarViewMonth == 7 && calendarViewYear == 2026)

                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> OrangeAccent
                                            isToday -> OrangeAccent.copy(alpha = 0.25f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isToday && !isSelected) 1.dp else 0.dp,
                                        color = if (isToday && !isSelected) OrangeAccent else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onDateSelected(dayNum, calendarViewMonth, calendarViewYear)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dayNum",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextMain
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSelectToday,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Jump to Today", fontSize = 12.sp, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = TextMuted, fontSize = 12.sp)
            }
        }
    )
}

private fun getDaysInMonth(month: Int, year: Int): Int {
    return when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
}

private fun getFirstDayOfWeekOffset(month: Int, year: Int): Int {
    // Basic day offset calculation
    return (year + month * 3 + 2) % 7
}
