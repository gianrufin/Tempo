package com.tempo.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.tempo.app.BuildConfig
import com.tempo.app.data.update.UpdateChecker
import com.tempo.app.domain.model.ThemeMode
import com.tempo.app.ui.components.shareCsv
import com.tempo.app.ui.theme.TempoExtraShapes
import kotlinx.coroutines.launch

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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
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
