package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["roomId"]),
        Index(value = ["timestamp"])
    ]
)
data class Message(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val roomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean = true,
    val isDelivered: Boolean = true,
    val isRead: Boolean = false,
    val messageType: String = "TEXT", // TEXT, IMAGE, AUDIO, FILE
    val mediaUrl: String? = null,
    val durationSeconds: Int = 0,
    val isEncrypted: Boolean = true
)
