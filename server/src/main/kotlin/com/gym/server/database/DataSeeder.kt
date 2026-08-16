package com.gym.server.database

import com.gym.server.documents.MemberDocument
import com.gym.server.documents.TimeSlotDocument
import com.gym.server.documents.TransactionDocument
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

object DataSeeder {
    fun seedIfEmpty() = runBlocking {
        // Seed Users
        if (MongoConfig.users.find().firstOrNull() == null) {
            println("🌱 Seeding Users (RBAC)...")
            MongoConfig.users.insertMany(
                listOf(
                    com.gym.server.documents.UserDocument("admin", "admin123", "SUPER_ADMIN"),
                    com.gym.server.documents.UserDocument("trainer", "trainer123", "TRAINER"),
                    com.gym.server.documents.UserDocument("member", "member123", "MEMBER")
                )
            )
        }

        // Seed Members
        if (MongoConfig.members.find().firstOrNull() == null) {
            println("🌱 Seeding Members...")
            MongoConfig.members.insertMany(
                listOf(
                    MemberDocument("M-101", "Alex Rivers", "alex@example.com", "+1 555-0192", "ACTIVE", "Gold Annual", "2025-01-15", "2026-01-15"),
                    MemberDocument("M-102", "Sam Chen", "sam.c@example.com", "+1 555-8832", "INACTIVE", "Silver Monthly", "2023-11-01", "2024-02-01"),
                    MemberDocument("M-103", "Jordan Lee", "jordan@example.com", "+1 555-9921", "ACTIVE", "Platinum Elite", "2024-06-10", "2025-06-10")
                )
            )
        }

        // Seed Time Slots
        if (MongoConfig.timeSlots.find().firstOrNull() == null) {
            println("🌱 Seeding Time Slots...")
            MongoConfig.timeSlots.insertMany(
                listOf(
                    TimeSlotDocument("TS-001", "Morning Power Yoga", "Coach Arjun", "MONDAY", "06:00", "07:30", 25, 18, "YOGA", true),
                    TimeSlotDocument("TS-002", "HIIT Blast", "Coach Sarah", "MONDAY", "18:00", "19:00", 20, 20, "HIIT", true),
                    TimeSlotDocument("TS-003", "Zumba Cardio", "Coach Priya", "WEDNESDAY", "17:30", "18:30", 30, 15, "CARDIO", true)
                )
            )
        }

        // Cleanup old bad data that caused the 500 error
        MongoConfig.transactions.deleteMany(
            com.mongodb.client.model.Filters.`in`("category", listOf("EQUIPMENT", "SALARY"))
        )

        // Seed Transactions
        if (MongoConfig.transactions.find().firstOrNull() == null) {
            println("🌱 Seeding Transactions...")
            MongoConfig.transactions.insertMany(
                listOf(
                    TransactionDocument("TX-1", "Gold Membership - Alex", 4800.0, "MEMBERSHIP_FEE", "INCOME", "2026-07-15", 2026, 7),
                    TransactionDocument("TX-2", "Equipment Maintenance", 1250.0, "EQUIPMENT_MAINTENANCE", "EXPENSE", "2026-07-10", 2026, 7),
                    TransactionDocument("TX-3", "Staff Salary - June", 8500.0, "TRAINER_PAYROLL", "EXPENSE", "2026-07-01", 2026, 7)
                )
            )
        }
        
        println("✅ Database Seeding Verification Complete.")
    }
}
