package com.gym.model.finance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Room KMP Data Access Object for Transactions.
 */
@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE month = :month AND year = :year ORDER BY dateIso DESC")
    suspend fun getTransactionsForMonth(month: Int, year: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY dateIso DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)
}
