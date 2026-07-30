package com.tempo.app.ui.screens.today

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.app.data.preferences.PreferencesRepository
import com.tempo.app.data.repository.HabitRepository
import com.tempo.app.domain.model.HabitWithTodayStatus
import com.tempo.app.domain.model.RoutineWithHabits
import com.tempo.app.domain.model.TimeOfDay
import com.tempo.app.widget.WidgetRefresher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TodayUiState(
    val greetingName: String = "",
    val today: LocalDate = LocalDate.now(),
    val selectedTimeOfDay: TimeOfDay = TimeOfDay.forCurrentTime(),
    val routineGroups: List<RoutineWithHabits> = emptyList(),
    val standaloneHabits: List<HabitWithTodayStatus> = emptyList(),
) {
    val isEmpty: Boolean get() = routineGroups.isEmpty() && standaloneHabits.isEmpty()
}

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val preferencesRepository: PreferencesRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val today: LocalDate = LocalDate.now()

    private val _selectedTimeOfDay = MutableStateFlow(TimeOfDay.forCurrentTime())

    val uiState: StateFlow<TodayUiState> = combine(
        preferencesRepository.userPreferences,
        repository.observeHabitsForDate(today),
        repository.observeActiveRoutines(),
        _selectedTimeOfDay,
    ) { prefs, habits, routines, timeOfDay ->
        val habitsForSlot = habits.filter { it.habit.timeOfDay == timeOfDay }
        val routinesById = routines.filter { it.timeOfDay == timeOfDay }

        val routineGroups = routinesById.mapNotNull { routine ->
            val habitsInRoutine = habitsForSlot.filter { it.habit.routineId == routine.id }
            if (habitsInRoutine.isEmpty()) null else RoutineWithHabits(routine, habitsInRoutine)
        }
        val standalone = habitsForSlot.filter { it.habit.routineId == null }

        TodayUiState(
            greetingName = prefs.displayName,
            today = today,
            selectedTimeOfDay = timeOfDay,
            routineGroups = routineGroups,
            standaloneHabits = standalone,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState(today = today))

    fun onSelectTimeOfDay(timeOfDay: TimeOfDay) {
        _selectedTimeOfDay.value = timeOfDay
    }

    fun onToggleHabit(habitId: Long) {
        viewModelScope.launch {
            repository.cycleCompletion(habitId, today)
            WidgetRefresher.refresh(appContext)
        }
    }
}
