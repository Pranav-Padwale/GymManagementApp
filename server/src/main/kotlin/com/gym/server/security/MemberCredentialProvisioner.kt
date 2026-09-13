package com.gym.server.security

import java.security.SecureRandom

/**
 * Freshly-provisioned login credentials for a newly created member.
 * [plainTemporaryPassword] is only ever available at creation time - only [passwordHash]
 * (the bcrypt hash) is persisted, so the caller (an admin/trainer) must hand the plaintext
 * value to the member out-of-band right away.
 */
data class ProvisionedCredentials(
    val username: String,
    val plainTemporaryPassword: String,
    val passwordHash: String
)

/**
 * Generates a unique username + a random temporary password for a new member account.
 * Fixes MEMBER-2 (see QA audit): previously every member created via the API silently fell
 * back to the same shared username ("member") and password ("member123").
 */
object MemberCredentialProvisioner {

    private const val TEMP_PASSWORD_LENGTH = 12
    private const val ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    private val random = SecureRandom()

    /** Derives a base username from the local part of an email address, e.g. "alex@x.com" -> "alex". */
    fun usernameFromEmail(email: String): String {
        val local = email.substringBefore('@').lowercase().filter { it.isLetterOrDigit() }
        return local.ifBlank { "member" }
    }

    fun generateTemporaryPassword(length: Int = TEMP_PASSWORD_LENGTH): String =
        (1..length).map { ALPHANUMERIC[random.nextInt(ALPHANUMERIC.length)] }.joinToString("")

    /**
     * Appends "-2", "-3", ... to [baseUsername] until the result isn't in [existingUsernames].
     * [baseUsername] itself is returned unchanged if it's already unique.
     */
    fun uniqueUsername(baseUsername: String, existingUsernames: Set<String>): String {
        if (baseUsername !in existingUsernames) return baseUsername
        var suffix = 2
        while ("$baseUsername-$suffix" in existingUsernames) suffix++
        return "$baseUsername-$suffix"
    }

    /** Provisions a random temporary password (hashed for storage) and a username derived from [email]. */
    fun provision(email: String): ProvisionedCredentials {
        val plain = generateTemporaryPassword()
        return ProvisionedCredentials(
            username = usernameFromEmail(email),
            plainTemporaryPassword = plain,
            passwordHash = PasswordHasher.hash(plain)
        )
    }
}
