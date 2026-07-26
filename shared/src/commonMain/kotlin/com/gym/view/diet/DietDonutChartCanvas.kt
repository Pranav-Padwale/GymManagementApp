package com.gym.view.diet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val OrangeSegment = Color(0xFFFF5722) // Top / Carbs
private val BlueSegment = Color(0xFF3B82F6)   // Right / Protein
private val GreenSegment = Color(0xFF84CC16)  // Bottom-Left / Fat
private val TextMain = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFFA1A1AA)

/**
 * Custom Compose Canvas rendering the Diet Plan Donut Ring matching the provided screenshot:
 * Three smooth color arcs (Orange, Blue, Green) with 🔥 fire emoji, "0/1968" and "Calories".
 */
@Composable
fun DietDonutChartCanvas(
    consumedCalories: Int,
    targetCalories: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 28.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Orange Arc (Top to Top-Right: ~140 degrees)
            drawArc(
                color = OrangeSegment,
                startAngle = -110f,
                sweepAngle = 135f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )

            // Blue Arc (Right to Bottom: ~110 degrees)
            drawArc(
                color = BlueSegment,
                startAngle = 27f,
                sweepAngle = 115f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )

            // Green Arc (Bottom to Left: ~110 degrees)
            drawArc(
                color = GreenSegment,
                startAngle = 144f,
                sweepAngle = 104f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
        }

        // Center Content: Fire Emoji 🔥 + "0/1968" + "Calories"
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Calories Burned",
                tint = OrangeSegment,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$consumedCalories/$targetCalories",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextMain
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Calories",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
        }
    }
}
