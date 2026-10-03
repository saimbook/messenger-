package com.example.data

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirebaseChatManager {
    const val FIREBASE_DATABASE_URL = "https://barta-messenger-prod-default-rtdb.firebaseio.com"

    private var currentUserId: String = ""
    private var currentUserName: String = ""
    private var currentUserEmail: String = ""

    fun setCurrentUser(uid: String, name: String, email: String) {
        currentUserId = uid
        currentUserName = name
        currentUserEmail = email
    }

    fun observeMessages(roomId: String): Flow<List<Message>> = callbackFlow {
        val database = try { FirebaseDatabase.getInstance() } catch (e: Exception) { null }
        val ref = database?.getReference("messages")?.child(roomId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (child in snapshot.children) {
                    val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                    val senderId = child.child("senderId").getValue(String::class.java) ?: ""
                    val senderName = child.child("senderName").getValue(String::class.java) ?: ""
                    val text = child.child("text").getValue(String::class.java) ?: ""
                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                    val messageType = child.child("messageType").getValue(String::class.java) ?: "TEXT"
                    val mediaUrl = child.child("mediaUrl").getValue(String::class.java)
                    val isOutgoing = senderId == currentUserId

                    messages.add(
                        Message(
                            id = id,
                            roomId = roomId,
                            senderId = senderId,
                            senderName = senderName,
                            text = text,
                            timestamp = timestamp,
                            isOutgoing = isOutgoing,
                            messageType = messageType,
                            mediaUrl = mediaUrl
                        )
                    )
                }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                // Ignore or log
            }
        }

        ref?.addValueEventListener(listener)
        awaitClose { ref?.removeEventListener(listener) }
    }

    fun sendMessage(roomId: String, message: Message) {
        val database = try { FirebaseDatabase.getInstance() } catch (e: Exception) { null }
        val ref = database?.getReference("messages")?.child(roomId)?.child(message.id)
        val data = mapOf(
            "id" to message.id,
            "roomId" to roomId,
            "senderId" to message.senderId,
            "senderName" to message.senderName,
            "text" to message.text,
            "timestamp" to message.timestamp,
            "messageType" to message.messageType,
            "mediaUrl" to message.mediaUrl
        )
        ref?.setValue(data)
    }
}
