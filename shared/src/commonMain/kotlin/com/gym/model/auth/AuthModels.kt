package com.gym.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val passwordHash: String // We will just send plaintext for this MVP, but named hash for future proofing
)

@Serializable
data class LoginResponse(
    val token: String,
    val role: UserRole,
    val username: String
)
