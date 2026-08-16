package com.gym.server.documents

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * MongoDB Document for Super Admin accounts in the 'super_admins' collection.
 */
@Serializable
data class AdminDocument(
    @SerialName("_id") val username: String,
    val passwordHash: String,
    val fullName: String = "Administrator",
    val email: String = "admin@forgegym.com",
    val role: String = "SUPER_ADMIN"
)
