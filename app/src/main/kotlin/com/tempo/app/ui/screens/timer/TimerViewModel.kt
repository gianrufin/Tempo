package com.tempo.app.ui.screens.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimerMode(val label: String) {
    POMODORO("Pomodoro"),
    STOPWATCH("Stopwatch"),
    COUNTDOWN("Timer"),
}

data class TimerUiState(
    val mode: TimerMode = TimerMode.POMODORO,
    val isRunning: Boolean = false,
    val pomodoroWorkMinutes: Int = 25,
    val pomodoroBreakMinutes: Int = 5,
    val pomodoroRemainingSeconds: Int = 25 * 60,
    val pomodoroIsBreak: Boolean = false,
    val stopwatchElapsedSeconds: Int = 0,
    val countdownSetMinutes: Int = 10,
    val countdownRemainingSeconds: Int = 10 * 60,
)

@HiltViewModel
class TimerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    fun onSelectMode(mode: TimerMode) {
        pause()
        _uiState.value = _uiState.value.copy(mode = mode)
    }

    fun start() {
        if (_uiState.value.isRunning) return
        _uiState.value = _uiState.value.copy(isRunning = true)
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                tick()
            }
        }
    }

    fun pause() {
        tickerJob?.cancel()
        tickerJob = null
        if (_uiState.value.isRunning) {
            _uiState.value = _uiState.value.copy(isRunning = false)
        }
    }

    fun reset() {
        pause()
        val state = _uiState.value
        _uiState.value = when (state.mode) {
            TimerMode.POMODORO -> state.copy(
                pomodoroRemainingSeconds = state.pomodoroWorkMinutes * 60,
                pomodoroIsBreak = false,
            )
            TimerMode.STOPWATCH -> state.copy(stopwatchElapsedSeconds = 0)
            TimerMode.COUNTDOWN -> state.copy(countdownRemainingSeconds = state.countdownSetMinutes * 60)
        }
    }

    fun onAdjustCountdownMinutes(deltaMinutes: Int) {
        val state = _uiState.value
        if (state.isRunning) return
        val newMinutes = (state.countdownSetMinutes + deltaMinutes).coerceIn(1, 180)
        _uiState.value = state.copy(countdownSetMinutes = newMinutes, countdownRemainingSeconds = newMinutes * 60)
    }

    private fun tick() {
        val state = _uiState.value
        _uiState.value = when (state.mode) {
            TimerMode.POMODORO -> {
                if (state.pomodoroRemainingSeconds <= 1) {
                    val nowBreak = !state.pomodoroIsBreak
                    state.copy(
                        pomodoroIsBreak = nowBreak,
                        pomodoroRemainingSeconds = if (nowBreak) state.pomodoroBreakMinutes * 60 else state.pomodoroWorkMinutes * 60,
                    )
                } else {
                    state.copy(pomodoroRemainingSeconds = state.pomodoroRemainingSeconds - 1)
                }
            }
            TimerMode.STOPWATCH -> state.copy(stopwatchElapsedSeconds = state.stopwatchElapsedSeconds + 1)
            TimerMode.COUNTDOWN -> {
                if (state.countdownRemainingSeconds <= 1) {
                    tickerJob?.cancel()
                    tickerJob = null
                    state.copy(countdownRemainingSeconds = 0, isRunning = false)
                } else {
                    state.copy(countdownRemainingSeconds = state.countdownRemainingSeconds - 1)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
