package com.tempo.app.data.preferences

import com.tempo.app.domain.model.ThemeMode

data class UserPreferences(
    val displayName: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val backupFolderUri: String? = null,
    val backupDailyEnabled: Boolean = false,
    val backupHour: Int = 21,
    val backupMinute: Int = 0,
    val lastBackupAtMillis: Long? = null,
)
