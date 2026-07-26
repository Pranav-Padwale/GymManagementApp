package com.gym.view.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
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
import com.gym.controller.finance.FinanceUiState
import com.gym.model.finance.TransactionCategory
import com.gym.model.finance.TransactionType
import com.gym.view.theme.*

/**
 * View (Passive Compose Multiplatform Screen): Income & Expense Balance Sheet Dashboard.
 * Dynamically adapts colors between Dark Mode and Light Mode.
 */
@Composable
fun FinanceDashboardScreen(
    state: FinanceUiState,
    isDarkMode: Boolean = true,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    onMonthRangeChanged: (month: Int, year: Int) -> Unit,
    onAddExpense: (title: String, amount: Double, category: TransactionCategory) -> Unit,
    onClearError: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = getAppThemeColors(isDarkMode)
    var isAddExpenseDialogOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bg)
            .padding(16.dp)
    ) {
        // Dashboard Header with Hamburger ☰ & Back Arrow ⬅️ Buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Hamburger Menu Trigger
                IconButton(onClick = onOpenDrawer, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Menu, contentDescription = "Open Hamburger Menu", tint = theme.textMain)
                }

                // Back Arrow Button
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Main Dashboard", tint = theme.textMain)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = "Balance Sheet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMain
                    )
                    Text(
                        text = "Monthly Financial Breakdown",
                        fontSize = 12.sp,
                        color = theme.textMuted
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Month Selector Button
                MonthYearSelectorButton(
                    selectedMonth = state.selectedMonth,
                    selectedYear = state.selectedYear,
                    theme = theme,
                    onMonthYearSelected = onMonthRangeChanged
                )

                // Refresh Button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.clip(CircleShape).background(theme.cardBg)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Data", tint = GymAccentPrimary)
                }
            }
        }

        // Error State Banner
        AnimatedVisibility(visible = state.errorMessage != null) {
            state.errorMessage?.let { errorMsg ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = GymAccentDanger.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = GymAccentDanger)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = errorMsg, color = GymAccentDanger, fontSize = 13.sp)
                        }
                        IconButton(onClick = onClearError, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = GymAccentDanger)
                        }
                    }
                }
            }
        }

        // Loading Indicator or Content
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GymAccentPrimary)
            }
        } else {
            // Balance Sheet Summary Card
            BalanceSheetSummaryCard(
                totalIncome = state.summary.totalIncome,
                totalExpenses = state.summary.totalExpenses,
                netBalance = state.summary.netBalance,
                theme = theme
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section Title & "Log Expense" Button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly Expense Breakdown",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textMain
            )

            Button(
                onClick = { isAddExpenseDialogOpen = true },
                colors = ButtonDefaults.buttonColors(containerColor = GymAccentDanger),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Log Expense", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // Expense Category Distribution List
        if (state.summary.expenseBreakdown.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No expenses recorded for this month.",
                    color = theme.textMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.summary.expenseBreakdown) { item ->
                    ExpenseCategoryRow(item.category.displayName, item.totalAmount, item.percentageOfTotalExpenses, theme)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recent Transactions Log",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.textMuted,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(state.summary.transactions) { tx ->
                    TransactionLogRow(tx.title, tx.category.displayName, tx.amount, tx.type, tx.dateIso, theme)
                }
            }
        }
    }

    // Input Form Modal for Logging New Expenses
    if (isAddExpenseDialogOpen) {
        AddExpenseDialog(
            theme = theme,
            onDismiss = { isAddExpenseDialogOpen = false },
            onSubmitExpense = { title, amount, category ->
                onAddExpense(title, amount, category)
                isAddExpenseDialogOpen = false
            }
        )
    }
}

