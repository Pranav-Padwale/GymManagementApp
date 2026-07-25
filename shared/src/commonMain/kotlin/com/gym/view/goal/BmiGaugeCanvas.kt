package com.gym.view.goal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val BlueArc = Color(0xFF2196F3)    // Underweight (<18.5)
private val GreenArc = Color(0xFF76FF03)   // Normal (18.5 - 24.9)
private val OrangeArc = Color(0xFFFF5722)  // Overweight (25.0 - 29.9)
private val RedArc = Color(0xFFFF1744)     // Obesity (30.0+)
private val TextMain = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFFA1A1AA)

/**
 * Custom Semi-circle Gauge Canvas for BMI & Body Fat % matching the screenshot.
 */
@Composable
fun BmiGaugeCanvas(
    bmi: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Semi-circle Speedometer Gauge
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 18.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                val arcSize = Size(diameter, diameter)

                val startAngle = 180f
                val sweepPerSeg = 45f

                // 1. Underweight (Blue)
                drawArc(color = BlueArc, startAngle = startAngle, sweepAngle = sweepPerSeg - 2f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                // 2. Normal (Green)
                drawArc(color = GreenArc, startAngle = startAngle + 45f, sweepAngle = sweepPerSeg - 2f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                // 3. Overweight (Orange)
                drawArc(color = OrangeArc, startAngle = startAngle + 90f, sweepAngle = sweepPerSeg - 2f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                // 4. Obesity (Red)
                drawArc(color = RedArc, startAngle = startAngle + 135f, sweepAngle = sweepPerSeg - 2f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))

                // Needle Pointer Angle based on BMI (15 to 35 range)
                val clampedBmi = bmi.coerceIn(15.0, 35.0)
                val normalizedBmi = (clampedBmi - 15.0) / 20.0 // 0.0 to 1.0
                val needleAngleDeg = 180.0 + (normalizedBmi * 180.0)
                val needleAngleRad = needleAngleDeg * (PI / 180.0)

                val center = Offset(size.width / 2, size.height / 2 + 10.dp.toPx())
                val needleLength = (diameter / 2) - 10.dp.toPx()
                val endOffset = Offset(
                    x = center.x + (needleLength * cos(needleAngleRad)).toFloat(),
                    y = center.y + (needleLength * sin(needleAngleRad)).toFloat()
                )

                // Draw Needle Line & Center Cap
                drawLine(color = Color.White, start = center, end = endOffset, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = center)
            }

            // BMI Text below gauge
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BMI : $bmi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
            }
        }

        // Gauge Legend Items
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LegendRow(color = BlueArc, label = "Underweight", rangeText = "(18.5<)")
            LegendRow(color = GreenArc, label = "Normal", rangeText = "(18.5-24.9)")
            LegendRow(color = OrangeArc, label = "Overweight", rangeText = "(25.0-29.9)")
            LegendRow(color = RedArc, label = "Obesity", rangeText = "(30.0>)")
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String, rangeText: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = color, style = Stroke(2.dp.toPx()))
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMain)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = rangeText, fontSize = 11.sp, color = TextMuted)
    }
}
