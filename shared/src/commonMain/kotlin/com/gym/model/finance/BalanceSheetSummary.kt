package com.gym.model.finance

/**
 * Compiled Balance Sheet Summary object containing total income, expenses, net balance,
 * category breakdowns, and transaction history.
 */
data class BalanceSheetSummary(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0,
    val categoryBreakdown: Map<TransactionCategory, Double> = emptyMap(),
    val expenseBreakdown: List<CategoryBreakdownItem> = emptyList(),
    val transactions: List<Transaction> = emptyList()
)

data class CategoryBreakdownItem(
    val category: TransactionCategory,
    val totalAmount: Double,
    val percentageOfTotalExpenses: Float
)
