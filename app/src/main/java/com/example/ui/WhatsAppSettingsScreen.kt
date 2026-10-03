package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettingsManager
import com.example.data.DataMode
import com.example.data.admin.AdminRole
import com.example.data.admin.AdminUser
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppSettingsScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel,
    onOpenAdminDashboard: ((AdminUser) -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val name by AppSettingsManager.displayName.collectAsState()
    val phone by AppSettingsManager.phoneNumber.collectAsState()
    val about by AppSettingsManager.aboutStatus.collectAsState()
    val dataMode by viewModel.dataMode.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("সেটিংস (Settings)", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BartaPrimaryDark)
            )
        },
        containerColor = BartaBackground,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Card
            Card(
                colors = CardDefaults.cardColors(containerColor = BartaSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("settings_profile_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BartaPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = phone, style = MaterialTheme.typography.bodyMedium, color = BartaTextSecondary)
                            Text(text = about, style = MaterialTheme.typography.bodySmall, color = Color(0xFF00A884))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { showEditProfileDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A3942)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("প্রোফাইল তথ্য পরিবর্তন করুন")
                    }
                }
            }

            // APK Download Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00A884).copy(alpha = 0.15f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("settings_apk_download_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = Color(0xFF00A884), modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("BARTA অফিশিয়াল APK ইনস্টলার", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF00A884))
                            Text("বিল্ড আউটপুট: app/build/outputs/apk/debug/app-debug.apk", style = MaterialTheme.typography.bodySmall, color = BartaTextSecondary)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "BARTA APK")
                                putExtra(android.content.Intent.EXTRA_TEXT, "BARTA অ্যান্ড্রয়েড মেসেঞ্জার অ্যাপটি ব্যবহার করতে AI Studio-র Export মেনু অথবা build/outputs/apk/debug/app-debug.apk থেকে সরাসরি সংগ্রহ করুন।")
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "BARTA অ্যাপ তথ্য শেয়ার করুন"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("APK তথ্য ও শেয়ার লিংক", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Admin Console Launcher
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF192D2B)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF00A884), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("স্মার্ট এডমিন ড্যাশবোর্ড", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("সিস্টেম কনট্রোল, ফিচার সুইচ ও ইউজার ম্যানেজমেন্ট", style = MaterialTheme.typography.bodySmall, color = BartaTextSecondary)
                    }
                    Button(
                        onClick = {
                            val admin = AdminUser(
                                uid = "master_admin",
                                email = phone,
                                displayName = name,
                                role = AdminRole.SUPER_ADMIN,
                                isMfaVerified = true
                            )
                            onOpenAdminDashboard?.invoke(admin)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("খুলুন")
                    }
                }
            }

            // Settings Items
            SettingsRow(Icons.Default.VpnKey, "অ্যাকাউন্ট ও নিরাপত্তা", "পাসওয়ার্ড, কিস্টোর এনক্রিপশন ও ব্যাকআপ") { }
            SettingsRow(Icons.Default.Lock, "প্রাইভেসি", "লাস্ট সিন, প্রোফাইল ফটো ও রিড রিসিপ্ট") { }
            SettingsRow(Icons.Default.Chat, "চ্যাট ও ওয়ালপেপার", "থিম, চ্যাট ব্যাকআপ ও ফন্ট সাইজ") { }
            SettingsRow(Icons.Default.Notifications, "নোটিফিকেশন", "মেসেজ টোন, কল রিংটোন ও ভাইব্রেশন") { }
            SettingsRow(Icons.Default.DataUsage, "স্টোরেজ ও ডেটা মোড", "বর্তমান মোড: ${dataMode.titleBn}") { }
            SettingsRow(Icons.Default.Help, "সাহায্য ও সাপোর্ট", "সহায়তা কেন্দ্র ও যোগাযোগের বিবরণ") { }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { onLogout?.invoke() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E1F24)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFFF5252))
                Spacer(Modifier.width(8.dp))
                Text("অ্যাকাউন্ট লগআউট করুন", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEditProfileDialog) {
        var newName by remember { mutableStateOf(name) }
        var newAbout by remember { mutableStateOf(about) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("প্রোফাইল এডিট করুন", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("নাম") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAbout,
                        onValueChange = { newAbout = it },
                        label = { Text("স্ট্যাটাস") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AppSettingsManager.setDisplayName(newName)
                        AppSettingsManager.setAboutStatus(newAbout)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("বাতিল", color = Color.White) }
            },
            containerColor = BartaSurface
        )
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BartaSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = BartaPrimary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = BartaTextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BartaTextSecondary)
        }
    }
}
