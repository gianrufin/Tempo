package com.tempo.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.app.data.repository.HabitRepository
import com.tempo.app.domain.model.InsightsPeriod
import com.tempo.app.domain.model.InsightsSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    private val _period = MutableStateFlow(InsightsPeriod.WEEK)
    val period: StateFlow<InsightsPeriod> = _period.asStateFlow()

    val summary: StateFlow<InsightsSummary?> = _period
        .flatMapLatest { repository.observeInsights(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onPeriodChange(period: InsightsPeriod) {
        _period.value = period
    }

    suspend fun exportCsv(): String = repository.exportAllCompletionsCsv()
}
