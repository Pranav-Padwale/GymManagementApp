package com.gym.server.documents

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceDocument(
    @SerialName("_id") val id: String,
    val slotId: String,
    val memberId: String,
    val memberName: String,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val status: String
)
