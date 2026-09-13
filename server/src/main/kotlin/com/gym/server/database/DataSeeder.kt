package com.gym.server.database

import com.gym.server.documents.AdminDocument
import com.gym.server.documents.MemberDocument
import com.gym.server.documents.TimeSlotDocument
import com.gym.server.documents.TrainerDocument
import com.gym.server.documents.TransactionDocument
import com.gym.server.security.PasswordHasher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

object DataSeeder {
    fun seedIfEmpty() = runBlocking {
        // 1. Seed Super Admins Collection
        if (MongoConfig.superAdmins.find().firstOrNull() == null) {
            println("🌱 Seeding Super Admins Collection ('super_admins')...")
            MongoConfig.superAdmins.insertMany(
                listOf(
                    AdminDocument(
                        username = "admin",
                        passwordHash = PasswordHasher.hash("admin123"),
                        fullName = "Super Administrator",
                        email = "admin@forgegym.com",
                        role = "SUPER_ADMIN"
                    )
                )
            )
        }

        // 2. Seed Trainers Collection
        if (MongoConfig.trainers.find().firstOrNull() == null) {
            println("🌱 Seeding Trainers Collection ('trainers')...")
            MongoConfig.trainers.insertMany(
                listOf(
                    TrainerDocument(
                        username = "trainer",
                        passwordHash = PasswordHasher.hash("trainer123"),
                        fullName = "Coach Sarah Jenkins",
                        email = "sarah.j@forgegym.com",
                        phone = "+1 555-0144",
                        specialization = "HIIT & Functional Training",
                        role = "TRAINER"
                    ),
                    TrainerDocument(
                        username = "arjun",
                        passwordHash = PasswordHasher.hash("arjun123"),
                        fullName = "Coach Arjun Mehta",
                        email = "arjun.m@forgegym.com",
                        phone = "+1 555-0188",
                        specialization = "Power Yoga & Flexibility",
                        role = "TRAINER"
                    ),
                    TrainerDocument(
                        username = "marcus",
                        passwordHash = PasswordHasher.hash("marcus123"),
                        fullName = "Coach Marcus Chen",
                        email = "marcus.c@forgegym.com",
                        phone = "+1 555-0199",
                        specialization = "Strength & Kettlebell",
                        role = "TRAINER"
                    )
                )
            )
        }

        // 3. Seed Members Collection
        if (MongoConfig.members.find().firstOrNull() == null) {
            println("🌱 Seeding Members Collection ('members')...")
            MongoConfig.members.insertMany(
                listOf(
                    MemberDocument(
                        id = "M-101",
                        username = "member",
                        passwordHash = PasswordHasher.hash("member123"),
                        fullName = "Alex Rivers",
                        email = "alex@example.com",
                        phone = "+1 555-0192",
                        status = "ACTIVE",
                        membershipPlan = "Gold Annual",
                        joinDate = "2025-01-15",
                        expiryDate = "2026-01-15",
                        role = "MEMBER"
                    ),
                    MemberDocument(
                        id = "M-102",
                        username = "sam",
                        passwordHash = PasswordHasher.hash("sam123"),
                        fullName = "Sam Chen",
                        email = "sam.c@example.com",
                        phone = "+1 555-8832",
                        status = "INACTIVE",
                        membershipPlan = "Silver Monthly",
                        joinDate = "2023-11-01",
                        expiryDate = "2024-02-01",
                        role = "MEMBER"
                    ),
                    MemberDocument(
                        id = "M-103",
                        username = "jordan",
                        passwordHash = PasswordHasher.hash("jordan123"),
                        fullName = "Jordan Lee",
                        email = "jordan@example.com",
                        phone = "+1 555-9921",
                        status = "ACTIVE",
                        membershipPlan = "Platinum Elite",
                        joinDate = "2024-06-10",
                        expiryDate = "2025-06-10",
                        role = "MEMBER"
                    )
                )
            )
        }

        // 4. Seed Time Slots Collection
        if (MongoConfig.timeSlots.find().firstOrNull() == null) {
            println("🌱 Seeding Time Slots Collection ('time_slots')...")
            MongoConfig.timeSlots.insertMany(
                listOf(
                    TimeSlotDocument("TS-001", "Morning Power Yoga", "Coach Arjun", "MONDAY", "06:00", "07:30", 25, 18, "YOGA", true),
                    TimeSlotDocument("TS-002", "HIIT Extreme", "Coach Sarah", "MONDAY", "18:00", "19:00", 20, 20, "HIIT", true),
                    TimeSlotDocument("TS-003", "Kettlebell Core", "Coach Marcus", "WEDNESDAY", "17:30", "18:30", 30, 15, "CARDIO", true)
                )
            )
        }

        // Cleanup old bad data that caused the 500 error
        MongoConfig.transactions.deleteMany(
            com.mongodb.client.model.Filters.`in`("category", listOf("EQUIPMENT", "SALARY"))
        )

        // 5. Seed Transactions Collection
        if (MongoConfig.transactions.find().firstOrNull() == null) {
            println("🌱 Seeding Transactions Collection ('transactions')...")
            MongoConfig.transactions.insertMany(
                listOf(
                    TransactionDocument("TX-1", "Gold Membership - Alex", 4800.0, "MEMBERSHIP_FEE", "INCOME", "2026-07-15", 2026, 7),
                    TransactionDocument("TX-2", "Equipment Maintenance", 1250.0, "EQUIPMENT_MAINTENANCE", "EXPENSE", "2026-07-10", 2026, 7),
                    TransactionDocument("TX-3", "Staff Salary - June", 8500.0, "TRAINER_PAYROLL", "EXPENSE", "2026-07-01", 2026, 7)
                )
            )
        }
        
        migrateLegacyPlaintextPasswords()

        println("✅ Multi-Collection Database Seeding Verification Complete.")
    }

