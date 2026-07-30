package com.tempo.app.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tempo.app.R
import com.tempo.app.alarm.TaskReminderActionReceiver
import com.tempo.app.domain.model.Habit
import com.tempo.app.domain.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    init {
        createChannels()
    }

    private fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Habit & task reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders to complete your habits and tasks"
            },
        )
        // No channel sound here on purpose — TimerAlarmActivity/AlarmSoundPlayer plays the
        // user-chosen alarm sound directly so Stop/Snooze can control it precisely.
        manager.createNotificationChannel(
            NotificationChannel(TIMER_CHANNEL_ID, "Timer alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Pomodoro and countdown timer alarms"
                setSound(null, null)
            },
        )
    }

    fun showReminder(habit: Habit) {
        val notificationId = habit.id.toInt()

        val markDoneIntent = actionIntent(ReminderActionReceiver.ACTION_MARK_DONE, habit.id, notificationId)
        val skipIntent = actionIntent(ReminderActionReceiver.ACTION_SKIP, habit.id, notificationId)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${habit.icon} ${habit.name}")
            .setContentText("Time for your habit")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .addAction(0, "Mark done", markDoneIntent)
            .addAction(0, "Skip today", skipIntent)
            .build()

        notifyIfPermitted(notificationId, notification)
    }

    fun showTaskReminder(task: Task) {
        val notificationId = TASK_REMINDER_NOTIFICATION_ID_OFFSET + task.id.toInt()

        val markDoneIntent = taskActionIntent(TaskReminderActionReceiver.ACTION_MARK_TASK_DONE, task.id, notificationId)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(task.title)
            .setContentText("Task reminder")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .addAction(0, "Mark done", markDoneIntent)
            .build()

        notifyIfPermitted(notificationId, notification)
    }

    fun showStreakRisk(habit: Habit, currentStreak: Int) {
        val notificationId = STREAK_RISK_NOTIFICATION_ID_OFFSET + habit.id.toInt()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🔥 ${habit.icon} ${habit.name}'s streak is at risk")
            .setContentText("$currentStreak day streak — complete it before the day ends")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        notifyIfPermitted(notificationId, notification)
    }

    fun showWeeklyRecap(overallRatePercent: Int, periodLabel: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Your week in Tempo")
            .setContentText("$overallRatePercent% overall completion, $periodLabel")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        notifyIfPermitted(WEEKLY_RECAP_NOTIFICATION_ID, notification)
    }

    /**
     * The heads-up/lock-screen fallback for a fired timer alarm: [fullScreenIntent] is what the
     * system launches directly over the lock screen (the branded [com.tempo.app.ui.alarm.TimerAlarmActivity]);
     * this notification is what shows if the system suppresses that (e.g. screen already on and
     * unlocked in another app), so Stop/Snooze must work from here too.
     */
    fun showTimerAlarm(
        notificationId: Int,
        title: String,
        text: String,
        fullScreenIntent: PendingIntent,
        stopIntent: PendingIntent,
        snoozeIntent: PendingIntent,
    ) {
        val notification = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenIntent, true)
            .addAction(0, "Snooze 5 min", snoozeIntent)
            .addAction(0, "Stop", stopIntent)
            .build()
        notifyIfPermitted(notificationId, notification)
    }

    fun cancelTimerAlarm(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun notifyIfPermitted(notificationId: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    private fun actionIntent(action: String, habitId: Long, notificationId: Int): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra(ReminderActionReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(ReminderActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            "$action-$habitId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun taskActionIntent(action: String, taskId: Long, notificationId: Int): PendingIntent {
        val intent = Intent(context, TaskReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra(TaskReminderActionReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            "$action-$taskId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL_ID = "habit_reminders"
        const val TIMER_CHANNEL_ID = "timer_alarms"
        private const val STREAK_RISK_NOTIFICATION_ID_OFFSET = 1_000_000
        private const val WEEKLY_RECAP_NOTIFICATION_ID = 2_000_000
        private const val TASK_REMINDER_NOTIFICATION_ID_OFFSET = 3_000_000
    }
}
