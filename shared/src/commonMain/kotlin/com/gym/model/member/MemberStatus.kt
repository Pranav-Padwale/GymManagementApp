package com.gym.model.member

import kotlinx.serialization.Serializable

/**
 * Enum representing member subscription and onboarding statuses.
 */
@Serializable
enum class MemberStatus(val displayName: String) {
    ACTIVE("Active Member"),
    ENQUIRE("Enquiry / Lead"),
    INACTIVE("Inactive"),
    EXPIRED("Membership Expired")
}
