package com.tempo.app.ui.screens.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tempo.app.domain.model.HabitCompletionStatus
import com.tempo.app.domain.model.HabitWithTodayStatus
import com.tempo.app.ui.theme.TempoExtraShapes

@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    onAddHabit: () -> Unit = {},
    onEditHabit: (Long) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val habits by viewModel.habits.collectAsState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddHabit,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New habit") },
                shape = TempoExtraShapes.pill,
            )
        },
    ) { innerPadding ->
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No habits yet — tap \"New habit\" to add your first one.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, innerPadding.calculateTopPadding(), 16.dp, innerPadding.calculateBottomPadding() + 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(habits, key = { it.habit.id }) { item ->
                    HabitRow(
                        item = item,
                        onToggle = { viewModel.onToggleHabit(item.habit.id) },
                        onClick = { onEditHabit(item.habit.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitRow(
    item: HabitWithTodayStatus,
    onToggle: () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = TempoExtraShapes.card,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusBadge(status = item.status, isOverdue = item.isOverdue, onClick = onToggle)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.habit.icon}  ${item.habit.name}",
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (item.status == HabitCompletionStatus.DONE) TextDecoration.LineThrough else null,
                )
                if (item.isOverdue) {
                    Text(
                        text = "Overdue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (item.currentStreak > 0) {
                StreakBadge(streak = item.currentStreak)
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: HabitCompletionStatus?,
    isOverdue: Boolean,
    onClick: () -> Unit,
) {
    val (containerColor, icon) = when (status) {
        HabitCompletionStatus.DONE -> MaterialTheme.colorScheme.primary to Icons.Filled.Check
        HabitCompletionStatus.SKIPPED_EXCUSED -> MaterialTheme.colorScheme.tertiaryContainer to Icons.Filled.AcUnit
        HabitCompletionStatus.MISSED, null -> MaterialTheme.colorScheme.surface to null
    }
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = containerColor,
        border = if (status == null) {
            BorderStroke(
                2.dp,
                if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            )
        } else null,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Toggle completion",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    Surface(
        shape = TempoExtraShapes.pill,
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = streak.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}
