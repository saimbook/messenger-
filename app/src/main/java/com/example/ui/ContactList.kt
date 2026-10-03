package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatRoom
import com.example.ui.theme.BartaPrimary
import com.example.ui.theme.BartaTextSecondary

@Composable
fun ContactList(
    rooms: List<ChatRoom>,
    activeRoomId: String?,
    onSelectConversation: (ChatRoom) -> Unit,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null
) {
    if (rooms.isEmpty()) {
        emptyContent?.invoke()
    } else {
        LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            items(
                items = rooms,
                key = { it.id }
            ) { room ->
                val isSelected = room.id == activeRoomId
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF2A3942) else Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { onSelectConversation(room) }
                        .testTag("chat_room_${room.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(room.avatarColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (room.isGroup) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = Color.White)
                            } else {
                                Text(
                                    text = room.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = room.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (room.isPinned) {
                                    Icon(Icons.Default.PushPin, contentDescription = null, tint = BartaPrimary, modifier = Modifier.size(14.dp))
                                }
                            }

                            Spacer(Modifier.height(2.dp))

                            Text(
                                text = room.lastMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = BartaTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (room.unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = BartaPrimary
                            ) {
                                Text(
                                    text = room.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
