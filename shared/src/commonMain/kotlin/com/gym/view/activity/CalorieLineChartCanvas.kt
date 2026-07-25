package com.gym.view.activity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.model.activity.CalorieGraphPoint

/**
 * Custom Canvas Line Chart for Calorie Consume/Burn Graph matching screenshot.
 */
@Composable
fun CalorieLineChartCanvas(
    points: List<CalorieGraphPoint>,
    showCalories: Boolean = true,
    showEaten: Boolean = true,
    showBurn: Boolean = true,
    modifier: Modifier = Modifier
) {
    val axisColor = Color(0xFF475569)
    val targetOrange = Color(0xFFFF6D25)
    val eatenTeal = Color(0xFF00E676)
    val burnPink = Color(0xFFFF1744)

    Box(modifier = modifier.fillMaxWidth().height(180.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val paddingLeft = 45.dp.toPx()
            val paddingBottom = 30.dp.toPx()
            val chartWidth = width - paddingLeft - 10.dp.toPx()
            val chartHeight = height - paddingBottom - 10.dp.toPx()

            // Draw Y-Axis Grid Lines (0, 750, 1500, 2250, 3000)
            val yLevels = listOf(0, 750, 1500, 2250, 3000)
            yLevels.forEach { level ->
                val yFraction = level / 3000f
                val yPos = height - paddingBottom - (yFraction * chartHeight)

                // Grid line
                drawLine(
                    color = axisColor.copy(alpha = 0.3f),
                    start = Offset(paddingLeft, yPos),
                    end = Offset(width, yPos),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            }

            // Draw X-Axis Line
            drawLine(
                color = axisColor,
                start = Offset(paddingLeft, height - paddingBottom),
                end = Offset(width, height - paddingBottom),
                strokeWidth = 1.5.dp.toPx()
            )

            // Draw Y-Axis Line
            drawLine(
                color = axisColor,
                start = Offset(paddingLeft, 0f),
                end = Offset(paddingLeft, height - paddingBottom),
                strokeWidth = 1.5.dp.toPx()
            )

            if (points.isNotEmpty()) {
                val stepX = chartWidth / (points.size - 1).coerceAtLeast(1)

                // Plot Target Calories (Orange)
                if (showCalories) {
                    val lastPoint = points.last()
                    val yFrac = (lastPoint.caloriesTotal / 3000.0).coerceIn(0.0, 1.0).toFloat()
                    val yPos = height - paddingBottom - (yFrac * chartHeight)
                    val xPos = paddingLeft + (points.size - 1) * stepX

                    drawCircle(color = targetOrange, radius = 4.dp.toPx(), center = Offset(xPos, yPos))
                }
            }
        }

        // Y-Axis Labels
        Column(
            modifier = Modifier.fillMaxHeight().padding(bottom = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("3000", "2250", "1500", "750", "0").forEach { label ->
                Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
            }
        }

        // X-Axis Date Label at bottom
        Box(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(start = 45.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = points.lastOrNull()?.dateLabel ?: "25-07-26",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
