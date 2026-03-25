package com.example.streakup_habbit_tracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class HomeActivity : AppCompatActivity() {

    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var nameInputEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        nameInputLayout = findViewById(R.id.nameInputLayout)
        nameInputEditText = findViewById(R.id.nameInputEditText)
        val startButton: MaterialButton = findViewById(R.id.startButton)

        if (HabitRepository.userName.isNotBlank()) {
            nameInputEditText.setText(HabitRepository.userName)
        }

        startButton.setOnClickListener { startDashboard() }
    }

    private fun startDashboard() {
        val name = nameInputEditText.text?.toString()?.trim().orEmpty()

        if (name.isBlank()) {
            nameInputLayout.error = getString(R.string.error_name_required)
            return
        }

        nameInputLayout.error = null
        HabitRepository.userName = name

        val intent = Intent(this, DashboardActivity::class.java).putExtra(EXTRA_USER_NAME, name)
        startActivity(intent)
    }

    companion object {
        const val EXTRA_USER_NAME = "extra_user_name"
    }
}
