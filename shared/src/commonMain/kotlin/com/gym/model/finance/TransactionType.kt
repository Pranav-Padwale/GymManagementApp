package com.gym.model.finance

import kotlinx.serialization.Serializable

/**
 * Core type of a transaction.
 */
@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE
}
