package com.gym.model.finance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Model Layer: FinancialRepository Contract
 */
interface FinancialRepository {
    suspend fun getTransactionsForMonth(month: Int, year: Int): Result<List<Transaction>>
    suspend fun calculateBalanceSheetSummary(month: Int, year: Int): Result<BalanceSheetSummary>
    suspend fun addExpense(title: String, amount: Double, category: TransactionCategory, month: Int, year: Int): Result<Unit>
    suspend fun addTransaction(transaction: Transaction): Result<Unit>
}

/**
 * Model Layer: Room KMP implementation of FinancialRepository
 */
class FinancialRepositoryImpl(
    private val transactionDao: TransactionDao? = null
) : FinancialRepository {

    // Initial seed dataset for demo/testing
    private val mockTransactions = mutableListOf(
        Transaction("TX-1", "Gold Membership Signups", 4800.00, TransactionCategory.MEMBERSHIP_FEE, TransactionType.INCOME, "2026-07-02", 2026, 7),
        Transaction("TX-2", "Personal Training Sessions - Coach Dan", 2450.00, TransactionCategory.PERSONAL_TRAINING_DUES, TransactionType.INCOME, "2026-07-05", 2026, 7),
        Transaction("TX-3", "Whey Protein & Creatine Inventory", 1120.00, TransactionCategory.STORE_PURCHASE, TransactionType.INCOME, "2026-07-10", 2026, 7),
        Transaction("TX-4", "Gym Management Software Subscription", 299.00, TransactionCategory.ADMINISTRATIVE_EXPENSE, TransactionType.EXPENSE, "2026-07-01", 2026, 7),
        Transaction("TX-5", "Facility Rent & Air Conditioning Utilities", 2200.00, TransactionCategory.UTILITIES_RENT, TransactionType.EXPENSE, "2026-07-03", 2026, 7),
        Transaction("TX-6", "Treadmill Cable Maintenance & Lubrication", 450.00, TransactionCategory.EQUIPMENT_MAINTENANCE, TransactionType.EXPENSE, "2026-07-12", 2026, 7),
        Transaction("TX-7", "Staff & Trainer Bi-Weekly Payroll", 3100.00, TransactionCategory.TRAINER_PAYROLL, TransactionType.EXPENSE, "2026-07-15", 2026, 7),
        Transaction("TX-8", "Sanitization & Towel Cleaning Supplies", 185.00, TransactionCategory.OTHER, TransactionType.EXPENSE, "2026-07-18", 2026, 7)
    )

    override suspend fun getTransactionsForMonth(month: Int, year: Int): Result<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            if (transactionDao != null) {
                val entities = transactionDao.getTransactionsForMonth(month, year)
                if (entities.isEmpty()) {
                    // Seed initial demo set
                    val seedForMonth = mockTransactions.filter { it.month == month && it.year == year }
                    if (seedForMonth.isNotEmpty()) {
                        transactionDao.insertAll(seedForMonth.map { TransactionEntity.fromDomain(it) })
                        Result.success(seedForMonth)
                    } else {
                        Result.success(emptyList())
                    }
                } else {
                    Result.success(entities.map { it.toDomain() })
                }
            } else {
                Result.success(mockTransactions.filter { it.month == month && it.year == year })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateBalanceSheetSummary(month: Int, year: Int): Result<BalanceSheetSummary> = withContext(Dispatchers.IO) {
        try {
            val txResult = getTransactionsForMonth(month, year)
            if (txResult.isFailure) return@withContext Result.failure(txResult.exceptionOrNull()!!)

            val transactions = txResult.getOrNull() ?: emptyList()

            val totalIncome = transactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }

            val totalExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            val netBalance = totalIncome - totalExpenses

            // Breakdown by category
            val categoryMap = mutableMapOf<TransactionCategory, Double>()
            transactions.forEach { tx ->
                val current = categoryMap.getOrElse(tx.category) { 0.0 }
                categoryMap[tx.category] = current + tx.amount
            }

            // Calculate percentage expense breakdown list
            val expenseItems = categoryMap
                .filter { (cat, _) -> cat.type == TransactionType.EXPENSE }
                .map { (cat, amount) ->
                    val pct = if (totalExpenses > 0) (amount / totalExpenses * 100).toFloat() else 0f
                    CategoryBreakdownItem(cat, amount, pct)
                }
                .sortedByDescending { it.totalAmount }

            val summary = BalanceSheetSummary(
                totalIncome = totalIncome,
                totalExpenses = totalExpenses,
                netBalance = netBalance,
                categoryBreakdown = categoryMap,
                expenseBreakdown = expenseItems,
                transactions = transactions
            )

            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addExpense(
        title: String,
        amount: Double,
        category: TransactionCategory,
        month: Int,
        year: Int
    ): Result<Unit> {
        if (amount <= 0.0) {
            return Result.failure(IllegalArgumentException("Expense amount must be greater than zero, was $amount"))
        }
        val newTx = Transaction(
            id = "TX-${kotlin.random.Random.nextLong(100000, 999999)}",
            title = title,
            amount = amount,
            category = category,
            type = TransactionType.EXPENSE,
            dateIso = "2026-${month.toString().padStart(2, '0')}-25",
            year = year,
            month = month
        )
        return addTransaction(newTx)
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> = withContext(Dispatchers.IO) {
        if (transaction.amount <= 0.0) {
            return@withContext Result.failure(
                IllegalArgumentException("Transaction amount must be greater than zero, was ${transaction.amount}")
            )
        }
        try {
            if (transactionDao != null) {
                transactionDao.insertTransaction(TransactionEntity.fromDomain(transaction))
            }
            mockTransactions.add(0, transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
