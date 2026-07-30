package com.tempo.app.ui.screens.habit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tempo.app.ui.theme.TempoExtraShapes
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AddEditHabitScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditHabitViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (state.habitId == null) "New habit" else "Edit habit") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.habitId != null) {
                        IconButton(onClick = viewModel::delete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete habit")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Habit name") },
                shape = TempoExtraShapes.card,
                modifier = Modifier.fillMaxWidth(),
            )

            Section(title = "Icon") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AddEditHabitUiState.DEFAULT_ICONS) { icon ->
                        FilterChip(
                            selected = state.icon == icon,
                            onClick = { viewModel.onIconChange(icon) },
                            label = { Text(icon) },
                            shape = TempoExtraShapes.pill,
                        )
                    }
                }
            }

            Section(title = "Color") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AddEditHabitUiState.DEFAULT_COLORS.forEach { colorLong ->
                        val selected = state.colorArgb == colorLong
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { viewModel.onColorChange(colorLong) },
                            shape = CircleShape,
                            color = Color(colorLong),
                            border = if (selected) {
                                BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                            } else null,
                        ) {}
                    }
                }
            }

            Section(title = "Repeats") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RecurrenceType.entries.forEach { type ->
                            FilterChip(
                                selected = state.recurrenceType == type,
                                onClick = { viewModel.onRecurrenceTypeChange(type) },
                                label = { Text(type.label()) },
                                shape = TempoExtraShapes.pill,
                            )
                        }
                    }

                    when (state.recurrenceType) {
                        RecurrenceType.SPECIFIC_WEEKDAYS -> WeekdayPicker(
                            selected = state.selectedWeekdays,
                            onToggle = viewModel::onToggleWeekday,
                        )
                        RecurrenceType.EVERY_N_DAYS -> Stepper(
                            label = "Every ${state.everyNDays} day(s)",
                            value = state.everyNDays,
                            onValueChange = viewModel::onEveryNDaysChange,
                        )
                        RecurrenceType.TIMES_PER_WEEK -> Stepper(
                            label = "${state.timesPerWeek} time(s) a week",
                            value = state.timesPerWeek,
                            onValueChange = viewModel::onTimesPerWeekChange,
                        )
                        RecurrenceType.MONTHLY_BY_DATE -> Stepper(
                            label = "Day ${state.monthlyDayOfMonth} of the month",
                            value = state.monthlyDayOfMonth,
                            onValueChange = viewModel::onMonthlyDayChange,
                        )
                        RecurrenceType.DAILY -> Text(
                            "Every day",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Section(title = "Streak freezes per week") {
                Stepper(
                    label = "${state.streakFreezeAllowance} freeze(s)",
                    value = state.streakFreezeAllowance,
                    onValueChange = viewModel::onStreakFreezeAllowanceChange,
                )
            }

            Section(title = "Grace period for overdue") {
                Stepper(
                    label = "${state.graceDays} day(s)",
                    value = state.graceDays,
                    onValueChange = viewModel::onGraceDaysChange,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::save,
                enabled = state.isValid,
                shape = TempoExtraShapes.pill,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save habit")
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun WeekdayPicker(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DayOfWeek.entries.forEach { day ->
            val label = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)
            FilterChip(
                selected = day in selected,
                onClick = { onToggle(day) },
                label = { Text(label) },
                shape = TempoExtraShapes.pill,
            )
        }
    }
}

@Composable
private fun Stepper(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = { onValueChange(value - 1) }, shape = TempoExtraShapes.pill) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
        }
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = { onValueChange(value + 1) }, shape = TempoExtraShapes.pill) {
            Icon(Icons.Filled.Add, contentDescription = "Increase")
        }
    }
}

private fun RecurrenceType.label(): String = when (this) {
    RecurrenceType.DAILY -> "Daily"
    RecurrenceType.SPECIFIC_WEEKDAYS -> "Weekdays"
    RecurrenceType.EVERY_N_DAYS -> "Every N days"
    RecurrenceType.TIMES_PER_WEEK -> "X / week"
    RecurrenceType.MONTHLY_BY_DATE -> "Monthly"
}
