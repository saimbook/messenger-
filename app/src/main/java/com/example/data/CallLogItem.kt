package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "call_logs")
data class CallLogItem(
    @PrimaryKey
    val callId: String = java.util.UUID.randomUUID().toString(),
    val partnerId: String = "",
    val partnerName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val isOutgoing: Boolean = true,
    val isMissed: Boolean = false,
    val callType: String = "AUDIO" // AUDIO, VIDEO
)
