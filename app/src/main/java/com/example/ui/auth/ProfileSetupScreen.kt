package com.example.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettingsManager
import com.example.data.BartaOneEngine
import com.example.data.FirebaseChatManager
import com.example.data.admin.AdminAuthorityManager
import com.example.data.security.DeviceSecurityManager
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    firebaseUser: FirebaseUser,
    onProfileCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val initialEmail = firebaseUser.email ?: ""
    val isMasterAdmin = initialEmail.equals("info.bappy1996@gmail.com", ignoreCase = true)
    val initialName = if (isMasterAdmin) "BAPPY (Super Admin)" else (firebaseUser.displayName ?: initialEmail.substringBefore("@").replace(".", " ").capitalize())
    val initialBartaId = if (isMasterAdmin) "@info_bappy1996" else ("@" + initialEmail.substringBefore("@").lowercase().replace(Regex("[^a-z0-9_]"), "_"))

    var displayName by remember { mutableStateOf(initialName) }
    var emailAddress by remember { mutableStateOf(initialEmail) }
    var bartaId by remember { mutableStateOf(initialBartaId) }
    var aboutStatus by remember { mutableStateOf(if (isMasterAdmin) "BARTA সুপার এডমিনিস্ট্রেটর" else "বার্তা (BARTA) ব্যবহার করছি!") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BartaBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            BartaPrimaryDark.copy(alpha = 0.3f),
                            BartaBackground,
                            BartaBackground
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .shadow(12.dp, CircleShape, spotColor = BartaPrimary)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(listOf(BartaPrimary, BartaPrimaryDark))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "প্রোফাইল নিশ্চিতকরণ",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = "বার্তা (BARTA)-তে আপনার অ্যাকাউন্টের বিবরণ সম্পন্ন করুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = BartaTextSecondary
                )

                Spacer(Modifier.height(20.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = BartaSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("আপনার নাম (Display Name)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("ইমেইল ঠিকানা (Email Address)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("profile_email_input")
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = bartaId,
                            onValueChange = { bartaId = it },
                            label = { Text("ব্যবহারকারী আইডি / BARTA ID") },
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("profile_id_input")
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = aboutStatus,
                            onValueChange = { aboutStatus = it },
                            label = { Text("স্ট্যাটাস (Bio / About)") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("profile_bio_input")
                        )

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                coroutineScope.launch {
                                    val uid = firebaseUser.uid
                                    val cleanName = displayName.trim().ifEmpty { "BARTA ব্যবহারকারী" }
                                    val cleanEmail = emailAddress.trim()
                                    val targetRole = if (isMasterAdmin) "SUPER_ADMIN" else "USER"

                                    try {
                                        val db = FirebaseDatabase.getInstance(FirebaseChatManager.FIREBASE_DATABASE_URL)
                                        val userMap = mapOf(
                                            "uid" to uid,
                                            "name" to cleanName,
                                            "email" to cleanEmail,
                                            "bartaId" to bartaId.trim(),
                                            "about" to aboutStatus.trim(),
                                            "role" to targetRole,
                                            "createdAt" to ServerValue.TIMESTAMP,
                                            "accountStatus" to "ACTIVE"
                                        )
                                        db.getReference("users").child(uid).setValue(userMap).await()

                                        if (isMasterAdmin) {
                                            AdminAuthorityManager.registerMasterSuperAdmin(uid, cleanName, cleanEmail)
                                        }
                                    } catch (e: Exception) {
                                        if (isMasterAdmin) {
                                            AdminAuthorityManager.registerMasterSuperAdmin(uid, cleanName, cleanEmail)
                                        }
                                    }

                                    try {
                                        DeviceSecurityManager.enrollCurrentDevice(context, uid)
                                    } catch (e: Exception) { }

                                    AppSettingsManager.setDisplayName(cleanName)
                                    AppSettingsManager.setPhoneNumber(cleanEmail)
                                    AppSettingsManager.setAboutStatus(aboutStatus.trim())
                                    FirebaseChatManager.setCurrentUser(uid, cleanName, cleanEmail)
                                    BartaOneEngine.onUserAuthenticated(context, uid, cleanName, cleanEmail)

                                    isLoading = false
                                    Toast.makeText(context, if (isMasterAdmin) "সুপার এডমিন প্রোফাইল সক্রিয় হয়েছে!" else "প্রোফাইল সফলভাবে তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                                    onProfileCompleted()
                                }
                            },
                            enabled = !isLoading && displayName.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary),
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_profile_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "প্রোফাইল নিশ্চিত করুন ও প্রবেশ করুন",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
