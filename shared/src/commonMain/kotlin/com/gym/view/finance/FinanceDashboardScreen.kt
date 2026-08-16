package com.gym.view.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.finance.FinanceUiState
import com.gym.model.finance.TransactionCategory
import com.gym.model.finance.TransactionType
import com.gym.view.theme.*

/**
 * View: Income & Expense Financial Balance Sheet matching Google Stitch ("Elite Gym Management Pro").
 * Supports real-time balance sheet ledger, categorized operational expenses, and expense logging modal.
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Finance & Ledger",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = theme.textMain,
                                maxLines = 1
                            )
                            val monthName = when (state.selectedMonth) { 1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"; 5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"; 9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; else -> "Dec" }
                            Surface(
                                color = GymAccentSecondary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$monthName ${state.selectedYear}",
                                    color = GymAccentSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Revenue & expense balance sheet",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textMuted,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(theme.cardBg)
                            .border(1.dp, theme.cardBorder, CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GymAccentPrimary)
                    }

                    Button(
                        onClick = { isAddExpenseDialogOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GymAccentPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }
            }

            // 2. Financial Summary Cards
            // Net Operating Balance (Hero)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = GymAccentPrimary.copy(alpha = 0.15f)),
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("NET OPERATING BALANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textMuted, letterSpacing = 1.sp)
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GymAccentPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$${state.summary.netBalance.toInt().toString().reversed().chunked(3).joinToString(",").reversed()}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = GymAccentPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Liquid reserves & working operating buffer", fontSize = 11.sp, color = theme.textMuted)
                }
            }

            // Gross Revenue & Expenses in a 2-Card Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gross Income Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("GROSS REVENUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textMuted, letterSpacing = 0.5.sp)
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = GymAccentSecondary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$${state.summary.totalIncome.toInt().toString().reversed().chunked(3).joinToString(",").reversed()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GymAccentSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Memberships & store", fontSize = 10.sp, color = theme.textMuted)
                    }
                }

                // Operating Expenses Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("EXPENSES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textMuted, letterSpacing = 0.5.sp)
                            Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = GymAccentDanger, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$${state.summary.totalExpenses.toInt().toString().reversed().chunked(3).joinToString(",").reversed()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GymAccentDanger
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Salaries & rent", fontSize = 10.sp, color = theme.textMuted)
                    }
                }
            }

            // 3. Expense Breakdown Progress Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXPENSE DISTRIBUTION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = theme.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.summary.expenseBreakdown.isEmpty()) {
                        Text("No expenses recorded for this billing cycle.", fontSize = 13.sp, color = theme.textMuted)
                    } else {
                        state.summary.expenseBreakdown.forEach { item ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.category.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = theme.textMain)
                                    Text(
                                        "$${item.totalAmount.toInt()} (${(item.percentageOfTotalExpenses * 100).toInt()}%)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = GymAccentDanger
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { item.percentageOfTotalExpenses.toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = GymAccentDanger,
                                    trackColor = theme.cardElevated
                                )
                            }
                        }
                    }
                }
            }

            // 4. Recent Transactions Ledger Feed
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RECENT TRANSACTIONS LEDGER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = theme.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.summary.transactions.isEmpty()) {
                        Text("No transactions logged for this month.", fontSize = 13.sp, color = theme.textMuted)
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            state.summary.transactions.forEach { tx ->
                                val isIncome = tx.type == TransactionType.INCOME
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(theme.cardElevated)
                                        .padding(12.dp),
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
                                                .background(if (isIncome) GymAccentSecondary.copy(alpha = 0.15f) else GymAccentDanger.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isIncome) Icons.Default.Add else Icons.Default.Remove,
                                                contentDescription = null,
                                                tint = if (isIncome) GymAccentSecondary else GymAccentDanger,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f, fill = false)) {
                                            Text(tx.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = theme.textMain, maxLines = 1)
                                            Text("${tx.dateIso} • ${tx.category.displayName}", fontSize = 11.sp, color = theme.textMuted, maxLines = 1)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "${if (isIncome) "+" else "-"}$${tx.amount.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = if (isIncome) GymAccentSecondary else GymAccentDanger
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Log Expense Dialog Modal
        if (isAddExpenseDialogOpen) {
            AddExpenseModalDialog(
                theme = theme,
                onDismiss = { isAddExpenseDialogOpen = false },
                onAdd = { title, amount, category ->
                    isAddExpenseDialogOpen = false
                    onAddExpense(title, amount, category)
                }
            )
        }
    }
}

@Composable
private fun AddExpenseModalDialog(
    theme: AppThemeColors,
    onDismiss: () -> Unit,
    onAdd: (title: String, amount: Double, category: TransactionCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.EQUIPMENT_MAINTENANCE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Record Operating Expense", fontWeight = FontWeight.Bold, color = theme.textMain)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Description (e.g. Kettlebell Rack Repair)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($ USD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Expense Category:", fontSize = 12.sp, color = theme.textMuted, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        TransactionCategory.EQUIPMENT_MAINTENANCE,
                        TransactionCategory.TRAINER_PAYROLL,
                        TransactionCategory.UTILITIES_RENT,
                        TransactionCategory.ADMINISTRATIVE_EXPENSE
                    ).forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) GymAccentPrimary.copy(alpha = 0.2f) else theme.cardElevated,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GymAccentPrimary) else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = when (cat) {
                                        TransactionCategory.EQUIPMENT_MAINTENANCE -> "Equipment"
                                        TransactionCategory.TRAINER_PAYROLL -> "Payroll"
                                        TransactionCategory.UTILITIES_RENT -> "Rent/Util"
                                        TransactionCategory.ADMINISTRATIVE_EXPENSE -> "Admin"
                                        else -> "Other"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GymAccentPrimary else theme.textMuted
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0.0) {
                        onAdd(title.trim(), amount, selectedCategory)
                    }
                },
                enabled = title.isNotBlank() && amountStr.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GymAccentDanger)
            ) {
                Text("Record Expense", color = Color.White)
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
