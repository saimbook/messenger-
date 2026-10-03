package com.example.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IncomingCallInfo(
    val callId: String,
    val callerId: String,
    val callerName: String,
    val timestamp: Long = System.currentTimeMillis()
)

object WebRtcCallManager {
    private val _incomingCall = MutableStateFlow<IncomingCallInfo?>(null)
    val incomingCall: StateFlow<IncomingCallInfo?> = _incomingCall.asStateFlow()

    fun triggerIncomingCall(callId: String, callerId: String, callerName: String) {
        _incomingCall.value = IncomingCallInfo(callId, callerId, callerName)
    }

    fun endCall() {
        _incomingCall.value = null
    }
}
