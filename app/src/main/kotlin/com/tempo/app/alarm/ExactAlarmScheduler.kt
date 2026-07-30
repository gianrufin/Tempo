package com.tempo.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.tempo.app.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Wraps [AlarmManager.setAlarmClock] — unlike [AlarmManager.setExactAndAllowWhileIdle], it's exempt
 * from the Android 12+ "Alarms & reminders" special permission on every API level, and the OS never
 * defers or batches it for Doze/battery optimization, since it's explicitly meant for user-visible
 * alarms. That's the guarantee this app's reminders and timers need: a reminder set for 12:00 AM
 * fires at 12:00 AM, not "sometime in the next 15 minutes" the way a periodic WorkManager check would.
 */
class ExactAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(AlarmManager::class.java)

    fun scheduleAlarmClock(requestCode: Int, triggerAtMillis: Long, operationIntent: Intent) {
        val operation = PendingIntent.getBroadcast(
            context,
            requestCode,
            operationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val showIntent = PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent), operation)
    }

    fun cancel(requestCode: Int, operationIntent: Intent) {
        val operation = PendingIntent.getBroadcast(
            context,
            requestCode,
            operationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(operation)
    }
}
