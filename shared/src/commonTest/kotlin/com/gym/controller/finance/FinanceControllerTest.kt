package com.gym.controller.finance

import com.gym.model.finance.BalanceSheetSummary
import com.gym.model.finance.FinancialRepository
import com.gym.model.finance.Transaction
import com.gym.model.finance.TransactionCategory
import com.gym.model.finance.TransactionType
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** In-memory fake with no dispatcher hops, plus a switch to simulate backend failures. */
private class FakeFinancialRepository : FinancialRepository {
    var shouldFail = false
    val transactions = mutableListOf<Transaction>()

    override suspend fun getTransactionsForMonth(month: Int, year: Int): Result<List<Transaction>> =
        if (shouldFail) Result.failure(Exception("simulated network timeout"))
        else Result.success(transactions.filter { it.month == month && it.year == year })

    override suspend fun calculateBalanceSheetSummary(month: Int, year: Int): Result<BalanceSheetSummary> {
        if (shouldFail) return Result.failure(Exception("simulated server 500"))
        val monthTx = transactions.filter { it.month == month && it.year == year }
        val income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        return Result.success(BalanceSheetSummary(totalIncome = income, totalExpenses = expense, netBalance = income - expense, transactions = monthTx))
    }

    override suspend fun addExpense(title: String, amount: Double, category: TransactionCategory, month: Int, year: Int): Result<Unit> {
        if (shouldFail) return Result.failure(Exception("simulated DB write failure"))
        transactions.add(Transaction("TX-${transactions.size}", title, amount, category, TransactionType.EXPENSE, "$year-${month}-01", year, month))
        return Result.success(Unit)
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        transactions.add(transaction)
        return Result.success(Unit)
    }
}

class FinanceControllerTest {

    private fun newController(repo: FinancialRepository): FinanceController {
        val scope = TestScope(UnconfinedTestDispatcher())
        return FinanceController(repository = repo, scope = scope)
    }

    @Test
    fun onAddExpense_blankTitle_isRejectedBeforeReachingRepository() = runTest {
        val repo = FakeFinancialRepository()
        val controller = newController(repo)

        controller.onAddExpense("   ", 50.0, TransactionCategory.OTHER)

        assertEquals("Expense description cannot be empty.", controller.uiState.value.errorMessage)
        assertTrue(repo.transactions.isEmpty())
    }

    @Test
    fun onAddExpense_zeroAmount_isRejectedBeforeReachingRepository() = runTest {
        val repo = FakeFinancialRepository()
        val controller = newController(repo)

        controller.onAddExpense("Rent", 0.0, TransactionCategory.UTILITIES_RENT)

        assertEquals("Expense amount must be greater than zero.", controller.uiState.value.errorMessage)
        assertTrue(repo.transactions.isEmpty())
    }

    @Test
    fun onAddExpense_negativeAmount_isRejectedBeforeReachingRepository() = runTest {
        val repo = FakeFinancialRepository()
        val controller = newController(repo)

        controller.onAddExpense("Rent", -20.0, TransactionCategory.UTILITIES_RENT)

        assertEquals("Expense amount must be greater than zero.", controller.uiState.value.errorMessage)
        assertTrue(repo.transactions.isEmpty())
    }

    @Test
    fun onAddExpense_validInput_addsExpenseAndRefreshesSummary() = runTest {
        val repo = FakeFinancialRepository()
        val controller = newController(repo)

        controller.onAddExpense("New Treadmill", 899.0, TransactionCategory.EQUIPMENT_MAINTENANCE)

        assertNull(controller.uiState.value.errorMessage)
        assertEquals(899.0, controller.uiState.value.summary.totalExpenses, absoluteTolerance = 0.001)
    }

    @Test
    fun refreshBalanceSheet_repositoryFailure_surfacesErrorMessageAndClearsLoading() = runTest {
        val repo = FakeFinancialRepository().apply { shouldFail = true }
        val controller = newController(repo)

        controller.refreshBalanceSheet()

        val state = controller.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("simulated server 500"))
        assertTrue(!state.isLoading)
    }

    @Test
    fun onClearError_removesErrorMessageFromState() = runTest {
        val repo = FakeFinancialRepository().apply { shouldFail = true }
        val controller = newController(repo)
        controller.refreshBalanceSheet()
        assertNotNull(controller.uiState.value.errorMessage)

        controller.onClearError()

        assertNull(controller.uiState.value.errorMessage)
    }
}
