package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.WebRtcCallManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = ChatRepository(database.chatDao())
    private val networkQualityManager = NetworkQualityManager(application)
    private val voiceMediaManager = VoiceMediaManager(application)

    val currentUserName = AppSettingsManager.displayName
    val currentUserPhone = AppSettingsManager.phoneNumber
    val networkQuality = networkQualityManager.networkQuality
    val incomingCall = WebRtcCallManager.incomingCall

    private val _dataMode = MutableStateFlow(DataMode.STANDARD)
    val dataMode: StateFlow<DataMode> = _dataMode.asStateFlow()

    private val _activeRoomId = MutableStateFlow<String?>("room_community")
    val activeRoomId: StateFlow<String?> = _activeRoomId.asStateFlow()

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _isCallActive = MutableStateFlow(false)
    val isCallActive: StateFlow<Boolean> = _isCallActive.asStateFlow()

    private val _activeCallName = MutableStateFlow("")
    val activeCallName: StateFlow<String> = _activeCallName.asStateFlow()

    val rooms: StateFlow<List<ChatRoom>> = repository.allRooms.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(
            ChatRoom("room_community", "বার্তা অফিশিয়াল গ্রুপ (BARTA)", "স্বাগতম! দ্রুত ও নিরাপদে যোগাযোগ করুন।", System.currentTimeMillis(), 0, true, 0xFF00A884.toInt(), true, true),
            ChatRoom("room_support", "BARTA সাপোর্ট ও হেল্পডেস্ক", "যেকোনো সহায়তায় আমাদের জানান।", System.currentTimeMillis() - 3600000L, 0, false, 0xFF128C7E.toInt(), true),
            ChatRoom("room_admin", "এডমিন কন্ট্রোল ও নোটিস", "সিস্টেম সংক্রান্ত গুরুত্বপূর্ণ বার্তা।", System.currentTimeMillis() - 7200000L, 1, false, 0xFF00A884.toInt(), true)
        )
    )

    val messages: StateFlow<List<Message>> = _activeRoomId.flatMapLatest { roomId ->
        if (roomId == null) flowOf(emptyList())
        else {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    FirebaseChatManager.observeMessages(roomId).collect { cloudMsgs ->
                        for (msg in cloudMsgs) {
                            repository.insertMessage(msg)
                        }
                    }
                } catch (e: Exception) { }
            }
            repository.getMessagesForRoom(roomId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            networkQualityManager.checkNetworkStatus()
            seedInitialRooms()
        }
    }

    private suspend fun seedInitialRooms() {
        val initialList = listOf(
            ChatRoom("room_community", "বার্তা অফিশিয়াল গ্রুপ (BARTA)", "স্বাগতম! দ্রুত ও নিরাপদে যোগাযোগ করুন।", System.currentTimeMillis(), 0, true, 0xFF00A884.toInt(), true, true),
            ChatRoom("room_support", "BARTA সাপোর্ট ও হেল্পডেস্ক", "যেকোনো সহায়তায় আমাদের জানান।", System.currentTimeMillis() - 3600000L, 0, false, 0xFF128C7E.toInt(), true),
            ChatRoom("room_admin", "এডমিন কন্ট্রোল ও নোটিস", "সিস্টেম সংক্রান্ত গুরুত্বপূর্ণ বার্তা।", System.currentTimeMillis() - 7200000L, 1, false, 0xFF00A884.toInt(), true)
        )
        for (room in initialList) {
            repository.insertRoom(room)
        }
    }

    fun selectRoom(roomId: String) {
        _activeRoomId.value = roomId
    }

    fun sendMessage(text: String) {
        val roomId = _activeRoomId.value ?: return
        if (text.isBlank()) return

        val msg = Message(
            roomId = roomId,
            senderId = currentUserPhone.value,
            senderName = currentUserName.value,
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            isOutgoing = true,
            messageType = "TEXT"
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMessage(msg)
            try {
                FirebaseChatManager.sendMessage(roomId, msg)
            } catch (e: Exception) { }
        }
    }

    fun createCustomRoom(name: String, subtitle: String) {
        val newRoom = ChatRoom(
            id = "room_" + System.currentTimeMillis(),
            name = name.ifBlank { "নতুন যোগাযোগ" },
            lastMessage = subtitle.ifBlank { "চ্যাট শুরু করুন" },
            lastMessageTime = System.currentTimeMillis(),
            avatarColor = 0xFF00A884.toInt()
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRoom(newRoom)
            _activeRoomId.value = newRoom.id
        }
    }

    fun startVoiceRecording() {
        _isRecordingVoice.value = true
        voiceMediaManager.startRecording()
    }

    fun stopVoiceRecording() {
        _isRecordingVoice.value = false
        val file = voiceMediaManager.stopRecording()
        if (file != null && file.exists()) {
            val roomId = _activeRoomId.value ?: return
            val msg = Message(
                roomId = roomId,
                senderId = currentUserPhone.value,
                senderName = currentUserName.value,
                text = "ভয়েস বার্তা (Voice Note)",
                timestamp = System.currentTimeMillis(),
                isOutgoing = true,
                messageType = "AUDIO"
            )
            FirebaseChatManager.sendMessage(roomId, msg)
        }
    }

    fun cancelVoiceRecording() {
        _isRecordingVoice.value = false
        voiceMediaManager.cancelRecording()
    }

    fun startCall(partnerId: String, partnerName: String) {
        _isCallActive.value = true
        _activeCallName.value = partnerName
        WebRtcCallManager.triggerIncomingCall("call_" + System.currentTimeMillis(), partnerId, partnerName)
    }

    fun endActiveCall() {
        _isCallActive.value = false
        _activeCallName.value = ""
        WebRtcCallManager.endCall()
    }

    fun setDataMode(mode: DataMode) {
        _dataMode.value = mode
    }
}
