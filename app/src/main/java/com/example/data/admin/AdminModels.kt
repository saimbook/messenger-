package com.example.data.admin

enum class AdminRole {
    USER,
    DRIVER,
    MODERATOR,
    ADMIN,
    SUPER_ADMIN;

    fun canManageUsers(): Boolean = this == ADMIN || this == SUPER_ADMIN
    fun canReviewReports(): Boolean = this == MODERATOR || this == ADMIN || this == SUPER_ADMIN
    fun canExportData(): Boolean = this == ADMIN || this == SUPER_ADMIN
    fun canManageRoles(): Boolean = this == SUPER_ADMIN
    fun canAccessSecurityConfig(): Boolean = this == SUPER_ADMIN
    fun canAccessAdminDashboard(): Boolean = this == MODERATOR || this == ADMIN || this == SUPER_ADMIN
    fun canAccessDriverDashboard(): Boolean = this == DRIVER
    fun canManageDrivers(): Boolean = this == ADMIN || this == SUPER_ADMIN
    fun canAccessIpLogs(): Boolean = this == ADMIN || this == SUPER_ADMIN
    fun canAccessGlobalMap(): Boolean = this == MODERATOR || this == ADMIN || this == SUPER_ADMIN
    fun canToggleAppFeatures(): Boolean = this == SUPER_ADMIN
    fun canSendSystemBroadcasts(): Boolean = this == SUPER_ADMIN || this == ADMIN
}

data class AdminUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: AdminRole,
    val lastLoginTimestamp: Long = System.currentTimeMillis(),
    val isMfaVerified: Boolean = false
)

data class BartaReport(
    val reportId: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val targetType: String = "MESSAGE",
    val targetId: String = "",
    val targetSenderId: String = "",
    val targetSenderName: String = "",
    val reportedContentSnippet: String = "",
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "OPEN" // OPEN, RESOLVED, DISMISSED
)

data class AdminAuditLog(
    val logId: String = "",
    val adminUid: String = "",
    val adminEmail: String = "",
    val action: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String = ""
)

data class AdminManagedUser(
    val uid: String,
    val displayName: String,
    val phoneNumber: String,
    val role: String = "USER",
    val accountStatus: String = "ACTIVE", // ACTIVE, SUSPENDED, DISABLED, BLOCKED
    val country: String = "Bangladesh",
    val createdAt: Long = System.currentTimeMillis() - 86400000L * 15,
    val isOnline: Boolean = true
)

data class SystemOverviewStats(
    val totalUsers: Int = 1240,
    val activeToday: Int = 890,
    val totalMessagesSent: Long = 48520L,
    val totalCallsMade: Long = 6210L,
    val totalReports: Int = 3,
    val activeDrivers: Int = 18,
    val serverStatus: String = "ALL_SYSTEMS_OPERATIONAL"
)

data class AppFeatureItem(
    val featureKey: String,
    val nameBn: String,
    val descBn: String,
    val isEnabled: Boolean = true,
    val category: String = "GENERAL" // GENERAL, MEDIA, CALLS, SECURITY, AI
)

data class SystemBroadcastMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
    val targetAudience: String = "ALL", // ALL, USERS, DRIVERS
    val timestamp: Long = System.currentTimeMillis(),
    val isEmergency: Boolean = false
)
