package com.gym.view.diet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.diet.DietPlanUiState
import com.gym.model.diet.FoodItem
import com.gym.model.diet.MealCategory
import com.gym.view.theme.*

/**
 * View: Meal Tracker & Diet Plan matching Google Stitch ("Elite Gym Management Pro").
 * Offers live macro tracking, calorie burn rings, meal logging, and assigned trainer controls.
 */
@Composable
fun DietPlanScreen(
    state: DietPlanUiState,
    isDarkMode: Boolean = true,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    onTrainerSelected: (String) -> Unit,
    onToggleTrainerDropdown: (Boolean) -> Unit,
    onCameraIconClicked: (MealCategory) -> Unit,
    onAddFoodItem: (name: String, quantityGrams: Double, calories: Double, category: MealCategory) -> Unit = { _, _, _, _ -> },
    onRemoveFoodItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = getAppThemeColors(isDarkMode)
    var selectedMealForAdd by remember { mutableStateOf<MealCategory?>(null) }

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
                            text = "Diet & Nutrition",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = theme.textMain,
                            maxLines = 1
                        )
                        Text(
                            text = "Macro telemetry • ${state.selectedDateDisplay}",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textMuted,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    color = GymAccentSecondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GymAccentSecondary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = GymAccentSecondary, modifier = Modifier.size(14.dp))
                        Text("Active", color = GymAccentSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. Assigned Trainer Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleTrainerDropdown(!state.isTrainerDropdownExpanded) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(GymAccentPrimary.copy(alpha = 0.3f), GymPrimaryGlow.copy(alpha = 0.15f))
                                    )
                                )
                                .border(1.5.dp, GymAccentPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SportsMartialArts, contentDescription = null, tint = GymAccentPrimary, modifier = Modifier.size(24.dp))
                        }

                        Column {
                            Text(
                                text = "ASSIGNED NUTRITION COACH",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = theme.textMuted
                            )
                            Text(
                                text = state.selectedTrainer.ifBlank { "Coach Sarah Jenkins" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = theme.textMain
                            )
                        }
                    }

                    Box {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Select Trainer",
                            tint = GymAccentPrimary
                        )

                        DropdownMenu(
                            expanded = state.isTrainerDropdownExpanded,
                            onDismissRequest = { onToggleTrainerDropdown(false) },
                            modifier = Modifier.background(theme.cardBg)
                        ) {
                            state.availableTrainers.forEach { trainer ->
                                DropdownMenuItem(
                                    text = { Text(trainer, color = theme.textMain, fontSize = 13.sp) },
                                    onClick = { onTrainerSelected(trainer) }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Macro Energy Ring & Progress Telemetry
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = GymAccentSecondary.copy(alpha = 0.1f)),
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAILY CALORIC INTAKE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = theme.textMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${state.consumedCalories}",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = GymAccentSecondary
                                )
                                Text(
                                    text = " / ${state.targetCalories} kcal",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = theme.textMuted,
                                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                                )
                            }
                        }

                        // Donut Ring
                        val calorieProgress = if (state.targetCalories > 0) (state.consumedCalories.toFloat() / state.targetCalories).coerceIn(0f, 1f) else 0f
                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 8.dp.toPx()
                                val diameter = size.minDimension - strokeWidth
                                val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
                                val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

                                drawArc(
                                    color = Color(0xFF222A3D),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                drawArc(
                                    color = GymAccentSecondary,
                                    startAngle = -90f,
                                    sweepAngle = 360f * calorieProgress,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = GymAccentSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = theme.dividerColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Macros: Carbs | Protein | Fat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MacroProgressBar(
                            label = "Carbs",
                            consumed = state.consumedCarbsGrams,
                            target = state.targetCarbsGrams,
                            color = GymAccentPrimary,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                        MacroProgressBar(
                            label = "Protein",
                            consumed = state.consumedProteinGrams,
                            target = state.targetProteinGrams,
                            color = GymAccentSecondary,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                        MacroProgressBar(
                            label = "Fat",
                            consumed = state.consumedFatGrams,
                            target = state.targetFatGrams,
                            color = GymAccentDanger,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Meal Categories Section (Breakfast, Lunch, Snacks, Dinner)
            Text(
                text = "MEAL SCHEDULE & LOGS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = theme.textMuted
            )

            MealCategory.entries.forEach { category ->
                MealCategoryCard(
                    category = category,
                    foodItems = state.getItemsForCategory(category),
                    theme = theme,
                    onAddFood = { selectedMealForAdd = category },
                    onCameraClick = { onCameraIconClicked(category) },
                    onRemoveFood = onRemoveFoodItem
                )
            }
        }

        // Add Food Item Modal Dialog
        selectedMealForAdd?.let { category ->
            AddFoodDialog(
                category = category,
                theme = theme,
                onDismiss = { selectedMealForAdd = null },
                onConfirm = { name, grams, cal ->
                    selectedMealForAdd = null
                    onAddFoodItem(name, grams, cal, category)
                }
            )
        }
    }
}

@Composable
private fun MacroProgressBar(
    label: String,
    consumed: Int,
    target: Int,
    color: Color,
    theme: AppThemeColors,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) (consumed.toFloat() / target).coerceIn(0f, 1f) else 0f
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = theme.textMuted, fontWeight = FontWeight.SemiBold)
            Text("${consumed}g", fontSize = 12.sp, color = theme.textMain, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = theme.cardElevated
        )
    }
}

