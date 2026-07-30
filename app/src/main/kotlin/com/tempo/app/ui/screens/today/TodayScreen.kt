package com.tempo.app.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tempo.app.domain.model.HabitCompletionStatus
import com.tempo.app.domain.model.HabitWithTodayStatus
import com.tempo.app.domain.model.RoutineWithHabits
import com.tempo.app.domain.model.TimeOfDay
import com.tempo.app.ui.theme.OnGradient
import com.tempo.app.ui.theme.TempoExtraShapes
import com.tempo.app.ui.theme.TempoGradients
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    onAddHabit: () -> Unit = {},
    onAddRoutine: () -> Unit = {},
    onOpenHabit: (Long) -> Unit = {},
    onOpenRoutine: (Long) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showAddMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TempoGradients.home),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            GreetingHeader(name = state.greetingName)
            TimeOfDayToggle(selected = state.selectedTimeOfDay, onSelect = viewModel::onSelectTimeOfDay)

            if (state.isEmpty) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nothing planned for ${state.selectedTimeOfDay.label.lowercase()} yet.",
                        color = OnGradient.textSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 140.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(state.routineGroups, key = { "routine-${it.routine.id}" }) { group ->
                        RoutineCard(group = group, onToggle = viewModel::onToggleHabit, onOpenRoutine = onOpenRoutine)
                    }
                    if (state.standaloneHabits.isNotEmpty()) {
                        item(key = "standalone-header") {
                            Text(
                                text = "Habits",
                                style = MaterialTheme.typography.titleMedium,
                                color = OnGradient.textPrimary,
                            )
                        }
                        items(state.standaloneHabits, key = { "habit-${it.habit.id}" }) { item ->
                            HabitRow(item = item, onToggle = { viewModel.onToggleHabit(item.habit.id) }, onClick = { onOpenHabit(item.habit.id) })
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 88.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = OnGradient.surfaceStrong,
                modifier = Modifier
                    .size(56.dp),
                onClick = { showAddMenu = true },
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = OnGradient.textPrimary)
                }
            }
            DropdownMenu(expanded = showAddMenu, onDismissRequest = { showAddMenu = false }) {
                DropdownMenuItem(text = { Text("New habit") }, onClick = { showAddMenu = false; onAddHabit() })
                DropdownMenuItem(text = { Text("New routine") }, onClick = { showAddMenu = false; onAddRoutine() })
            }
        }
    }
}

@Composable
private fun GreetingHeader(name: String) {
    val today = remember { java.time.LocalDate.now() }
    val dateLabel = remember(today) {
        today.format(DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault()))
    }
    val greeting = if (name.isBlank()) "Hello there" else "Hello, $name"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Text(text = greeting, style = MaterialTheme.typography.bodyLarge, color = OnGradient.textSecondary)
            Text(
                text = "Today\n$dateLabel",
                style = MaterialTheme.typography.headlineMedium,
                color = OnGradient.textPrimary,
            )
        }
        Surface(shape = CircleShape, color = OnGradient.surfaceStrong, modifier = Modifier.size(48.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "T",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnGradient.textPrimary,
                )
            }
        }
    }
}

@Composable
private fun TimeOfDayToggle(selected: TimeOfDay, onSelect: (TimeOfDay) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TimeOfDay.entries.forEach { tod ->
            val isSelected = tod == selected
            Surface(
                shape = CircleShape,
                color = if (isSelected) OnGradient.textPrimary else OnGradient.surface,
                modifier = Modifier.size(48.dp),
                onClick = { onSelect(tod) },
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = tod.icon(),
                        contentDescription = tod.label,
                        tint = if (isSelected) TempoAccentOnLight else OnGradient.textPrimary,
                    )
                }
            }
        }
    }
}

private val TempoAccentOnLight = Color(0xFF6B4CE0)

private fun TimeOfDay.icon(): ImageVector = when (this) {
    TimeOfDay.MORNING -> Icons.Filled.WbTwilight
    TimeOfDay.AFTERNOON -> Icons.Filled.WbSunny
    TimeOfDay.EVENING -> Icons.Outlined.Brightness4
    TimeOfDay.NIGHT -> Icons.Filled.NightsStay
}

@Composable
private fun RoutineCard(
    group: RoutineWithHabits,
    onToggle: (Long) -> Unit,
    onOpenRoutine: (Long) -> Unit,
) {
    Surface(
        shape = TempoExtraShapes.card,
        color = OnGradient.surface,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenRoutine(group.routine.id) },
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${group.routine.icon} Your ${group.routine.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnGradient.textPrimary,
                )
            }
            group.habits.forEach { item ->
                HabitRow(item = item, onToggle = { onToggle(item.habit.id) }, onClick = { onToggle(item.habit.id) }, translucent = true)
            }
        }
    }
}

@Composable
private fun HabitRow(
    item: HabitWithTodayStatus,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    translucent: Boolean = false,
) {
    val done = item.status == HabitCompletionStatus.DONE
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (translucent) PaddingValues(vertical = 4.dp) else PaddingValues(16.dp)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.habit.icon}  ${item.habit.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnGradient.textPrimary,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                )
                if (item.currentStreak > 0) {
                    Text(
                        text = "🔥 ${item.currentStreak} day streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnGradient.textSecondary,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = TempoExtraShapes.pill,
                color = if (done) OnGradient.surfaceStrong else OnGradient.surface,
                onClick = onToggle,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (done) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = OnGradient.textPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        text = if (done) "Marked as done" else "Mark as done",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnGradient.textPrimary,
                    )
                }
            }
        }
    }

    if (translucent) {
        content()
    } else {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = TempoExtraShapes.card,
            color = OnGradient.surface,
        ) {
            content()
        }
    }
}
