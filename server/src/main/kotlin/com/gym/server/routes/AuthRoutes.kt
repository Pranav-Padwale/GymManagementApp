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
        
        // Find user in MongoDB (since @BsonId is used, the MongoDB field name is "_id")
        val user = MongoConfig.users.find(Filters.eq("_id", request.username)).firstOrNull()
        
        if (user != null && user.passwordHash == request.passwordHash) {
            // Generate JWT
            val token = JWT.create()
                .withAudience(JWT_AUDIENCE)
                .withIssuer(JWT_ISSUER)
                .withClaim("username", user.username)
                .withClaim("role", user.role)
                .withExpiresAt(Date(System.currentTimeMillis() + 86400000)) // 1 day
                .sign(Algorithm.HMAC256(JWT_SECRET))
                
            val role = UserRole.valueOf(user.role)
            call.respond(LoginResponse(token, role, user.username))
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
        }
    }
}
