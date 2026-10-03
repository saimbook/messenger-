package com.example.network

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri

object CallRingtoneManager {
    private var activeRingtone: Ringtone? = null

    fun startRinging(context: Context, callId: String, callerName: String) {
        try {
            stopRinging()
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, notification)
            ringtone?.play()
            activeRingtone = ringtone
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun stopRinging() {
        try {
            activeRingtone?.stop()
            activeRingtone = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}
