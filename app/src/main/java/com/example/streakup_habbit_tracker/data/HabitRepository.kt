package com.example.streakup_habbit_tracker.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object HabitRepository {

    enum class CompleteHabitResult {
        COMPLETED,
        ALREADY_COMPLETED,
        NOT_FOUND
    }

    private val habits = mutableListOf<Habit>()
    var userName: String = ""

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    @Synchronized
    fun addHabit(title: String, note: String) {
        habits.add(Habit(title = title, note = note))
    }

    @Synchronized
    fun getHabits(): List<Habit> = habits.map { it.copy() }

    @Synchronized
    fun deleteHabit(habitId: String) {
        habits.removeAll { it.id == habitId }
    }

    @Synchronized
    fun updateHabit(habitId: String, newTitle: String, newNote: String): Boolean {
        val existingHabit = habits.find { it.id == habitId } ?: return false
        existingHabit.title = newTitle
        existingHabit.note = newNote
        return true
    }

    @Synchronized
    fun completeHabitForToday(habitId: String): CompleteHabitResult {
        val habit = habits.find { it.id == habitId } ?: return CompleteHabitResult.NOT_FOUND

        val today = todayKey()
        if (habit.lastCompletedDate == today) {
            return CompleteHabitResult.ALREADY_COMPLETED
        }

        habit.streakCount = if (habit.lastCompletedDate == yesterdayKey()) {
            habit.streakCount + 1
        } else {
            1
        }
        habit.lastCompletedDate = today

        return CompleteHabitResult.COMPLETED
    }

    fun hasCompletedToday(habit: Habit): Boolean = habit.lastCompletedDate == todayKey()

    @Synchronized
    fun getHabitCount(): Int = habits.size

    private fun todayKey(): String = dateFormatter.format(Date())

    private fun yesterdayKey(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormatter.format(calendar.time)
    }
}
