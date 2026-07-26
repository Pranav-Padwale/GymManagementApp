package com.gym.view.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.activity.ActivityUiState
import com.gym.model.activity.NutritionMacro
import com.gym.model.activity.TimeRange
import com.gym.view.theme.AppThemeColors
import com.gym.view.theme.getAppThemeColors

private val OrangeAccent = Color(0xFFFF6D25)
private val BlueAccent = Color(0xFF3B82F6)
private val GreenAccent = Color(0xFF84CC16)
private val TealAccent = Color(0xFF00E676)
private val PinkAccent = Color(0xFFFF1744)

/**
 * View (Passive Compose Multiplatform Screen): Member Fitness & Nutrition Activity Dashboard.
 * Supports dynamic Dark Mode / Light Mode theme switching.
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Header Bar with Clickable Menu Drawer & Interactive Calendar Trigger
            ActivityTopHeader(
                selectedDateDisplay = state.selectedDateDisplay,
                targetCalories = state.calorieData.totalTargetCalories,
                theme = theme,
                onOpenDrawer = onOpenDrawer,
                onOpenCalendar = { onToggleCalendarDialog(true) }
            )

            // 2. Calorie Donut Ring Card
            CalorieDonutRingCard(
                caloriesLeft = state.calorieData.caloriesLeft,
                burned = state.calorieData.caloriesBurned,
                eaten = state.calorieData.caloriesEaten,
                macros = state.macros,
                theme = theme
            )

            // 3. Health Sync Permission Banner
            if (!state.isHealthPermissionGranted) {
                HealthPermissionBannerCard(theme = theme, onGrantPermission = onGrantHealthPermission)
            }

            // 4. Nutrition Table Card
            NutritionTableCard(macros = state.macros, theme = theme)

            // 5. Calorie Consume / Burn Line Chart Card
            CalorieGraphCard(
                state = state,
                theme = theme,
                onTimeRangeChanged = onGraphTimeRangeChanged,
                onToggleSeries = onToggleSeries
            )

            // 6. Dosh Test Card
            DoshTestCard(theme = theme)

            // 7. Quick Action Grid (Exercise & Yoga)
            QuickActionGrid(theme = theme)
        }
    }

    // Interactive Calendar Modal Dialog
    if (state.isCalendarOpen) {
        InteractiveCalendarDialog(
            selectedDay = state.selectedDay,
            selectedMonth = state.selectedMonth,
            selectedYear = state.selectedYear,
            calendarViewMonth = state.calendarViewMonth,
            calendarViewYear = state.calendarViewYear,
            onDismiss = { onToggleCalendarDialog(false) },
            onDateSelected = onCalendarDateSelected,
            onMonthNav = onCalendarMonthNav,
            onSelectToday = onSelectToday
        )
    }
}

@Composable
private fun ActivityTopHeader(
    selectedDateDisplay: String,
    targetCalories: Double,
    theme: AppThemeColors,
    onOpenDrawer: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenDrawer, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = theme.textMain)
            }
            Spacer(modifier = Modifier.width(8.dp))

            // Clickable Interactive Date Picker Trigger
            Surface(
                onClick = onOpenCalendar,
                shape = RoundedCornerShape(12.dp),
                color = theme.cardBg,
                modifier = Modifier.border(1.dp, theme.cardBorder, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar", tint = OrangeAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = selectedDateDisplay, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = theme.textMuted, modifier = Modifier.size(18.dp))
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = "QR Scanner", tint = theme.textMain, modifier = Modifier.size(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Calories : $targetCalories", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
            }
        }
    }
}

@Composable
private fun CalorieDonutRingCard(
    caloriesLeft: Double,
    burned: Double,
    eaten: Double,
    macros: List<NutritionMacro>,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Burned Callout
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = "Burned", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
                    Text(text = "$burned", fontSize = 14.sp, color = theme.textMuted)
                }

                // Donut Ring Canvas
                DonutChartCanvas(caloriesLeft = caloriesLeft)

                // Eaten Callout
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Eaten", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
                    Text(text = "$eaten", fontSize = 14.sp, color = theme.textMuted)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Macro Nutrients Summary Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                macros.forEach { macro ->
                    val color = when (macro.name) {
                        "Carbs" -> OrangeAccent
                        "Protein" -> BlueAccent
                        else -> GreenAccent
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = macro.emojiIcon, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "${macro.totalGrams} g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                            Text(text = macro.name, fontSize = 11.sp, color = theme.textMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthPermissionBannerCard(theme: AppThemeColors, onGrantPermission: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF332015)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Permission needed", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
                Text(
                    text = "Allow access to your health data to sync steps, calories, and more.",
                    fontSize = 11.sp,
                    color = theme.textMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onGrantPermission,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = "Grant permission", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun NutritionTableCard(macros: List<NutritionMacro>, theme: AppThemeColors) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Table Header Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Nutrition", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textMain, modifier = Modifier.weight(1.5f))
                Text(text = "Total", fontSize = 12.sp, color = theme.textMuted, modifier = Modifier.weight(1f))
                Text(text = "Consumed", fontSize = 12.sp, color = theme.textMuted, modifier = Modifier.weight(1f))
                Text(text = "Remaining", fontSize = 12.sp, color = theme.textMuted, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = theme.dividerColor)
            Spacer(modifier = Modifier.height(10.dp))

            macros.forEach { macro ->
                val color = when (macro.name) {
                    "Carbs" -> OrangeAccent
                    "Protein" -> BlueAccent
                    else -> GreenAccent
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = macro.emojiIcon, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${macro.name} (${macro.percentage} %)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = theme.textMain)
                    }
                    Text(text = "${macro.totalGrams} g", fontSize = 12.sp, color = theme.textMain, modifier = Modifier.weight(1f))
                    Text(text = "${macro.consumedGrams} g", fontSize = 12.sp, color = theme.textMain, modifier = Modifier.weight(1f))
                    Text(text = "${macro.remainingGrams} g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CalorieGraphCard(
    state: ActivityUiState,
    theme: AppThemeColors,
    onTimeRangeChanged: (TimeRange) -> Unit,
    onToggleSeries: (Boolean, Boolean, Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Calorie Consume/Burn Graph", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textMain)

            Spacer(modifier = Modifier.height(12.dp))

            // Series Toggle Switches
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = state.showCaloriesSeries,
                        onCheckedChange = { onToggleSeries(it, state.showEatenSeries, state.showBurnSeries) },
                        colors = SwitchDefaults.colors(checkedThumbColor = OrangeAccent, checkedTrackColor = OrangeAccent.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Calories", fontSize = 12.sp, color = theme.textMain)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = state.showEatenSeries,
                        onCheckedChange = { onToggleSeries(state.showCaloriesSeries, it, state.showBurnSeries) },
                        colors = SwitchDefaults.colors(checkedThumbColor = TealAccent, checkedTrackColor = TealAccent.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Eaten", fontSize = 12.sp, color = theme.textMain)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = state.showBurnSeries,
                        onCheckedChange = { onToggleSeries(state.showCaloriesSeries, state.showEatenSeries, it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = PinkAccent, checkedTrackColor = PinkAccent.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Burn", fontSize = 12.sp, color = theme.textMain)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Range Tabs
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(TimeRange.entries) { range ->
                    val isSelected = state.selectedTimeRange == range
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onTimeRangeChanged(range) }
                    ) {
                        Text(
                            text = range.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) theme.textMain else theme.textMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isSelected) {
                            Box(modifier = Modifier.width(28.dp).height(2.dp).background(OrangeAccent))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Line Chart
            CalorieLineChartCanvas(
                points = state.graphHistory,
                showCalories = state.showCaloriesSeries,
                showEaten = state.showEatenSeries,
                showBurn = state.showBurnSeries
            )
        }
    }
}

@Composable
private fun DoshTestCard(theme: AppThemeColors) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Dosh Test", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badges
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFA3E635).copy(alpha = 0.3f), modifier = Modifier.width(130.dp)) {
                        Text(text = "🌊 KAPHA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA3E635), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF43F5E).copy(alpha = 0.3f), modifier = Modifier.width(130.dp)) {
                        Text(text = "🔥 PITTA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF38BDF8).copy(alpha = 0.3f), modifier = Modifier.width(130.dp)) {
                        Text(text = "🌍 VATA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }

                // Orange CTA Button
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(text = "Get Your First\nDosh Test", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun QuickActionGrid(theme: AppThemeColors) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Exercise Card
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f).height(140.dp).border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = theme.textMain, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Exercise", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
            }
        }

        // Yoga Card
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f).height(140.dp).border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = theme.textMain, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Yoga", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
            }
        }
    }
}

@Composable
private fun ActivityBottomNavBar(
    activeTab: String,
    theme: AppThemeColors,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(containerColor = theme.cardBg, tonalElevation = 8.dp) {
        val tabs = listOf(
            Triple("Activity", Icons.Default.TrackChanges, "Activity"),
            Triple("Goal", Icons.AutoMirrored.Filled.FormatListBulleted, "Goal"),
            Triple("Workout Plan", Icons.Default.FitnessCenter, "Workout Plan"),
            Triple("Diet Plan", Icons.Default.LocalFireDepartment, "Diet Plan"),
            Triple("Profile", Icons.Default.AccountCircle, "Profile")
        )

        tabs.forEach { (label, icon, key) ->
            val isSelected = activeTab == key
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(key) },
                icon = { Icon(icon, contentDescription = label, tint = if (isSelected) OrangeAccent else theme.textMuted) },
                label = { Text(label, fontSize = 10.sp, color = if (isSelected) OrangeAccent else theme.textMuted) }
            )
        }
    }
}
