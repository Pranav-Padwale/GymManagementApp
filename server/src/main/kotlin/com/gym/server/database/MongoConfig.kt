package com.gym.server.database

import com.gym.server.documents.AdminDocument
import com.gym.server.documents.AttendanceDocument
import com.gym.server.documents.MemberDocument
import com.gym.server.documents.TimeSlotDocument
import com.gym.server.documents.TrainerDocument
import com.gym.server.documents.TransactionDocument
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase

object MongoConfig {
    private lateinit var client: MongoClient
    private lateinit var database: MongoDatabase

    // Exposed Role-Specific Collections
    lateinit var superAdmins: MongoCollection<AdminDocument>
    lateinit var trainers: MongoCollection<TrainerDocument>
    lateinit var members: MongoCollection<MemberDocument>

    // Operational Collections
    lateinit var timeSlots: MongoCollection<TimeSlotDocument>
    lateinit var attendanceRecords: MongoCollection<AttendanceDocument>
    lateinit var transactions: MongoCollection<TransactionDocument>

    fun init() {
        val uri = "mongodb+srv://pranavpadwale92_db_user:Pranav10@cluster0.vnrbdf3.mongodb.net/?appName=Cluster0"
        
        client = MongoClient.create(uri)
        database = client.getDatabase("gym_db")

        // Role-Specific Collections
        superAdmins = database.getCollection("super_admins")
        trainers = database.getCollection("trainers")
        members = database.getCollection("members")

        // Operational Collections
        timeSlots = database.getCollection("time_slots")
        attendanceRecords = database.getCollection("attendance_records")
        transactions = database.getCollection("transactions")

        println("✅ Connected to MongoDB at database: gym_db with dedicated role collections")
    }

    fun close() {
        if (this::client.isInitialized) {
            client.close()
            println("🛑 MongoDB connection closed.")
        }
    }
}
