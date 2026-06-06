package com.example.streakup_habbit_tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val id: String,
    var title: String,
    var note: String,
    var streakCount: Int = 0,
    var lastCompletedDate: String = "",
    var previousStreakCount: Int = 0,
    var previousLastCompletedDate: String = "",
    var isFlexible: Boolean = false,
    var targetValue: Int = 1,
    var currentValue: Int = 0,
    var unit: String = "",
    // We will store dailyNotes as a JSON string
    var dailyNotesJson: String = "{}"
) {
    fun toHabit(): Habit {
        val habit = Habit(
            id = id,
            title = title,
            note = note,
            streakCount = streakCount,
            lastCompletedDate = lastCompletedDate,
            previousStreakCount = previousStreakCount,
            previousLastCompletedDate = previousLastCompletedDate,
            isFlexible = isFlexible,
            targetValue = targetValue,
            currentValue = currentValue,
            unit = unit
        )
        try {
            val jsonObject = org.json.JSONObject(dailyNotesJson)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                habit.dailyNotes[key] = jsonObject.getString(key)
            }
        } catch (e: Exception) {
            // Ignore parsing errors
        }
        return habit
    }

    companion object {
        fun fromHabit(habit: Habit): HabitEntity {
            val jsonObject = org.json.JSONObject()
            habit.dailyNotes.forEach { (key, value) ->
                jsonObject.put(key, value)
            }
            return HabitEntity(
                id = habit.id,
                title = habit.title,
                note = habit.note,
                streakCount = habit.streakCount,
                lastCompletedDate = habit.lastCompletedDate,
                previousStreakCount = habit.previousStreakCount,
                previousLastCompletedDate = habit.previousLastCompletedDate,
                isFlexible = habit.isFlexible,
                targetValue = habit.targetValue,
                currentValue = habit.currentValue,
                unit = habit.unit,
                dailyNotesJson = jsonObject.toString()
            )
        }
    }
}
