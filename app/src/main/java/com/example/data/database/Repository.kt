package com.example.data.database

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlin.math.*

class CivicRepository(
    private val userDao: UserDao,
    private val complaintDao: ComplaintDao,
    private val chatDao: ChatDao
) {
    private val TAG = "CivicRepository"

    val allComplaints: Flow<List<Complaint>> = complaintDao.getAllComplaintsFlow()
    val allMessages: Flow<List<ChatMessage>> = chatDao.getAllMessagesFlow()

    /**
     * Seeds initial demo accounts and mock complaints if the database is empty.
     */
    suspend fun seedDatabaseIfEmpty() {
        val users = userDao.getAllUsers()
        if (users.isEmpty()) {
            Log.i(TAG, "Database is empty. Seeding demo accounts and complaints...")

            // 1. Seed Users
            userDao.insertUser(User(email = "citizen@civic.com", passwordHash = "citizen123", role = "Citizen", name = "Ranjeet Kumar"))
            userDao.insertUser(User(email = "officer@civic.com", passwordHash = "officer123", role = "Officer", name = "Officer Dave", department = "Public Works"))
            userDao.insertUser(User(email = "admin@civic.com", passwordHash = "admin123", role = "Admin", name = "Admin Controller"))

            // 2. Seed Complaints
            // Let's base them around San Francisco center: lat = 37.7749, lng = -122.4194
            complaintDao.insertComplaint(
                Complaint(
                    title = "Deep Broadway Pothole",
                    summary = "Deep crater-like pothole blocking the left lane on Broadway Blvd.",
                    description = "A deep pothole has opened up in the left lane. It is very difficult to see at night and is causing vehicles to swerve suddenly, risking accidents.",
                    category = "Pothole",
                    latitude = 37.7849,
                    longitude = -122.4094,
                    severity = "High",
                    safetyRisk = "High",
                    priorityScore = 85,
                    status = "New",
                    department = "Public Works",
                    reportCount = 3,
                    reporterName = "Sarah Jenkins",
                    reporterId = 1,
                    verifiedUserIds = "1"
                )
            )

            complaintDao.insertComplaint(
                Complaint(
                    title = "Dark Corridor - Broken Street Lamps",
                    summary = "Three consecutive street lamps are completely unlit near Market St intersection.",
                    description = "Three streetlights are broken on the pedestrian walkway, making the area pitch black after sunset. Safety concern for residents walking home.",
                    category = "Street Light",
                    latitude = 37.7739,
                    longitude = -122.4312,
                    severity = "Medium",
                    safetyRisk = "Medium",
                    priorityScore = 60,
                    status = "Progress",
                    department = "Electricity",
                    reportCount = 5,
                    reporterName = "Michael Chen",
                    reporterId = 1,
                    verifiedUserIds = "1"
                )
            )

            complaintDao.insertComplaint(
                Complaint(
                    title = "Sewer Overflow Main St",
                    summary = "Open drainage sewer is overflowing heavily near the main grocery store.",
                    description = "A blocked storm drain is overflowing with sewage onto Main Street. Foul odor and health hazard for the entire neighborhood.",
                    category = "Open Drain",
                    latitude = 37.7649,
                    longitude = -122.4194,
                    severity = "Urgent",
                    safetyRisk = "High",
                    priorityScore = 95,
                    status = "Urgent",
                    department = "Water Board",
                    reportCount = 12,
                    reporterName = "Emma Watson",
                    reporterId = 1,
                    verifiedUserIds = "1"
                )
            )

            complaintDao.insertComplaint(
                Complaint(
                    title = "Garbage Pile Near Community Park",
                    summary = "Large illegal trash dump site accumulating next to the park entrance.",
                    description = "A large pile of plastic bags, furniture, and kitchen waste has been dumped illegally near the entrance of our neighborhood park.",
                    category = "Garbage Dump",
                    latitude = 37.7801,
                    longitude = -122.4201,
                    severity = "Medium",
                    safetyRisk = "Low",
                    priorityScore = 45,
                    status = "Resolved",
                    department = "Municipal",
                    reportCount = 2,
                    reporterName = "David Beckham",
                    reporterId = 1,
                    verifiedUserIds = "1",
                    resolvedTimestamp = System.currentTimeMillis() - 86400000
                )
            )
            Log.i(TAG, "Database seeded successfully.")
        }
    }

    // --- Authentication ---

    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    suspend fun getUserById(id: Int): User? = userDao.getUserById(id)
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)

    // --- Complaints & Duplicate Detection ---

    /**
     * Submit a new complaint. Implements geographic duplicate detection (150m same-category rule).
     * If duplicate is found, merges by updating reportCount, verifiedUserIds, and bumping priorityScore.
     * Returns a Pair: (ID of the complaint, Boolean indicating if it was a duplicate merged)
     */
    suspend fun submitComplaint(newComplaint: Complaint): Pair<Int, Boolean> {
        val activeComplaints = complaintDao.getAllComplaints().filter { it.status != "Resolved" }
        
        // Find if there is any active complaint of the same category within 150m
        val duplicate = activeComplaints.firstOrNull { existing ->
            existing.category.equals(newComplaint.category, ignoreCase = true) &&
            calculateDistanceInMeters(
                existing.latitude, existing.longitude,
                newComplaint.latitude, newComplaint.longitude
            ) <= 150.0
        }

        return if (duplicate != null) {
            // Merge duplicate
            val verifiedList = duplicate.getVerifiedIds().toMutableList()
            if (!verifiedList.contains(newComplaint.reporterId)) {
                verifiedList.add(newComplaint.reporterId)
            }
            
            val updatedVerifiedIds = verifiedList.joinToString(",")
            val newReportCount = duplicate.reportCount + 1
            
            // Bump priority score slightly due to community reports (up to 100)
            val bumpedPriority = min(100, duplicate.priorityScore + 4)
            
            val merged = duplicate.copy(
                reportCount = newReportCount,
                verifiedUserIds = updatedVerifiedIds,
                priorityScore = bumpedPriority
            )
            
            complaintDao.updateComplaint(merged)
            Log.i(TAG, "Merged new complaint ${newComplaint.title} into existing complaint ID ${duplicate.id} (Distance <= 150m)")
            Pair(duplicate.id, true)
        } else {
            // Insert new complaint
            val newId = complaintDao.insertComplaint(newComplaint).toInt()
            Log.i(TAG, "Inserted brand new complaint ${newComplaint.title} with ID $newId")
            Pair(newId, false)
        }
    }

    /**
     * Verify/Upvote a complaint. Bumps reporter count and priority score.
     */
    suspend fun verifyComplaint(complaintId: Int, userId: Int): Boolean {
        val complaint = complaintDao.getComplaintById(complaintId) ?: return false
        val verifiedList = complaint.getVerifiedIds().toMutableList()
        
        if (verifiedList.contains(userId)) {
            return false // Already verified by this user
        }
        
        verifiedList.add(userId)
        val updatedVerifiedIds = verifiedList.joinToString(",")
        val updatedReportCount = complaint.reportCount + 1
        val updatedPriority = min(100, complaint.priorityScore + 3)
        
        complaintDao.updateComplaint(
            complaint.copy(
                reportCount = updatedReportCount,
                verifiedUserIds = updatedVerifiedIds,
                priorityScore = updatedPriority
            )
        )
        return true
    }

    /**
     * Resolve/Update status of complaint (for Officer/Admin flows)
     */
    suspend fun updateComplaintStatus(complaintId: Int, newStatus: String, resolvedBy: String? = null) {
        val complaint = complaintDao.getComplaintById(complaintId) ?: return
        val resolvedTime = if (newStatus == "Resolved") System.currentTimeMillis() else null
        
        complaintDao.updateComplaint(
            complaint.copy(
                status = newStatus,
                resolvedTimestamp = resolvedTime
            )
        )
    }

    suspend fun getComplaintById(id: Int): Complaint? = complaintDao.getComplaintById(id)
    fun getComplaintByIdFlow(id: Int): Flow<Complaint?> = complaintDao.getComplaintByIdFlow(id)

    // --- Chat Assistants ---

    suspend fun insertChatMessage(message: ChatMessage) = chatDao.insertMessage(message)
    suspend fun clearChatHistory() = chatDao.clearAllMessages()

    // --- Distance Helper (Haversine formula) ---

    private fun calculateDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