@Composable
private fun BalanceSheetSummaryCard(
    totalIncome: Double,
    totalExpenses: Double,
    netBalance: Double,
    theme: AppThemeColors
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Net Monthly Balance",
                fontSize = 12.sp,
                color = theme.textMuted,
                fontWeight = FontWeight.SemiBold
            )
            val netColor = if (netBalance >= 0) GymAccentSecondary else GymAccentDanger
            Text(
                text = "$${formatAmount(netBalance)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = netColor
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = theme.dividerColor)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Income Column
                Column {
                    Text(text = "Total Income", fontSize = 12.sp, color = theme.textMuted)
                    Text(
                        text = "+$${formatAmount(totalIncome)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymAccentSecondary
                    )
                }

                // Total Expenses Column
                Column {
                    Text(text = "Total Expenses", fontSize = 12.sp, color = theme.textMuted)
                    Text(
                        text = "-$${formatAmount(totalExpenses)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymAccentDanger
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseCategoryRow(
    categoryName: String,
    totalAmount: Double,
    percentage: Float,
    theme: AppThemeColors
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = categoryName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = theme.textMain)
                Text(text = "$${formatAmount(totalAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GymAccentDanger)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = GymAccentDanger,
                trackColor = theme.dividerColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatAmount(percentage.toDouble())}% of total monthly expenses",
                fontSize = 11.sp,
                color = theme.textMuted
            )
        }
    }
}

@Composable
private fun TransactionLogRow(
    title: String,
    categoryName: String,
    amount: Double,
    type: TransactionType,
    dateIso: String,
    theme: AppThemeColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.cardBg, RoundedCornerShape(8.dp))
            .border(1.dp, theme.cardBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
            Text(text = "$categoryName • $dateIso", fontSize = 11.sp, color = theme.textMuted)
        }
        val isIncome = type == TransactionType.INCOME
        Text(
            text = if (isIncome) "+$${formatAmount(amount)}" else "-$${formatAmount(amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isIncome) GymAccentSecondary else GymAccentDanger
        )
    }
}

@Composable
private fun MonthYearSelectorButton(
    selectedMonth: Int,
    selectedYear: Int,
    theme: AppThemeColors,
    onMonthYearSelected: (month: Int, year: Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = theme.cardBg,
            modifier = Modifier.border(1.dp, theme.cardBorder, RoundedCornerShape(8.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${months.getOrNull(selectedMonth - 1) ?: "Jul"} $selectedYear",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMain
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = theme.textMuted)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(theme.cardBg)
        ) {
            months.forEachIndexed { index, mName ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "$mName $selectedYear",
                            fontSize = 13.sp,
                            color = if (index + 1 == selectedMonth) GymAccentPrimary else theme.textMain
                        )
                    },
                    onClick = {
                        expanded = false
                        onMonthYearSelected(index + 1, selectedYear)
                    }
                )
            }
        }
    }
}

/**
 * Text Input Form Dialog for Logging New Expenses instantly forwarding data to Controller.
 */
@Composable
private fun AddExpenseDialog(
    theme: AppThemeColors,
    onDismiss: () -> Unit,
    onSubmitExpense: (title: String, amount: Double, category: TransactionCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.ADMINISTRATIVE_EXPENSE) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val expenseCategories = TransactionCategory.entries.filter { it.type == TransactionType.EXPENSE }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.cardBg,
        title = {
            Text(text = "Log New Expense", color = theme.textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Description Text Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Description (e.g. Towel Service)", color = theme.textMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textMain,
                        unfocusedTextColor = theme.textMain,
                        focusedBorderColor = GymAccentPrimary,
                        unfocusedBorderColor = theme.textMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount Text Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($)", color = theme.textMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textMain,
                        unfocusedTextColor = theme.textMain,
                        focusedBorderColor = GymAccentPrimary,
                        unfocusedBorderColor = theme.textMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown Picker
                Text(text = "Expense Category:", fontSize = 12.sp, color = theme.textMuted)
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { categoryDropdownExpanded = true },
                        shape = RoundedCornerShape(4.dp),
                        color = theme.cardBg,
                        modifier = Modifier.fillMaxWidth().border(1.dp, theme.textMuted, RoundedCornerShape(4.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = selectedCategory.displayName, color = theme.textMain, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = theme.textMuted)
                        }
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier.background(theme.cardBg)
                    ) {
                        expenseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName, color = theme.textMain, fontSize = 13.sp) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    onSubmitExpense(title, amount, selectedCategory)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GymAccentDanger)
            ) {
                Text("Log Expense", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = theme.textMuted)
            }
        }
    )
}

private fun formatAmount(value: Double): String {
    val rounded = (value * 100).toLong() / 100.0
    return rounded.toString()
}
