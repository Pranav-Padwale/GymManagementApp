package com.gym.server.security

import org.mindrot.jbcrypt.BCrypt

/**
 * Bcrypt password hashing, with a read-only fallback for legacy plaintext values that
 * predate this change (see QA audit AUTH-2). New passwords are always hashed; verification
 * accepts an old plaintext-stored value once, so [com.gym.server.database.PasswordMigration]
 * can upgrade it to a real hash without breaking existing accounts' next login.
 */
object PasswordHasher {

    private val BCRYPT_PATTERN = Regex("^\\$2[aby]\\$\\d{2}\\$.{53}$")

    fun hash(plainPassword: String): String = BCrypt.hashpw(plainPassword, BCrypt.gensalt())

    fun isBcryptHash(value: String): Boolean = BCRYPT_PATTERN.matches(value)

    /**
     * True if [candidatePlainPassword] matches [storedValue] - either by bcrypt verification
     * (the normal case), or by direct comparison when [storedValue] is a pre-migration
     * plaintext value that hasn't been rehashed yet.
     */
    fun verify(candidatePlainPassword: String, storedValue: String): Boolean =
        if (isBcryptHash(storedValue)) {
            BCrypt.checkpw(candidatePlainPassword, storedValue)
        } else {
            candidatePlainPassword == storedValue
        }
}
