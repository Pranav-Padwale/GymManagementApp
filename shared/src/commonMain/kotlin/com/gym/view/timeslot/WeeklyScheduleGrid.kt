package com.gym.view.timeslot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.model.timeslot.DayOfWeek
import com.gym.model.timeslot.SlotCategory
import com.gym.model.timeslot.TimeSlot
import com.gym.view.theme.AppThemeColors

// Color mapping for Canvas rendering
private fun getCategoryCanvasColor(category: SlotCategory): Color = when (category) {
    SlotCategory.YOGA -> Color(0xFF14B8A6)
    SlotCategory.CARDIO -> Color(0xFFF97316)
    SlotCategory.STRENGTH -> Color(0xFF3B82F6)
    SlotCategory.CROSSFIT -> Color(0xFFEF4444)
    SlotCategory.ZUMBA -> Color(0xFFA855F7)
    SlotCategory.OPEN_GYM -> Color(0xFF6366F1)
}

/**
 * View (Canvas Component): Weekly Schedule Grid Overview.
 * Renders a compact 7-column visual grid with time axis (6 AM – 10 PM)
 * and color-coded slot blocks positioned by their day and time range.
 * Purely visual — no click interaction.
 */
@Composable
fun WeeklyScheduleGrid(
    allSlots: List<TimeSlot>,
    theme: AppThemeColors,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, theme.cardBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📅 Weekly Overview",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textMain
            )
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                drawWeeklyGrid(
                    allSlots = allSlots,
                    isDarkMode = isDarkMode,
                    textColor = theme.textMain,
                    gridLineColor = theme.dividerColor,
                    mutedTextColor = theme.textMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SlotCategory.entries.forEach { category ->
                    Row {
                        Canvas(modifier = Modifier.size(8.dp).offset(y = 4.dp)) {
                            drawCircle(color = getCategoryCanvasColor(category), radius = size.minDimension / 2)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = category.displayName,
                            fontSize = 9.sp,
                            color = theme.textMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Canvas draw function for the weekly schedule grid.
 * Time range: 6:00 (6 AM) to 22:00 (10 PM) = 16 hours.
 */
private fun DrawScope.drawWeeklyGrid(
    allSlots: List<TimeSlot>,
    isDarkMode: Boolean,
    textColor: Color,
    gridLineColor: Color,
    mutedTextColor: Color
) {
    val leftMargin = 36f
    val topMargin = 20f
    val gridWidth = size.width - leftMargin - 8f
    val gridHeight = size.height - topMargin - 8f
    val columnWidth = gridWidth / 7f

    val startHour = 6    // 6 AM
    val endHour = 22     // 10 PM
    val totalHours = endHour - startHour // 16

    // Draw day headers (Mon, Tue, ... Sun)
    DayOfWeek.entries.forEachIndexed { index, day ->
        val x = leftMargin + (index * columnWidth) + (columnWidth / 2)
        drawContext.canvas.nativeCanvas.apply {
            drawString(
                day.short,
                x - 10f,
                14f,
                mutedTextColor
            )
        }
    }

    // Draw horizontal time grid lines (every 4 hours for clarity)
    val hourMarkers = listOf(6, 10, 14, 18, 22)
    hourMarkers.forEach { hour ->
        val yFraction = (hour - startHour).toFloat() / totalHours
        val y = topMargin + (yFraction * gridHeight)

        // Grid line
        drawLine(
            color = gridLineColor.copy(alpha = 0.4f),
            start = Offset(leftMargin, y),
            end = Offset(size.width - 8f, y),
            strokeWidth = 0.5f
        )

        // Time label
        val label = if (hour <= 12) "${hour}a" else "${hour - 12}p"
        drawContext.canvas.nativeCanvas.apply {
            drawString(label, 2f, y + 4f, mutedTextColor)
        }
    }

    // Draw vertical column dividers
    for (i in 0..7) {
        val x = leftMargin + (i * columnWidth)
        drawLine(
            color = gridLineColor.copy(alpha = 0.2f),
            start = Offset(x, topMargin),
            end = Offset(x, topMargin + gridHeight),
            strokeWidth = 0.5f
        )
    }

    // Draw slot blocks
    allSlots.forEach { slot ->
        val dayIndex = slot.dayOfWeek.ordinal
        val startFraction = parseTimeToFraction(slot.startTime, startHour, totalHours)
        val endFraction = parseTimeToFraction(slot.endTime, startHour, totalHours)

        if (startFraction != null && endFraction != null && endFraction > startFraction) {
            val x = leftMargin + (dayIndex * columnWidth) + 2f
            val y = topMargin + (startFraction * gridHeight)
            val blockHeight = (endFraction - startFraction) * gridHeight
            val blockWidth = columnWidth - 4f

            val categoryColor = getCategoryCanvasColor(slot.category)

            // Block background
            drawRoundRect(
                color = categoryColor.copy(alpha = 0.7f),
                topLeft = Offset(x, y),
                size = Size(blockWidth, blockHeight.coerceAtLeast(8f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // Lighter inner highlight
            drawRoundRect(
                color = categoryColor.copy(alpha = 0.3f),
                topLeft = Offset(x + 1f, y + 1f),
                size = Size((blockWidth - 2f).coerceAtLeast(0f), (blockHeight - 2f).coerceAtLeast(2f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
        }
    }
}

/**
 * Parses a time string like "06:00" or "18:30" into a fraction of the total grid range.
 */
private fun parseTimeToFraction(timeStr: String, startHour: Int, totalHours: Int): Float? {
    return try {
        val parts = timeStr.split(":")
        val hour = parts[0].toInt()
        val minute = parts.getOrNull(1)?.toInt() ?: 0
        val totalMinutes = hour * 60 + minute
        val startMinutes = startHour * 60
        val rangeMinutes = totalHours * 60
        ((totalMinutes - startMinutes).toFloat() / rangeMinutes).coerceIn(0f, 1f)
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension to draw simple text on a native canvas.
 * Uses a basic approach compatible with JVM desktop Compose.
 */
private fun org.jetbrains.skia.Canvas.drawString(
    text: String,
    x: Float,
    y: Float,
    color: Color
) {
    val paint = org.jetbrains.skia.Paint().apply {
        this.color = org.jetbrains.skia.Color.makeARGB(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
        isAntiAlias = true
    }
    val font = org.jetbrains.skia.Font().apply {
        this.size = 10f
    }
    drawString(text, x, y, font, paint)
}
