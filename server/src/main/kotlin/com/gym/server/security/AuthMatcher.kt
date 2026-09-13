package com.gym.server.security

import com.gym.model.auth.LoginRequest
import com.gym.model.auth.UserRole
import com.gym.server.documents.AdminDocument
import com.gym.server.documents.MemberDocument
import com.gym.server.documents.TrainerDocument

/**
 * Result of a successful credential match: the display name, the raw role string stored
 * on the account document, and the strongly-typed [UserRole] used in the JWT response.
 */
data class AuthenticatedUser(val displayName: String, val roleStr: String, val role: UserRole)

/**
 * Pure decision logic for `POST /api/login`, kept free of MongoDB I/O so it can be unit
 * tested directly. The caller is responsible for looking up [admin]/[trainer]/[member]
 * (each may be null if no matching document was found) and passing them in.
 *
 * Checks super admins, then trainers, then members, returning the first exact
 * (case-sensitive, non-empty) passwordHash match. There is no fallback/bypass path -
 * every account must match on its own stored credential.
 */
fun matchCredentials(
    request: LoginRequest,
    admin: AdminDocument?,
    trainer: TrainerDocument?,
    member: MemberDocument?
): AuthenticatedUser? {
    if (admin != null && admin.passwordHash == request.passwordHash) {
        return AuthenticatedUser(admin.username, admin.role, UserRole.SUPER_ADMIN)
    }
    if (trainer != null && trainer.passwordHash == request.passwordHash) {
        return AuthenticatedUser(trainer.username, trainer.role, UserRole.TRAINER)
    }
    if (member != null && member.passwordHash == request.passwordHash) {
        return AuthenticatedUser(member.fullName.ifBlank { member.username }, "MEMBER", UserRole.MEMBER)
    }
    return null
}

/**
 * RBAC decision logic: is [role] (the JWT's "role" claim, or null if absent/unauthenticated)
 * permitted to perform an action guarded to [allowedRoles]?
 */
fun isRoleAllowed(role: String?, allowedRoles: List<String>): Boolean =
    role != null && role in allowedRoles
