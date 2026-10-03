package com.example.ui.admin

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.admin.*
import com.example.ui.theme.*

enum class AdminTab(val titleBn: String, val icon: ImageVector) {
    OVERVIEW("ওভারভিউ", Icons.Default.Dashboard),
    USERS("ইউজার ও রোলস", Icons.Default.People),
    FEATURES("ফিচার কন্ট্রোল", Icons.Default.ToggleOn),
    BROADCAST("ব্রডকাস্ট নোটিশ", Icons.Default.Campaign),
    REPORTS("রিপোর্ট ডেস্ক", Icons.Default.ReportProblem),
    AUDIT("সিকিউরিটি লগ", Icons.Default.Security)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminUser: AdminUser,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }
    val systemStats by AdminAuthorityManager.systemStats.collectAsState()
    val managedUsers by AdminAuthorityManager.managedUsers.collectAsState()
    val reports by AdminAuthorityManager.reports.collectAsState()
    val auditLogs by AdminAuthorityManager.auditLogs.collectAsState()
    val appFeatures by AppFeatureToggleManager.features.collectAsState()
    val broadcasts by AdminAuthorityManager.systemBroadcasts.collectAsState()

    var showAddFeatureDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var selectedUserForEdit by remember { mutableStateOf<AdminManagedUser?>(null) }

    BackHandler {
        onClose()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BARTA স্মার্ট এডমিন কনসোল",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF00A884).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00A884))
                            ) {
                                Text(
                                    text = adminUser.role.name,
                                    color = Color(0xFF25D366),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "অ্যাডমিনিস্ট্রেটর: ${adminUser.displayName} (${adminUser.email})",
                            style = MaterialTheme.typography.bodySmall,
                            color = BartaTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("admin_close_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "সিস্টেম রিফ্রেশ সম্পন্ন!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "রিফ্রেশ", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BartaPrimaryDark)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = BartaSurface) {
                AdminTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.titleBn) },
                        label = { Text(tab.titleBn, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            indicatorColor = BartaPrimary,
                            unselectedIconColor = BartaTextSecondary,
                            unselectedTextColor = BartaTextSecondary
                        )
                    )
                }
            }
        },
        containerColor = BartaBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AdminTab.OVERVIEW -> OverviewTab(
                    stats = systemStats,
                    onNavigateToUsers = { selectedTab = AdminTab.USERS },
                    onNavigateToFeatures = { selectedTab = AdminTab.FEATURES },
                    onSendBroadcastClick = { showBroadcastDialog = true }
                )
                AdminTab.USERS -> UsersManagementTab(
                    users = managedUsers,
                    onEditUser = { selectedUserForEdit = it }
                )
                AdminTab.FEATURES -> FeaturesControlTab(
                    features = appFeatures,
                    onToggle = { key, state -> AppFeatureToggleManager.toggleFeature(key, state) },
                    onAddNewFeature = { showAddFeatureDialog = true }
                )
                AdminTab.BROADCAST -> BroadcastTab(
                    broadcasts = broadcasts,
                    onNewBroadcastClick = { showBroadcastDialog = true }
                )
                AdminTab.REPORTS -> ReportsDeskTab(
                    reports = reports,
                    onResolve = { id -> AdminAuthorityManager.resolveReport(id, "ACTION_TAKEN") }
                )
                AdminTab.AUDIT -> SecurityAuditTab(auditLogs = auditLogs)
            }
        }
    }

    // Dialog: Add Custom Dynamic Feature
    if (showAddFeatureDialog) {
        AddFeatureDialog(
            onDismiss = { showAddFeatureDialog = false },
            onAdd = { key, nameBn, descBn, category ->
                AppFeatureToggleManager.addCustomFeature(key, nameBn, descBn, category)
                showAddFeatureDialog = false
                Toast.makeText(context, "নতুন ফিচার '$nameBn' সফলভাবে যোগ করা হয়েছে!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Send System Broadcast
    if (showBroadcastDialog) {
        SendBroadcastDialog(
            onDismiss = { showBroadcastDialog = false },
            onSend = { title, body, audience, isEmergency ->
                AdminAuthorityManager.sendBroadcastNotification(title, body, audience, isEmergency)
                showBroadcastDialog = false
                Toast.makeText(context, "ব্রডকাস্ট নোটিশ সফলভাবে প্রেরণ করা হয়েছে!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Edit User Role & Status
    selectedUserForEdit?.let { user ->
        EditUserDialog(
            user = user,
            onDismiss = { selectedUserForEdit = null },
            onSave = { targetUid, newRole, newStatus ->
                AdminAuthorityManager.updateUserRole(targetUid, newRole)
                AdminAuthorityManager.updateUserStatus(targetUid, newStatus)
                selectedUserForEdit = null
                Toast.makeText(context, "${user.displayName}-এর তথ্য হালনাগাদ করা হয়েছে!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// ==========================================
// 1. OVERVIEW TAB
// ==========================================
@Composable
fun OverviewTab(
    stats: SystemOverviewStats,
    onNavigateToUsers: () -> Unit,
    onNavigateToFeatures: () -> Unit,
    onSendBroadcastClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BartaSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00A884))
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "সার্ভার ও ক্লাউড স্ট্যাটাস: অপারেশনাল",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Firebase RTDB, Keystore E2EE & BARTA Core Online",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF00A884)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "সিস্টেম মেট্রিক্স ও পরিসংখ্যান",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("মোট ইউজার", stats.totalUsers.toString(), Icons.Default.Group, Color(0xFF2196F3), Modifier.weight(1f))
                StatCard("সক্রিয় আজ", stats.activeToday.toString(), Icons.Default.OnlinePrediction, Color(0xFF4CAF50), Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("মেসেজ বিনিময়", stats.totalMessagesSent.toString(), Icons.Default.ChatBubble, Color(0xFFFF9800), Modifier.weight(1f))
                StatCard("ভয়েস কল", stats.totalCallsMade.toString(), Icons.Default.Call, Color(0xFF9C27B0), Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("ড্রাইভার ও রাইডার", stats.activeDrivers.toString(), Icons.Default.DirectionsCar, Color(0xFF00BCD4), Modifier.weight(1f))
                StatCard("ওপেন রিপোর্ট", stats.totalReports.toString(), Icons.Default.Warning, Color(0xFFE91E63), Modifier.weight(1f))
            }
        }

        item {
            Text(
                text = "দ্রুত অ্যাকশন (Quick Actions)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSendBroadcastClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("জরুরি বার্তা", fontSize = 13.sp)
                }

                Button(
                    onClick = onNavigateToFeatures,
                    colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ফিচার পরিবর্তন", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BartaSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(text = label, style = MaterialTheme.typography.bodySmall, color = BartaTextSecondary)
            }
        }
    }
}

// ==========================================
// 2. USERS & ROLES TAB
// ==========================================
@Composable
fun UsersManagementTab(
    users: List<AdminManagedUser>,
    onEditUser: (AdminManagedUser) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "নিবন্ধিত ইউজার ও অথরিটি রোলস (${users.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(users) { user ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BartaSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable { onEditUser(user) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (user.role == "SUPER_ADMIN") Color(0xFF00A884)
                                else if (user.role == "DRIVER") Color(0xFF2196F3)
                                else Color(0xFF374248)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (user.accountStatus == "ACTIVE") Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFF44336).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = user.accountStatus,
                                    color = if (user.accountStatus == "ACTIVE") Color(0xFF4CAF50) else Color(0xFFF44336),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(text = user.phoneNumber, style = MaterialTheme.typography.bodySmall, color = BartaTextSecondary)
                        Text(text = "রোল: ${user.role}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF00A884))
                    }

                    IconButton(onClick = { onEditUser(user) }) {
                        Icon(Icons.Default.Edit, contentDescription = "সম্পাদনা", tint = BartaTextSecondary)
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. FEATURES CONTROL TAB
// ==========================================
@Composable
fun FeaturesControlTab(
    features: List<AppFeatureItem>,
    onToggle: (String, Boolean) -> Unit,
    onAddNewFeature: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "অ্যাপ্লিকেশন ফিচার সুইচবোর্ড",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = onAddNewFeature,
                    colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("নতুন ফিচার যোগ", fontSize = 12.sp)
                }
            }
        }

        items(features) { feature ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BartaSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = feature.nameBn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = feature.descBn,
                            style = MaterialTheme.typography.bodySmall,
                            color = BartaTextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "কী: ${feature.featureKey} • ক্যাটাগরি: ${feature.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00A884)
                        )
                    }

                    Switch(
                        checked = feature.isEnabled,
                        onCheckedChange = { onToggle(feature.featureKey, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BartaPrimary
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. BROADCAST TAB
// ==========================================
@Composable
fun BroadcastTab(
    broadcasts: List<SystemBroadcastMessage>,
    onNewBroadcastClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সিস্টেম ব্রডকাস্ট ও সতর্কতা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = onNewBroadcastClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("নোটিশ পাঠান", fontSize = 12.sp)
                }
            }
        }

        if (broadcasts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("কোনো সক্রিয় ব্রডকাস্ট নোটিশ নেই।", color = BartaTextSecondary)
                }
            }
        } else {
            items(broadcasts) { msg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.isEmergency) Color(0xFF3E1F24) else BartaSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (msg.isEmergency) Icons.Default.Emergency else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (msg.isEmergency) Color(0xFFFF5252) else Color(0xFF00A884)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = msg.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(text = msg.body, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "টার্গেট: ${msg.targetAudience}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BartaTextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. REPORTS DESK TAB
// ==========================================
@Composable
fun ReportsDeskTab(
    reports: List<BartaReport>,
    onResolve: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "ইউজার রিপোর্ট ও কমপ্লেইন্ট ডেস্ক (${reports.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(reports) { rep ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BartaSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "রিপোর্ট আইডি: ${rep.reportId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00A884)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (rep.status == "OPEN") Color(0xFFFF5252).copy(alpha = 0.2f) else Color(0xFF4CAF50).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = rep.status,
                                color = if (rep.status == "OPEN") Color(0xFFFF5252) else Color(0xFF4CAF50),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(text = "কারণ: ${rep.reason}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "অভিযোগ: \"${rep.reportedContentSnippet}\"", style = MaterialTheme.typography.bodySmall, color = BartaTextSecondary)
                    Text(text = "অভিযোগকারী: ${rep.reporterName} • টার্গেট: ${rep.targetSenderName}", style = MaterialTheme.typography.labelSmall, color = BartaTextSecondary)

                    if (rep.status == "OPEN") {
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { onResolve(rep.reportId) },
                            colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text("রিপোর্ট সমাধান ও ক্লোজ করুন", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. SECURITY AUDIT TAB
// ==========================================
@Composable
fun SecurityAuditTab(auditLogs: List<AdminAuditLog>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "সিস্টেম ও সিকিউরিটি অডিট লগ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(auditLogs) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BartaSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00A884), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = log.action, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = log.reason, style = MaterialTheme.typography.bodySmall, color = BartaTextSecondary)
                        Text(text = "দ্বারা: ${log.adminEmail}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00A884))
                    }
                }
            }
        }
    }
}

// ==========================================
// DIALOGS
// ==========================================
@Composable
fun AddFeatureDialog(
    onDismiss: () -> Unit,
    onAdd: (key: String, nameBn: String, descBn: String, category: String) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var descBn by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("GENERAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন অ্যাপ ফিচার যোগ করুন", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it.uppercase().replace(" ", "_") },
                    label = { Text("ফিচার কী (যেমন: DARK_THEME)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("ফিচারের নাম (বাংলায়)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descBn,
                    onValueChange = { descBn = it },
                    label = { Text("বিবরণ") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (key.isNotBlank() && nameBn.isNotBlank()) onAdd(key, nameBn, descBn, category) },
                colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary)
            ) {
                Text("যোগ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.White) }
        },
        containerColor = BartaSurface
    )
}

@Composable
fun SendBroadcastDialog(
    onDismiss: () -> Unit,
    onSend: (title: String, body: String, audience: String, isEmergency: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var isEmergency by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("সিস্টেম ব্রডকাস্ট নোটিশ প্রেরণ", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("নোটিশের শিরোনাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("বার্তা / বিবরণ") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isEmergency, onCheckedChange = { isEmergency = it })
                    Spacer(Modifier.width(6.dp))
                    Text("জরুরি লাল সতর্কতা (Emergency Alert)", color = Color.White, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && body.isNotBlank()) onSend(title, body, "ALL", isEmergency) },
                colors = ButtonDefaults.buttonColors(containerColor = if (isEmergency) Color(0xFFE91E63) else BartaPrimary)
            ) {
                Text("পাঠান")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.White) }
        },
        containerColor = BartaSurface
    )
}

@Composable
fun EditUserDialog(
    user: AdminManagedUser,
    onDismiss: () -> Unit,
    onSave: (uid: String, role: String, status: String) -> Unit
) {
    var role by remember { mutableStateOf(user.role) }
    var status by remember { mutableStateOf(user.accountStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${user.displayName} - কন্ট্রোল", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ব্যবহারকারীর ভূমিকা (Role):", color = BartaTextSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("USER", "DRIVER", "ADMIN", "SUPER_ADMIN").forEach { r ->
                        FilterChip(
                            selected = role == r,
                            onClick = { role = r },
                            label = { Text(r, fontSize = 11.sp) }
                        )
                    }
                }

                Text("অ্যাকাউন্ট স্ট্যাটাস (Status):", color = BartaTextSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ACTIVE", "SUSPENDED", "BLOCKED").forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(user.uid, role, status) },
                colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.White) }
        },
        containerColor = BartaSurface
    )
}
