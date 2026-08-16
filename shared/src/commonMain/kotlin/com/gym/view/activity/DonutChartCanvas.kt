package com.gym.view.activity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.view.theme.GymAccentPrimary
import com.gym.view.theme.GymTextPrimary
import com.gym.view.theme.GymTextSecondary

/**
 * Custom Canvas Rendering for the Multi-color Calorie Donut Ring matching Stitch design.
 */
@Composable
fun DonutChartCanvas(
    caloriesLeft: Double,
    carbsPercentage: Float = 0.60f,
    proteinPercentage: Float = 0.25f,
    fatPercentage: Float = 0.15f,
    modifier: Modifier = Modifier
) {
    val carbsColor = Color(0xFF6366F1) // Electric Indigo
    val proteinColor = Color(0xFF4EDEA3) // Emerald Green
    val fatColor = Color(0xFFFF5451) // Coral Red
    val trackBgColor = Color(0xFF222A3D) // Subtle dark track

    Box(
        modifier = modifier.size(210.dp),
        contentAlignment = Alignment.Center
    ) {
        // Radial glow behind the donut
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GymAccentPrimary.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

            // 1. Draw full background track
            drawArc(
                color = trackBgColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            var startAngle = -90f

            // 2. Carbs Arc (Indigo / 60%)
            val carbsSweep = 360f * carbsPercentage
            if (carbsSweep > 0) {
                drawArc(
                    color = carbsColor,
                    startAngle = startAngle,
                    sweepAngle = carbsSweep - 4f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += carbsSweep
            }

            // 3. Protein Arc (Emerald / 25%)
            val proteinSweep = 360f * proteinPercentage
            if (proteinSweep > 0) {
                drawArc(
                    color = proteinColor,
                    startAngle = startAngle,
                    sweepAngle = proteinSweep - 4f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += proteinSweep
            }

            // 4. Fat Arc (Coral Red / 15%)
            val fatSweep = 360f * fatPercentage
            if (fatSweep > 0) {
                drawArc(
                    color = fatColor,
                    startAngle = startAngle,
                    sweepAngle = fatSweep - 4f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = GymAccentPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${caloriesLeft.toInt()}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GymTextPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Calories left",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = GymTextSecondary
            )
        }
    }
}

