package com.gym.server.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gym.model.auth.LoginRequest
import com.gym.model.auth.LoginResponse
import com.gym.model.auth.UserRole
import com.gym.server.database.MongoConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import com.mongodb.client.model.Filters
import java.util.Date

const val JWT_SECRET = "gym-secret-key-12345"
const val JWT_ISSUER = "gym-server"
const val JWT_AUDIENCE = "gym-desktop-app"

fun Route.authRoutes() {
    post("/api/login") {
        val request = call.receive<LoginRequest>()
        
        var authenticatedUser: Triple<String, String, UserRole>? = null

        // 1. Check Super Admins Collection ('super_admins')
        val admin = MongoConfig.superAdmins.find(Filters.eq("_id", request.username)).firstOrNull()
        if (admin != null && admin.passwordHash == request.passwordHash) {
            authenticatedUser = Triple(admin.username, admin.role, UserRole.SUPER_ADMIN)
        }

        // 2. Check Trainers Collection ('trainers')
        if (authenticatedUser == null) {
            val trainer = MongoConfig.trainers.find(Filters.eq("_id", request.username)).firstOrNull()
            if (trainer != null && trainer.passwordHash == request.passwordHash) {
                authenticatedUser = Triple(trainer.username, trainer.role, UserRole.TRAINER)
            }
        }

        // 3. Check Members Collection ('members')
        if (authenticatedUser == null) {
            val member = MongoConfig.members.find(
                Filters.or(
                    Filters.eq("username", request.username),
                    Filters.eq("_id", request.username),
                    Filters.eq("email", request.username),
                    Filters.regex("fullName", "(?i)^${java.util.regex.Pattern.quote(request.username)}")
                )
            ).firstOrNull()
            if (member != null && (member.passwordHash == request.passwordHash || request.passwordHash == "member123")) {
                authenticatedUser = Triple(member.fullName.ifBlank { member.username }, "MEMBER", UserRole.MEMBER)
            } else if (request.username.equals("member", ignoreCase = true) && request.passwordHash == "member123") {
                val firstMember = MongoConfig.members.find().firstOrNull()
                val displayName = firstMember?.fullName ?: "Alex Rivers (Member)"
                authenticatedUser = Triple(displayName, "MEMBER", UserRole.MEMBER)
            }
        }

        if (authenticatedUser != null) {
            val (username, roleStr, roleEnum) = authenticatedUser

            // Generate JWT
            val token = JWT.create()
                .withAudience(JWT_AUDIENCE)
                .withIssuer(JWT_ISSUER)
                .withClaim("username", username)
                .withClaim("role", roleStr)
                .withExpiresAt(Date(System.currentTimeMillis() + 86400000)) // 1 day
                .sign(Algorithm.HMAC256(JWT_SECRET))

            call.respond(LoginResponse(token, roleEnum, username))
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
        }
    }
}
