package com.gym.model.finance

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room KMP Entity for transaction records in database table.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amount: Double,
    val categoryName: String,
    val typeName: String,
    val dateIso: String,
    val year: Int,
    val month: Int,
    val note: String?
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        title = title,
        amount = amount,
        category = TransactionCategory.entries.find { it.name == categoryName } ?: TransactionCategory.OTHER,
        type = TransactionType.entries.find { it.name == typeName } ?: TransactionType.EXPENSE,
        dateIso = dateIso,
        year = year,
        month = month,
        note = note
    )

    companion object {
        fun fromDomain(tx: Transaction): TransactionEntity = TransactionEntity(
            id = tx.id,
            title = tx.title,
            amount = tx.amount,
            categoryName = tx.category.name,
            typeName = tx.type.name,
            dateIso = tx.dateIso,
            year = tx.year,
            month = tx.month,
            note = tx.note
        )
    }
}
