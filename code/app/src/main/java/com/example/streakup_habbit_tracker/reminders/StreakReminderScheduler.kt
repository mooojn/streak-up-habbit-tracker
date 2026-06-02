package com.example.streakup_habbit_tracker.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.streakup_habbit_tracker.data.HabitRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit

object StreakReminderScheduler {

    const val UNIQUE_WORK_NAME = "streak_reminder_daily"

    fun schedule(context: Context) {
        val hour = HabitRepository.reminderHour
        val minute = HabitRepository.reminderMinute

        val periodicRequest = PeriodicWorkRequestBuilder<StreakReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(computeInitialDelayMillis(hour, minute), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicRequest
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    private fun computeInitialDelayMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val nextReminderTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return (nextReminderTime.timeInMillis - now.timeInMillis).coerceAtLeast(1L)
    }
}
