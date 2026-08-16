package com.gym.server.database

import com.gym.server.documents.AttendanceDocument
import com.gym.server.documents.MemberDocument
import com.gym.server.documents.TimeSlotDocument
import com.gym.server.documents.TransactionDocument
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase

object MongoConfig {
    private lateinit var client: MongoClient
    private lateinit var database: MongoDatabase

    // Exposed Collections
    lateinit var members: MongoCollection<MemberDocument>
    lateinit var timeSlots: MongoCollection<TimeSlotDocument>
    lateinit var attendanceRecords: MongoCollection<AttendanceDocument>
    lateinit var transactions: MongoCollection<TransactionDocument>
    lateinit var users: MongoCollection<com.gym.server.documents.UserDocument>

    fun init() {
        // Read connection string from environment, fallback to localhost for quick local testing if missing
        val uri = "mongodb+srv://pranavpadwale92_db_user:Pranav10@cluster0.vnrbdf3.mongodb.net/?appName=Cluster0"
        
        client = MongoClient.create(uri)
        database = client.getDatabase("gym_db")

        members = database.getCollection("members")
        timeSlots = database.getCollection("time_slots")
        attendanceRecords = database.getCollection("attendance_records")
        transactions = database.getCollection("transactions")
        users = database.getCollection("users")

        println("✅ Connected to MongoDB at database: gym_db")
    }

    fun close() {
        if (this::client.isInitialized) {
            client.close()
            println("🛑 MongoDB connection closed.")
        }
    }
}
