package com.gym.controller.finance

import com.gym.model.finance.BalanceSheetSummary

/**
 * Immutable State object passed from FinanceController to passive View.
 */
data class FinanceUiState(
    val selectedMonth: Int = 7, // July
    val selectedYear: Int = 2026,
    val summary: BalanceSheetSummary = BalanceSheetSummary(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAddExpenseDialogOpen: Boolean = false
)
