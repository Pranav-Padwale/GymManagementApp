package com.gym.server.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordHasherTest {

    @Test
    fun hash_producesABcryptFormattedValue() {
        val hashed = PasswordHasher.hash("admin123")

        assertTrue(PasswordHasher.isBcryptHash(hashed))
    }

    @Test
    fun hash_neverReturnsThePlaintextInput() {
        val hashed = PasswordHasher.hash("admin123")

        assertTrue(hashed != "admin123")
    }

    @Test
    fun hash_sameInputTwice_producesDifferentHashes_dueToRandomSalt() {
        val first = PasswordHasher.hash("admin123")
        val second = PasswordHasher.hash("admin123")

        assertTrue(first != second, "Each hash call must use a fresh random salt")
    }

    @Test
    fun verify_correctPasswordAgainstItsHash_succeeds() {
        val hashed = PasswordHasher.hash("admin123")

        assertTrue(PasswordHasher.verify("admin123", hashed))
    }

    @Test
    fun verify_wrongPasswordAgainstAHash_fails() {
        val hashed = PasswordHasher.hash("admin123")

        assertFalse(PasswordHasher.verify("wrong-password", hashed))
    }

    @Test
    fun verify_legacyPlaintextValue_stillMatchesExactly_forMigrationCompatibility() {
        // Pre-AUTH-2 accounts store the raw password as passwordHash. verify() must accept
        // this once so DataSeeder's migration can upgrade it on the next startup.
        assertTrue(PasswordHasher.verify("admin123", "admin123"))
    }

    @Test
    fun verify_legacyPlaintextValue_wrongCandidate_fails() {
        assertFalse(PasswordHasher.verify("wrong-password", "admin123"))
    }

    @Test
    fun isBcryptHash_recognizesAllThreeCommonBcryptVersionPrefixes() {
        assertTrue(PasswordHasher.isBcryptHash("\$2a\$10\$" + "a".repeat(53)))
        assertTrue(PasswordHasher.isBcryptHash("\$2b\$12\$" + "b".repeat(53)))
        assertTrue(PasswordHasher.isBcryptHash("\$2y\$10\$" + "c".repeat(53)))
    }

    @Test
    fun isBcryptHash_plainString_isNotMisclassifiedAsAHash() {
        assertFalse(PasswordHasher.isBcryptHash("admin123"))
        assertFalse(PasswordHasher.isBcryptHash(""))
    }
}
