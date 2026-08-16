package com.gym.server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.routing
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.gym.server.routes.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        anyHost() // Allow all for development
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    // Connect to MongoDB and seed data
    com.gym.server.database.MongoConfig.init()
    com.gym.server.database.DataSeeder.seedIfEmpty()

    install(io.ktor.server.auth.Authentication) {
        jwt {
            verifier(
                com.auth0.jwt.JWT.require(com.auth0.jwt.algorithms.Algorithm.HMAC256(com.gym.server.routes.JWT_SECRET))
                    .withAudience(com.gym.server.routes.JWT_AUDIENCE)
                    .withIssuer(com.gym.server.routes.JWT_ISSUER)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    io.ktor.server.auth.jwt.JWTPrincipal(credential.payload)
                } else null
            }
        }
    }

    environment.monitor.subscribe(io.ktor.server.application.ApplicationStopped) {
        com.gym.server.database.MongoConfig.close()
    }

    routing {
        authRoutes()
        memberRoutes()
        timeSlotRoutes()
        financeRoutes()
        profileRoutes()
    }
}
