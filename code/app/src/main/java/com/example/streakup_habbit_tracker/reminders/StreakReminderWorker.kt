package com.example.streakup_habbit_tracker.reminders

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.streakup_habbit_tracker.data.Habit
import com.example.streakup_habbit_tracker.data.HabitRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StreakReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun doWork(): Result {
        HabitRepository.initialize(applicationContext)

        val habitsAtRisk = getHabitsAtRisk(HabitRepository.getHabits())
        if (habitsAtRisk.isEmpty()) {
            return Result.success()
        }

        StreakReminderNotifier.ensureChannel(applicationContext)
        StreakReminderNotifier.showReminder(applicationContext, habitsAtRisk.map { it.title })
        return Result.success()
    }

    private fun getHabitsAtRisk(habits: List<Habit>): List<Habit> {
        val yesterday = yesterdayKey()
        return habits.filter { habit ->
            habit.streakCount > 0 && habit.lastCompletedDate == yesterday
        }
    }

    private fun yesterdayKey(): String {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return dateFormatter.format(calendar.time)
    }
}
