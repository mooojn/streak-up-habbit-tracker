package com.example.streakup_habbit_tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.example.streakup_habbit_tracker.ui.AddHabitFragment
import com.example.streakup_habbit_tracker.ui.HabitsFragment
import com.example.streakup_habbit_tracker.ui.ProfileFragment
import com.example.streakup_habbit_tracker.ui.TrackerFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DashboardActivity : AppCompatActivity() {

    private lateinit var dashboardToolbar: MaterialToolbar
    private lateinit var bottomNavigationView: BottomNavigationView
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HabitRepository.initialize(applicationContext)  // safety net
        setContentView(R.layout.activity_dashboard)
        requestNotificationPermissionIfNeeded()

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

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (shouldShowRequestPermissionRationale(permission)) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.notification_permission_title)
                .setMessage(R.string.notification_permission_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_allow) { _, _ ->
                    notificationPermissionLauncher.launch(permission)
                }
                .show()
        } else {
            notificationPermissionLauncher.launch(permission)
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

            R.id.nav_ai_insights -> {
                fragment = com.example.streakup_habbit_tracker.ui.AiInsightsFragment()
                titleRes = R.string.title_ai_insights
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
