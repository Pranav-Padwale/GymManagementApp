package com.gym.server.documents

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * MongoDB Document for Trainer accounts in the 'trainers' collection.
 */
@Serializable
data class TrainerDocument(
    @SerialName("_id") val username: String,
    val passwordHash: String,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val specialization: String = "Strength & Conditioning",
    val role: String = "TRAINER"
)
