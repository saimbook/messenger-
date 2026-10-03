package com.example.data.admin

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object AdminAuthorityManager {
    private const val TAG = "AdminAuthorityManager"

    private val _currentAdmin = MutableStateFlow<AdminUser?>(null)
    val currentAdmin: StateFlow<AdminUser?> = _currentAdmin.asStateFlow()

    private val _reports = MutableStateFlow<List<BartaReport>>(emptyList())
    val reports: StateFlow<List<BartaReport>> = _reports.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    private val _managedUsers = MutableStateFlow<List<AdminManagedUser>>(emptyList())
    val managedUsers: StateFlow<List<AdminManagedUser>> = _managedUsers.asStateFlow()

    private val _systemStats = MutableStateFlow(SystemOverviewStats())
    val systemStats: StateFlow<SystemOverviewStats> = _systemStats.asStateFlow()

    private val _systemBroadcasts = MutableStateFlow<List<SystemBroadcastMessage>>(emptyList())
    val systemBroadcasts: StateFlow<List<SystemBroadcastMessage>> = _systemBroadcasts.asStateFlow()

    private var database: FirebaseDatabase? = null
    private var auth: FirebaseAuth? = null

    fun initialize(context: Context) {
        try {
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance()
            loadMockOrRealData()
        } catch (e: Exception) {
            Log.e(TAG, "AdminAuthorityManager init error: ${e.message}")
        }
    }

    private fun loadMockOrRealData() {
        _managedUsers.value = listOf(
            AdminManagedUser("master_admin", "BAPPY (Super Admin)", "info.bappy1996@gmail.com", "SUPER_ADMIN", "ACTIVE", "Bangladesh", isOnline = true),
            AdminManagedUser("user_101", "তানভীর আহমেদ", "+8801711223344", "USER", "ACTIVE", "Bangladesh", isOnline = true),
            AdminManagedUser("user_102", "সাদিয়া রহমান", "+8801822334455", "USER", "ACTIVE", "Bangladesh", isOnline = false),
            AdminManagedUser("driver_201", "কামাল হোসেন (রাইডার/ড্রাইভার)", "+8801933445566", "DRIVER", "ACTIVE", "Bangladesh", isOnline = true),
            AdminManagedUser("user_103", "রাকিবুল হাসান", "+8801644556677", "USER", "SUSPENDED", "Bangladesh", isOnline = false)
        )

        _reports.value = listOf(
            BartaReport("rep_1", "user_102", "সাদিয়া রহমান", "MESSAGE", "msg_99", "user_103", "রাকিবুল হাসান", "স্প্যাম ও অনাকাঙ্ক্ষিত লিংক পাঠানো হচ্ছে", "SPAM_HARASSMENT", System.currentTimeMillis() - 3600000L, "OPEN"),
            BartaReport("rep_2", "user_101", "তানভীর আহমেদ", "PROFILE", "user_103", "user_103", "রাকিবুল হাসান", "সন্দেহজনক ভুয়া পরিচয়পত্র", "FAKE_IDENTITY", System.currentTimeMillis() - 7200000L, "OPEN")
        )

        _auditLogs.value = listOf(
            AdminAuditLog("log_1", "master_admin", "info.bappy1996@gmail.com", "LOGIN_SUCCESS", "SYSTEM", "SECURITY", "সুপার এডমিন কনসোলে সফল প্রবেশ", System.currentTimeMillis() - 1800000L),
            AdminAuditLog("log_2", "master_admin", "info.bappy1996@gmail.com", "FEATURE_UPDATED", "FEATURE", "AI_CHAT_ASSISTANT", "এআই অ্যাসিস্ট্যান্ট ফিচার সফলভাবে সক্রিয় করা হয়েছে", System.currentTimeMillis() - 900000L)
        )
    }

    fun registerMasterSuperAdmin(uid: String, name: String, email: String): AdminUser {
        try {
            val dbInstance = database ?: FirebaseDatabase.getInstance()
            val adminData = mapOf(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "role" to "SUPER_ADMIN",
                "createdAt" to ServerValue.TIMESTAMP,
                "status" to "ACTIVE"
            )
            dbInstance.getReference("admins").child(uid).updateChildren(adminData)
            dbInstance.getReference("admins").child("info_bappy1996_gmail_com").updateChildren(adminData)
            dbInstance.getReference("users").child(uid).child("role").setValue("SUPER_ADMIN")
        } catch (e: Exception) {
            Log.e(TAG, "registerMasterSuperAdmin warning: ${e.message}")
        }

        val userObj = AdminUser(
            uid = uid,
            email = email,
            displayName = name,
            role = AdminRole.SUPER_ADMIN,
            isMfaVerified = true
        )
        _currentAdmin.value = userObj
        return userObj
    }

    fun updateUserRole(targetUid: String, newRole: String) {
        try {
            val dbInstance = database ?: FirebaseDatabase.getInstance()
            dbInstance.getReference("users").child(targetUid).child("role").setValue(newRole)
            if (newRole == "SUPER_ADMIN" || newRole == "ADMIN" || newRole == "MODERATOR") {
                dbInstance.getReference("admins").child(targetUid).child("role").setValue(newRole)
            } else {
                dbInstance.getReference("admins").child(targetUid).removeValue()
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateUserRole error: ${e.message}")
        }

        _managedUsers.value = _managedUsers.value.map {
            if (it.uid == targetUid) it.copy(role = newRole) else it
        }
    }

    fun updateUserStatus(targetUid: String, newStatus: String) {
        try {
            val dbInstance = database ?: FirebaseDatabase.getInstance()
            dbInstance.getReference("users").child(targetUid).child("accountStatus").setValue(newStatus)
        } catch (e: Exception) {
            Log.e(TAG, "updateUserStatus error: ${e.message}")
        }

        _managedUsers.value = _managedUsers.value.map {
            if (it.uid == targetUid) it.copy(accountStatus = newStatus) else it
        }
    }

    fun resolveReport(reportId: String, actionTaken: String) {
        try {
            val dbInstance = database ?: FirebaseDatabase.getInstance()
            dbInstance.getReference("reports").child(reportId).child("status").setValue("RESOLVED")
        } catch (e: Exception) {
            Log.e(TAG, "resolveReport error: ${e.message}")
        }

        _reports.value = _reports.value.map {
            if (it.reportId == reportId) it.copy(status = "RESOLVED") else it
        }
    }

    fun sendBroadcastNotification(title: String, message: String, audience: String, isEmergency: Boolean = false) {
        val item = SystemBroadcastMessage(
            title = title,
            body = message,
            targetAudience = audience,
            isEmergency = isEmergency
        )
        try {
            val dbInstance = database ?: FirebaseDatabase.getInstance()
            dbInstance.getReference("systemBroadcasts").child(item.id).setValue(item)
        } catch (e: Exception) {
            Log.e(TAG, "sendBroadcast error: ${e.message}")
        }

        _systemBroadcasts.value = listOf(item) + _systemBroadcasts.value
    }
}
