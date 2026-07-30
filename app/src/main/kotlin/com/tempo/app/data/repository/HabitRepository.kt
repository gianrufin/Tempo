package com.tempo.app.data.repository

import com.tempo.app.domain.model.DayAggregate
import com.tempo.app.domain.model.Habit
import com.tempo.app.domain.model.HabitDetail
import com.tempo.app.domain.model.HabitWithTodayStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface HabitRepository {
    fun observeActiveHabits(): Flow<List<Habit>>
    fun observeHabitsForDate(date: LocalDate): Flow<List<HabitWithTodayStatus>>
    suspend fun getHabit(id: Long): Habit?
    suspend fun addHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun archiveHabit(id: Long)

    /** Cycles a habit's status for [date]: none -> done -> excused (if freezes remain) -> none. */
    suspend fun cycleCompletion(habitId: Long, date: LocalDate)

    /** Streak, completion rate, and a trailing heatmap for a single habit's detail screen. */
    fun observeHabitDetail(habitId: Long, heatmapDays: Int = 98): Flow<HabitDetail?>

    /** Per-day scheduled/done/excused counts across all habits, for the month Calendar screen. */
    fun observeMonthAggregate(month: YearMonth): Flow<List<DayAggregate>>
}
