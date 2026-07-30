package com.tempo.app.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tempo.app.R
import com.tempo.app.domain.model.Habit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    init {
        createChannel()
    }

    private fun createChannel() {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Habit reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Reminders to complete your habits"
        }
        manager.createNotificationChannel(channel)
    }

    fun showReminder(habit: Habit) {
        val notificationId = habit.id.toInt()

        val markDoneIntent = actionIntent(ReminderActionReceiver.ACTION_MARK_DONE, habit.id, notificationId)
        val skipIntent = actionIntent(ReminderActionReceiver.ACTION_SKIP, habit.id, notificationId)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${habit.icon} ${habit.name}")
            .setContentText("Time for your habit")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(0, "Mark done", markDoneIntent)
            .addAction(0, "Skip today", skipIntent)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
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

    private fun notifyIfPermitted(notificationId: Int, notification: android.app.Notification) {
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

    companion object {
        const val CHANNEL_ID = "habit_reminders"
        private const val STREAK_RISK_NOTIFICATION_ID_OFFSET = 1_000_000
        private const val WEEKLY_RECAP_NOTIFICATION_ID = 2_000_000
    }
}
