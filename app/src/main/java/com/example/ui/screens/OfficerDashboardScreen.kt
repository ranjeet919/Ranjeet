package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiService
import com.example.data.database.ChatMessage
import com.example.data.database.Complaint
import com.example.data.database.User
import com.example.ui.CivicViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerDashboardScreen(
    viewModel: CivicViewModel,
    user: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val complaints by viewModel.allComplaints.collectAsState()

    // Filter complaints based on officer's department
    val departmentComplaints = remember(complaints, user.department) {
        if (user.role == "Admin") complaints else complaints.filter { it.department.equals(user.department, ignoreCase = true) }
    }

    val activeQueue = remember(departmentComplaints) {
        departmentComplaints.filter { it.status != "Resolved" }.sortedByDescending { it.priorityScore }
    }

    val resolvedQueue = remember(departmentComplaints) {
        departmentComplaints.filter { it.status == "Resolved" }
    }

    // AI smart insights state
    var aiInsights by remember { mutableStateOf<String?>(null) }
    var isGeneratingInsights by remember { mutableStateOf(false) }

    // Generate smart insights from active list
    val generateInsights = {
        if (activeQueue.isEmpty()) {
            aiInsights = "Operational Queue is clear! All department services are operating within normal parameters. Continue monitoring incoming reports."
        } else {
            isGeneratingInsights = true
            scope.launch {
                try {
                    val activeIssuesList = activeQueue.joinToString("\n") { c ->
                        "- ${c.title} (Category: ${c.category}, Severity: ${c.severity}, AI Priority Score: ${c.priorityScore}, Citizen Count: ${c.reportCount})"
                    }
                    val prompt = """
                        You are the Senior Smart City Operations Director. Analyze the following list of active complaints in my department (${user.department ?: "Municipal Support"}):
                        
                        $activeIssuesList
                        
                        Based on severity, safety risks, and citizen reporting counts, synthesize exactly 3 bulleted strategic recommendations (maximum 2 sentences per recommendation) for the field crew.
                        Focus heavily on critical traffic safety, public health hazards, or utility blackout priorities. Be direct, authoritative, and professional. 
                        Do not include introductory or concluding conversational text. Start directly with the bullet points.
                    """.trimIndent()
                    
                    val response = GeminiService.getAssistantResponse(emptyList(), prompt)
                    aiInsights = response
                } catch (e: Exception) {
                    aiInsights = "Operational Notice: Multiple high-priority potholes and street leaks reported. Dispatch crews sequentially starting with priority scores > 80. Coordinate street illumination with the electrical board immediately."
                } finally {
                    isGeneratingInsights = false
                }
            }
        }
    }

    // Auto-generate insights on start
    LaunchedEffect(activeQueue.size) {
        generateInsights()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OneUIBlueDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("O", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (user.role == "Admin") "Admin Dashboard" else "Officer Terminal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = OneUITextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OneUISurface,
                    titleContentColor = OneUITextPrimary
                )
            )
        },
        containerColor = OneUIBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile & Department Header card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = OneUISurface),
                    border = BorderStroke(1.dp, Color(0x0DFFFFFF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome back, ${user.name}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = OneUITextPrimary
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(OneUIBluePrimary.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = user.department ?: "Director of Operations",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OneUIBlueDark
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Command ID: #${1000 + user.id}",
                                    fontSize = 12.sp,
                                    color = OneUITextSecondary
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(OneUIBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = OneUIBlueDark)
                        }
                    }
                }
            }

            // Operations Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Assigned Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = OneUISurface),
                        border = BorderStroke(1.dp, Color(0x0DFFFFFF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Queue Load", fontSize = 11.sp, color = OneUITextSecondary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${activeQueue.size} active",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (activeQueue.isNotEmpty()) OneUIOrange else OneUITextPrimary
                            )
                        }
                    }

                    // Average Priority
                    val avgPriority = remember(activeQueue) {
                        if (activeQueue.isEmpty()) 0 else activeQueue.map { it.priorityScore }.average().toInt()
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = OneUISurface),
                        border = BorderStroke(1.dp, Color(0x0DFFFFFF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Avg Priority", fontSize = 11.sp, color = OneUITextSecondary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$avgPriority / 100",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = when {
                                    avgPriority >= 75 -> OneUIRed
                                    avgPriority >= 45 -> OneUIOrange
                                    else -> OneUIGreen
                                }
                            )
                        }
                    }

                    // Resolved Assigned Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = OneUISurface),
                        border = BorderStroke(1.dp, Color(0x0DFFFFFF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resolved", fontSize = 11.sp, color = OneUITextSecondary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${resolvedQueue.size} closed",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = OneUIGreen
                            )
                        }
                    }
                }
            }

            // Department load Heatmap (Canvas bar chart)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = OneUISurface),
                    border = BorderStroke(1.dp, Color(0x0DFFFFFF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Workload Distribution",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OneUITextPrimary
                        )
                        Text(
                            text = "Assigned active tickets by issue type",
                            fontSize = 12.sp,
                            color = OneUITextSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                        )

                        // Compute category load sizes
                        val categories = listOf("Pothole", "Street Light", "Water Leak", "Garbage Dump", "Open Drain", "Others")
                        val categoryCounts = remember(activeQueue) {
                            categories.map { cat ->
                                if (cat == "Others") {
                                    activeQueue.count { !categories.contains(it.category) }
                                } else {
                                    activeQueue.count { it.category == cat }
                                }
                            }
                        }
                        val maxCount = remember(categoryCounts) {
                            val maxVal = categoryCounts.maxOrNull() ?: 0
                            if (maxVal == 0) 1 else maxVal
                        }

                        // Bar Chart drawn with Custom Canvas!
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            val barCount = categories.size
                            val spacing = 16.dp.toPx()
                            val totalSpacing = spacing * (barCount - 1)
                            val barWidth = (w - totalSpacing) / barCount

                            for (i in 0 until barCount) {
                                val count = categoryCounts[i]
                                val barHeight = (count.toFloat() / maxCount.toFloat()) * (h - 24.dp.toPx())
                                val x = i * (barWidth + spacing)
                                val y = h - barHeight - 16.dp.toPx()

                                // Draw bar background guide track
                                drawRoundRect(
                                    color = Color(0xFFEDF2F7),
                                    topLeft = Offset(x, 0f),
                                    size = Size(barWidth, h - 16.dp.toPx()),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                               )

                                // Draw active bar content
                                if (count > 0) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(OneUIBluePrimary, OneUIBlueDark)
                                        ),
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                    )
                                }
                            }
                        }

                        // Label Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            categories.forEach { cat ->
                                Text(
                                    text = cat.take(4),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OneUITextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(36.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Gemini Smart Officer Insights
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = OneUIBlueLight.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, OneUIBluePrimary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OneUIBlueDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Smart Ward Recommendations",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OneUIBlueDark
                                )
                            }
                            
                            IconButton(onClick = { generateInsights() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = OneUIBlueDark, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isGeneratingInsights) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.6f)).shimmerEffect())
                                Box(modifier = Modifier.fillMaxWidth(0.85f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.6f)).shimmerEffect())
                                Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.6f)).shimmerEffect())
                            }
                        } else {
                            Text(
                                text = aiInsights ?: "Operational Insights loaded.",
                                fontSize = 13.sp,
                                color = OneUITextPrimary,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Priority Queue Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Department Priority Queue (${activeQueue.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OneUITextPrimary
                    )
                    Text(
                        text = "Actionable Tasks",
                        fontSize = 12.sp,
                        color = OneUIOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (activeQueue.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = OneUISurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = "Clear Queue",
                                tint = OneUIGreen,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("All Tickets Resolved!", fontWeight = FontWeight.Bold, color = OneUITextPrimary)
                            Text("Excellent work. Your department caseload is completely resolved.", fontSize = 11.sp, color = OneUITextSecondary, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                items(activeQueue, key = { it.id }) { complaint ->
                    OfficerComplaintCard(
                        complaint = complaint,
                        onResolve = {
                            viewModel.updateStatus(complaint.id, "Resolved")
                            Toast.makeText(context, "Ticket ID #${complaint.id} marked as Resolved! Live Civic Score updated.", Toast.LENGTH_SHORT).show()
                        },
                        onMarkProgress = {
                            viewModel.updateStatus(complaint.id, "Progress")
                            Toast.makeText(context, "Ticket ID #${complaint.id} status set to In Progress.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OfficerComplaintCard(
    complaint: Complaint,
    onResolve: () -> Unit,
    onMarkProgress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("officer_task_card_${complaint.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = OneUISurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(OneUIBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(complaint.category),
                            contentDescription = null,
                            tint = OneUIBluePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(complaint.category, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OneUITextPrimary)
                }

                // AI Priority badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(OneUIBluePrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Priority: ${complaint.priorityScore}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OneUIBlueDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body
            Text(complaint.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OneUITextPrimary)
            Text(complaint.summary, fontSize = 12.sp, color = OneUITextSecondary, modifier = Modifier.padding(top = 2.dp))

            Spacer(modifier = Modifier.height(12.dp))

            // Geo Coordinates display & Citizen upvote count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = OneUITextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Lat: ${String.format("%.4f", complaint.latitude)}, Lng: ${String.format("%.4f", complaint.longitude)}",
                        fontSize = 10.sp,
                        color = OneUITextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = OneUIBluePrimary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${complaint.reportCount} reports",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OneUIBlueDark
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEDF2F7))

            // Status Row & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current status indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ", fontSize = 11.sp, color = OneUITextSecondary)
                    val statusColor = when (complaint.status) {
                        "Progress" -> OneUIOrange
                        "Urgent" -> OneUIRed
                        else -> OneUIBluePrimary
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(complaint.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                }

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (complaint.status != "Progress") {
                        OutlinedButton(
                            onClick = onMarkProgress,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Mark Progress", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = onResolve,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OneUIGreen),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resolve", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// Simple Shimmer modifier extension for loading states
fun Modifier.shimmerEffect(): Modifier = this.drawBehind {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.5f),
                Color.White.copy(alpha = 0.15f)
            )
        )
    )
}
