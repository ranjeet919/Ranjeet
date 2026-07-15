package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ChatMessage
import com.example.data.database.Complaint
import com.example.data.database.User
import com.example.ui.CivicViewModel
import com.example.ui.SubmitState
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class CitizenTab {
    Home, Feed, Map, Report, Assistant
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenDashboardScreen(
    viewModel: CivicViewModel,
    user: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(CitizenTab.Home) }

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
                                .background(OneUIBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("C", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (activeTab) {
                                CitizenTab.Home -> "Civic Portal"
                                CitizenTab.Feed -> "AI Priority Feed"
                                CitizenTab.Map -> "Interactive City Map"
                                CitizenTab.Report -> "AI Smart Report"
                                CitizenTab.Assistant -> "Civic AI Assistant"
                            },
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
        bottomBar = {
            NavigationBar(
                containerColor = OneUISurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == CitizenTab.Home,
                    onClick = { activeTab = CitizenTab.Home },
                    icon = { Icon(if (activeTab == CitizenTab.Home) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OneUIBluePrimary,
                        selectedTextColor = OneUIBluePrimary,
                        indicatorColor = OneUIBlueLight
                    ),
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = activeTab == CitizenTab.Feed,
                    onClick = { activeTab = CitizenTab.Feed },
                    icon = { Icon(if (activeTab == CitizenTab.Feed) Icons.Filled.Warning else Icons.Outlined.Warning, contentDescription = "Feed") },
                    label = { Text("Feed", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OneUIBluePrimary,
                        selectedTextColor = OneUIBluePrimary,
                        indicatorColor = OneUIBlueLight
                    ),
                    modifier = Modifier.testTag("nav_feed")
                )
                NavigationBarItem(
                    selected = activeTab == CitizenTab.Map,
                    onClick = { activeTab = CitizenTab.Map },
                    icon = { Icon(if (activeTab == CitizenTab.Map) Icons.Filled.Place else Icons.Outlined.Place, contentDescription = "Map") },
                    label = { Text("Map", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OneUIBluePrimary,
                        selectedTextColor = OneUIBluePrimary,
                        indicatorColor = OneUIBlueLight
                    ),
                    modifier = Modifier.testTag("nav_map")
                )
                NavigationBarItem(
                    selected = activeTab == CitizenTab.Report,
                    onClick = { activeTab = CitizenTab.Report },
                    icon = { Icon(if (activeTab == CitizenTab.Report) Icons.Filled.AddAPhoto else Icons.Outlined.AddAPhoto, contentDescription = "Report") },
                    label = { Text("Report", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OneUIBluePrimary,
                        selectedTextColor = OneUIBluePrimary,
                        indicatorColor = OneUIBlueLight
                    ),
                    modifier = Modifier.testTag("nav_report")
                )
                NavigationBarItem(
                    selected = activeTab == CitizenTab.Assistant,
                    onClick = { activeTab = CitizenTab.Assistant },
                    icon = { Icon(if (activeTab == CitizenTab.Assistant) Icons.Filled.SmartToy else Icons.Outlined.SmartToy, contentDescription = "Assistant") },
                    label = { Text("AI Agent", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OneUIBluePrimary,
                        selectedTextColor = OneUIBluePrimary,
                        indicatorColor = OneUIBlueLight
                    ),
                    modifier = Modifier.testTag("nav_assistant")
                )
            }
        },
        containerColor = OneUIBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                CitizenTab.Home -> HomeTabContent(viewModel, user)
                CitizenTab.Feed -> FeedTabContent(viewModel)
                CitizenTab.Map -> MapTabContent(viewModel, user)
                CitizenTab.Report -> ReportTabContent(viewModel)
                CitizenTab.Assistant -> AssistantTabContent(viewModel)
            }
        }
    }
}

// ==========================================
// 1. HOME TAB
// ==========================================

@Composable
fun HomeTabContent(viewModel: CivicViewModel, user: User) {
    val civicHealthScore by viewModel.civicHealthScore.collectAsState()
    val complaints by viewModel.allComplaints.collectAsState()

    val totalActive = complaints.count { it.status != "Resolved" }
    val totalResolved = complaints.count { it.status == "Resolved" }
    val urgentCount = complaints.count { it.status == "Urgent" || it.severity == "Urgent" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Greeting Card
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
                            text = "Hello, ${user.name}!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = OneUITextPrimary
                        )
                        Text(
                            text = "Ward 14 • Smart City Portal",
                            fontSize = 13.sp,
                            color = OneUITextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(OneUIBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "User avatar", tint = OneUIBluePrimary)
                    }
                }
            }
        }

        // Circular Civic Health Score Gauge
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = OneUISurface),
                border = BorderStroke(1.dp, Color(0x0DFFFFFF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ward Civic Health Index",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OneUITextPrimary
                    )
                    Text(
                        text = "Real-time municipal performance metric",
                        fontSize = 12.sp,
                        color = OneUITextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 24.dp)
                    )

                    // Draw the Gauge
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val animatedScore by animateFloatAsState(
                            targetValue = civicHealthScore.toFloat(),
                            animationSpec = spring()
                        )

                        Canvas(modifier = Modifier.size(150.dp)) {
                            // Background track (dark slate-800 equivalent for night mode)
                            drawArc(
                                color = Color(0xFF1E293B),
                                startAngle = -220f,
                                sweepAngle = 260f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Foreground track based on score
                            val color = when {
                                animatedScore >= 80f -> OneUIGreen
                                animatedScore >= 50f -> OneUIOrange
                                else -> OneUIRed
                            }
                            drawArc(
                                color = color,
                                startAngle = -220f,
                                sweepAngle = 260f * (animatedScore / 100f),
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${civicHealthScore}",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = OneUITextPrimary
                            )
                            Text(
                                text = when {
                                    civicHealthScore >= 80 -> "Excellent"
                                    civicHealthScore >= 50 -> "Moderate"
                                    else -> "Critical"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    civicHealthScore >= 80 -> OneUIGreen
                                    civicHealthScore >= 50 -> OneUIOrange
                                    else -> OneUIRed
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Civic Score sponsored by Smart City Ward Authority",
                        fontSize = 10.sp,
                        color = OneUITextSecondary,
                        style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.5.sp)
                    )
                }
            }
        }

        // Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Active Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = OneUISurface),
                    border = BorderStroke(1.dp, Color(0x0DFFFFFF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(OneUIOrange.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = "Active", tint = OneUIOrange)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Active Issues", fontSize = 12.sp, color = OneUITextSecondary)
                        Text("${totalActive}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OneUITextPrimary)
                    }
                }

                // Resolved Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = OneUISurface),
                    border = BorderStroke(1.dp, Color(0x0DFFFFFF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(OneUIGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Resolved", tint = OneUIGreen)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Resolved", fontSize = 12.sp, color = OneUITextSecondary)
                        Text("${totalResolved}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OneUITextPrimary)
                    }
                }

                // Urgent Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = OneUISurface),
                    border = BorderStroke(1.dp, Color(0x0DFFFFFF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(OneUIRed.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NewReleases, contentDescription = "Urgent", tint = OneUIRed)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Urgent", fontSize = 12.sp, color = OneUITextSecondary)
                        Text("${urgentCount}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OneUITextPrimary)
                    }
                }
            }
        }

        // Helpful Info Callout
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = OneUIBluePrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Tip",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI Citizen Tip",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Reporting duplicate reports within 150m automatically merges them, combining civic efforts and bumping resolution urgency!",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. PRIORITY FEED TAB
// ==========================================

@Composable
fun FeedTabContent(viewModel: CivicViewModel) {
    val complaints by viewModel.allComplaints.collectAsState()
    val activeComplaints = remember(complaints) {
        complaints.filter { it.status != "Resolved" }.sortedByDescending { it.priorityScore }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Priority Queue (${activeComplaints.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OneUITextPrimary
            )
            Text(
                text = "Sorted by AI Priority",
                fontSize = 12.sp,
                color = OneUIBluePrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeComplaints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = "Empty Queue",
                        tint = OneUITextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Active Complaints!", fontWeight = FontWeight.Bold, color = OneUITextPrimary)
                    Text("The ward is completely clear of open issues.", fontSize = 12.sp, color = OneUITextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeComplaints, key = { it.id }) { complaint ->
                    ComplaintCard(complaint = complaint, onVerify = { viewModel.verifyComplaint(complaint.id) })
                }
            }
        }
    }
}

@Composable
fun ComplaintCard(
    complaint: Complaint,
    onVerify: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("complaint_card_${complaint.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = OneUISurface),
        border = BorderStroke(1.dp, Color(0x0FFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category & Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OneUIBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(complaint.category),
                            contentDescription = complaint.category,
                            tint = OneUIBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = complaint.category,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = OneUITextPrimary
                    )
                }

                // AI Priority badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OneUIBluePrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AI Priority: ${complaint.priorityScore}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OneUIBlueDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Title & Summary
            Text(
                text = complaint.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OneUITextPrimary
            )
            
            Text(
                text = complaint.summary,
                fontSize = 12.sp,
                color = OneUITextSecondary,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Department
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x1AFFFFFF))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(complaint.department, fontSize = 10.sp, color = OneUITextSecondary, fontWeight = FontWeight.Medium)
                }

                // Severity
                val severityColor = when (complaint.severity) {
                    "Urgent" -> OneUIRed
                    "High" -> OneUIRed.copy(alpha = 0.8f)
                    "Medium" -> OneUIOrange
                    else -> OneUIGreen
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(severityColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        complaint.severity,
                        fontSize = 10.sp,
                        color = severityColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Safety Risk
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x10FFFFFF))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Risk: ${complaint.safetyRisk}", fontSize = 10.sp, color = OneUITextSecondary, fontWeight = FontWeight.Medium)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0x0FFFFFFF))

            // Footer actions: Verify / Reporter details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Reported by ${complaint.reporterName}", fontSize = 10.sp, color = OneUITextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Default.Group, contentDescription = "Citizens", tint = OneUIBluePrimary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (complaint.reportCount > 1) "${complaint.reportCount} citizens reported" else "1 citizen reported",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OneUIBlueDark
                        )
                    }
                }

                // Verify Button
                Button(
                    onClick = onVerify,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OneUIBlueLight, contentColor = OneUIBlueDark),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("verify_button_${complaint.id}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Verify", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verify", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 3. MAP TAB
// ==========================================

@Composable
fun MapTabContent(viewModel: CivicViewModel, user: User) {
    val complaints by viewModel.allComplaints.collectAsState()
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedComplaint by remember { mutableStateOf<Complaint?>(null) }

    // Map Coordinates boundaries
    // San Francisco coordinates
    val centerLat = 37.7749
    val centerLng = -122.4194
    val latDelta = 0.03
    val lngDelta = 0.04

    val categories = listOf("All", "Pothole", "Street Light", "Water Leak", "Garbage Dump", "Open Drain")

    val filteredComplaints = remember(complaints, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") complaints else complaints.filter { it.category == selectedCategoryFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(OneUISurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategoryFilter == cat,
                    onClick = {
                        selectedCategoryFilter = cat
                        selectedComplaint = null
                    },
                    label = { Text(cat, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OneUIBluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Custom Vector Map Canvas Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF121620))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(filteredComplaints) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val height = size.height
 
                            // Search if clicked near any complaint pin
                            val clicked = filteredComplaints.firstOrNull { c ->
                                // Map complaint lat/lng to canvas coordinates
                                val x = ((c.longitude - (centerLng - lngDelta / 2)) / lngDelta) * width
                                val y = (1.0 - (c.latitude - (centerLat - latDelta / 2)) / latDelta) * height
                                
                                val dx = offset.x - x
                                val dy = offset.y - y
                                val dist = sqrt((dx * dx + dy * dy).toDouble())
                                dist <= 40.0 // 40 pixel tap tolerance (about 12-15dp)
                            }
                            selectedComplaint = clicked
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height
 
                // 1. Draw River background (fluid organic aesthetic)
                val riverPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, canvasH * 0.75f)
                    quadraticTo(canvasW * 0.4f, canvasH * 0.6f, canvasW * 0.7f, canvasH * 0.95f)
                    lineTo(canvasW, canvasH * 0.95f)
                    lineTo(canvasW, canvasH)
                    lineTo(0f, canvasH)
                    close()
                }
                drawPath(riverPath, color = Color(0xFF1E3A8A).copy(alpha = 0.4f))
 
                // 2. Draw Central Park rectangle
                drawRoundRect(
                    color = Color(0xFF064E3B).copy(alpha = 0.4f),
                    topLeft = Offset(canvasW * 0.15f, canvasH * 0.2f),
                    size = Size(canvasW * 0.25f, canvasH * 0.25f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                )
 
                // 3. Draw City Street Grid
                val gridColor = Color(0xFF272F3F)
                val streetStroke = 12.dp.toPx()

                // Broadway Blvd (Diagonal)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, canvasH * 0.1f),
                    end = Offset(canvasW, canvasH * 0.9f),
                    strokeWidth = streetStroke,
                    cap = StrokeCap.Round
                )

                // Market St (Horizontal)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, canvasH * 0.5f),
                    end = Offset(canvasW, canvasH * 0.5f),
                    strokeWidth = streetStroke,
                    cap = StrokeCap.Round
                )

                // 5th Ave (Vertical Left)
                drawLine(
                    color = gridColor,
                    start = Offset(canvasW * 0.2f, 0f),
                    end = Offset(canvasW * 0.2f, canvasH),
                    strokeWidth = streetStroke,
                    cap = StrokeCap.Round
                )

                // Oak St (Vertical Right)
                drawLine(
                    color = gridColor,
                    start = Offset(canvasW * 0.75f, 0f),
                    end = Offset(canvasW * 0.75f, canvasH),
                    strokeWidth = streetStroke,
                    cap = StrokeCap.Round
                )

                // 4. Draw Complaint Pins
                filteredComplaints.forEach { c ->
                    val x = ((c.longitude - (centerLng - lngDelta / 2)) / lngDelta) * canvasW
                    val y = (1.0 - (c.latitude - (centerLat - latDelta / 2)) / latDelta) * canvasH

                    val pinColor = when (c.status) {
                        "Resolved" -> OneUIGreen
                        "Progress" -> OneUIOrange
                        "Urgent" -> OneUIRed
                        else -> OneUIBluePrimary
                    }

                    // Outer glowing concentric ring for selected pin
                    if (selectedComplaint?.id == c.id) {
                        drawCircle(
                            color = pinColor.copy(alpha = 0.35f),
                            radius = 22.dp.toPx(),
                            center = Offset(x.toFloat(), y.toFloat())
                        )
                    }

                    // Standard pin outer circle
                    drawCircle(
                        color = Color.White,
                        radius = 11.dp.toPx(),
                        center = Offset(x.toFloat(), y.toFloat())
                    )

                    // Pin core solid color
                    drawCircle(
                        color = pinColor,
                        radius = 8.dp.toPx(),
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }

            // Legend Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OneUISurface.copy(alpha = 0.9f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LegendItem(color = OneUIBluePrimary, text = "New Report")
                LegendItem(color = OneUIOrange, text = "In Progress")
                LegendItem(color = OneUIGreen, text = "Resolved")
                LegendItem(color = OneUIRed, text = "Urgent Action")
            }

            // Expanded detailed bottom sheet overlay for selected pin
            androidx.compose.animation.AnimatedVisibility(
                visible = selectedComplaint != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                selectedComplaint?.let { complaint ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = OneUISurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                    Text(complaint.category, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                // Status Tag
                                val statColor = when (complaint.status) {
                                    "Resolved" -> OneUIGreen
                                    "Progress" -> OneUIOrange
                                    "Urgent" -> OneUIRed
                                    else -> OneUIBluePrimary
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        complaint.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(complaint.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OneUITextPrimary)
                            Text(complaint.summary, fontSize = 11.sp, color = OneUITextSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${complaint.reportCount} citizen reports • routed to ${complaint.department}",
                                    fontSize = 10.sp,
                                    color = OneUITextSecondary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { selectedComplaint = null },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = OneUITextSecondary)
                                    }

                                    Button(
                                        onClick = { viewModel.verifyComplaint(complaint.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = OneUIBlueLight, contentColor = OneUIBlueDark),
                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Upvote / Verify", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text, fontSize = 10.sp, color = OneUITextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

// ==========================================
// 4. REPORT TAB
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportTabContent(viewModel: CivicViewModel) {
    val context = LocalContext.current
    val submitStatus by viewModel.submitStatus.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingReport.collectAsState()
    val analysisResult by viewModel.lastResultOfAnalysis.collectAsState()

    var descriptionInput by remember { mutableStateOf("") }
    var mockImageSelected by remember { mutableStateOf<String?>(null) }
    
    // SF center with slight variation
    val lat = 37.7749 + (Math.random() - 0.5) * 0.02
    val lng = -122.4194 + (Math.random() - 0.5) * 0.02

    LaunchedEffect(submitStatus) {
        if (submitStatus is SubmitState.Success) {
            val isMerged = (submitStatus as SubmitState.Success).isMerged
            val message = if (isMerged) {
                "Duplicate detected within 150m! Report successfully merged. Upvote recorded!"
            } else {
                "Report registered successfully with Ward 14!"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            descriptionInput = ""
            mockImageSelected = null
            viewModel.clearAnalysis()
        } else if (submitStatus is SubmitState.Error) {
            Toast.makeText(context, (submitStatus as SubmitState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (analysisResult == null) {
            // Initial Reporting View
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = OneUISurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "File a Smart Civic Report",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OneUITextPrimary
                    )
                    Text(
                        text = "Our server-side Gemini AI automatically categorizes, routes, scores, and merges complaints.",
                        fontSize = 12.sp,
                        color = OneUITextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    // Description Input
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        placeholder = { Text("Describe the issue (e.g. 'deep water pipe leak on market st overflowing', 'broken lamp rendering path dark')", fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("report_description_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OneUIBluePrimary,
                            focusedLabelColor = OneUIBluePrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mock Gallery Photo Picker
                    Text("Attach Visual Asset", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OneUITextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (mockImageSelected == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEDF2F7))
                                .clickable {
                                    // Simulated high fidelity picker based on current description keywords
                                    mockImageSelected = "ic_mock_pothole_upload"
                                    Toast
                                        .makeText(context, "Mock civic photo captured!", Toast.LENGTH_SHORT)
                                        .show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = "Camera", tint = OneUITextSecondary, modifier = Modifier.size(28.dp))
                                Text("Tap to simulate Photo / Camera capture", fontSize = 11.sp, color = OneUITextSecondary, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    } else {
                        // Image picked display
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = OneUIBlueLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(OneUIBluePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = "Mock asset", tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("civic_evidence_003.jpg", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OneUIBlueDark)
                                    Text("960 KB • Base64 Compressed", fontSize = 10.sp, color = OneUITextSecondary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(
                                        onClick = { mockImageSelected = null },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Text("Remove", color = OneUIRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Analyze button
                    Button(
                        onClick = { viewModel.analyzeDescription(descriptionInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("analyze_report_button"),
                        enabled = descriptionInput.trim().isNotEmpty() && !isAnalyzing,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OneUIBluePrimary)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("AI Analyzing...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze with Gemini AI", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // AI Analysis Results view
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = OneUISurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OneUIBluePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gemini Synthesis Result", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OneUIBluePrimary)
                        }
                        TextButton(onClick = { viewModel.clearAnalysis() }) {
                            Text("Edit Input", color = OneUITextSecondary, fontSize = 12.sp)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEDF2F7))

                    // Title
                    Text("SUGGESTED TITLE", fontSize = 10.sp, color = OneUITextSecondary, fontWeight = FontWeight.Bold)
                    Text(analysisResult!!.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OneUITextPrimary)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Summary
                    Text("AI SUMMARY GENERATED", fontSize = 10.sp, color = OneUITextSecondary, fontWeight = FontWeight.Bold)
                    Text(analysisResult!!.summary, fontSize = 12.sp, color = OneUITextPrimary)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid of AI Parameters
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AIParamCard(title = "CATEGORY", value = analysisResult!!.category, modifier = Modifier.weight(1f))
                        AIParamCard(title = "ROUTED DEPT", value = analysisResult!!.department, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AIParamCard(title = "SEVERITY", value = analysisResult!!.severity, modifier = Modifier.weight(1f), highlightColor = when (analysisResult!!.severity) {
                            "Urgent", "High" -> OneUIRed
                            "Medium" -> OneUIOrange
                            else -> OneUIGreen
                        })
                        AIParamCard(title = "AI PRIORITY", value = "${analysisResult!!.priorityScore} / 100", modifier = Modifier.weight(1f), highlightColor = OneUIBluePrimary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Coordinates Display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7FAFC))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = OneUITextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("COORDINATES INJECTED", fontSize = 9.sp, color = OneUITextSecondary, fontWeight = FontWeight.Bold)
                            Text("Lat: ${String.format("%.5f", lat)} • Lng: ${String.format("%.5f", lng)}", fontSize = 11.sp, color = OneUITextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearAnalysis() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Cancel", fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.submitReport(
                                    title = analysisResult!!.title,
                                    summary = analysisResult!!.summary,
                                    description = descriptionInput,
                                    category = analysisResult!!.category,
                                    severity = analysisResult!!.severity,
                                    safetyRisk = analysisResult!!.safetyRisk,
                                    priorityScore = analysisResult!!.priorityScore,
                                    department = analysisResult!!.department,
                                    latitude = lat,
                                    longitude = lng,
                                    imageUrl = mockImageSelected
                                )
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .testTag("submit_report_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OneUIBluePrimary)
                        ) {
                            if (submitStatus is SubmitState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Submit to Ward", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIParamCard(title: String, value: String, modifier: Modifier = Modifier, highlightColor: Color? = null) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 8.sp, color = OneUITextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = highlightColor ?: OneUITextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==========================================
// 5. ASSISTANT TAB (CHAT)
// ==========================================

@Composable
fun AssistantTabContent(viewModel: CivicViewModel) {
    val messages by viewModel.allMessages.collectAsState()
    val isGenerating by viewModel.isGeneratingResponse.collectAsState()
    
    var chatInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val suggestionChips = listOf(
        "Report a pothole",
        "Check Civic Health Score",
        "What is duplicate detection?"
    )

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Conversation log
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(OneUIBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = OneUIBluePrimary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Civic AI Agent", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OneUITextPrimary)
                        Text(
                            text = "Welcome! I'm here to assist you with municipal reporting guidelines, explaining civic ratings, or resolving inquiries.",
                            fontSize = 12.sp,
                            color = OneUITextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp).padding(top = 4.dp)
                        )
                    }
                }
            } else {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 60.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(OneUIBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = OneUIBluePrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = OneUISurface)
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                Text("Civic AI is generating...", fontSize = 12.sp, color = OneUITextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Suggestion Chips Row
        if (messages.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestionChips.forEach { suggestion ->
                    SuggestionChip(
                        onClick = {
                            viewModel.sendMessageToAssistant(suggestion)
                        },
                        label = { Text(suggestion, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OneUIBlueDark) }
                    )
                }
            }
        }

        // Input Tray
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = OneUISurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.clearChatHistory() }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = OneUITextSecondary)
                }

                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Ask Civic AI Assistant...", fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                IconButton(
                    onClick = {
                        val toSend = chatInput.trim()
                        if (toSend.isNotEmpty()) {
                            viewModel.sendMessageToAssistant(toSend)
                            chatInput = ""
                        }
                    },
                    modifier = Modifier.testTag("send_chat_button"),
                    enabled = chatInput.trim().isNotEmpty() && !isGenerating
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (chatInput.trim().isNotEmpty()) OneUIBluePrimary else OneUITextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(OneUIBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = OneUIBluePrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = if (isUser) {
                RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
            } else {
                RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
            },
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) OneUIBluePrimary else OneUISurface
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    fontSize = 13.sp,
                    color = if (isUser) Color.White else OneUITextPrimary,
                    lineHeight = 17.sp
                )
                Text(
                    text = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)),
                    fontSize = 8.sp,
                    color = if (isUser) Color.White.copy(alpha = 0.7f) else OneUITextSecondary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(OneUIBluePrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = OneUIBluePrimary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ==========================================
// HELPERS
// ==========================================

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Pothole" -> Icons.Default.Warning
        "Street Light" -> Icons.Default.Lightbulb
        "Water Leak" -> Icons.Default.WaterDrop
        "Garbage Dump" -> Icons.Default.Delete
        "Broken Sidewalk" -> Icons.Default.NaturePeople
        "Traffic Signal" -> Icons.Default.Traffic
        "Illegal Parking" -> Icons.Default.LocalParking
        "Public Nuisance" -> Icons.Default.VolumeUp
        "Strayed Animal" -> Icons.Default.Pets
        "Fallen Tree" -> Icons.Default.Forest
        "Open Drain" -> Icons.Default.Waves
        "Power Outage" -> Icons.Default.FlashOff
        else -> Icons.Default.Report
    }
}
