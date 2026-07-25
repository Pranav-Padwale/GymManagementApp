package com.gym.model.finance

/**
 * Domain model representing a financial transaction.
 * Pure Kotlin data layer object.
 */
data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: TransactionCategory,
    val type: TransactionType,
    val dateIso: String, // e.g. "2026-07-25"
    val year: Int,       // e.g. 2026
    val month: Int,      // e.g. 7
    val note: String? = null
)
