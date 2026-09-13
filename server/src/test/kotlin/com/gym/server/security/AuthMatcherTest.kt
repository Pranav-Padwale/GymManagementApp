package com.gym.server.security

import com.gym.model.auth.LoginRequest
import com.gym.model.auth.UserRole
import com.gym.server.documents.AdminDocument
import com.gym.server.documents.MemberDocument
import com.gym.server.documents.TrainerDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthMatcherTest {

    private val admin = AdminDocument(username = "admin", passwordHash = "admin123", role = "SUPER_ADMIN")
    private val trainer = TrainerDocument(username = "sarah", passwordHash = "trainer123", fullName = "Coach Sarah", email = "s@x.com")
    private val member = MemberDocument(
        id = "M-101", username = "alex", passwordHash = "alexpw", fullName = "Alex Rivers",
        email = "alex@x.com", phone = "1", status = "ACTIVE", membershipPlan = "Gold",
        joinDate = "2026-01-01", expiryDate = "2027-01-01"
    )

    @Test
    fun matchCredentials_correctAdminPassword_returnsSuperAdmin() {
        val result = matchCredentials(LoginRequest("admin", "admin123"), admin, null, null)

        assertEquals(UserRole.SUPER_ADMIN, result?.role)
        assertEquals("admin", result?.displayName)
    }

    @Test
    fun matchCredentials_correctTrainerPassword_returnsTrainer() {
        val result = matchCredentials(LoginRequest("sarah", "trainer123"), null, trainer, null)

        assertEquals(UserRole.TRAINER, result?.role)
    }

    @Test
    fun matchCredentials_correctMemberPassword_returnsMember() {
        val result = matchCredentials(LoginRequest("alex", "alexpw"), null, null, member)

        assertEquals(UserRole.MEMBER, result?.role)
        assertEquals("Alex Rivers", result?.displayName)
    }

    @Test
    fun matchCredentials_wrongPassword_returnsNull() {
        val result = matchCredentials(LoginRequest("admin", "wrong-password"), admin, null, null)

        assertNull(result)
    }

    @Test
    fun matchCredentials_noDocumentsFound_returnsNull() {
        val result = matchCredentials(LoginRequest("ghost", "anything"), null, null, null)

        assertNull(result)
    }

    @Test
    fun matchCredentials_universalMemberBypassPassword_isNoLongerAccepted() {
        // Regression test for AUTH-1 (see QA report): "member123" used to authenticate as
        // ANY resolved member document regardless of that member's real stored password.
        // A matched member whose real password is NOT "member123" must be rejected.
        val victimWithDifferentRealPassword = member.copy(passwordHash = "theirRealSecret")

        val result = matchCredentials(LoginRequest("alex", "member123"), null, null, victimWithDifferentRealPassword)

        assertNull(result, "The universal member123 bypass must be gone - only the member's own real passwordHash should authenticate them")
    }

    @Test
    fun matchCredentials_magicMemberUsernameShortcut_isNoLongerAccepted() {
        // Regression test for AUTH-1: username == "member" combined with passwordHash ==
        // "member123" used to log the caller in as an arbitrary "first member in DB" even
        // when no member document matched the request at all.
        val result = matchCredentials(LoginRequest("member", "member123"), null, null, null)

        assertNull(result)
    }

    @Test
    fun matchCredentials_adminCheckedBeforeTrainerAndMember_whenAllThreeCoexist() {
        // If somehow the same username collides across collections, admin must win first,
        // matching the original checking order (super admin -> trainer -> member).
        val collidingTrainer = trainer.copy(username = "admin", passwordHash = "admin123")
        val result = matchCredentials(LoginRequest("admin", "admin123"), admin, collidingTrainer, null)

        assertEquals(UserRole.SUPER_ADMIN, result?.role)
    }
}
