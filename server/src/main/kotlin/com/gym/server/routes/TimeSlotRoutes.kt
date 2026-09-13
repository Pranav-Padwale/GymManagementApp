package com.gym.server.routes

import com.gym.model.timeslot.DayOfWeek
import com.gym.model.timeslot.SlotCategory
import com.gym.model.timeslot.TimeSlot
import com.gym.server.database.MongoConfig
import com.gym.server.documents.TimeSlotDocument
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList

fun Route.timeSlotRoutes() {
    route("/api/timeslots") {
        get {
            val docs = MongoConfig.timeSlots.find().toList()
            val slots = docs.map { it.toSharedModel() }
            call.respond(slots)
        }
    }

    authenticate {
        route("/api/timeslots") {
            post {
                if (!call.requireRole("SUPER_ADMIN", "TRAINER")) return@post

                val slot = call.receive<TimeSlot>()
                MongoConfig.timeSlots.insertOne(slot.toDocument())
                call.respond(HttpStatusCode.Created, slot)
            }
        }
    }
}

// Extensions
fun TimeSlotDocument.toSharedModel() = TimeSlot(
    id = id,
    title = title,
    trainerName = trainerName,
    dayOfWeek = DayOfWeek.valueOf(dayOfWeek),
    startTime = startTime,
    endTime = endTime,
    maxCapacity = maxCapacity,
    currentBookings = currentBookings,
    category = SlotCategory.valueOf(category),
    isActive = isActive
)

fun TimeSlot.toDocument() = TimeSlotDocument(
    id = id,
    title = title,
    trainerName = trainerName,
    dayOfWeek = dayOfWeek.name,
    startTime = startTime,
    endTime = endTime,
    maxCapacity = maxCapacity,
    currentBookings = currentBookings,
    category = category.name,
    isActive = isActive
)
