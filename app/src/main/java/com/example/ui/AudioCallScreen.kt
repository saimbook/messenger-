package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BartaBackground
import com.example.ui.theme.BartaPrimary
import com.example.ui.theme.BartaTextSecondary

@Composable
fun AudioCallScreen(
    partnerName: String,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BartaBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(BartaPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = partnerName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = partnerName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "BARTA ক্রিস্টাল ক্লিয়ার লো-ব্যান্ডউইথ ভয়েস কল",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF00A884)
            )

            Spacer(Modifier.height(60.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isMuted = !isMuted },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isMuted) Color(0xFFE91E63) else Color(0xFF2A3942),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = "মিউট")
                }

                IconButton(
                    onClick = onEndCall,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFFFF3B30),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(70.dp)
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "কল শেষ করুন", modifier = Modifier.size(34.dp))
                }

                IconButton(
                    onClick = { isSpeakerOn = !isSpeakerOn },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isSpeakerOn) BartaPrimary else Color(0xFF2A3942),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "স্পিকার")
                }
            }
        }
    }
}
