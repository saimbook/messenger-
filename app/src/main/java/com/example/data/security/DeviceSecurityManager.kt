package com.example.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object DeviceSecurityManager {
    private const val PREFS_NAME = "barta_device_security_prefs"

    fun enrollCurrentDevice(context: Context, uid: String) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            prefs.edit()
                .putString("enrolled_uid", uid)
                .putLong("enrollment_time", System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            // Fallback for non-standard keystores
            val fallbackPrefs = context.getSharedPreferences("barta_sec_fallback", Context.MODE_PRIVATE)
            fallbackPrefs.edit()
                .putString("enrolled_uid", uid)
                .putLong("enrollment_time", System.currentTimeMillis())
                .apply()
        }
    }
}
