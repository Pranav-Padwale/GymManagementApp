package com.gym.server.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MemberCredentialProvisionerTest {

    @Test
    fun usernameFromEmail_takesTheLocalPartBeforeAt() {
        assertEquals("alex", MemberCredentialProvisioner.usernameFromEmail("alex@example.com"))
    }

    @Test
    fun usernameFromEmail_lowercasesAndStripsNonAlphanumericCharacters() {
        assertEquals("alexriversgym", MemberCredentialProvisioner.usernameFromEmail("Alex.Rivers+gym@example.com"))
    }

    @Test
    fun usernameFromEmail_blankOrSymbolOnlyLocalPart_fallsBackToGenericName() {
        assertEquals("member", MemberCredentialProvisioner.usernameFromEmail("...@example.com"))
        assertEquals("member", MemberCredentialProvisioner.usernameFromEmail("@example.com"))
    }

    @Test
    fun generateTemporaryPassword_defaultLength_is12Characters() {
        assertEquals(12, MemberCredentialProvisioner.generateTemporaryPassword().length)
    }

    @Test
    fun generateTemporaryPassword_onlyContainsAlphanumericCharacters() {
        val password = MemberCredentialProvisioner.generateTemporaryPassword(200)

        assertTrue(password.all { it.isLetterOrDigit() })
    }

    @Test
    fun generateTemporaryPassword_consecutiveCalls_areNotIdentical() {
        // Astronomically unlikely to collide by chance at 12 chars; a real bug (e.g. an
        // unseeded/fixed RNG) would make this fail deterministically.
        val a = MemberCredentialProvisioner.generateTemporaryPassword()
        val b = MemberCredentialProvisioner.generateTemporaryPassword()

        assertNotEquals(a, b)
    }

    @Test
    fun uniqueUsername_noCollision_returnsBaseNameUnchanged() {
        val result = MemberCredentialProvisioner.uniqueUsername("alex", existingUsernames = emptySet())

        assertEquals("alex", result)
    }

    @Test
    fun uniqueUsername_oneCollision_appendsDashTwo() {
        val result = MemberCredentialProvisioner.uniqueUsername("alex", existingUsernames = setOf("alex"))

        assertEquals("alex-2", result)
    }

    @Test
    fun uniqueUsername_multipleCollisions_incrementsUntilFree() {
        val result = MemberCredentialProvisioner.uniqueUsername(
            "alex",
            existingUsernames = setOf("alex", "alex-2", "alex-3")
        )

        assertEquals("alex-4", result)
    }

    @Test
    fun provision_returnsAHashNotThePlaintextPassword() {
        val credentials = MemberCredentialProvisioner.provision("alex@example.com")

        assertEquals("alex", credentials.username)
        assertTrue(PasswordHasher.isBcryptHash(credentials.passwordHash))
        assertTrue(PasswordHasher.verify(credentials.plainTemporaryPassword, credentials.passwordHash))
    }
}
