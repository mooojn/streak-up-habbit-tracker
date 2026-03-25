package com.example.streakup_habbit_tracker.data

object HabitRepository {

    private val habits = mutableListOf<Habit>()
    var userName: String = ""

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
    fun getHabitCount(): Int = habits.size
}
