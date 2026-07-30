package com.tempo.app.data.repository

import com.tempo.app.data.local.dao.HabitCompletionDao
import com.tempo.app.data.local.dao.HabitDao
import com.tempo.app.data.local.entity.HabitCompletionEntity
import com.tempo.app.domain.RecurrenceEngine
import com.tempo.app.domain.StreakCalculator
import com.tempo.app.domain.model.Habit
import com.tempo.app.domain.model.HabitCompletionStatus
import com.tempo.app.domain.model.HabitWithTodayStatus
import com.tempo.app.domain.model.RecurrenceRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
) : HabitRepository {

    override fun observeActiveHabits(): Flow<List<Habit>> =
        habitDao.observeActive().map { entities -> entities.map { it.toDomain() } }

    override fun observeHabitsForDate(date: LocalDate): Flow<List<HabitWithTodayStatus>> =
        combine(habitDao.observeActive(), completionDao.observeAll()) { entities, completions ->
            val completionsByHabit = completions.groupBy { it.habitId }
            entities.mapNotNull { entity ->
                val habit = entity.toDomain()
                val completionsByDate = completionsByHabit[entity.id].orEmpty()
                    .associate { it.date to it.status }
                buildStatusForDate(habit, date, completionsByDate)
            }
        }

    private fun buildStatusForDate(
        habit: Habit,
        date: LocalDate,
        completionsByDate: Map<LocalDate, HabitCompletionStatus>,
    ): HabitWithTodayStatus? {
        if (date.isBefore(habit.createdAt)) return null

        val scheduledToday = isEffectivelyScheduled(habit, date, completionsByDate)
        val statusToday = completionsByDate[date]

        var overdue = false
        for (daysBack in 1..habit.graceDays) {
            val pastDate = date.minusDays(daysBack.toLong())
            if (pastDate.isBefore(habit.createdAt)) continue
            val wasScheduled = isEffectivelyScheduled(habit, pastDate, completionsByDate)
            val wasSatisfied = completionsByDate[pastDate].let {
                it == HabitCompletionStatus.DONE || it == HabitCompletionStatus.SKIPPED_EXCUSED
            }
            if (wasScheduled && !wasSatisfied) {
                overdue = true
                break
            }
        }

        if (!scheduledToday && statusToday == null && !overdue) return null

        val streak = StreakCalculator.calculate(habit.recurrenceRule, habit.createdAt, completionsByDate, date)
        val weekStart = RecurrenceEngine.startOfWeek(date)
        val excusedThisWeek = completionsByDate.count { (d, status) ->
            status == HabitCompletionStatus.SKIPPED_EXCUSED && RecurrenceEngine.startOfWeek(d) == weekStart
        }
        val freezesRemaining = (habit.streakFreezeAllowance - excusedThisWeek).coerceAtLeast(0)

        return HabitWithTodayStatus(
            habit = habit,
            forDate = date,
            status = statusToday,
            isOverdue = overdue,
            currentStreak = streak.current,
            bestStreak = streak.best,
            freezesRemainingThisWeek = freezesRemaining,
        )
    }

    private fun isEffectivelyScheduled(
        habit: Habit,
        date: LocalDate,
        completionsByDate: Map<LocalDate, HabitCompletionStatus>,
    ): Boolean {
        val rule = habit.recurrenceRule
        if (date.isBefore(habit.createdAt)) return false
        if (rule !is RecurrenceRule.TimesPerWeek) return RecurrenceEngine.isScheduledOn(rule, date)

        val weekStart = RecurrenceEngine.startOfWeek(date)
        if (completionsByDate[date] == HabitCompletionStatus.DONE) return true
        val doneBeforeDate = completionsByDate.count { (d, status) ->
            status == HabitCompletionStatus.DONE && RecurrenceEngine.startOfWeek(d) == weekStart && d.isBefore(date)
        }
        return doneBeforeDate < rule.times
    }

    override suspend fun getHabit(id: Long): Habit? = habitDao.getById(id)?.toDomain()

    override suspend fun addHabit(habit: Habit): Long = habitDao.insert(habit.toEntity())

    override suspend fun updateHabit(habit: Habit) = habitDao.update(habit.toEntity())

    override suspend fun archiveHabit(id: Long) = habitDao.archive(id)

    override suspend fun cycleCompletion(habitId: Long, date: LocalDate) {
        val existing = completionDao.getForHabitAndDate(habitId, date)
        when (existing?.status) {
            null -> completionDao.upsert(
                HabitCompletionEntity(
                    habitId = habitId,
                    date = date,
                    status = HabitCompletionStatus.DONE,
                    completedAt = Instant.now(),
                ),
            )
            HabitCompletionStatus.DONE -> {
                if (freezesRemaining(habitId, date) > 0) {
                    completionDao.upsert(existing.copy(status = HabitCompletionStatus.SKIPPED_EXCUSED, completedAt = null))
                } else {
                    completionDao.delete(habitId, date)
                }
            }
            HabitCompletionStatus.SKIPPED_EXCUSED, HabitCompletionStatus.MISSED ->
                completionDao.delete(habitId, date)
        }
    }

    private suspend fun freezesRemaining(habitId: Long, date: LocalDate): Int {
        val habit = habitDao.getById(habitId)?.toDomain() ?: return 0
        val weekStart = RecurrenceEngine.startOfWeek(date)
        val weekEnd = weekStart.plusDays(6)
        val excusedThisWeek = completionDao.getForHabitInRange(habitId, weekStart, weekEnd)
            .count { it.status == HabitCompletionStatus.SKIPPED_EXCUSED }
        return (habit.streakFreezeAllowance - excusedThisWeek).coerceAtLeast(0)
    }
}
