package com.gym.server.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gym.model.auth.LoginRequest
import com.gym.model.auth.LoginResponse
import com.gym.server.database.MongoConfig
import com.gym.server.security.matchCredentials
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

        val admin = MongoConfig.superAdmins.find(Filters.eq("_id", request.username)).firstOrNull()
        val trainer = MongoConfig.trainers.find(Filters.eq("_id", request.username)).firstOrNull()
        val member = MongoConfig.members.find(
            Filters.or(
                Filters.eq("username", request.username),
                Filters.eq("_id", request.username),
                Filters.eq("email", request.username),
                Filters.regex("fullName", "(?i)^${java.util.regex.Pattern.quote(request.username)}")
            )
        ).firstOrNull()

        val authenticatedUser = matchCredentials(request, admin, trainer, member)

        if (authenticatedUser != null) {
            // Generate JWT
            val token = JWT.create()
                .withAudience(JWT_AUDIENCE)
                .withIssuer(JWT_ISSUER)
                .withClaim("username", authenticatedUser.displayName)
                .withClaim("role", authenticatedUser.roleStr)
                .withExpiresAt(Date(System.currentTimeMillis() + 86400000)) // 1 day
                .sign(Algorithm.HMAC256(JWT_SECRET))

            call.respond(LoginResponse(token, authenticatedUser.role, authenticatedUser.displayName))
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
        }
    }
}
