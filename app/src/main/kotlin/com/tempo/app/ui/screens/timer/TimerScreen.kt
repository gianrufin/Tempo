package com.tempo.app.ui.screens.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tempo.app.ui.theme.TempoExtraShapes

@Composable
fun TimerScreen(modifier: Modifier = Modifier, viewModel: TimerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(text = "Timer", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimerMode.entries.forEach { mode ->
                FilterChip(
                    selected = state.mode == mode,
                    onClick = { viewModel.onSelectMode(mode) },
                    label = { Text(mode.label) },
                    shape = TempoExtraShapes.pill,
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (state.mode) {
                TimerMode.POMODORO -> PomodoroContent(state, viewModel)
                TimerMode.STOPWATCH -> StopwatchContent(state, viewModel)
                TimerMode.COUNTDOWN -> CountdownContent(state, viewModel)
            }
        }
    }
}

@Composable
private fun PomodoroContent(state: TimerUiState, viewModel: TimerViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(
            text = if (state.pomodoroIsBreak) "Break" else "Focus",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        TimeDisplay(seconds = state.pomodoroRemainingSeconds)
        TimerControls(isRunning = state.isRunning, onStart = viewModel::start, onPause = viewModel::pause, onReset = viewModel::reset)
    }
}

@Composable
private fun StopwatchContent(state: TimerUiState, viewModel: TimerViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        TimeDisplay(seconds = state.stopwatchElapsedSeconds)
        TimerControls(isRunning = state.isRunning, onStart = viewModel::start, onPause = viewModel::pause, onReset = viewModel::reset)
    }
}

@Composable
private fun CountdownContent(state: TimerUiState, viewModel: TimerViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        if (!state.isRunning) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = { viewModel.onAdjustCountdownMinutes(-1) }, shape = TempoExtraShapes.pill) {
                    Icon(Icons.Filled.Remove, contentDescription = "Fewer minutes")
                }
                Text("${state.countdownSetMinutes} min", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { viewModel.onAdjustCountdownMinutes(1) }, shape = TempoExtraShapes.pill) {
                    Icon(Icons.Filled.Add, contentDescription = "More minutes")
                }
            }
        }
        TimeDisplay(seconds = state.countdownRemainingSeconds)
        TimerControls(isRunning = state.isRunning, onStart = viewModel::start, onPause = viewModel::pause, onReset = viewModel::reset)
    }
}

@Composable
private fun TimeDisplay(seconds: Int) {
    val minutes = seconds / 60
    val secs = seconds % 60
    Surface(shape = TempoExtraShapes.pill, color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            text = "%02d:%02d".format(minutes, secs),
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 24.dp),
        )
    }
}

@Composable
private fun TimerControls(isRunning: Boolean, onStart: () -> Unit, onPause: () -> Unit, onReset: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedButton(onClick = onReset, shape = TempoExtraShapes.pill) {
            Icon(Icons.Filled.Replay, contentDescription = "Reset")
        }
        Button(
            onClick = if (isRunning) onPause else onStart,
            shape = TempoExtraShapes.pill,
            modifier = Modifier.fillMaxWidth(0.5f),
        ) {
            Icon(if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = if (isRunning) "Pause" else "Start")
            Text(if (isRunning) "  Pause" else "  Start")
        }
    }
}
