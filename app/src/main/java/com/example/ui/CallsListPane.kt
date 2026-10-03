package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatRoom
import com.example.ui.theme.BartaPrimary
import com.example.ui.theme.BartaTextSecondary

@Composable
fun CallsListPane(
    rooms: List<ChatRoom>,
    onStartCall: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        items(
            items = rooms,
            key = { it.id }
        ) { room ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartCall(room.id, room.avatarColor) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(room.avatarColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = room.name.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CallMade, contentDescription = null, tint = Color(0xFF00A884), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "আউটগোয়িং ভয়েস কল • এইচডি অডিও",
                            style = MaterialTheme.typography.bodySmall,
                            color = BartaTextSecondary
                        )
                    }
                }

                IconButton(onClick = { onStartCall(room.id, room.avatarColor) }) {
                    Icon(Icons.Default.Call, contentDescription = "কল করুন", tint = BartaPrimary)
                }
            }
            HorizontalDivider(color = Color(0xFF2A3942).copy(alpha = 0.5f))
        }
    }
}
