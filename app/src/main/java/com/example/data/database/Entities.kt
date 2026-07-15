package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val passwordHash: String,
    val role: String, // "Citizen", "Officer", "Admin"
    val name: String,
    val department: String? = null // e.g. "Public Works", "Electricity", "Water Board", "Traffic Police", "Municipal", "Police", "Transport"
)

@Entity(tableName = "complaints")
data class Complaint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val summary: String,
    val description: String,
    val category: String, // e.g., "Pothole", "Street Light", "Water Leak", "Garbage Dump", "Broken Sidewalk", "Traffic Signal", "Illegal Parking", "Public Nuisance", "Strayed Animal", "Fallen Tree", "Open Drain", "Power Outage"
    val latitude: Double,
    val longitude: Double,
    val severity: String, // "Low", "Medium", "High", "Urgent"
    val safetyRisk: String, // "Low", "Medium", "High"
    val priorityScore: Int, // 0 - 100
    val status: String, // "New", "Progress", "Resolved", "Urgent"
    val department: String, // e.g. "Public Works", "Electricity", "Water Board", "Traffic Police", "Municipal", "Police", "Transport"
    val imageUrl: String? = null,
    val reportCount: Int = 1,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val resolvedTimestamp: Long? = null,
    val reporterName: String,
    val reporterId: Int,
    val verifiedUserIds: String = "" // Comma-separated list of User IDs who verified this
) {
    fun getVerifiedIds(): List<Int> {
        if (verifiedUserIds.isEmpty()) return emptyList()
        return verifiedUserIds.split(",").mapNotNull { it.toIntOrNull() }
    }
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
