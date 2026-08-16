package com.gym.server.routes

import com.gym.model.drawer.UserProfile
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.profileRoutes() {
    route("/api/profile") {
        get {
            // Hardcoded profile since there's no auth/users table for this yet
            call.respond(UserProfile(
                name = "Pranav Padwale",
                phone = "+91 8010129371",
                isDarkMode = true
            ))
        }
    }
}
