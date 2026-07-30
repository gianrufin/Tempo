package com.tempo.app.data.preferences

import com.tempo.app.domain.model.ThemeMode

data class UserPreferences(
    val displayName: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
)
