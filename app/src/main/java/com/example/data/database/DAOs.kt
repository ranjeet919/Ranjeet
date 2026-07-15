package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
}

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints ORDER BY createdTimestamp DESC")
    fun getAllComplaintsFlow(): Flow<List<Complaint>>

    @Query("SELECT * FROM complaints ORDER BY createdTimestamp DESC")
    suspend fun getAllComplaints(): List<Complaint>

    @Query("SELECT * FROM complaints WHERE category = :category ORDER BY createdTimestamp DESC")
    fun getComplaintsByCategory(category: String): Flow<List<Complaint>>

    @Query("SELECT * FROM complaints WHERE id = :id LIMIT 1")
    suspend fun getComplaintById(id: Int): Complaint?

    @Query("SELECT * FROM complaints WHERE id = :id LIMIT 1")
    fun getComplaintByIdFlow(id: Int): Flow<Complaint?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: Complaint): Long

    @Update
    suspend fun updateComplaint(complaint: Complaint)

    @Delete
    suspend fun deleteComplaint(complaint: Complaint)

    @Query("DELETE FROM complaints")
    suspend fun clearAllComplaints()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()
}
