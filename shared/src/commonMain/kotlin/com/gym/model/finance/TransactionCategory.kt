package com.gym.model.finance

import kotlinx.serialization.Serializable

/**
 * Transaction categories for PT dues, store purchases, administrative expenses, etc.
 */
@Serializable
enum class TransactionCategory(val displayName: String, val type: TransactionType) {
    // Income Categories
    MEMBERSHIP_FEE("Membership Subscription Fees", TransactionType.INCOME),
    PERSONAL_TRAINING_DUES("Personal Training Dues", TransactionType.INCOME),
    STORE_PURCHASE("Supplement & Store Sales", TransactionType.INCOME),

    // Expense Categories
    ADMINISTRATIVE_EXPENSE("Administrative & Software Expenses", TransactionType.EXPENSE),
    UTILITIES_RENT("Facility Rent & Utilities", TransactionType.EXPENSE),
    EQUIPMENT_MAINTENANCE("Equipment Purchase & Maintenance", TransactionType.EXPENSE),
    TRAINER_PAYROLL("Trainer & Staff Payroll", TransactionType.EXPENSE),
    OTHER("Miscellaneous Expenses", TransactionType.EXPENSE)
}
