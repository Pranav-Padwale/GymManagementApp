package com.gym.server.documents

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * MongoDB Document for Member accounts and records in the 'members' collection.
 */
@Serializable
data class MemberDocument(
    @SerialName("_id") val id: String,
    val username: String = "member",
    val passwordHash: String = "member123",
    val fullName: String,
    val email: String,
    val phone: String,
    val status: String,
    val membershipPlan: String,
    val joinDate: String,
    val expiryDate: String,
    val role: String = "MEMBER"
)
