package com.gym.view.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.activity.ActivityUiState
import com.gym.model.activity.NutritionMacro
import com.gym.model.activity.TimeRange
import com.gym.view.theme.*

/**
 * View: Member Fitness & Activity Tracker matching Google Stitch ("Elite Gym Management Pro").
 * Features concentric calorie burn rings, time-series metabolic telemetry, and macro nutrient breakdowns.
 */
@Composable
fun ActivityDashboardScreen(
    state: ActivityUiState,
    isDarkMode: Boolean = true,
    onOpenDrawer: () -> Unit,
    onToggleCalendarDialog: (isOpen: Boolean) -> Unit,
    onCalendarDateSelected: (day: Int, month: Int, year: Int) -> Unit,
    onCalendarMonthNav: (deltaMonth: Int) -> Unit,
    onSelectToday: () -> Unit,
    onGrantHealthPermission: () -> Unit,
    onGraphTimeRangeChanged: (TimeRange) -> Unit,
    onToggleSeries: (showCalories: Boolean, showEaten: Boolean, showBurn: Boolean) -> Unit,
    onBottomTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = getAppThemeColors(isDarkMode)

    Scaffold(
        bottomBar = {
            ActivityBottomNavBar(
                activeTab = state.activeBottomTab,
                theme = theme,
                onTabSelected = onBottomTabSelected
            )
        },
        containerColor = theme.bg,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(theme.bg)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Banner (if present)
            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GymAccentDanger.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GymAccentDanger.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = error,
                            color = GymAccentDanger,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

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

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Activity & Bio-Metrics",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = theme.textMain,
                            maxLines = 1
                        )
                        Text(
                            text = "Target: ${state.calorieData.totalTargetCalories} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textMuted,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    onClick = { onToggleCalendarDialog(true) },
                    shape = RoundedCornerShape(16.dp),
                    color = theme.cardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GymAccentPrimary, modifier = Modifier.size(15.dp))
                        Text(state.selectedDateDisplay, fontSize = 11.sp, color = theme.textMain, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = theme.textMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // 2. Calorie Energy Donut Ring Hero Card
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(20.dp), spotColor = GymAccentSecondary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Column: Remaining Calories Hero
                        Column {
                            Text(
                                text = "CALORIES REMAINING",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = theme.textMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${state.calorieData.caloriesLeft}",
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = GymAccentSecondary
                            )
                            Text(
                                text = "of ${state.calorieData.totalTargetCalories} kcal daily goal",
                                fontSize = 12.sp,
                                color = theme.textMuted
                            )
                        }

                        // Right: Concentric Rings Canvas
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 10.dp.toPx()
                                val diameter = size.minDimension - strokeWidth
                                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                                val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

                                // Track
                                drawArc(
                                    color = Color(0xFF222A3D),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                // Eaten Arc (Indigo)
                                val eatenPct: Float = if (state.calorieData.totalTargetCalories > 0) (state.calorieData.caloriesEaten.toFloat() / state.calorieData.totalTargetCalories.toFloat()).coerceIn(0f, 1f) else 0f
                                drawArc(
                                    color = GymAccentPrimary,
                                    startAngle = -90f,
                                    sweepAngle = 360f * eatenPct,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                // Burned Arc (Emerald)
                                val burnPct: Float = if (state.calorieData.totalTargetCalories > 0) (state.calorieData.caloriesBurned.toFloat() / state.calorieData.totalTargetCalories.toFloat()).coerceIn(0f, 1f) else 0f
                                val innerStroke = 6.dp.toPx()
                                val innerDiameter = diameter - 24.dp.toPx()
                                val innerTopLeft = Offset((size.width - innerDiameter) / 2, (size.height - innerDiameter) / 2)
                                val innerArcSize = androidx.compose.ui.geometry.Size(innerDiameter, innerDiameter)

                                drawArc(
                                    color = GymAccentSecondary,
                                    startAngle = -90f,
                                    sweepAngle = 360f * burnPct,
                                    useCenter = false,
                                    topLeft = innerTopLeft,
                                    size = innerArcSize,
                                    style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = GymAccentSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = theme.dividerColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Eaten vs Burned Breakdown Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Eaten Intake", fontSize = 11.sp, color = theme.textMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${state.calorieData.caloriesEaten} kcal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GymAccentPrimary)
                        }
                        VerticalDivider(modifier = Modifier.height(28.dp), color = theme.dividerColor)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Active Burn", fontSize = 11.sp, color = theme.textMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${state.calorieData.caloriesBurned} kcal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GymAccentSecondary)
                        }
                        VerticalDivider(modifier = Modifier.height(28.dp), color = theme.dividerColor)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Net Energy", fontSize = 11.sp, color = theme.textMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${state.calorieData.caloriesEaten - state.calorieData.caloriesBurned} kcal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
                        }
                    }
                }
            }

            // 3. Health Sync Permission Banner
            if (!state.isHealthPermissionGranted) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GymAccentPrimary.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GymAccentPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
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
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(GymAccentPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text("Sync Health Connect", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.textMain, maxLines = 1)
                                Text("Import heart rate & wearable telemetry", fontSize = 11.sp, color = theme.textMuted, maxLines = 1)
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = onGrantHealthPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = GymAccentPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // 4. Macro Nutrient Telemetry Table
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "MACRONUTRIENT INTAKE PROGRESS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = theme.textMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    state.macros.forEach { macro ->
                        val ratio: Float = if (macro.totalGrams > 0) (macro.consumedGrams.toFloat() / macro.totalGrams.toFloat()).coerceIn(0f, 1f) else 0f
                        val barColor = when (macro.name.lowercase()) {
                            "carbs" -> GymAccentPrimary
                            "protein" -> GymAccentSecondary
                            "fat" -> GymAccentDanger
                            else -> GymAccentWarning
                        }

                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(macro.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = theme.textMain)
                                Text(
                                    "${macro.consumedGrams.toInt()} / ${macro.totalGrams.toInt()}g (${(ratio * 100).toInt()}%)",
                                    fontSize = 12.sp,
                                    color = theme.textMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = barColor,
                                trackColor = theme.cardElevated
                            )
                        }
                    }
                }
            }

            // 5. Activity Energy Time-Series Chart
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "METABOLIC TIME SERIES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = theme.textMuted
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TimeRange.entries.forEach { range ->
                                val isSelected = state.selectedTimeRange == range
                                Surface(
                                    onClick = { onGraphTimeRangeChanged(range) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) GymAccentPrimary.copy(alpha = 0.2f) else theme.cardElevated,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GymAccentPrimary) else null
                                ) {
                                    Text(
                                        text = range.name.take(1) + range.name.drop(1).lowercase(),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) GymAccentPrimary else theme.textMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Area Chart Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw subtle grid lines
                            val gridLines = 4
                            for (i in 0..gridLines) {
                                val y = h * (i.toFloat() / gridLines)
                                drawLine(
                                    color = Color(0xFF222A3D),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Generate smooth mock spline
                            val points = listOf(
                                Offset(0f, h * 0.7f),
                                Offset(w * 0.2f, h * 0.5f),
                                Offset(w * 0.4f, h * 0.65f),
                                Offset(w * 0.6f, h * 0.35f),
                                Offset(w * 0.8f, h * 0.45f),
                                Offset(w, h * 0.2f)
                            )

                            val path = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val cpX = (prev.x + curr.x) / 2
                                    cubicTo(cpX, prev.y, cpX, curr.y, curr.x, curr.y)
                                }
                            }

                            // Draw gradient area
                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(GymAccentSecondary.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )

                            // Draw line
                            drawPath(
                                path = path,
                                color = GymAccentSecondary,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            // 6. Quick Workout Category Grid (Strength, HIIT, Yoga, Mobility)
            Text(
                text = "FEATURED TRAINING SUITES",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = theme.textMuted
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TrainingCard("Strength & Hypertrophy", "45 min • 380 kcal", "🏋️", GymAccentPrimary, theme, Modifier.weight(1f))
                TrainingCard("Metabolic HIIT & Core", "30 min • 420 kcal", "🔥", GymAccentSecondary, theme, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TrainingCard(
    title: String,
    subtitle: String,
    emoji: String,
    accentColor: Color,
    theme: AppThemeColors,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
        modifier = modifier.shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.textMain)
            Text(subtitle, fontSize = 11.sp, color = theme.textMuted)
        }
    }
}

@Composable
private fun ActivityBottomNavBar(
    activeTab: String,
    theme: AppThemeColors,
    onTabSelected: (String) -> Unit
) {
    Surface(
        color = theme.cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(64.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                "Activity" to Icons.Default.TrackChanges,
                "Diet Plan" to Icons.Default.Restaurant,
                "Goal" to Icons.Default.Speed,
                "Profile" to Icons.Default.AccountCircle
            )

            tabs.forEach { (name, icon) ->
                val isSelected = activeTab == name
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSelected(name) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = if (isSelected) GymAccentPrimary else theme.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) GymAccentPrimary else theme.textMuted
                    )
                }
            }
        }
    }
}
