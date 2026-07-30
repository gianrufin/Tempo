package com.tempo.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.tempo.app.data.backup.BackupScheduler
import com.tempo.app.reminder.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TempoApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var reminderScheduler: ReminderScheduler

    @Inject lateinit var backupScheduler: BackupScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        reminderScheduler.schedule()
        reminderScheduler.scheduleStreakRiskCheck()
        reminderScheduler.scheduleWeeklyRecap()
        backupScheduler.scheduleDaily(hour = DEFAULT_BACKUP_HOUR, minute = 0)
    }

    private companion object {
        const val DEFAULT_BACKUP_HOUR = 21
    }
}
