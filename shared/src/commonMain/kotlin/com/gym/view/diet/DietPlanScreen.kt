package com.gym.view.diet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.diet.DietPlanUiState
import com.gym.model.diet.FoodItem
import com.gym.model.diet.MealCategory
import com.gym.view.theme.AppThemeColors
import com.gym.view.theme.getAppThemeColors

private val OrangeAccent = Color(0xFFFF6D25)
private val BlueAccent = Color(0xFF3B82F6)
private val GreenAccent = Color(0xFF84CC16)

/**
 * View (Passive Compose Multiplatform Screen): Diet Plan for Member.
 * Dynamically adapts colors between Dark Mode and Light Mode.
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
    onRemoveFoodItem: (String) -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Navigation Bar: Hamburger Menu ☰, Back Arrow ⬅️, "Diet Plan for", Date Picker Trigger
            DietPlanTopHeader(
                selectedDateDisplay = state.selectedDateDisplay,
                theme = theme,
                onOpenDrawer = onOpenDrawer,
                onNavigateBack = onNavigateBack
            )

            // 2. Trainer Selection Dropdown Bar ("No trainer assigned ▼")
            TrainerSelectorDropdown(
                selectedTrainer = state.selectedTrainer,
                availableTrainers = state.availableTrainers,
                isExpanded = state.isTrainerDropdownExpanded,
                theme = theme,
                onToggleDropdown = onToggleTrainerDropdown,
                onTrainerSelected = onTrainerSelected
            )

            // 3. Calorie & Macro Donut Ring Card
            DietMacroRingCard(
                consumedCalories = state.consumedCalories,
                targetCalories = state.targetCalories,
                consumedCarbs = state.consumedCarbsGrams,
                targetCarbs = state.targetCarbsGrams,
                consumedProtein = state.consumedProteinGrams,
                targetProtein = state.targetProteinGrams,
                consumedFat = state.consumedFatGrams,
                targetFat = state.targetFatGrams,
                theme = theme
            )

            // 4. Meal Section Cards (Breakfast, Lunch, Snacks, Dinner)
            MealCategory.entries.forEach { category ->
                MealSectionCard(
                    category = category,
                    foodItems = state.getItemsForCategory(category),
                    theme = theme,
                    onCameraClick = { onCameraIconClicked(category) },
                    onRemoveItem = onRemoveFoodItem
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Centered Footer Text matching screenshot ("No diet plan found !")
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No diet plan found !",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMain
                )
            }
        }
    }
}

@Composable
private fun DietPlanTopHeader(
    selectedDateDisplay: String,
    theme: AppThemeColors,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Hamburger Menu Trigger ☰
            IconButton(onClick = onOpenDrawer, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Menu, contentDescription = "Open Hamburger Menu", tint = theme.textMain)
            }

            // Back Arrow Button ⬅️
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textMain)
            }

            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Diet Plan for",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textMain
            )
        }

        // Date Display Badge (📅 26/07/2026)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = theme.cardBg,
            modifier = Modifier.border(1.dp, theme.cardBorder, RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Date",
                    tint = theme.textMain,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedDateDisplay,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMain
                )
            }
        }
    }
}

@Composable
private fun TrainerSelectorDropdown(
    selectedTrainer: String,
    availableTrainers: List<String>,
    isExpanded: Boolean,
    theme: AppThemeColors,
    onToggleDropdown: (Boolean) -> Unit,
    onTrainerSelected: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = { onToggleDropdown(!isExpanded) },
            shape = RoundedCornerShape(10.dp),
            color = theme.cardBg,
            modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedTrainer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.textMain
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = theme.textMain
                )
            }
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onToggleDropdown(false) },
            modifier = Modifier.background(theme.cardBg)
        ) {
            availableTrainers.forEach { trainer ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = trainer,
                            fontSize = 13.sp,
                            color = if (trainer == selectedTrainer) OrangeAccent else theme.textMain,
                            fontWeight = if (trainer == selectedTrainer) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { onTrainerSelected(trainer) }
                )
            }
        }
    }
}

@Composable
private fun DietMacroRingCard(
    consumedCalories: Int,
    targetCalories: Int,
    consumedCarbs: Int,
    targetCarbs: Int,
    consumedProtein: Int,
    targetProtein: Int,
    consumedFat: Int,
    targetFat: Int,
    theme: AppThemeColors
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Donut Chart Canvas
            DietDonutChartCanvas(
                consumedCalories = consumedCalories,
                targetCalories = targetCalories
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Macro Nutrients Row below Donut Ring (Carbs, Protein, Fat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Carbs (🥖 0/221 g Carbs)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🥖", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "$consumedCarbs/$targetCarbs g",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )
                        Text(text = "Carbs", fontSize = 11.sp, color = theme.textMuted)
                    }
                }

                // Protein (🥩 0/98 g Protein)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🥩", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "$consumedProtein/$targetProtein g",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueAccent
                        )
                        Text(text = "Protein", fontSize = 11.sp, color = theme.textMuted)
                    }
                }

                // Fat (🥑 0/77 g Fat)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🥑", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "$consumedFat/$targetFat g",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent
                        )
                        Text(text = "Fat", fontSize = 11.sp, color = theme.textMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun MealSectionCard(
    category: MealCategory,
    foodItems: List<FoodItem>,
    theme: AppThemeColors,
    onCameraClick: () -> Unit,
    onRemoveItem: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Section Title (e.g. "Breakfast") + Camera Button 📷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMain
                )

                // Orange Outlined Camera Button 📷
                Surface(
                    onClick = onCameraClick,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .size(height = 42.dp, width = 56.dp)
                        .border(1.5.dp, OrangeAccent, RoundedCornerShape(12.dp))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Log ${category.displayName} Photo",
                            tint = OrangeAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Table Columns Header Row: Food | Qty(gm) | Calories | Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Food",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMain,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "Qty(gm)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMain,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "Calories",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMain,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "Action",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMain,
                    modifier = Modifier.weight(1f)
                )
            }

            // Food Items List (if present)
            if (foodItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = theme.dividerColor)
                foodItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 13.sp,
                            color = theme.textMain,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "${item.quantityGrams.toInt()}",
                            fontSize = 13.sp,
                            color = theme.textMuted,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = "${item.calories.toInt()}",
                            fontSize = 13.sp,
                            color = theme.textMuted,
                            modifier = Modifier.weight(1.2f)
                        )
                        IconButton(
                            onClick = { onRemoveItem(item.id) },
                            modifier = Modifier.weight(1f).size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete item",
                                tint = Color(0xFFFF1744),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
