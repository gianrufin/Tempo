package com.tempo.app.ui.screens.habit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.app.data.repository.HabitRepository
import com.tempo.app.domain.model.HabitDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    repository: HabitRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val habitId: Long = checkNotNull(savedStateHandle.get<Long>("habitId"))

    val detail: StateFlow<HabitDetail?> = repository.observeHabitDetail(habitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
