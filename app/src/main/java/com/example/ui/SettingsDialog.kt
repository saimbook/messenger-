package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DataMode
import com.example.ui.theme.BartaPrimary
import com.example.ui.theme.BartaSurface

@Composable
fun SettingsDialog(
    currentDataMode: DataMode,
    onDataModeChange: (DataMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("দ্রুত ডেটা সেটিংস", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DataMode.values().forEach { mode ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentDataMode == mode) Color(0xFF00A884).copy(alpha = 0.2f) else Color(0xFF2A3942)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        onClick = { onDataModeChange(mode) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(mode.titleBn, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(mode.descBn, color = Color(0xFF8696A0), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = BartaPrimary)) {
                Text("সম্পন্ন")
            }
        },
        containerColor = BartaSurface
    )
}
