package com.example.streakup_habbit_tracker.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
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

    private const val PREFS_NAME = "streakup_prefs"
    private const val KEY_USER_NAME = "key_user_name"
    private const val KEY_HABITS = "key_habits"
    private const val KEY_DAILY_COMPLETIONS = "key_daily_completions"

    private val habits = mutableListOf<Habit>()
    private val dailyCompletionCounts = mutableMapOf<String, Int>()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private var preferences: SharedPreferences? = null
    private var isInitialized = false
    private var userNameBacking: String = ""

    var userName: String
        get() = userNameBacking
        set(value) {
            userNameBacking = value.trim()
            persistUserName()
        }

    fun initialize(context: Context) {
        if (isInitialized) return

        preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        userNameBacking = preferences?.getString(KEY_USER_NAME, "").orEmpty()
        loadHabitsFromStorage()
        loadDailyCompletionsFromStorage()
        isInitialized = true
    }

    @Synchronized
    fun addHabit(title: String, note: String) {
        habits.add(Habit(title = title, note = note))
        persistHabits()
    }

    @Synchronized
    fun getHabits(): List<Habit> = habits.map { it.copy() }

    @Synchronized
    fun deleteHabit(habitId: String) {
        habits.removeAll { it.id == habitId }
        persistHabits()
    }

    @Synchronized
    fun updateHabit(habitId: String, newTitle: String, newNote: String): Boolean {
        val existingHabit = habits.find { it.id == habitId } ?: return false
        existingHabit.title = newTitle
        existingHabit.note = newNote
        persistHabits()
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

        dailyCompletionCounts[today] = (dailyCompletionCounts[today] ?: 0) + 1

        persistHabits()
        persistDailyCompletions()

        return CompleteHabitResult.COMPLETED
    }

    fun hasCompletedToday(habit: Habit): Boolean = habit.lastCompletedDate == todayKey()

    @Synchronized
    fun getCompletionCountByDate(dateKey: String): Int = dailyCompletionCounts[dateKey] ?: 0

    @Synchronized
    fun getHabitCount(): Int = habits.size

    private fun persistUserName() {
        preferences?.edit()?.putString(KEY_USER_NAME, userNameBacking)?.apply()
    }

    @Synchronized
    private fun persistHabits() {
        val array = JSONArray()
        habits.forEach { habit ->
            val jsonHabit = JSONObject().apply {
                put("id", habit.id)
                put("title", habit.title)
                put("note", habit.note)
                put("streakCount", habit.streakCount)
                put("lastCompletedDate", habit.lastCompletedDate)
            }
            array.put(jsonHabit)
        }
        preferences?.edit()?.putString(KEY_HABITS, array.toString())?.apply()
    }

    @Synchronized
    private fun persistDailyCompletions() {
        val json = JSONObject()
        dailyCompletionCounts.forEach { (dateKey, count) ->
            json.put(dateKey, count)
        }
        preferences?.edit()?.putString(KEY_DAILY_COMPLETIONS, json.toString())?.apply()
    }

    @Synchronized
    private fun loadHabitsFromStorage() {
        habits.clear()

        val rawHabits = preferences?.getString(KEY_HABITS, null) ?: return
        if (rawHabits.isBlank()) return

        try {
            val array = JSONArray(rawHabits)
            for (index in 0 until array.length()) {
                val jsonHabit = array.optJSONObject(index) ?: continue

                val id = jsonHabit.optString("id", "").trim()
                val title = jsonHabit.optString("title", "").trim()
                if (id.isBlank() || title.isBlank()) continue

                habits.add(
                    Habit(
                        id = id,
                        title = title,
                        note = jsonHabit.optString("note", ""),
                        streakCount = jsonHabit.optInt("streakCount", 0).coerceAtLeast(0),
                        lastCompletedDate = jsonHabit.optString("lastCompletedDate", "")
                    )
                )
            }
        } catch (_: Exception) {
            habits.clear()
        }
    }

    @Synchronized
    private fun loadDailyCompletionsFromStorage() {
        dailyCompletionCounts.clear()

        val rawData = preferences?.getString(KEY_DAILY_COMPLETIONS, null) ?: return
        if (rawData.isBlank()) return

        try {
            val json = JSONObject(rawData)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.optInt(key, 0)
                if (value > 0) {
                    dailyCompletionCounts[key] = value
                }
            }
        } catch (_: Exception) {
            dailyCompletionCounts.clear()
        }
    }

    private fun todayKey(): String = dateFormatter.format(Date())

    private fun yesterdayKey(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormatter.format(calendar.time)
    }
}
