package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.database.ChatMessage
import com.example.data.database.Complaint
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- API Models ---

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class ResponseFormatText(
    val mimeType: String
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    val text: ResponseFormatText? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseFormat: ResponseFormat? = null,
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class PartResponse(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class ContentResponse(
    val parts: List<PartResponse>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: ContentResponse? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class ClassificationResult(
    val title: String,
    val summary: String,
    val category: String,
    val severity: String,
    val safetyRisk: String,
    val priorityScore: Int,
    val department: String
)

// --- Retrofit Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Checks if the Gemini API key is available.
     */
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrEmpty() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Parse a description into structured fields using Gemini.
     */
    suspend fun classifyComplaint(description: String): ClassificationResult {
        if (!isApiKeyConfigured()) {
            Log.w(TAG, "Gemini API key is not configured, using mock classification.")
            return generateMockClassification(description)
        }

        val prompt = """
            You are a smart city Civic Analyst. Categorize the following civic complaint into one of these 12 categories:
            Pothole, Street Light, Water Leak, Garbage Dump, Broken Sidewalk, Traffic Signal, Illegal Parking, Public Nuisance, Strayed Animal, Fallen Tree, Open Drain, Power Outage.
            
            Determine the target department:
            - Public Works (for Pothole, Broken Sidewalk, Fallen Tree)
            - Electricity (for Street Light, Power Outage)
            - Water Board (for Water Leak, Open Drain)
            - Traffic Police (for Traffic Signal, Illegal Parking)
            - Municipal (for Garbage Dump, Public Nuisance)
            - Police (for safety or security complaints)
            - Transport (for public transit/bus issues)
            
            Assess severity (Low, Medium, High, Urgent), safetyRisk (Low, Medium, High), and a priorityScore (0-100) based on severity, safety, and urgency.
            
            Provide a concise, catchy Title and a 1-sentence Summary.
            
            Complaint: "$description"
            
            Response MUST be in strict JSON format matching this exact schema:
            {
              "title": "Catchy short title (max 5 words)",
              "summary": "1-sentence summary of the problem",
              "category": "Exactly one of the 12 listed categories",
              "severity": "Low" or "Medium" or "High" or "Urgent",
              "safetyRisk": "Low" or "Medium" or "High",
              "priorityScore": 0-100 integer,
              "department": "Exactly one of the 7 listed departments"
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(listOf(Part(prompt)))),
            generationConfig = GenerationConfig(
                responseFormat = ResponseFormat(ResponseFormatText("application/json")),
                temperature = 0.2f
            )
        )

        return try {
            val response = api.generateContent(BuildConfig.GEMINI_API_KEY, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrEmpty()) {
                val adapter = moshi.adapter(ClassificationResult::class.java)
                adapter.fromJson(jsonText) ?: generateMockClassification(description)
            } else {
                generateMockClassification(description)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API for classification: ${e.message}", e)
            generateMockClassification(description)
        }
    }

    /**
     * Get chatbot responses using chat history.
     */
    suspend fun getAssistantResponse(history: List<ChatMessage>, newMessage: String): String {
        if (!isApiKeyConfigured()) {
            return generateMockAssistantResponse(newMessage)
        }

        // Construct contents array with role mappings
        val contents = mutableListOf<Content>()
        
        // Add chat history
        history.forEach { msg ->
            val role = if (msg.role == "user") "user" else "model"
            contents.add(Content(listOf(Part(msg.content))))
        }
        
        // Add current user prompt
        contents.add(Content(listOf(Part(newMessage))))

        val systemInstruction = """
            You are 'Civic AI', the official intelligent civic assistant for the smart city platform.
            Your role is to help citizens report complaints, check municipal policies, explain the routing system, 
            give updates on local civic health, or guide officers on resolving reports.
            Be extremely professional, encouraging, and clear. Avoid verbose replies. Limit replies to max 3 sentences where possible.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = contents,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(listOf(Part(systemInstruction)))
        )

        return try {
            val response = api.generateContent(BuildConfig.GEMINI_API_KEY, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I am experiencing difficulty connecting at the moment. Please try again shortly."
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API for assistant: ${e.message}", e)
            generateMockAssistantResponse(newMessage)
        }
    }

    // --- Elegant Mock Fallbacks ---

    private fun generateMockClassification(description: String): ClassificationResult {
        val text = description.lowercase()
        return when {
            text.contains("pothole") || text.contains("road") || text.contains("hole") -> {
                ClassificationResult(
                    title = "Severe Pothole on Main Road",
                    summary = "Deep pothole blocking lane causing traffic slowdown and safety risk.",
                    category = "Pothole",
                    severity = "High",
                    safetyRisk = "High",
                    priorityScore = 85,
                    department = "Public Works"
                )
            }
            text.contains("light") || text.contains("dark") || text.contains("lamp") -> {
                ClassificationResult(
                    title = "Broken Street Light",
                    summary = "Street light is completely blacked out, rendering the walkway dark.",
                    category = "Street Light",
                    severity = "Medium",
                    safetyRisk = "Medium",
                    priorityScore = 60,
                    department = "Electricity"
                )
            }
            text.contains("leak") || text.contains("water") || text.contains("pipe") -> {
                ClassificationResult(
                    title = "Major Water Pipe Leak",
                    summary = "Water leaking heavily from main pipeline onto the pedestrian path.",
                    category = "Water Leak",
                    severity = "High",
                    safetyRisk = "Low",
                    priorityScore = 75,
                    department = "Water Board"
                )
            }
            text.contains("garbage") || text.contains("trash") || text.contains("waste") -> {
                ClassificationResult(
                    title = "Overflowing Garbage Dumpster",
                    summary = "Uncollected garbage piling up in public space creating public hazard.",
                    category = "Garbage Dump",
                    severity = "Medium",
                    safetyRisk = "Low",
                    priorityScore = 55,
                    department = "Municipal"
                )
            }
            text.contains("parking") || text.contains("car") || text.contains("vehicle") -> {
                ClassificationResult(
                    title = "Illegal Sidewalk Parking",
                    summary = "Car parked directly blocking stroller/wheelchair ramp access.",
                    category = "Illegal Parking",
                    severity = "Low",
                    safetyRisk = "Medium",
                    priorityScore = 40,
                    department = "Traffic Police"
                )
            }
            else -> {
                // Generic parser based on text indicators
                val isUrgent = text.contains("urgent") || text.contains("emergency") || text.contains("danger")
                ClassificationResult(
                    title = "Report: ${description.take(20)}...",
                    summary = "Civic complaint reported in municipal ward.",
                    category = "Public Nuisance",
                    severity = if (isUrgent) "Urgent" else "Medium",
                    safetyRisk = if (isUrgent) "High" else "Medium",
                    priorityScore = if (isUrgent) 90 else 50,
                    department = "Municipal"
                )
            }
        }
    }

    private fun generateMockAssistantResponse(message: String): String {
        val query = message.lowercase()
        return when {
            query.contains("pothole") -> "Reporting a pothole is easy! Just go to the 'Report' tab, input a description (e.g., 'deep pothole on Oak Street'), and submit. Our AI will auto-categorize it, route it to Public Works, and assign a priority score."
            query.contains("civic health") || query.contains("score") -> "The Civic Health Score is calculated live. It represents the ratio of resolved-to-active civic complaints, weighted by severity. Bumping resolving rates increases your local score!"
            query.contains("officer") -> "As an Officer, you can use the Officer Dashboard to view your department's load, monitor priority queues, and change complaint statuses from 'New' to 'Progress' or 'Resolved'."
            query.contains("hello") || query.contains("hi") -> "Hello! I am Civic AI, your smart city assistant. How can I help you today? You can ask me how to report complaints, inquire about civic health, or get guidance."
            else -> "Thank you for reaching out to Civic AI. I'm here to help you navigate our Smart City. If you want to report an issue, please head over to the Report page. For urgent matters, you can raise an alert to trigger emergency routing!"
        }
    }
}
