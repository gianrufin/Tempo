package com.tempo.app.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.tempo.app.BuildConfig
import com.tempo.app.data.preferences.UserPreferences
import com.tempo.app.data.update.UpdateChecker
import com.tempo.app.domain.model.ThemeMode
import com.tempo.app.ui.components.restartApp
import com.tempo.app.ui.components.shareCsv
import com.tempo.app.ui.theme.TempoExtraShapes
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val prefs by viewModel.preferences.collectAsState()
    val updateState by viewModel.updateCheckState.collectAsState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    var nameField by remember(prefs.displayName) { mutableStateOf(prefs.displayName) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineSmall)

        SettingsSection(title = "Profile") {
            OutlinedTextField(
                value = nameField,
                onValueChange = {
                    nameField = it
                    viewModel.onDisplayNameChange(it)
                },
                label = { Text("Your name") },
                shape = TempoExtraShapes.card,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SettingsSection(title = "Appearance") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = prefs.themeMode == mode,
                            onClick = { viewModel.onThemeModeChange(mode) },
                            label = { Text(mode.label) },
                            shape = TempoExtraShapes.pill,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Dynamic color (Material You)", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = prefs.dynamicColorEnabled, onCheckedChange = viewModel::onDynamicColorChange)
                }
            }
        }

        SettingsSection(title = "Data") {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val csv = viewModel.exportCsv()
                        shareCsv(context, csv)
                    }
                },
                shape = TempoExtraShapes.pill,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.FileDownload, contentDescription = null)
                Text("  Export history as CSV")
            }
        }

        SettingsSection(title = "Backup") {
            BackupSection(viewModel = viewModel, prefs = prefs)
        }

        SettingsSection(title = "Updates") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = viewModel::checkForUpdate,
                    enabled = !updateState.isChecking,
                    shape = TempoExtraShapes.pill,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (updateState.isChecking) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Filled.SystemUpdate, contentDescription = null)
                    }
                    Text("  Check for updates")
                }

                updateState.result?.let { result ->
                    Surface(shape = TempoExtraShapes.card, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = if (result.upToDate) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = if (result.upToDate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                )
                                Text(
                                    text = if (result.upToDate) "You're on the latest build" else "A newer build is available",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            Text(
                                text = "Installed: ${result.currentSha}" +
                                    (result.latestSha?.let { " · Latest: ${it.take(12)}" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            result.error?.let {
                                Text("Couldn't check: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                            if (!result.upToDate) {
                                OutlinedButton(
                                    onClick = { uriHandler.openUri(UpdateChecker.RELEASES_URL) },
                                    shape = TempoExtraShapes.pill,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("View latest release")
                                }
                            }
                        }
                    }
                }
            }
        }

        SettingsSection(title = "About") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tempo ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyLarge)
                Text("Build ${BuildConfig.GIT_SHA}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun BackupSection(viewModel: SettingsViewModel, prefs: UserPreferences) {
    val backupState by viewModel.backupState.collectAsState()
    val restoreState by viewModel.restoreState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val timeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }
    val context = LocalContext.current

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
            viewModel.onBackupFolderSelected(uri)
        }
    }

    val restoreFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pendingRestoreUri = uri
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Back up to a local folder or a Google Drive folder — Drive shows up as a normal " +
                "destination in the picker below, no sign-in setup needed here.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedButton(
            onClick = { folderPicker.launch(null) },
            shape = TempoExtraShapes.pill,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (prefs.backupFolderUri != null) "Change backup folder" else "Choose backup folder")
        }

        if (prefs.backupFolderUri != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Daily automatic backup", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = prefs.backupDailyEnabled, onCheckedChange = viewModel::onBackupDailyEnabledChange)
            }

            if (prefs.backupDailyEnabled) {
                OutlinedButton(onClick = { showTimePicker = true }, shape = TempoExtraShapes.pill) {
                    val time = java.time.LocalTime.of(prefs.backupHour, prefs.backupMinute)
                    Text("Backup time: ${time.format(timeFormatter)}")
                }
            }

            Button(
                onClick = viewModel::backupNow,
                enabled = !backupState.isRunning,
                shape = TempoExtraShapes.pill,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (backupState.isRunning) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null)
                }
                Text("  Back up now")
            }

            prefs.lastBackupAtMillis?.let { millis ->
                val lastBackup = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                Text(
                    "Last backup: ${lastBackup.toLocalDate()} ${lastBackup.toLocalTime().format(timeFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            backupState.lastResultMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }

        OutlinedButton(
            onClick = { restoreFilePicker.launch(arrayOf("*/*")) },
            enabled = !restoreState.isRunning,
            shape = TempoExtraShapes.pill,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (restoreState.isRunning) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            } else {
                Icon(Icons.Filled.CloudDownload, contentDescription = null)
            }
            Text("  Restore from backup file")
        }
        restoreState.errorMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }

    pendingRestoreUri?.let { uri ->
        RestoreConfirmDialog(
            onDismiss = { pendingRestoreUri = null },
            onConfirm = {
                pendingRestoreUri = null
                viewModel.restoreFrom(uri)
            },
        )
    }

    if (restoreState.restoredSuccessfully) {
        RestoreCompleteDialog(onRestart = { restartApp(context) })
    }

    if (showTimePicker) {
        BackupTimePickerDialog(
            initialHour = prefs.backupHour,
            initialMinute = prefs.backupMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                viewModel.onBackupTimeChange(hour, minute)
                showTimePicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = false)
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = TempoExtraShapes.card, color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TimePicker(state = state)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onConfirm(state.hour, state.minute) }, shape = TempoExtraShapes.pill) { Text("Set") }
                }
            }
        }
    }
}

@Composable
private fun RestoreConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = TempoExtraShapes.card, color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Restore from backup?", style = MaterialTheme.typography.titleMedium)
                Text(
                    "This replaces every habit, task, routine, mood entry, and goal currently in " +
                        "Tempo with what's in the backup file. This can't be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = onConfirm, shape = TempoExtraShapes.pill) { Text("Restore") }
                }
            }
        }
    }
}

@Composable
private fun RestoreCompleteDialog(onRestart: () -> Unit) {
    Dialog(onDismissRequest = {}) {
        Surface(shape = TempoExtraShapes.card, color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Backup restored", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Tempo needs to restart to load the restored data.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = onRestart, shape = TempoExtraShapes.pill, modifier = Modifier.fillMaxWidth()) {
                    Text("Restart now")
                }
            }
        }
    }
}