    /**
     * One-time-per-startup migration for accounts created before AUTH-2's bcrypt hashing was
     * introduced: any passwordHash value that isn't already a valid bcrypt hash is a leftover
     * plaintext value (from before this fix, or from a pre-existing live database) and gets
     * rehashed in place. Safe to run on every startup - already-hashed accounts are untouched.
     */
    private suspend fun migrateLegacyPlaintextPasswords() {
        val legacyAdmins = MongoConfig.superAdmins.find().toList()
            .filter { !PasswordHasher.isBcryptHash(it.passwordHash) }
        if (legacyAdmins.isNotEmpty()) {
            println("🔐 Migrating ${legacyAdmins.size} legacy plaintext admin password(s) to bcrypt...")
            legacyAdmins.forEach { admin ->
                MongoConfig.superAdmins.replaceOne(
                    com.mongodb.client.model.Filters.eq("_id", admin.username),
                    admin.copy(passwordHash = PasswordHasher.hash(admin.passwordHash))
                )
            }
        }

        val legacyTrainers = MongoConfig.trainers.find().toList()
            .filter { !PasswordHasher.isBcryptHash(it.passwordHash) }
        if (legacyTrainers.isNotEmpty()) {
            println("🔐 Migrating ${legacyTrainers.size} legacy plaintext trainer password(s) to bcrypt...")
            legacyTrainers.forEach { trainer ->
                MongoConfig.trainers.replaceOne(
                    com.mongodb.client.model.Filters.eq("_id", trainer.username),
                    trainer.copy(passwordHash = PasswordHasher.hash(trainer.passwordHash))
                )
            }
        }

        val legacyMembers = MongoConfig.members.find().toList()
            .filter { !PasswordHasher.isBcryptHash(it.passwordHash) }
        if (legacyMembers.isNotEmpty()) {
            println("🔐 Migrating ${legacyMembers.size} legacy plaintext member password(s) to bcrypt...")
            legacyMembers.forEach { member ->
                MongoConfig.members.replaceOne(
                    com.mongodb.client.model.Filters.eq("_id", member.id),
                    member.copy(passwordHash = PasswordHasher.hash(member.passwordHash))
                )
            }
        }
    }
}
