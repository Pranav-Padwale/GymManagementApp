package com.gym.model.member

/**
 * Enum representing member subscription and onboarding statuses.
 */
enum class MemberStatus(val displayName: String) {
    ACTIVE("Active Member"),
    ENQUIRE("Enquiry / Lead"),
    INACTIVE("Inactive"),
    EXPIRED("Membership Expired")
}
