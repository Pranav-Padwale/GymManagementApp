package com.gym.server.documents

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDocument(
    @SerialName("_id") val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String,
    val dateIso: String,
    val year: Int,
    val month: Int
)
