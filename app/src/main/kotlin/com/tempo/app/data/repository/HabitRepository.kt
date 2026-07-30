package com.tempo.app.data.repository

import com.tempo.app.domain.model.Habit
import com.tempo.app.domain.model.HabitWithTodayStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {
    fun observeActiveHabits(): Flow<List<Habit>>
    fun observeHabitsForDate(date: LocalDate): Flow<List<HabitWithTodayStatus>>
    suspend fun getHabit(id: Long): Habit?
    suspend fun addHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun archiveHabit(id: Long)

    /** Cycles a habit's status for [date]: none -> done -> excused (if freezes remain) -> none. */
    suspend fun cycleCompletion(habitId: Long, date: LocalDate)
}
