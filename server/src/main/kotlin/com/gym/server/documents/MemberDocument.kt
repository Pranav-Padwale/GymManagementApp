package com.gym.server.documents

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberDocument(
    @SerialName("_id") val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val status: String,
    val membershipPlan: String,
    val joinDate: String,
    val expiryDate: String
)
