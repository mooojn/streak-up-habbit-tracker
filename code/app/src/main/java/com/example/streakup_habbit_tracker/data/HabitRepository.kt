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

    enum class HabitToggleResult {
        COMPLETED,
        UNCOMPLETED,
        NOT_FOUND
    }

    private const val PREFS_NAME = "streakup_prefs"
    private const val KEY_USER_NAME = "key_user_name"
    private const val KEY_HABITS = "key_habits"
    private const val KEY_DAILY_COMPLETIONS = "key_daily_completions"
    private const val KEY_NGROK_URL = "key_ngrok_url"
    private const val KEY_DARK_MODE = "key_dark_mode"

    private val habits = mutableListOf<Habit>()
    private val dailyCompletionCounts = mutableMapOf<String, Int>()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private var preferences: SharedPreferences? = null
    private var isInitialized = false
    private var userNameBacking: String = ""
    private var ngrokUrlBacking: String = ""

    var userName: String
        get() = userNameBacking
        set(value) {
            userNameBacking = value.trim()
            persistUserName()
        }

    var ngrokUrl: String
        get() = ngrokUrlBacking.ifEmpty { "https://flock-argue-unengaged.ngrok-free.dev" }
        set(value) {
            ngrokUrlBacking = value.trim()
            preferences?.edit()?.putString(KEY_NGROK_URL, ngrokUrlBacking)?.apply()
        }

    private const val KEY_REMINDER_HOUR = "key_reminder_hour"
    private const val KEY_REMINDER_MINUTE = "key_reminder_minute"
    private const val KEY_REMINDER_ENABLED = "key_reminder_enabled"

    var reminderHour: Int
        get() = preferences?.getInt(KEY_REMINDER_HOUR, 20) ?: 20
        set(value) { preferences?.edit()?.putInt(KEY_REMINDER_HOUR, value)?.apply() }

    var reminderMinute: Int
        get() = preferences?.getInt(KEY_REMINDER_MINUTE, 0) ?: 0
        set(value) { preferences?.edit()?.putInt(KEY_REMINDER_MINUTE, value)?.apply() }

    var reminderEnabled: Boolean
        get() = preferences?.getBoolean(KEY_REMINDER_ENABLED, true) ?: true
        set(value) { preferences?.edit()?.putBoolean(KEY_REMINDER_ENABLED, value)?.apply() }

    var darkMode: Int
        // -1 = system default, 1 = night (dark), 2 = day (light)
        get() = preferences?.getInt(KEY_DARK_MODE, -1) ?: -1
        set(value) { preferences?.edit()?.putInt(KEY_DARK_MODE, value)?.apply() }

    fun initialize(context: Context) {
        if (isInitialized) return

        preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        userNameBacking = preferences?.getString(KEY_USER_NAME, "").orEmpty()
        ngrokUrlBacking = preferences?.getString(KEY_NGROK_URL, "").orEmpty()
        loadHabitsFromStorage()
        loadDailyCompletionsFromStorage()
        isInitialized = true
    }

    @Synchronized
    fun addHabit(title: String, note: String, isFlexible: Boolean = false, targetValue: Int = 1, unit: String = "") {
        habits.add(Habit(title = title, note = note, isFlexible = isFlexible, targetValue = targetValue, unit = unit))
        persistHabits()
    }

    @Synchronized
    fun getHabits(): List<Habit> = habits.map { it.copy(dailyNotes = it.dailyNotes.toMutableMap()) }

    @Synchronized
    fun getDailyNote(habitId: String, date: String): String =
        habits.find { it.id == habitId }?.dailyNotes?.get(date) ?: ""

    @Synchronized
    fun saveDailyNote(habitId: String, date: String, note: String) {
        val habit = habits.find { it.id == habitId } ?: return
        if (note.isBlank()) {
            habit.dailyNotes.remove(date)
        } else {
            habit.dailyNotes[date] = note.trim()
        }
        persistHabits()
    }

    @Synchronized
    fun deleteHabit(habitId: String) {
        habits.removeAll { it.id == habitId }
        persistHabits()
    }

    @Synchronized
    fun updateHabit(habitId: String, newTitle: String, newNote: String, isFlexible: Boolean = false, targetValue: Int = 1, unit: String = ""): Boolean {
        val existingHabit = habits.find { it.id == habitId } ?: return false
        existingHabit.title = newTitle
        existingHabit.note = newNote
        existingHabit.isFlexible = isFlexible
        existingHabit.targetValue = targetValue
        existingHabit.unit = unit
        persistHabits()
        return true
    }

    @Synchronized
    fun toggleHabitForToday(habitId: String): HabitToggleResult {
        val habit = habits.find { it.id == habitId } ?: return HabitToggleResult.NOT_FOUND

        val today = todayKey()
        if (habit.lastCompletedDate == today) {
            val restoredStreak = habit.previousStreakCount.coerceAtLeast(0)
            habit.streakCount = restoredStreak
            habit.lastCompletedDate = habit.previousLastCompletedDate
            habit.currentValue = 0

            val dailyCount = dailyCompletionCounts[today] ?: 0
            when {
                dailyCount <= 1 -> dailyCompletionCounts.remove(today)
                else -> dailyCompletionCounts[today] = dailyCount - 1
            }

            persistHabits()
            persistDailyCompletions()
            return HabitToggleResult.UNCOMPLETED
        }

        habit.previousStreakCount = habit.streakCount
        habit.previousLastCompletedDate = habit.lastCompletedDate

        habit.streakCount = if (habit.lastCompletedDate == yesterdayKey()) {
            habit.streakCount + 1
        } else {
            1
        }
        habit.lastCompletedDate = today
        if (habit.isFlexible) {
            habit.currentValue = habit.targetValue
        }

        dailyCompletionCounts[today] = (dailyCompletionCounts[today] ?: 0) + 1

        persistHabits()
        persistDailyCompletions()

        return HabitToggleResult.COMPLETED
    }

    @Synchronized
    fun incrementHabitProgress(habitId: String): Boolean {
        val habit = habits.find { it.id == habitId } ?: return false
        if (!habit.isFlexible) return false
        
        val today = todayKey()
        if (habit.lastCompletedDate != today && habit.lastCompletedDate != "") {
            // It's a new day, current value should start from 0 if not completed today
            if (habit.lastCompletedDate != today) {
                habit.currentValue = 0
            }
        }
        
        if (habit.currentValue < habit.targetValue) {
            habit.currentValue += 1
            if (habit.currentValue == habit.targetValue) {
                toggleHabitForToday(habitId)
            } else {
                persistHabits()
            }
        }
        return true
    }

    @Synchronized
    fun decrementHabitProgress(habitId: String): Boolean {
        val habit = habits.find { it.id == habitId } ?: return false
        if (!habit.isFlexible) return false
        
        val today = todayKey()
        if (habit.lastCompletedDate == today && habit.currentValue == habit.targetValue) {
            toggleHabitForToday(habitId)
            habit.currentValue = habit.targetValue - 1
            persistHabits()
            return true
        }
        
        if (habit.currentValue > 0) {
            habit.currentValue -= 1
            persistHabits()
        }
        return true
    }

    @Synchronized
    fun completeHabitsForToday(habitIds: Set<String>): Int {
        if (habitIds.isEmpty()) return 0

        val today = todayKey()
        val yesterday = yesterdayKey()
        var completedCount = 0
        habitIds.forEach { habitId ->
            val habit = habits.find { it.id == habitId } ?: return@forEach
            if (habit.lastCompletedDate == today) return@forEach

            habit.previousStreakCount = habit.streakCount
            habit.previousLastCompletedDate = habit.lastCompletedDate
            habit.streakCount = if (habit.lastCompletedDate == yesterday) {
                habit.streakCount + 1
            } else {
                1
            }
            habit.lastCompletedDate = today
            dailyCompletionCounts[today] = (dailyCompletionCounts[today] ?: 0) + 1
            completedCount += 1
        }

        if (completedCount > 0) {
            persistHabits()
            persistDailyCompletions()
        }

        return completedCount
    }

    fun hasCompletedToday(habit: Habit): Boolean = habit.lastCompletedDate == todayKey()

    @Synchronized
    fun getCompletionCountByDate(dateKey: String): Int = dailyCompletionCounts[dateKey] ?: 0

    @Synchronized
    fun getHabitCount(): Int = habits.size

    @Synchronized
    fun getBadges(): List<Badge> {
        val maxStreak = habits.maxOfOrNull { it.streakCount } ?: 0
        return listOf(
            Badge("badge_7", "7-Day Warrior", "Maintain a 7-day streak on any habit", 7, maxStreak >= 7),
            Badge("badge_30", "Monthly Master", "Maintain a 30-day streak on any habit", 30, maxStreak >= 30),
            Badge("badge_100", "Century Club", "Maintain a 100-day streak on any habit", 100, maxStreak >= 100)
        )
    }

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
                put("previousStreakCount", habit.previousStreakCount)
                put("previousLastCompletedDate", habit.previousLastCompletedDate)
                put("isFlexible", habit.isFlexible)
                put("targetValue", habit.targetValue)
                put("currentValue", habit.currentValue)
                put("unit", habit.unit)
                val notesJson = JSONObject()
                habit.dailyNotes.forEach { (date, text) -> notesJson.put(date, text) }
                put("dailyNotes", notesJson)
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

                val dailyNotes = mutableMapOf<String, String>()
                val notesJson = jsonHabit.optJSONObject("dailyNotes")
                if (notesJson != null) {
                    val notesKeys = notesJson.keys()
                    while (notesKeys.hasNext()) {
                        val k = notesKeys.next()
                        val v = notesJson.optString(k, "")
                        if (v.isNotBlank()) dailyNotes[k] = v
                    }
                }

                habits.add(
                    Habit(
                        id = id,
                        title = title,
                        note = jsonHabit.optString("note", ""),
                        streakCount = jsonHabit.optInt("streakCount", 0).coerceAtLeast(0),
                        lastCompletedDate = jsonHabit.optString("lastCompletedDate", ""),
                        previousStreakCount = jsonHabit.optInt("previousStreakCount", 0).coerceAtLeast(0),
                        previousLastCompletedDate = jsonHabit.optString("previousLastCompletedDate", ""),
                        isFlexible = jsonHabit.optBoolean("isFlexible", false),
                        targetValue = jsonHabit.optInt("targetValue", 1),
                        currentValue = jsonHabit.optInt("currentValue", 0),
                        unit = jsonHabit.optString("unit", ""),
                        dailyNotes = dailyNotes
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
