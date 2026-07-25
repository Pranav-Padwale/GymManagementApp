package com.gym.controller.finance

import com.gym.model.finance.FinancialRepository
import com.gym.model.finance.TransactionCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Controller: Dedicated Kotlin class for Income & Expense Balance Sheet feature.
 * Triggers data fetching for specific month ranges, handles database errors,
 * builds compiled balance sheet summaries, and forwards state updates to the View.
 */
class FinanceController(
    private val repository: FinancialRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        // Initial load for current month/year
        refreshBalanceSheet()
    }

    /**
     * User action handler: Trigger data fetching for a specific month & year range.
     */
    fun onMonthRangeChanged(month: Int, year: Int) {
        _uiState.update { it.copy(selectedMonth = month, selectedYear = year) }
        loadBalanceSheetForMonth(month, year)
    }

    /**
     * User action handler: Forward new logged expense back to Controller and Model.
     */
    fun onAddExpense(title: String, amount: Double, category: TransactionCategory) {
        if (title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Expense description cannot be empty.") }
            return
        }
        if (amount <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Expense amount must be greater than zero.") }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val currentMonth = _uiState.value.selectedMonth
            val currentYear = _uiState.value.selectedYear

            val result = repository.addExpense(
                title = title.trim(),
                amount = amount,
                category = category,
                month = currentMonth,
                year = currentYear
            )

            result.fold(
                onSuccess = {
                    // Re-query balance sheet summary and update UI state
                    loadBalanceSheetForMonth(currentMonth, currentYear)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Database pull error while saving expense: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Action handler: Clear error message banner.
     */
    fun onClearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Force refresh current month balance sheet summary.
     */
    fun refreshBalanceSheet() {
        val month = _uiState.value.selectedMonth
        val year = _uiState.value.selectedYear
        loadBalanceSheetForMonth(month, year)
    }

    private fun loadBalanceSheetForMonth(month: Int, year: Int) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.calculateBalanceSheetSummary(month, year)
            result.fold(
                onSuccess = { compiledSummary ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summary = compiledSummary,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { databaseError ->
                    // Handle database failure gracefully by setting error state
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Database fetch failure: ${databaseError.message ?: "Unable to query financial records"}"
                        )
                    }
                }
            )
        }
    }
}
