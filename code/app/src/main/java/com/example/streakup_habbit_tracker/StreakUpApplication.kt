package com.example.streakup_habbit_tracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.streakup_habbit_tracker.data.HabitRepository

class StreakUpApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize repo first so darkMode preference is readable
        HabitRepository.initialize(applicationContext)
        // Apply the saved night-mode setting exactly once, at process start.
        // Doing this here prevents the Activity recreation loop that occurs
        // when setDefaultNightMode() is called inside Activity.onCreate().
        AppCompatDelegate.setDefaultNightMode(HabitRepository.darkMode)
    }
}
