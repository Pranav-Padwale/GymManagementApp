package com.gym.model.finance

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FinancialRepositoryImplTest {

    @Test
    fun calculateBalanceSheetSummary_seededJuly2026_computesCorrectTotalsAndNetBalance() = runTest {
        val repo = FinancialRepositoryImpl()

        val result = repo.calculateBalanceSheetSummary(month = 7, year = 2026)

        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        // Income: 4800.00 + 2450.00 + 1120.00 = 8370.00
        assertEquals(8370.00, summary.totalIncome, absoluteTolerance = 0.001)
        // Expenses: 299.00 + 2200.00 + 450.00 + 3100.00 + 185.00 = 6234.00
        assertEquals(6234.00, summary.totalExpenses, absoluteTolerance = 0.001)
        assertEquals(2136.00, summary.netBalance, absoluteTolerance = 0.001)
    }

    @Test
    fun calculateBalanceSheetSummary_monthWithNoTransactions_returnsAllZerosNotAnError() = runTest {
        val repo = FinancialRepositoryImpl()

        val result = repo.calculateBalanceSheetSummary(month = 1, year = 2026)

        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(0.0, summary.totalIncome)
        assertEquals(0.0, summary.totalExpenses)
        assertEquals(0.0, summary.netBalance)
        assertTrue(summary.categoryBreakdown.isEmpty())
        assertTrue(summary.expenseBreakdown.isEmpty())
        assertTrue(summary.transactions.isEmpty())
    }

    @Test
    fun calculateBalanceSheetSummary_expenseBreakdownPercentages_sumToApproximately100() = runTest {
        val repo = FinancialRepositoryImpl()

        val summary = repo.calculateBalanceSheetSummary(month = 7, year = 2026).getOrThrow()

        val totalPct = summary.expenseBreakdown.sumOf { it.percentageOfTotalExpenses.toDouble() }
        assertEquals(100.0, totalPct, absoluteTolerance = 0.1)
    }

    @Test
    fun calculateBalanceSheetSummary_categoryBreakdown_groupsIncomeAndExpenseSeparately() = runTest {
        val repo = FinancialRepositoryImpl()

        val summary = repo.calculateBalanceSheetSummary(month = 7, year = 2026).getOrThrow()

        // Only EXPENSE-typed categories should appear in expenseBreakdown, income categories excluded.
        assertTrue(summary.expenseBreakdown.none { it.category.type == TransactionType.INCOME })
        assertTrue(summary.categoryBreakdown.containsKey(TransactionCategory.MEMBERSHIP_FEE))
    }

    @Test
    fun addTransaction_newIncomeEntry_isReflectedInNextSummaryQuery() = runTest {
        val repo = FinancialRepositoryImpl()
        val before = repo.calculateBalanceSheetSummary(month = 9, year = 2026).getOrThrow()
        assertEquals(0.0, before.totalIncome)

        repo.addTransaction(
            Transaction(
                id = "TX-TEST-1", title = "Test Signup Fee", amount = 500.0,
                category = TransactionCategory.MEMBERSHIP_FEE, type = TransactionType.INCOME,
                dateIso = "2026-09-01", year = 2026, month = 9
            )
        )

        val after = repo.calculateBalanceSheetSummary(month = 9, year = 2026).getOrThrow()
        assertEquals(500.0, after.totalIncome, absoluteTolerance = 0.001)
    }

    @Test
    fun addExpense_negativeAmount_isAcceptedWithNoValidation_documentsMissingModelLayerGuard() = runTest {
        // NOTE: documents a real gap (see QA report "FIN-1"): FinancialRepositoryImpl has
        // no amount validation of its own. The only guard against non-positive amounts lives
        // in FinanceController.onAddExpense's UI-layer check, so any other caller of this
        // Model-layer API (tests, a future second UI, a batch import) can silently corrupt
        // balance sheet totals with negative "expenses".
        val repo = FinancialRepositoryImpl()

        val result = repo.addExpense(
            title = "Refund adjustment", amount = -100.0,
            category = TransactionCategory.OTHER, month = 10, year = 2026
        )

        assertTrue(result.isSuccess, "Repository currently accepts negative expense amounts without validation")
        val summary = repo.calculateBalanceSheetSummary(month = 10, year = 2026).getOrThrow()
        assertEquals(-100.0, summary.totalExpenses, absoluteTolerance = 0.001)
    }

    @Test
    fun getTransactionsForMonth_onlyReturnsTransactionsMatchingBothMonthAndYear() = runTest {
        val repo = FinancialRepositoryImpl()
        repo.addTransaction(
            Transaction("TX-Y1", "Old year same month", 10.0, TransactionCategory.OTHER, TransactionType.EXPENSE, "2025-07-01", 2025, 7)
        )

        val julyResult = repo.getTransactionsForMonth(month = 7, year = 2026).getOrThrow()

        assertTrue(julyResult.none { it.id == "TX-Y1" }, "A July-2025 transaction must not leak into the July-2026 query")
    }
}
