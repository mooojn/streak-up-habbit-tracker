package com.example.streakup_habbit_tracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.streakup_habbit_tracker.data.HabitRepository

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        val destination = if (HabitRepository.userName.isNotBlank()) {
            DashboardActivity::class.java
        } else {
            HomeActivity::class.java
        }

        startActivity(Intent(this, destination))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HabitRepository.initialize(applicationContext)
        setContentView(R.layout.activity_splash)
        handler.postDelayed(navigateRunnable, 1400L)
    }

    override fun onDestroy() {
        handler.removeCallbacks(navigateRunnable)
        super.onDestroy()
    }
}
