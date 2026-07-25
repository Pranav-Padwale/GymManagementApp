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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
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

private val DarkBg = Color(0xFF151518)
private val CardBg = Color(0xFF202024)
private val OrangeAccent = Color(0xFFFF6D25)
private val TextMain = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFFA1A1AA)

/**
 * View (Passive Compose Multiplatform Screen): Edit Goal.
 * Strictly passive UI rendering data from [state] and emitting user inputs to GoalController.
 */
@Composable
fun EditGoalScreen(
    state: GoalUiState,
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextMain)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Edit Goal",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
        }

        // 2. Body Type Selector Section
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Body Type", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PillSelectionButton(
                        label = "Female",
                        isSelected = state.gender == Gender.FEMALE,
                        onClick = { onGenderSelected(Gender.FEMALE) },
                        modifier = Modifier.weight(1f)
                    )
                    PillSelectionButton(
                        label = "Male",
                        isSelected = state.gender == Gender.MALE,
                        onClick = { onGenderSelected(Gender.MALE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Personal Info Section
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Personal Info", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMain)
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
                        label = { Text("Age", color = TextMuted, fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextMain, unfocusedTextColor = TextMain, focusedBorderColor = OrangeAccent, unfocusedBorderColor = TextMuted),
                        modifier = Modifier.weight(1f)
                    )

                    // My Height (cm)
                    OutlinedTextField(
                        value = state.heightStr,
                        onValueChange = onHeightChanged,
                        label = { Text("My Height (cm)", color = TextMuted, fontSize = 11.sp) },
                        trailingIcon = { ToggleUnitBadge("cm") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextMain, unfocusedTextColor = TextMain, focusedBorderColor = OrangeAccent, unfocusedBorderColor = TextMuted),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // My Weight (kg)
                    OutlinedTextField(
                        value = state.weightStr,
                        onValueChange = onWeightChanged,
                        label = { Text("My Weight (kg)", color = TextMuted, fontSize = 11.sp) },
                        trailingIcon = { ToggleUnitBadge("kg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextMain, unfocusedTextColor = TextMain, focusedBorderColor = OrangeAccent, unfocusedBorderColor = TextMuted),
                        modifier = Modifier.weight(1f)
                    )

                    // Goal Weight (kg)
                    OutlinedTextField(
                        value = state.goalWeightStr,
                        onValueChange = onGoalWeightChanged,
                        label = { Text("Goal Weight (kg)", color = TextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextMain, unfocusedTextColor = TextMain, focusedBorderColor = OrangeAccent, unfocusedBorderColor = TextMuted),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Ideal Weight Banner
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Your ideal weight range: From ${state.goalResult.idealWeightMinKg.toInt()} KG to ${state.goalResult.idealWeightMaxKg.toInt()} KG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMain,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.Info, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(18.dp))
                }
            }
        }

        // 4. Body Fat % / BMI Speedometer Gauge Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Body Fat %", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Spacer(modifier = Modifier.height(12.dp))
                BmiGaugeCanvas(bmi = state.goalResult.bmi)
            }
        }

        // 5. Weight Goal Selector (2x2 Grid)
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Weight Goal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Spacer(modifier = Modifier.height(12.dp))
                GridSelector(
                    items = WeightGoal.entries,
                    selectedItem = state.weightGoal,
                    displayName = { it.displayName },
                    onSelect = onWeightGoalSelected
                )
            }
        }

        // 6. Activity Level Selector (2x2 Grid)
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Activity Level", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Spacer(modifier = Modifier.height(12.dp))
                GridSelector(
                    items = ActivityLevel.entries,
                    selectedItem = state.activityLevel,
                    displayName = { it.displayName },
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
                Text(text = "View Result", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextMain)
            }
        }

        // 8. Calculated Macro Results Card
        AnimatedVisibility(visible = state.isResultCalculated) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
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
                            color = TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2x2 Macro Metric Output Grid
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MacroOutputColumn("Calories", "${state.goalResult.calculatedCalories}", "kcal", modifier = Modifier.weight(1f))
                        MacroOutputColumn("Fat", "${state.goalResult.calculatedFatGrams}", "Gram Per Day", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MacroOutputColumn("Protein", "${state.goalResult.calculatedProteinGrams}", "Gram Per Day", modifier = Modifier.weight(1f))
                        MacroOutputColumn("Carbs", "${state.goalResult.calculatedCarbsGrams}", "Gram Per Day", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Last Update : ${state.goalResult.lastUpdatedIso}", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun PillSelectionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) OrangeAccent else CardBg,
        modifier = modifier.height(44.dp).border(1.dp, if (isSelected) OrangeAccent else TextMuted, RoundedCornerShape(8.dp))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMain)
        }
    }
}

@Composable
private fun <T> GridSelector(
    items: List<T>,
    selectedItem: T,
    displayName: (T) -> String,
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
                        color = if (isSelected) OrangeAccent else CardBg,
                        modifier = Modifier.weight(1f).height(44.dp).border(1.dp, if (isSelected) OrangeAccent else TextMuted, RoundedCornerShape(8.dp))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = displayName(item), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain)
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.width(120.dp).height(2.dp).background(OrangeAccent))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
        Text(text = unit, fontSize = 11.sp, color = TextMuted)
    }
}

@Composable
private fun ToggleUnitBadge(unit: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = OrangeAccent) {
        Text(text = unit, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMain, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}
