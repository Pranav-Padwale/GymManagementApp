package com.gym.server.routes

import com.gym.model.member.Member
import com.gym.model.member.MemberStatus
import com.gym.server.database.MongoConfig
import com.gym.server.documents.MemberDocument
import io.ktor.http.*
import io.ktor.server.application.*
import com.gym.server.security.isRoleAllowed
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList

fun Route.memberRoutes() {
    authenticate {
        route("/api/members") {
            get {
                val docs = MongoConfig.members.find().toList()
                val members = docs.map { it.toSharedModel() }
                call.respond(members)
            }

            post {
                if (!call.requireRole("SUPER_ADMIN", "TRAINER")) return@post

                val newMember = call.receive<Member>()
                val doc = newMember.toDocument()
                MongoConfig.members.insertOne(doc)
                call.respond(HttpStatusCode.Created, newMember)
            }

            patch("{id}/status") {
                if (!call.requireRole("SUPER_ADMIN", "TRAINER")) return@patch

                val id = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing id")
                // For now, accept the plain string from the request body or query parameter
                val newStatusStr = call.receive<String>()
                val newStatus = try {
                    MemberStatus.valueOf(newStatusStr)
                } catch (e: Exception) {
                    return@patch call.respond(HttpStatusCode.BadRequest, "Invalid status")
                }

                val doc = MongoConfig.members.find(com.mongodb.client.model.Filters.eq("_id", id)).toList().firstOrNull()
                if (doc != null) {
                    val updated = doc.copy(status = newStatus.name)
                    MongoConfig.members.replaceOne(com.mongodb.client.model.Filters.eq("_id", id), updated)
                    call.respond(HttpStatusCode.OK, updated.toSharedModel())
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

/**
 * RBAC guard: responds 403 and returns false when the caller's JWT role isn't in [allowedRoles].
 */
suspend fun ApplicationCall.requireRole(vararg allowedRoles: String): Boolean {
    val role = principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
    if (!isRoleAllowed(role, allowedRoles.toList())) {
        respond(HttpStatusCode.Forbidden, "Access Denied: requires one of ${allowedRoles.toList()}")
        return false
    }
    return true
}

// Extension functions for mapping
fun MemberDocument.toSharedModel(): Member = Member(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    status = MemberStatus.valueOf(status),
    membershipPlan = membershipPlan,
    joinDate = joinDate,
    expiryDate = expiryDate
)

fun Member.toDocument(): MemberDocument = MemberDocument(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    status = status.name,
    membershipPlan = membershipPlan,
    joinDate = joinDate,
    expiryDate = expiryDate
)
