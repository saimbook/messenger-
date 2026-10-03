package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettingsManager
import com.example.data.ChatRoom
import com.example.data.FirebaseChatManager
import com.example.data.Message
import com.example.data.admin.AdminAuthorityManager
import com.example.data.admin.AdminRole
import com.example.data.admin.AdminUser
import com.example.network.WebRtcCallManager
import com.example.ui.*
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.ProfileSetupScreen
import com.example.ui.driver.DriverDashboardScreen
import com.example.ui.theme.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            FirebaseApp.initializeApp(this)
            AdminAuthorityManager.initialize(this)
        } catch (e: Exception) { }

        setContent {
            BartaTheme {
                BartaMainApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BartaMainApp(viewModel: ChatViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    var currentAuthUser by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }
    var hasProfile by remember { mutableStateOf(false) }
    var isCheckingProfile by remember { mutableStateOf(true) }
    var currentUserRole by remember { mutableStateOf("USER") }

    var showWhatsAppSettings by remember { mutableStateOf(false) }
    var showDriverDashboard by remember { mutableStateOf(false) }
    var activeAdminUser by remember { mutableStateOf<AdminUser?>(null) }
    var showAddRoomDialog by remember { mutableStateOf(false) }

    val rooms by viewModel.rooms.collectAsState()
    val activeRoomId by viewModel.activeRoomId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isCallActive by viewModel.isCallActive.collectAsState()
    val activeCallName by viewModel.activeCallName.collectAsState()

    var currentTab by remember { mutableStateOf("chats") } // chats, calls, driver
    var showChatDetailOnMobile by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    LaunchedEffect(currentAuthUser) {
        val user = currentAuthUser
        if (user != null) {
            isCheckingProfile = true
            val isMasterAdmin = user.email?.equals("info.bappy1996@gmail.com", ignoreCase = true) == true
            try {
                val db = FirebaseDatabase.getInstance(FirebaseChatManager.FIREBASE_DATABASE_URL)
                val snapshot = db.getReference("users").child(user.uid).get().await()

                if (snapshot.exists() && snapshot.child("name").value != null) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: (user.displayName ?: "")
                    val email = snapshot.child("email").getValue(String::class.java) ?: (user.email ?: "")
                    val role = if (isMasterAdmin) "SUPER_ADMIN" else (snapshot.child("role").getValue(String::class.java) ?: "USER")
                    currentUserRole = role
                    if (isMasterAdmin) {
                        AdminAuthorityManager.registerMasterSuperAdmin(user.uid, name.ifEmpty { "BAPPY (Super Admin)" }, email)
                    }
                    AppSettingsManager.setDisplayName(name)
                    AppSettingsManager.setPhoneNumber(email)
                    FirebaseChatManager.setCurrentUser(user.uid, name, email)
                    hasProfile = true
                } else if (isMasterAdmin) {
                    val adminName = "BAPPY (Super Admin)"
                    val adminEmail = user.email ?: "info.bappy1996@gmail.com"
                    currentUserRole = "SUPER_ADMIN"
                    AdminAuthorityManager.registerMasterSuperAdmin(user.uid, adminName, adminEmail)
                    AppSettingsManager.setDisplayName(adminName)
                    AppSettingsManager.setPhoneNumber(adminEmail)
                    FirebaseChatManager.setCurrentUser(user.uid, adminName, adminEmail)
                    hasProfile = true
                } else {
                    hasProfile = false
                }
            } catch (e: Exception) {
                if (isMasterAdmin) {
                    currentUserRole = "SUPER_ADMIN"
                    AdminAuthorityManager.registerMasterSuperAdmin(user.uid, "BAPPY (Super Admin)", user.email ?: "info.bappy1996@gmail.com")
                    hasProfile = true
                } else {
                    hasProfile = AppSettingsManager.displayName.value.isNotEmpty()
                }
            } finally {
                isCheckingProfile = false
            }
        } else {
            hasProfile = false
            isCheckingProfile = false
        }
    }

    // 1. Unauthenticated -> Auth Screen
    if (currentAuthUser == null) {
        AuthScreen(
            onAuthSuccess = { user ->
                currentAuthUser = user
            }
        )
        return
    }

    // 2. Profile Setup Screen
    if (!hasProfile && !isCheckingProfile) {
        ProfileSetupScreen(
            firebaseUser = currentAuthUser!!,
            onProfileCompleted = {
                hasProfile = true
            }
        )
        return
    }

    // 3. Audio Call Screen Overlay
    if (isCallActive) {
        AudioCallScreen(
            partnerName = activeCallName.ifEmpty { "পরিচিতি" },
            onEndCall = { viewModel.endActiveCall() }
        )
        return
    }

    // 4. Admin Dashboard Full Screen
    if (activeAdminUser != null) {
        AdminDashboardScreen(
            adminUser = activeAdminUser!!,
            onClose = { activeAdminUser = null }
        )
        return
    }

    // 5. Driver Dashboard Full Screen
    if (showDriverDashboard) {
        DriverDashboardScreen(onClose = { showDriverDashboard = false })
        return
    }

    // 6. WhatsApp Settings Screen
    if (showWhatsAppSettings) {
        WhatsAppSettingsScreen(
            onBack = { showWhatsAppSettings = false },
            viewModel = viewModel,
            onOpenAdminDashboard = { admin ->
                activeAdminUser = admin
                showWhatsAppSettings = false
            },
            onLogout = {
                auth.signOut()
                currentAuthUser = null
                hasProfile = false
                showWhatsAppSettings = false
            }
        )
        return
    }

    // 7. Main Messaging Interface (Responsive Tablet / Mobile)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (currentTab == "calls") "কল (Calls)" else "বার্তা (BARTA)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00A884))
                        )
                    }
                },
                actions = {
                    // Master Super Admin Shield Icon
                    if (currentUserRole == "SUPER_ADMIN" || currentUserRole == "ADMIN" || currentAuthUser?.email.equals("info.bappy1996@gmail.com", ignoreCase = true)) {
                        IconButton(
                            onClick = {
                                activeAdminUser = AdminAuthorityManager.currentAdmin.value ?: AdminUser(
                                    uid = currentAuthUser?.uid ?: "master_admin",
                                    email = currentAuthUser?.email ?: "info.bappy1996@gmail.com",
                                    displayName = viewModel.currentUserName.value.ifEmpty { "BAPPY (Super Admin)" },
                                    role = AdminRole.SUPER_ADMIN,
                                    isMfaVerified = true
                                )
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color(0xFF00A884).copy(alpha = 0.2f),
                                contentColor = Color(0xFF00A884)
                            ),
                            modifier = Modifier.testTag("admin_shield_button")
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "স্মার্ট এডমিন ড্যাশবোর্ড")
                        }
                    }

                    IconButton(
                        onClick = { showWhatsAppSettings = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "হোয়াটসঅ্যাপ সেটিংস", tint = Color.White)
                    }

                    IconButton(
                        onClick = { showAddRoomDialog = true },
                        modifier = Modifier.testTag("add_room_button")
                    ) {
                        Icon(Icons.Default.AddComment, contentDescription = "নতুন চ্যাট", tint = BartaPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BartaPrimaryDark)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = BartaSurface) {
                NavigationBarItem(
                    selected = currentTab == "chats",
                    onClick = { currentTab = "chats" },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "চ্যাটস") },
                    label = { Text("চ্যাট") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = BartaPrimary,
                        unselectedIconColor = BartaTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "calls",
                    onClick = { currentTab = "calls" },
                    icon = { Icon(Icons.Default.Call, contentDescription = "কলসমূহ") },
                    label = { Text("কলস") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = BartaPrimary,
                        unselectedIconColor = BartaTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "driver",
                    onClick = { showDriverDashboard = true },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "ড্রাইভার") },
                    label = { Text("রাইডার") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = BartaPrimary,
                        unselectedIconColor = BartaTextSecondary
                    )
                )
            }
        },
        containerColor = BartaBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val activeRoom = rooms.find { it.id == activeRoomId } ?: rooms.firstOrNull()

            if (isWideScreen) {
                // Tablet Master-Detail Layout
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.width(360.dp).fillMaxHeight()) {
                        if (currentTab == "calls") {
                            CallsListPane(rooms = rooms, onStartCall = { id, color ->
                                val target = rooms.find { it.id == id }
                                viewModel.startCall(id, target?.name ?: "পরিচিতি")
                            })
                        } else {
                            ContactList(
                                rooms = rooms,
                                activeRoomId = activeRoomId,
                                onSelectConversation = { room ->
                                    viewModel.selectRoom(room.id)
                                }
                            )
                        }
                    }

                    VerticalDivider(color = Color(0xFF2A3942))

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        if (activeRoom != null) {
                            ChatDetailPane(
                                room = activeRoom,
                                messages = messages,
                                onSendMessage = viewModel::sendMessage,
                                onStartVoiceRecord = viewModel::startVoiceRecording,
                                onStopVoiceRecord = viewModel::stopVoiceRecording,
                                onStartCall = { viewModel.startCall(activeRoom.id, activeRoom.name) }
                            )
                        }
                    }
                }
            } else {
                // Mobile Layout
                if (showChatDetailOnMobile && activeRoom != null) {
                    BackHandler { showChatDetailOnMobile = false }
                    ChatDetailPane(
                        room = activeRoom,
                        messages = messages,
                        onSendMessage = viewModel::sendMessage,
                        onStartVoiceRecord = viewModel::startVoiceRecording,
                        onStopVoiceRecord = viewModel::stopVoiceRecording,
                        onStartCall = { viewModel.startCall(activeRoom.id, activeRoom.name) },
                        onBack = { showChatDetailOnMobile = false }
                    )
                } else {
                    if (currentTab == "calls") {
                        CallsListPane(rooms = rooms, onStartCall = { id, color ->
                            val target = rooms.find { it.id == id }
                            viewModel.startCall(id, target?.name ?: "পরিচিতি")
                        })
                    } else {
                        ContactList(
                            rooms = rooms,
                            activeRoomId = activeRoomId,
                            onSelectConversation = { room ->
                                viewModel.selectRoom(room.id)
                                showChatDetailOnMobile = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddRoomDialog) {
        var newRoomName by remember { mutableStateOf("") }
        var newRoomSubtitle by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddRoomDialog = false },
            title = { Text("নতুন চ্যাট শুরু করুন", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newRoomName,
                        onValueChange = { newRoomName = it },
                        label = { Text("ব্যক্তি বা গ্রুপের নাম") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRoomSubtitle,
                        onValueChange = { newRoomSubtitle = it },
                        label = { Text("পরিচিতি / নম্বর / বিবরণ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRoomName.isNotBlank()) {
                            viewModel.createCustomRoom(newRoomName, newRoomSubtitle)
                            showAddRoomDialog = false
                            showChatDetailOnMobile = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary)
                ) {
                    Text("চ্যাট খুলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRoomDialog = false }) { Text("বাতিল", color = Color.White) }
            },
            containerColor = BartaSurface
        )
    }
}

@Composable
fun ChatDetailPane(
    room: ChatRoom,
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onStartVoiceRecord: () -> Unit,
    onStopVoiceRecord: () -> Unit,
    onStartCall: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    var textInput by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BartaBackground)
    ) {
        // Chat Header
        Surface(
            color = BartaPrimaryDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(room.avatarColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = room.name.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (room.isOnline) "অনলাইন • এন্ড-টু-এন্ড এনক্রিপ্টেড" else "বার্তা (BARTA)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (room.isOnline) Color(0xFF00A884) else BartaTextSecondary
                    )
                }

                IconButton(onClick = onStartCall, modifier = Modifier.testTag("chat_call_button")) {
                    Icon(Icons.Default.Call, contentDescription = "ভয়েস কল", tint = BartaPrimary)
                }
            }
        }

        // Messages List
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            reverseLayout = false
        ) {
            items(
                items = messages,
                key = { it.id }
            ) { msg ->
                val isOutgoing = msg.isOutgoing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isOutgoing) 12.dp else 2.dp,
                            bottomEnd = if (isOutgoing) 2.dp else 12.dp
                        ),
                        color = if (isOutgoing) BartaOutgoingBubble else BartaIncomingBubble,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            if (!isOutgoing && msg.senderName.isNotBlank()) {
                                Text(
                                    text = msg.senderName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00A884)
                                )
                                Spacer(Modifier.height(2.dp))
                            }
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Message Input Bar
        Surface(
            color = BartaSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("একটি বার্তা লিখুন...", color = BartaTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BartaPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = BartaSurfaceVariant,
                        unfocusedContainerColor = BartaSurfaceVariant,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("chat_input_field")
                )

                Spacer(Modifier.width(8.dp))

                if (textInput.isNotBlank()) {
                    IconButton(
                        onClick = {
                            onSendMessage(textInput)
                            textInput = ""
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = BartaPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(46.dp).testTag("chat_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "পাঠান")
                    }
                } else {
                    IconButton(
                        onClick = {
                            if (!isRecording) {
                                isRecording = true
                                onStartVoiceRecord()
                            } else {
                                isRecording = false
                                onStopVoiceRecord()
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isRecording) Color(0xFFFF5252) else BartaPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(46.dp).testTag("chat_voice_button")
                    ) {
                        Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = "ভয়েস রেকর্ড")
                    }
                }
            }
        }
    }
}
