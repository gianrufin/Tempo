package com.tempo.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tempo.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private object PreferenceKeys {
    val DISPLAY_NAME = stringPreferencesKey("display_name")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
}

class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            displayName = prefs[PreferenceKeys.DISPLAY_NAME] ?: "",
            themeMode = prefs[PreferenceKeys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            dynamicColorEnabled = prefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED] ?: true,
        )
    }

    suspend fun setDisplayName(name: String) {
        dataStore.edit { it[PreferenceKeys.DISPLAY_NAME] = name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[PreferenceKeys.THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.DYNAMIC_COLOR_ENABLED] = enabled }
    }
}