@Composable
private fun MealCategoryCard(
    category: MealCategory,
    foodItems: List<FoodItem>,
    theme: AppThemeColors,
    onAddFood: () -> Unit,
    onCameraClick: () -> Unit,
    onRemoveFood: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(foodItems.isNotEmpty()) }
    val totalCalories = foodItems.sumOf { it.calories }.toInt()

    val (emoji, iconColor) = when (category) {
        MealCategory.BREAKFAST -> "🍳" to GymAccentWarning
        MealCategory.LUNCH -> "🥗" to GymAccentSecondary
        MealCategory.SNACKS -> "🍎" to GymAccentPrimary
        MealCategory.DINNER -> "🥩" to GymAccentDanger
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 20.sp)
                    }

                    Column {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = theme.textMain
                        )
                        Text(
                            text = "$totalCalories kcal • ${foodItems.size} items",
                            fontSize = 12.sp,
                            color = if (totalCalories > 0) iconColor else theme.textMuted
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(onClick = onAddFood, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Food", tint = GymAccentPrimary, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = onCameraClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Photo Log", tint = theme.textMuted, modifier = Modifier.size(18.dp))
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = theme.textMuted
                    )
                }
            }

            // Expanded Food Items List
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = theme.dividerColor)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (foodItems.isEmpty()) {
                        Text(
                            text = "No food logged yet for ${category.displayName}.",
                            fontSize = 12.sp,
                            color = theme.textMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        foodItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = theme.textMain)
                                    Text("${item.quantityGrams.toInt()}g", fontSize = 11.sp, color = theme.textMuted)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${item.calories.toInt()} kcal",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = GymAccentSecondary
                                    )
                                    IconButton(
                                        onClick = { onRemoveFood(item.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = GymAccentDanger, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddFoodDialog(
    category: MealCategory,
    theme: AppThemeColors,
    onDismiss: () -> Unit,
    onConfirm: (name: String, grams: Double, calories: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var gramsStr by remember { mutableStateOf("150") }
    var caloriesStr by remember { mutableStateOf("250") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Log Food for ${category.displayName}", fontWeight = FontWeight.Bold, color = theme.textMain)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food / Meal Name (e.g. Oatmeal with Berries)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = gramsStr,
                    onValueChange = { gramsStr = it },
                    label = { Text("Portion (grams)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = caloriesStr,
                    onValueChange = { caloriesStr = it },
                    label = { Text("Calories (kcal)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val g = gramsStr.toDoubleOrNull() ?: 100.0
                    val cal = caloriesStr.toDoubleOrNull() ?: 200.0
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), g, cal)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GymAccentPrimary)
            ) {
                Text("Add Food", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = theme.textMuted)
            }
        },
        containerColor = theme.cardBg
    )
}
