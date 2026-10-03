package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "chat_rooms")
data class ChatRoom(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val avatarColor: Int = 0xFF128C7E.toInt(),
    val isOnline: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
)
