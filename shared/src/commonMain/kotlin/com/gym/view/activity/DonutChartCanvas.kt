package com.gym.view.activity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.view.theme.GymTextPrimary
import com.gym.view.theme.GymTextSecondary

/**
 * Custom Canvas Rendering for the Multi-color Calorie Donut Ring matching the screenshot.
 */
@Composable
fun DonutChartCanvas(
    caloriesLeft: Double,
    carbsPercentage: Float = 0.60f,
    proteinPercentage: Float = 0.25f,
    fatPercentage: Float = 0.15f,
    modifier: Modifier = Modifier
) {
    val orangeColor = Color(0xFFFF5722) // Carbs / Target
    val blueColor = Color(0xFF2196F3)   // Protein
    val greenColor = Color(0xFF76FF03)  // Fat

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 26.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

            var startAngle = -90f

            // Orange Arc (Carbs / 60%)
            val carbsSweep = 360f * carbsPercentage
            drawArc(
                color = orangeColor,
                startAngle = startAngle,
                sweepAngle = carbsSweep - 4f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += carbsSweep

            // Blue Arc (Protein / 25%)
            val proteinSweep = 360f * proteinPercentage
            drawArc(
                color = blueColor,
                startAngle = startAngle,
                sweepAngle = proteinSweep - 4f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += proteinSweep

            // Green Arc (Fat / 15%)
            val fatSweep = 360f * fatPercentage
            drawArc(
                color = greenColor,
                startAngle = startAngle,
                sweepAngle = fatSweep - 4f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center Content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = orangeColor,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "$caloriesLeft",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GymTextPrimary
            )
            Text(
                text = "Calories left",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GymTextSecondary
            )
        }
    }
}
