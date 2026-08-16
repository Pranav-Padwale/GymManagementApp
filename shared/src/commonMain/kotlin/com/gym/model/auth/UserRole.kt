package com.gym.model.auth

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    SUPER_ADMIN,
    TRAINER,
    MEMBER
}
