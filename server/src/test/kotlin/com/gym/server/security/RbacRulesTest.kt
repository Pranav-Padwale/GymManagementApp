package com.gym.server.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RbacRulesTest {

    @Test
    fun isRoleAllowed_roleInAllowList_isTrue() {
        assertTrue(isRoleAllowed("SUPER_ADMIN", listOf("SUPER_ADMIN", "TRAINER")))
    }

    @Test
    fun isRoleAllowed_roleNotInAllowList_isFalse() {
        assertFalse(isRoleAllowed("MEMBER", listOf("SUPER_ADMIN", "TRAINER")))
    }

    @Test
    fun isRoleAllowed_nullRole_unauthenticatedCaller_isFalse() {
        assertFalse(isRoleAllowed(null, listOf("SUPER_ADMIN", "TRAINER")))
    }

    @Test
    fun isRoleAllowed_emptyAllowList_alwaysFalse() {
        assertFalse(isRoleAllowed("SUPER_ADMIN", emptyList()))
    }

    @Test
    fun isRoleAllowed_isCaseSensitive_doesNotMatchLowercaseRoleClaim() {
        // JWT role claims are always emitted in upper snake case (see AuthRoutes); a lowercase
        // or mismatched claim must not accidentally satisfy the allow-list check.
        assertFalse(isRoleAllowed("super_admin", listOf("SUPER_ADMIN")))
    }
}
