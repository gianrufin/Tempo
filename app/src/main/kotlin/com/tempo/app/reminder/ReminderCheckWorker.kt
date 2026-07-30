package com.tempo.app.reminder

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tempo.app.data.local.dao.HabitCompletionDao
import com.tempo.app.data.local.dao.HabitDao
import com.tempo.app.data.repository.toDomain
import com.tempo.app.domain.AdaptiveReminderCalculator
import com.tempo.app.domain.model.HabitCompletionStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.LocalTime

/**
 * Runs every 15 minutes (WorkManager's minimum periodic interval). For each active habit with
 * reminders, checks whether "now" falls within 15 minutes of its effective reminder time — the
 * user-set time, nudged toward their recent completion times via [AdaptiveReminderCalculator] —
 * and if the habit isn't already done/excused today, shows a notification.
 */
@HiltWorker
class ReminderCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val nowTime = LocalTime.now()

        habitDao.getAllActive().forEach { entity ->
            val habit = entity.toDomain()
            if (habit.reminderTimes.isEmpty() || today.isBefore(habit.createdAt)) return@forEach

            val todaysCompletion = completionDao.getForHabitAndDate(habit.id, today)
            if (todaysCompletion?.status == HabitCompletionStatus.DONE ||
                todaysCompletion?.status == HabitCompletionStatus.SKIPPED_EXCUSED
            ) {
                return@forEach
            }

            val recentDone = completionDao.getRecentDone(habit.id, 10)

            habit.reminderTimes.forEach { reminderTime ->
                val effectiveTime = AdaptiveReminderCalculator.suggestTime(
                    recentCompletionTimestamps = recentDone.mapNotNull { it.completedAt },
                    fallback = reminderTime,
                )
                // Wraps across midnight: a reminder at 23:50 checked at 00:05 should still fire.
                val secondsSinceMidnightDiff = nowTime.toSecondOfDay() - effectiveTime.toSecondOfDay()
                val minutesSince = Math.floorMod(secondsSinceMidnightDiff, 24 * 60 * 60) / 60
                if (minutesSince in 0 until 15) {
                    notificationHelper.showReminder(habit)
                }
            }
        }
        return Result.success()
    }
}
