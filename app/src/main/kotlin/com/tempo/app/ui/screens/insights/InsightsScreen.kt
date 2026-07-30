package com.tempo.app.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tempo.app.domain.model.HabitInsight
import com.tempo.app.domain.model.InsightsPeriod
import com.tempo.app.domain.model.InsightsSummary
import com.tempo.app.ui.components.shareCsv
import com.tempo.app.ui.theme.TempoExtraShapes
import kotlinx.coroutines.launch

@Composable
fun InsightsScreen(modifier: Modifier = Modifier, viewModel: InsightsViewModel = hiltViewModel()) {
    val period by viewModel.period.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InsightsPeriod.entries.forEach { p ->
                FilterChip(
                    selected = period == p,
                    onClick = { viewModel.onPeriodChange(p) },
                    label = { Text(p.label) },
                    shape = TempoExtraShapes.pill,
                )
            }
        }

        val currentSummary = summary
        if (currentSummary == null) {
            Text("No data yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            OverallCard(summary = currentSummary)

            if (currentSummary.habitInsights.isEmpty()) {
                Text("Add a habit to see insights here.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(currentSummary.habitInsights, key = { it.habit.id }) { insight ->
                        HabitInsightRow(insight)
                    }
                }
            }
        }

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
}

@Composable
private fun OverallCard(summary: InsightsSummary) {
    Surface(shape = TempoExtraShapes.card, color = MaterialTheme.colorScheme.primaryContainer) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "${summary.overallRatePercent}%",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "Overall completion, ${summary.period.label.lowercase()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun HabitInsightRow(insight: HabitInsight) {
    Surface(shape = TempoExtraShapes.card, color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "${insight.habit.icon}  ${insight.habit.name}", style = MaterialTheme.typography.titleMedium)
                Text(text = "${insight.completionRatePercent}%", style = MaterialTheme.typography.titleMedium)
            }
            ProgressBar(fraction = insight.completionRatePercent / 100f)
            Text(
                text = "${insight.doneCount + insight.excusedCount} / ${insight.scheduledCount} completed",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ProgressBar(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(TempoExtraShapes.pill)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(10.dp)
                .clip(TempoExtraShapes.pill)
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}
