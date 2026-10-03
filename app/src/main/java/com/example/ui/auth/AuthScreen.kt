package com.example.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettingsManager
import com.example.data.BartaOneEngine
import com.example.data.FirebaseChatManager
import com.example.data.admin.AdminAuthorityManager
import com.example.data.security.DeviceSecurityManager
import com.example.ui.theme.*
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (FirebaseUser) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    var isSignUpMode by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("info.bappy1996@gmail.com") }
    var passwordInput by remember { mutableStateOf("B@ppi2022") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var fullNameInput by remember { mutableStateOf("BAPPY (Super Admin)") }
    var bartaIdInput by remember { mutableStateOf("@info_bappy1996") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun handleAuthenticationComplete(user: FirebaseUser, customDisplayName: String? = null, customBartaId: String? = null) {
        coroutineScope.launch {
            val uid = user.uid
            val email = user.email ?: ""
            val isMasterAdmin = email.equals("info.bappy1996@gmail.com", ignoreCase = true)
            val resolvedName = customDisplayName?.trim()?.ifEmpty { null }
                ?: (if (isMasterAdmin) "BAPPY (Super Admin)" else (user.displayName ?: email.substringBefore("@").replace(".", " ").capitalize()))
            val resolvedBartaId = customBartaId?.trim()?.ifEmpty { null }
                ?: (if (isMasterAdmin) "@info_bappy1996" else ("@" + email.substringBefore("@").lowercase().replace(Regex("[^a-z0-9_]"), "_")))

            try {
                val db = FirebaseDatabase.getInstance(FirebaseChatManager.FIREBASE_DATABASE_URL)
                val userRef = db.getReference("users").child(uid)
                val userMap = mapOf(
                    "name" to resolvedName,
                    "email" to email,
                    "bartaId" to resolvedBartaId,
                    "about" to (if (isMasterAdmin) "BARTA সুপার এডমিনিস্ট্রেটর" else "বার্তা (BARTA) ব্যবহার করছি!"),
                    "role" to (if (isMasterAdmin) "SUPER_ADMIN" else "USER"),
                    "createdAt" to System.currentTimeMillis()
                )
                userRef.setValue(userMap).await()

                if (isMasterAdmin) {
                    AdminAuthorityManager.registerMasterSuperAdmin(uid, resolvedName, email)
                }
            } catch (dbErr: Exception) {
                if (isMasterAdmin) {
                    AdminAuthorityManager.registerMasterSuperAdmin(uid, resolvedName, email)
                }
            }

            try {
                DeviceSecurityManager.enrollCurrentDevice(context, uid)
            } catch (e: Exception) { }

            AppSettingsManager.setDisplayName(resolvedName)
            AppSettingsManager.setPhoneNumber(email)
            FirebaseChatManager.setCurrentUser(uid, resolvedName, email)
            BartaOneEngine.onUserAuthenticated(context, uid, resolvedName, email)

            isLoading = false
            Toast.makeText(context, if (isMasterAdmin) "সুপার এডমিন হিসেবে স্বাগতম, $resolvedName!" else "স্বাগতম, $resolvedName!", Toast.LENGTH_SHORT).show()
            onAuthSuccess(user)
        }
    }

    fun handleLogin() {
        val cleanEmail = emailInput.trim()
        val cleanPassword = passwordInput.trim()
        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            errorMessage = "ইমেইল ও পাসওয়ার্ড প্রদান করুন।"
            return
        }

        isLoading = true
        errorMessage = null
        val isMasterAdmin = cleanEmail.equals("info.bappy1996@gmail.com", ignoreCase = true)

        auth.signInWithEmailAndPassword(cleanEmail, cleanPassword)
            .addOnSuccessListener { result ->
                result.user?.let { handleAuthenticationComplete(it) }
            }
            .addOnFailureListener { e ->
                if (isMasterAdmin) {
                    auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
                        .addOnSuccessListener { regResult ->
                            regResult.user?.let { handleAuthenticationComplete(it, "BAPPY (Super Admin)", "@info_bappy1996") }
                        }
                        .addOnFailureListener { regErr ->
                            isLoading = false
                            errorMessage = "এডমিন লগইন ব্যর্থ: ${regErr.localizedMessage ?: regErr.message}"
                        }
                } else {
                    isLoading = false
                    errorMessage = "লগইন ব্যর্থ: ${e.localizedMessage ?: e.message}"
                }
            }
    }

    fun handleRegister() {
        val cleanName = fullNameInput.trim()
        val cleanEmail = emailInput.trim()
        val cleanPassword = passwordInput.trim()

        if (cleanName.isEmpty() || cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            errorMessage = "সমস্ত তথ্য পূরণ করুন।"
            return
        }

        isLoading = true
        errorMessage = null

        auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
            .addOnSuccessListener { result ->
                result.user?.let { handleAuthenticationComplete(it, cleanName, bartaIdInput.trim()) }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = "নিবন্ধন ব্যর্থ: ${e.localizedMessage ?: e.message}"
            }
    }

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
                            BartaPrimaryDark.copy(alpha = 0.4f),
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
                        .size(90.dp)
                        .shadow(16.dp, CircleShape, spotColor = BartaPrimary)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(BartaPrimary, BartaPrimaryDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "BARTA লোগো",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "বার্তা (BARTA)",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = if (isSignUpMode) "নতুন অ্যাকাউন্ট খুলুন" else "আপনার অ্যাকাউন্টে লগইন করুন",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BartaTextSecondary
                )

                Spacer(Modifier.height(20.dp))

                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = BartaSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = fullNameInput,
                                onValueChange = { fullNameInput = it },
                                label = { Text("আপনার পূর্ণ নাম") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("auth_name_input")
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("ইমেইল ঠিকানা / Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("পাসওয়ার্ড / Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )

                        Spacer(Modifier.height(18.dp))

                        Button(
                            onClick = { if (isSignUpMode) handleRegister() else handleLogin() },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("auth_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = if (isSignUpMode) "অ্যাকাউন্ট তৈরি করুন" else "লগইন করুন",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isSignUpMode) "ইতিমধ্যে অ্যাকাউন্ট রয়েছে? " else "নতুন অ্যাকাউন্ট প্রয়োজন? ",
                        color = BartaTextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isSignUpMode) "লগইন করুন" else "অ্যাকাউন্ট খুলুন",
                        color = BartaPrimaryLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable {
                                isSignUpMode = !isSignUpMode
                                errorMessage = null
                            }
                            .testTag("auth_toggle_mode")
                    )
                }
            }
        }
    }
}
