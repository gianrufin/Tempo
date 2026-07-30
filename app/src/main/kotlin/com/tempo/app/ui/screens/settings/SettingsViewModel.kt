package com.tempo.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.app.data.preferences.PreferencesRepository
import com.tempo.app.data.preferences.UserPreferences
import com.tempo.app.data.repository.HabitRepository
import com.tempo.app.data.update.UpdateCheckResult
import com.tempo.app.data.update.UpdateChecker
import com.tempo.app.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpdateCheckUiState(
    val isChecking: Boolean = false,
    val result: UpdateCheckResult? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val habitRepository: HabitRepository,
    private val updateChecker: UpdateChecker,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    private val _updateCheckState = MutableStateFlow(UpdateCheckUiState())
    val updateCheckState = _updateCheckState.asStateFlow()

    fun onDisplayNameChange(name: String) {
        viewModelScope.launch { preferencesRepository.setDisplayName(name) }
    }

    fun onThemeModeChange(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun onDynamicColorChange(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDynamicColorEnabled(enabled) }
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            _updateCheckState.value = UpdateCheckUiState(isChecking = true)
            val result = updateChecker.checkForUpdate()
            _updateCheckState.value = UpdateCheckUiState(isChecking = false, result = result)
        }
    }

    suspend fun exportCsv(): String = habitRepository.exportAllCompletionsCsv()
}
