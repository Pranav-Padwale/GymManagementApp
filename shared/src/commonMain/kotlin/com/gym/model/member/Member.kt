package com.gym.model.member

/**
 * Domain model representing a gym member.
 * Completely independent of UI and Controllers.
 */
data class Member(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val status: MemberStatus,
    val membershipPlan: String,
    val joinDate: String,
    val expiryDate: String
)
