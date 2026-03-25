package com.example.streakup_habbit_tracker.ui

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
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class ProfileFragment : Fragment() {

    private var profileGreetingText: TextView? = null
    private var profileHabitCountText: TextView? = null
    private var editNameButton: MaterialButton? = null

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

        editNameButton?.setOnClickListener { showEditNameDialog() }
    }

    override fun onResume() {
        super.onResume()
        updateProfile()
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
