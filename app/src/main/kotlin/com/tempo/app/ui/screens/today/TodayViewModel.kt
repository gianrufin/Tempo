package com.tempo.app.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.app.data.repository.HabitRepository
import com.tempo.app.domain.model.HabitWithTodayStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    val today: LocalDate = LocalDate.now()

    val habits: StateFlow<List<HabitWithTodayStatus>> = repository.observeHabitsForDate(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onToggleHabit(habitId: Long) {
        viewModelScope.launch { repository.cycleCompletion(habitId, today) }
    }
}
