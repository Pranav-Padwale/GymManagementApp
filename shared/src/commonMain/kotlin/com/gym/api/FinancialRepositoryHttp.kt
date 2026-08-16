package com.gym.api

import com.gym.model.finance.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinancialRepositoryHttp : FinancialRepository {
    var authToken: String? = null

    override suspend fun getTransactionsForMonth(month: Int, year: Int): Result<List<Transaction>> = withContext(Dispatchers.Default) {
        try {
            val response = HttpClientProvider.client.get("${HttpClientProvider.BASE_URL}/api/transactions") {
                authToken?.let { header("Authorization", "Bearer $it") }
            }
            if (response.status.isSuccess()) {
                val allTx: List<Transaction> = response.body()
                Result.success(allTx.filter { it.month == month && it.year == year })
            } else {
                Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun calculateBalanceSheetSummary(month: Int, year: Int): Result<BalanceSheetSummary> = withContext(Dispatchers.Default) {
        val txResult = getTransactionsForMonth(month, year)
        if (txResult.isFailure) return@withContext Result.failure(txResult.exceptionOrNull()!!)
        
        val transactions = txResult.getOrNull() ?: emptyList()
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpenses

        val categoryMap = mutableMapOf<TransactionCategory, Double>()
        transactions.forEach { tx ->
            categoryMap[tx.category] = (categoryMap[tx.category] ?: 0.0) + tx.amount
        }

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
    }

    override suspend fun addExpense(
        title: String, amount: Double, category: TransactionCategory, month: Int, year: Int
    ): Result<Unit> {
        val newTx = Transaction(
            id = "TX-${kotlin.random.Random.nextInt(1000, 9999)}",
            title = title, amount = amount, category = category,
            type = TransactionType.EXPENSE, dateIso = "2026-${month.toString().padStart(2, '0')}-25",
            year = year, month = month
        )
        return addTransaction(newTx)
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val response = HttpClientProvider.client.post("${HttpClientProvider.BASE_URL}/api/transactions") {
                contentType(ContentType.Application.Json)
                authToken?.let { header("Authorization", "Bearer $it") }
                setBody(transaction)
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
