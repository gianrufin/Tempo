package com.tempo.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Habit(
    val id: Long = 0L,
    val name: String,
    val icon: String,
    val colorArgb: Long,
    val category: String? = null,
    val recurrenceRule: RecurrenceRule,
    val reminderTimes: List<LocalTime> = emptyList(),
    val streakFreezeAllowance: Int = 1,
    val graceDays: Int = 1,
    val createdAt: LocalDate = LocalDate.now(),
    val archived: Boolean = false,
)

/** A habit as it should appear on a given day, with its resolved status for that day. */
data class HabitWithTodayStatus(
    val habit: Habit,
    val forDate: LocalDate,
    val status: HabitCompletionStatus?,
    val isOverdue: Boolean,
    val currentStreak: Int,
    val bestStreak: Int,
    val freezesRemainingThisWeek: Int,
)
