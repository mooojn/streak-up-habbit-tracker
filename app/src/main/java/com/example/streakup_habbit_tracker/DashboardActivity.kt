package com.example.streakup_habbit_tracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.example.streakup_habbit_tracker.ui.AddHabitFragment
import com.example.streakup_habbit_tracker.ui.HabitsFragment
import com.example.streakup_habbit_tracker.ui.ProfileFragment
import com.example.streakup_habbit_tracker.ui.TrackerFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    private lateinit var dashboardToolbar: MaterialToolbar
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HabitRepository.initialize(applicationContext)
        setContentView(R.layout.activity_dashboard)

        dashboardToolbar = findViewById(R.id.dashboardToolbar)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        val incomingName = intent.getStringExtra(HomeActivity.EXTRA_USER_NAME)?.trim().orEmpty()
        if (incomingName.isNotBlank()) {
            HabitRepository.userName = incomingName
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            openTab(item.itemId)
            true
        }

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_habits
        }
    }

    fun showHabitsTab() {
        bottomNavigationView.selectedItemId = R.id.nav_habits
    }

    private fun openTab(itemId: Int) {
        val fragment: Fragment
        val titleRes: Int

        when (itemId) {
            R.id.nav_add -> {
                fragment = AddHabitFragment()
                titleRes = R.string.title_add_habit
            }

            R.id.nav_tracker -> {
                fragment = TrackerFragment()
                titleRes = R.string.title_tracker
            }

            R.id.nav_profile -> {
                fragment = ProfileFragment()
                titleRes = R.string.title_profile
            }

            else -> {
                fragment = HabitsFragment()
                titleRes = R.string.title_habits
            }
        }

        dashboardToolbar.setTitle(titleRes)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
