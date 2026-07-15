package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ClassificationResult
import com.example.data.api.GeminiService
import com.example.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

class CivicViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "CivicViewModel"

    private val database = AppDatabase.getDatabase(application)
    private val repository = CivicRepository(
        userDao = database.userDao(),
        complaintDao = database.complaintDao(),
        chatDao = database.chatDao()
    )

    // --- Flows ---
    val allComplaints: StateFlow<List<Complaint>> = repository.allComplaints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAnalyzingReport = MutableStateFlow(false)
    val isAnalyzingReport: StateFlow<Boolean> = _isAnalyzingReport.asStateFlow()

    private val _isGeneratingResponse = MutableStateFlow(false)
    val isGeneratingResponse: StateFlow<Boolean> = _isGeneratingResponse.asStateFlow()

    private val _lastResultOfAnalysis = MutableStateFlow<ClassificationResult?>(null)
    val lastResultOfAnalysis: StateFlow<ClassificationResult?> = _lastResultOfAnalysis.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _submitStatus = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitStatus: StateFlow<SubmitState> = _submitStatus.asStateFlow()

    // --- Dynamic Civic Health Score live calculation ---
    val civicHealthScore: StateFlow<Int> = allComplaints.map { list ->
        val activeList = list.filter { it.status != "Resolved" }
        if (activeList.isEmpty()) {
            100
        } else {
            var score = 100
            activeList.forEach { c ->
                score -= when (c.severity) {
                    "Urgent" -> 7
                    "High" -> 4
                    "Medium" -> 2
                    "Low" -> 1
                    else -> 2
                }
            }
            max(15, min(100, score))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 82)

    init {
        viewModelScope.launch {
            // Seed database immediately on first initialization
            withContext(Dispatchers.IO) {
                repository.seedDatabaseIfEmpty()
            }
        }
    }

    // --- Authentication ---

    fun login(email: String, passwordHash: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _loginError.value = null
            val user = repository.getUserByEmail(email)
            if (user != null && user.passwordHash == passwordHash) {
                _currentUser.value = user
                withContext(Dispatchers.Main) {
                    onSuccess(user)
                }
            } else {
                _loginError.value = "Invalid email or password"
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _lastResultOfAnalysis.value = null
        _submitStatus.value = SubmitState.Idle
    }

    // --- AI Complaint Reporting Flow ---

    fun analyzeDescription(description: String) {
        if (description.isEmpty()) return
        viewModelScope.launch {
            _isAnalyzingReport.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    GeminiService.classifyComplaint(description)
                }
                _lastResultOfAnalysis.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error during analysis: ${e.message}")
            } finally {
                _isAnalyzingReport.value = false
            }
        }
    }

    fun clearAnalysis() {
        _lastResultOfAnalysis.value = null
        _submitStatus.value = SubmitState.Idle
    }

    fun submitReport(
        title: String,
        summary: String,
        description: String,
        category: String,
        severity: String,
        safetyRisk: String,
        priorityScore: Int,
        department: String,
        latitude: Double,
        longitude: Double,
        imageUrl: String? = null
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _submitStatus.value = SubmitState.Loading
            
            val complaint = Complaint(
                title = title,
                summary = summary,
                description = description,
                category = category,
                latitude = latitude,
                longitude = longitude,
                severity = severity,
                safetyRisk = safetyRisk,
                priorityScore = priorityScore,
                status = if (severity == "Urgent") "Urgent" else "New",
                department = department,
                imageUrl = imageUrl,
                reportCount = 1,
                reporterName = user.name,
                reporterId = user.id,
                verifiedUserIds = user.id.toString()
            )

            try {
                val (complaintId, isMerged) = withContext(Dispatchers.IO) {
                    repository.submitComplaint(complaint)
                }
                _submitStatus.value = SubmitState.Success(isMerged)
                _lastResultOfAnalysis.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting complaint: ${e.message}")
                _submitStatus.value = SubmitState.Error("Failed to submit. Please try again.")
            }
        }
    }

    // --- Upvotes / Verifications ---

    fun verifyComplaint(complaintId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.verifyComplaint(complaintId, user.id)
        }
    }

    // --- Officer Controls ---

    fun updateStatus(complaintId: Int, newStatus: String) {
        val user = _currentUser.value ?: return
        if (user.role != "Officer" && user.role != "Admin") return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateComplaintStatus(complaintId, newStatus)
        }
    }

    // --- AI Civic Assistant Chat ---

    fun sendMessageToAssistant(content: String) {
        if (content.trim().isEmpty()) return
        
        viewModelScope.launch {
            // Save user message
            val userMsg = ChatMessage(role = "user", content = content)
            withContext(Dispatchers.IO) {
                repository.insertChatMessage(userMsg)
            }
            
            _isGeneratingResponse.value = true
            
            try {
                val history = allMessages.value
                val aiReplyText = withContext(Dispatchers.IO) {
                    GeminiService.getAssistantResponse(history, content)
                }
                
                val aiMsg = ChatMessage(role = "model", content = aiReplyText)
                withContext(Dispatchers.IO) {
                    repository.insertChatMessage(aiMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending chat: ${e.message}")
            } finally {
                _isGeneratingResponse.value = false
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChatHistory()
        }
    }
}

sealed interface SubmitState {
    object Idle : SubmitState
    object Loading : SubmitState
    data class Success(val isMerged: Boolean) : SubmitState
    data class Error(val message: String) : SubmitState
}
