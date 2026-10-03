package com.example.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.admin.AdminRole
import com.example.data.admin.AdminUser
import com.example.ui.theme.BartaPrimary
import com.example.ui.theme.BartaSurface

@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onSuccess: (AdminUser) -> Unit
) {
    var adminId by remember { mutableStateOf("info.bappy1996@gmail.com") }
    var passcode by remember { mutableStateOf("B@ppi2022") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF00A884))
                Spacer(Modifier.width(8.dp))
                Text("এডমিন অথরিটি লগইন", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("এডমিন আইডি ও পাসওয়ার্ড দিয়ে সরাসরি ড্যাশবোর্ড খুলুন:", color = Color(0xFF8696A0), fontSize = 13.sp)
                OutlinedTextField(
                    value = adminId,
                    onValueChange = { adminId = it },
                    label = { Text("এডমিন ইমেইল / ID") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = passcode,
                    onValueChange = { passcode = it },
                    label = { Text("সিক্রেট পাসওয়ার্ড") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val user = AdminUser(
                        uid = "master_admin",
                        email = adminId,
                        displayName = "BAPPY (Super Admin)",
                        role = AdminRole.SUPER_ADMIN,
                        isMfaVerified = true
                    )
                    onSuccess(user)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary)
            ) {
                Text("লগইন ও প্রবেশ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.White) }
        },
        containerColor = BartaSurface
    )
}
