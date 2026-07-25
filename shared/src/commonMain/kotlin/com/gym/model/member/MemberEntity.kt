package com.gym.model.member

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room KMP Database Entity for storing member profiles in SQLite database.
 */
@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val status: String, // Stored as String name of MemberStatus
    val membershipPlan: String,
    val joinDate: String,
    val expiryDate: String
) {
    fun toDomain(): Member = Member(
        id = id,
        fullName = fullName,
        email = email,
        phone = phone,
        status = MemberStatus.entries.find { it.name == status } ?: MemberStatus.INACTIVE,
        membershipPlan = membershipPlan,
        joinDate = joinDate,
        expiryDate = expiryDate
    )

    companion object {
        fun fromDomain(member: Member): MemberEntity = MemberEntity(
            id = member.id,
            fullName = member.fullName,
            email = member.email,
            phone = member.phone,
            status = member.status.name,
            membershipPlan = member.membershipPlan,
            joinDate = member.joinDate,
            expiryDate = member.expiryDate
        )
    }
}
