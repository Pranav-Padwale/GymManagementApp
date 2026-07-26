package com.gym.view.goal

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.goal.GoalUiState
import com.gym.model.goal.ActivityLevel
import com.gym.model.goal.Gender
import com.gym.model.goal.WeightGoal
import com.gym.view.theme.AppThemeColors
import com.gym.view.theme.getAppThemeColors

private val OrangeAccent = Color(0xFFFF6D25)

/**
 * View (Passive Compose Multiplatform Screen): Edit Goal.
 * Dynamically adapts colors between Dark Mode and Light Mode.
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Bar with Hamburger ☰ & Back Arrow ⬅️
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hamburger Menu Trigger ☰
            IconButton(onClick = onOpenDrawer, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Menu, contentDescription = "Open Hamburger Menu", tint = theme.textMain)
            }

            // Back Arrow Button ⬅️
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textMain)
            }

            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Edit Goal",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textMain
            )
        }

        // 2. Body Type Selector Section
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Body Type", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PillSelectionButton(
                        label = "Female",
                        isSelected = state.gender == Gender.FEMALE,
                        theme = theme,
                        onClick = { onGenderSelected(Gender.FEMALE) },
                        modifier = Modifier.weight(1f)
                    )
                    PillSelectionButton(
                        label = "Male",
                        isSelected = state.gender == Gender.MALE,
                        theme = theme,
                        onClick = { onGenderSelected(Gender.MALE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Personal Info Section
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Personal Info", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
                    Text(
                        text = "Calculate Age",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent,
                        modifier = Modifier.clickable { }
                    )
                }

                // Inputs Grid 2x2
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Age
                    OutlinedTextField(
                        value = state.ageStr,
                        onValueChange = onAgeChanged,
                        label = { Text("Age", color = theme.textMuted, fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = theme.textMuted, modifier = Modifier.size(16.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = theme.textMain, unfocusedTextColor = theme.textMain, focusedBorderColor = OrangeAccent, unfocusedBorderColor = theme.textMuted),
                        modifier = Modifier.weight(1f)
                    )

                    // My Height (cm)
                    OutlinedTextField(
                        value = state.heightStr,
                        onValueChange = onHeightChanged,
                        label = { Text("My Height (cm)", color = theme.textMuted, fontSize = 11.sp) },
                        trailingIcon = { ToggleUnitBadge("cm") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = theme.textMain, unfocusedTextColor = theme.textMain, focusedBorderColor = OrangeAccent, unfocusedBorderColor = theme.textMuted),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // My Weight (kg)
                    OutlinedTextField(
                        value = state.weightStr,
                        onValueChange = onWeightChanged,
                        label = { Text("My Weight (kg)", color = theme.textMuted, fontSize = 11.sp) },
                        trailingIcon = { ToggleUnitBadge("kg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = theme.textMain, unfocusedTextColor = theme.textMain, focusedBorderColor = OrangeAccent, unfocusedBorderColor = theme.textMuted),
                        modifier = Modifier.weight(1f)
                    )

                    // Goal Weight (kg)
                    OutlinedTextField(
                        value = state.goalWeightStr,
                        onValueChange = onGoalWeightChanged,
                        label = { Text("Goal Weight (kg)", color = theme.textMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = theme.textMain, unfocusedTextColor = theme.textMain, focusedBorderColor = OrangeAccent, unfocusedBorderColor = theme.textMuted),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Ideal Weight Banner
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Your ideal weight range: From ${state.goalResult.idealWeightMinKg.toInt()} KG to ${state.goalResult.idealWeightMaxKg.toInt()} KG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.textMain,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.Info, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(18.dp))
                }
            }
        }

        // 4. Body Fat % / BMI Speedometer Gauge Card
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Body Fat %", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
                Spacer(modifier = Modifier.height(12.dp))
                BmiGaugeCanvas(bmi = state.goalResult.bmi)
            }
        }

        // 5. Weight Goal Selector (2x2 Grid)
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Weight Goal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
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

        // 6. Activity Level Selector (2x2 Grid)
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Activity Level", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
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

        // 7. View Result CTA Button
        Button(
            onClick = onViewResultClicked,
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "View Result", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // 8. Calculated Macro Results Card
        AnimatedVisibility(visible = state.isResultCalculated) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(24.dp).clip(CircleShape).background(OrangeAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Below calculations are based on Harris-Benedict equations revised by Mifflin & St Jeor equation 1990",
                            fontSize = 11.sp,
                            color = theme.textMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2x2 Macro Metric Output Grid
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MacroOutputColumn("Calories", "${state.goalResult.calculatedCalories}", "kcal", theme = theme, modifier = Modifier.weight(1f))
                        MacroOutputColumn("Fat", "${state.goalResult.calculatedFatGrams}", "Gram Per Day", theme = theme, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MacroOutputColumn("Protein", "${state.goalResult.calculatedProteinGrams}", "Gram Per Day", theme = theme, modifier = Modifier.weight(1f))
                        MacroOutputColumn("Carbs", "${state.goalResult.calculatedCarbsGrams}", "Gram Per Day", theme = theme, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Last Update : ${state.goalResult.lastUpdatedIso}", fontSize = 11.sp, color = theme.textMuted, fontWeight = FontWeight.Medium)
                }
            }
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
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) OrangeAccent else theme.cardBg,
        modifier = modifier.height(44.dp).border(1.dp, if (isSelected) OrangeAccent else theme.textMuted, RoundedCornerShape(8.dp))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else theme.textMain)
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val rows = items.chunked(2)
        rows.forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { item ->
                    val isSelected = item == selectedItem
                    Surface(
                        onClick = { onSelect(item) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) OrangeAccent else theme.cardBg,
                        modifier = Modifier.weight(1f).height(44.dp).border(1.dp, if (isSelected) OrangeAccent else theme.textMuted, RoundedCornerShape(8.dp))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = displayName(item), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else theme.textMain)
                        }
                    }
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
    theme: AppThemeColors,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.width(120.dp).height(2.dp).background(OrangeAccent))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = theme.textMain)
        Text(text = unit, fontSize = 11.sp, color = theme.textMuted)
    }
}

@Composable
private fun ToggleUnitBadge(unit: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = OrangeAccent) {
        Text(text = unit, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}
