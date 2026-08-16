package com.gym.server.documents

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimeSlotDocument(
    @SerialName("_id") val id: String,
    val title: String,
    val trainerName: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val maxCapacity: Int,
    val currentBookings: Int,
    val category: String,
    val isActive: Boolean
)
