package com.gym.view.goal

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.goal.GoalUiState
import com.gym.model.goal.ActivityLevel
import com.gym.model.goal.Gender
import com.gym.model.goal.WeightGoal
import com.gym.view.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * View: Fitness Goal & TDEE / BMI Speedometer Calculator matching Google Stitch ("Elite Gym Management Pro").
 * Offers Mifflin-St Jeor metabolic telemetry, dynamic BMI gauge needle, and macro targets.
 */
@Composable
fun EditGoalScreen(
    state: GoalUiState,
    isDarkMode: Boolean = true,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    onGenderSelected: (Gender) -> Unit,
    onAgeChanged: (String) -> Unit,
    onHeightChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onGoalWeightChanged: (String) -> Unit,
    onWeightGoalSelected: (WeightGoal) -> Unit,
    onActivityLevelSelected: (ActivityLevel) -> Unit,
    onViewResultClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = getAppThemeColors(isDarkMode)

    Scaffold(
        containerColor = theme.bg,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(theme.bg)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        Text(
                            text = "TDEE & Metabolic Goal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = theme.textMain,
                            maxLines = 1
                        )
                        Text(
                            text = "Energy Expenditure & BMI Telemetry",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textMuted,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    color = GymAccentPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GymAccentPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = GymAccentPrimary, modifier = Modifier.size(14.dp))
                        Text("Engine", color = GymAccentPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. Body Type (Gender) Selector Card
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "BIOLOGICAL PROFILE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = theme.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        PillSelectionButton(
                            label = "Female ♀",
                            isSelected = state.gender == Gender.FEMALE,
                            theme = theme,
                            onClick = { onGenderSelected(Gender.FEMALE) },
                            modifier = Modifier.weight(1f)
                        )
                        PillSelectionButton(
                            label = "Male ♂",
                            isSelected = state.gender == Gender.MALE,
                            theme = theme,
                            onClick = { onGenderSelected(Gender.MALE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Biometrics Input Grid (Age, Height, Current Weight, Goal Weight)
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PHYSICAL METRICS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = theme.textMuted
                        )
                        Text(
                            text = "Ideal: ${state.goalResult.idealWeightMinKg.toInt()} - ${state.goalResult.idealWeightMaxKg.toInt()} kg",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymAccentSecondary
                        )
                    }

                    // 2x2 Input Grid
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = state.ageStr,
                            onValueChange = onAgeChanged,
                            label = { Text("Age (yrs)", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = theme.cardElevated,
                                unfocusedContainerColor = theme.cardElevated,
                                focusedBorderColor = GymAccentPrimary,
                                unfocusedBorderColor = theme.cardBorder,
                                focusedTextColor = theme.textMain,
                                unfocusedTextColor = theme.textMain
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = state.heightStr,
                            onValueChange = onHeightChanged,
                            label = { Text("Height (cm)", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = theme.cardElevated,
                                unfocusedContainerColor = theme.cardElevated,
                                focusedBorderColor = GymAccentPrimary,
                                unfocusedBorderColor = theme.cardBorder,
                                focusedTextColor = theme.textMain,
                                unfocusedTextColor = theme.textMain
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = state.weightStr,
                            onValueChange = onWeightChanged,
                            label = { Text("Current Weight (kg)", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = theme.cardElevated,
                                unfocusedContainerColor = theme.cardElevated,
                                focusedBorderColor = GymAccentPrimary,
                                unfocusedBorderColor = theme.cardBorder,
                                focusedTextColor = theme.textMain,
                                unfocusedTextColor = theme.textMain
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = state.goalWeightStr,
                            onValueChange = onGoalWeightChanged,
                            label = { Text("Goal Weight (kg)", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = theme.cardElevated,
                                unfocusedContainerColor = theme.cardElevated,
                                focusedBorderColor = GymAccentPrimary,
                                unfocusedBorderColor = theme.cardBorder,
                                focusedTextColor = theme.textMain,
                                unfocusedTextColor = theme.textMain
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. BMI Speedometer Gauge
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BODY MASS INDEX (BMI) GAUGE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = theme.textMuted
                        )
                        Surface(
                            color = when {
                                state.goalResult.bmi < 18.5 -> GymAccentPrimary.copy(alpha = 0.15f)
                                state.goalResult.bmi < 25.0 -> GymAccentSecondary.copy(alpha = 0.15f)
                                state.goalResult.bmi < 30.0 -> GymAccentWarning.copy(alpha = 0.15f)
                                else -> GymAccentDanger.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = when {
                                    state.goalResult.bmi < 18.5 -> "Underweight"
                                    state.goalResult.bmi < 25.0 -> "Optimal / Normal"
                                    state.goalResult.bmi < 30.0 -> "Overweight"
                                    else -> "Obese"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    state.goalResult.bmi < 18.5 -> GymAccentPrimary
                                    state.goalResult.bmi < 25.0 -> GymAccentSecondary
                                    state.goalResult.bmi < 30.0 -> GymAccentWarning
                                    else -> GymAccentDanger
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speedometer Canvas
                    BmiSpeedometerGauge(bmi = state.goalResult.bmi, theme = theme)
                }
            }

            // 5. Weight Goal Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PRIMARY FITNESS OBJECTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = theme.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GridSelector(
                        items = WeightGoal.entries,
                        selectedItem = state.weightGoal,
                        displayName = { it.displayName },
                        theme = theme,
                        onSelect = onWeightGoalSelected
                    )
                }
            }

            // 6. Activity Level Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PHYSICAL ACTIVITY FREQUENCY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = theme.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GridSelector(
                        items = ActivityLevel.entries,
                        selectedItem = state.activityLevel,
                        displayName = { it.displayName },
                        theme = theme,
                        onSelect = onActivityLevelSelected
                    )
                }
            }

            // 7. Calculate Results CTA Button
            Button(
                onClick = onViewResultClicked,
                colors = ButtonDefaults.buttonColors(containerColor = GymAccentPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = GymAccentPrimary.copy(alpha = 0.3f))
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.White)
                        Text(text = "Compute TDEE & Macro Blueprint", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // 8. Calculated Macro & Calorie Blueprint Results Card
            AnimatedVisibility(
                visible = state.isResultCalculated,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GymAccentSecondary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RECOMMENDED DAILY ENERGY TARGET",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = theme.textMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${state.goalResult.calculatedCalories} kcal",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = GymAccentSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = theme.dividerColor)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroOutputColumn("Carbs", "${state.goalResult.calculatedCarbsGrams.toInt()}", "g/day", GymAccentPrimary, theme, Modifier.weight(1f))
                            MacroOutputColumn("Protein", "${state.goalResult.calculatedProteinGrams.toInt()}", "g/day", GymAccentSecondary, theme, Modifier.weight(1f))
                            MacroOutputColumn("Fat", "${state.goalResult.calculatedFatGrams.toInt()}", "g/day", GymAccentDanger, theme, Modifier.weight(1f))
                            MacroOutputColumn("Target", "${state.goalResult.goalWeightKg.toInt()}", "kg", Color(0xFF38BDF8), theme, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BmiSpeedometerGauge(
    bmi: Double,
    theme: AppThemeColors
) {
    Box(
        modifier = Modifier.size(width = 240.dp, height = 130.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.width - strokeWidth
            val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, size.height - diameter / 2)
            val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

            // Multi-segment arc from 180° to 360° (half circle):
            // 180 to 225: Blue/Indigo (Underweight)
            drawArc(
                color = GymAccentPrimary,
                startAngle = 180f,
                sweepAngle = 45f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            // 225 to 270: Emerald (Normal)
            drawArc(
                color = GymAccentSecondary,
                startAngle = 225f,
                sweepAngle = 45f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            // 270 to 315: Amber (Overweight)
            drawArc(
                color = GymAccentWarning,
                startAngle = 270f,
                sweepAngle = 45f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            // 315 to 360: Coral Red (Obese)
            drawArc(
                color = GymAccentDanger,
                startAngle = 315f,
                sweepAngle = 45f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            // Needle angle based on BMI (clamped 15 to 35 -> 180° to 360°)
            val clampedBmi = bmi.coerceIn(15.0, 35.0)
            val angleDeg = 180f + ((clampedBmi - 15.0) / 20.0 * 180f).toFloat()
            val angleRad = (angleDeg * kotlin.math.PI / 180.0)

            val centerX = size.width / 2
            val centerY = size.height
            val needleLen = (diameter / 2) - 10.dp.toPx()

            val endX = (centerX + needleLen * cos(angleRad)).toFloat()
            val endY = (centerY + needleLen * sin(angleRad)).toFloat()

            // Draw needle pointer
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                end = androidx.compose.ui.geometry.Offset(endX, endY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Center pivot
            drawCircle(
                color = GymAccentPrimary,
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(centerX, centerY)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Text(
                text = "${(bmi * 10).toInt() / 10.0}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = theme.textMain
            )
            Text("BMI Score", fontSize = 10.sp, color = theme.textMuted)
        }
    }
}

@Composable
private fun PillSelectionButton(
    label: String,
    isSelected: Boolean,
    theme: AppThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) GymAccentPrimary.copy(alpha = 0.2f) else theme.cardElevated,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) GymAccentPrimary else theme.cardBorder
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) GymAccentPrimary else theme.textMuted
            )
        }
    }
}

@Composable
private fun <T> GridSelector(
    items: List<T>,
    selectedItem: T,
    displayName: (T) -> String,
    theme: AppThemeColors,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    val isSelected = item == selectedItem
                    Surface(
                        onClick = { onSelect(item) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) GymAccentPrimary.copy(alpha = 0.2f) else theme.cardElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) GymAccentPrimary else theme.cardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName(item),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) GymAccentPrimary else theme.textMuted
                            )
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MacroOutputColumn(
    label: String,
    value: String,
    unit: String,
    color: Color,
    theme: AppThemeColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, color = theme.textMuted, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(unit, fontSize = 10.sp, color = theme.textMuted)
    }
}
