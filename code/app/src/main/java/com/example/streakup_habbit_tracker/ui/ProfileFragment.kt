package com.example.streakup_habbit_tracker.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.example.streakup_habbit_tracker.reminders.StreakReminderScheduler
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatDelegate

class ProfileFragment : Fragment() {

    private var profileGreetingText: TextView? = null
    private var profileHabitCountText: TextView? = null
    private var editNameButton: MaterialButton? = null
    private var ngrokUrlEditText: TextInputEditText? = null
    private var saveNgrokUrlButton: MaterialButton? = null
    private var reminderToggle: SwitchMaterial? = null
    private var reminderTimeLabel: TextView? = null
    private var changeReminderTimeButton: MaterialButton? = null
    private var friendsButton: MaterialButton? = null
    private var darkModeToggle: SwitchMaterial? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileGreetingText = view.findViewById(R.id.profileGreetingText)
        profileHabitCountText = view.findViewById(R.id.profileHabitCountText)
        editNameButton = view.findViewById(R.id.editNameButton)
        ngrokUrlEditText = view.findViewById(R.id.ngrokUrlEditText)
        saveNgrokUrlButton = view.findViewById(R.id.saveNgrokUrlButton)
        reminderToggle = view.findViewById(R.id.reminderToggle)
        reminderTimeLabel = view.findViewById(R.id.reminderTimeLabel)
        changeReminderTimeButton = view.findViewById(R.id.changeReminderTimeButton)
        friendsButton = view.findViewById(R.id.friendsButton)
        darkModeToggle = view.findViewById(R.id.darkModeToggle)

        editNameButton?.setOnClickListener { showEditNameDialog() }

        friendsButton?.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.dashboardToolbar)?.setTitle(R.string.title_friends)
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, FriendsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Ngrok URL
        ngrokUrlEditText?.setText(HabitRepository.ngrokUrl)
        saveNgrokUrlButton?.setOnClickListener {
            val url = ngrokUrlEditText?.text?.toString()?.trim() ?: ""
            if (url.isNotEmpty() && !url.startsWith("http")) {
                ngrokUrlEditText?.error = "URL must start with http or https"
            } else {
                HabitRepository.ngrokUrl = url
                ngrokUrlEditText?.error = null
                Toast.makeText(requireContext(), "Ngrok URL saved", Toast.LENGTH_SHORT).show()
            }
        }

        // Reminder toggle
        reminderToggle?.isChecked = HabitRepository.reminderEnabled
        updateReminderLabel()
        reminderToggle?.setOnCheckedChangeListener { _, isChecked ->
            HabitRepository.reminderEnabled = isChecked
            if (isChecked) {
                StreakReminderScheduler.schedule(requireContext())
                Toast.makeText(requireContext(), "Daily reminder enabled", Toast.LENGTH_SHORT).show()
            } else {
                StreakReminderScheduler.cancel(requireContext())
                Toast.makeText(requireContext(), "Reminders disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Dark mode toggle
        darkModeToggle?.isChecked = HabitRepository.darkMode == AppCompatDelegate.MODE_NIGHT_YES
        darkModeToggle?.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            HabitRepository.darkMode = mode
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // Reminder time picker
        changeReminderTimeButton?.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    HabitRepository.reminderHour = hour
                    HabitRepository.reminderMinute = minute
                    updateReminderLabel()
                    if (HabitRepository.reminderEnabled) {
                        StreakReminderScheduler.schedule(requireContext())
                        Toast.makeText(requireContext(), "Reminder rescheduled", Toast.LENGTH_SHORT).show()
                    }
                },
                HabitRepository.reminderHour,
                HabitRepository.reminderMinute,
                true
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateProfile()
    }

    private fun updateReminderLabel() {
        val h = HabitRepository.reminderHour
        val m = HabitRepository.reminderMinute
        reminderTimeLabel?.text = "Daily reminder time: %02d:%02d".format(h, m)
    }

    private fun updateProfile() {
        val displayName = HabitRepository.userName.trim().ifBlank { "Streak Champion" }
        profileGreetingText?.text = getString(R.string.profile_greeting, displayName)

        val habitCount = HabitRepository.getHabitCount()
        val summary = resources.getQuantityString(R.plurals.habit_count_summary, habitCount, habitCount)
        profileHabitCountText?.text = summary
    }

    private fun showEditNameDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_name, null)
        val nameInput: TextInputEditText = dialogView.findViewById(R.id.editProfileNameInput)

        nameInput.setText(HabitRepository.userName)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_name_title)
            .setView(dialogView)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save, null)
            .create()

        dialog.setOnShowListener {
            val width = (resources.displayMetrics.widthPixels * 0.9f).toInt()
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val updatedName = nameInput.text?.toString()?.trim().orEmpty()
                if (updatedName.isBlank()) {
                    nameInput.error = getString(R.string.error_name_required)
                    return@setOnClickListener
                }

                HabitRepository.userName = updatedName
                updateProfile()
                Toast.makeText(requireContext(), R.string.name_updated, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
