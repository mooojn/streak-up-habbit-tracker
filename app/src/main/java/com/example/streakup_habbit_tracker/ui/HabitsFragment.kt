package com.example.streakup_habbit_tracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.Habit
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class HabitsFragment : Fragment() {

    private var habitAdapter: HabitAdapter? = null
    private var emptyStateText: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val habitsRecyclerView: RecyclerView = view.findViewById(R.id.habitsRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)

        habitAdapter = HabitAdapter(object : HabitAdapter.HabitActionListener {
            override fun onEdit(habit: Habit) {
                showEditHabitDialog(habit)
            }

            override fun onDelete(habit: Habit) {
                showDeleteConfirmation(habit)
            }

            override fun onCompleteToday(habit: Habit) {
                completeHabitForToday(habit)
            }
        })

        habitsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        habitsRecyclerView.adapter = habitAdapter
    }

    override fun onResume() {
        super.onResume()
        refreshHabits()
    }

    private fun refreshHabits() {
        val habits = HabitRepository.getHabits()
        habitAdapter?.setHabits(habits)
        emptyStateText?.isVisible = habits.isEmpty()
    }

    private fun completeHabitForToday(habit: Habit) {
        when (HabitRepository.completeHabitForToday(habit.id)) {
            HabitRepository.CompleteHabitResult.COMPLETED -> {
                refreshHabits()
                Toast.makeText(requireContext(), R.string.habit_completed_today, Toast.LENGTH_SHORT).show()
            }

            HabitRepository.CompleteHabitResult.ALREADY_COMPLETED -> {
                Toast.makeText(requireContext(), R.string.habit_already_completed_today, Toast.LENGTH_SHORT)
                    .show()
            }

            HabitRepository.CompleteHabitResult.NOT_FOUND -> {
                refreshHabits()
            }
        }
    }

    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_habit, null)
        val titleInput: TextInputEditText = dialogView.findViewById(R.id.editHabitTitleInput)
        val noteInput: TextInputEditText = dialogView.findViewById(R.id.editHabitNoteInput)

        titleInput.setText(habit.title)
        noteInput.setText(habit.note)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_habit_title)
            .setView(dialogView)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save, null)
            .create()

        dialog.setOnShowListener {
            val width = (resources.displayMetrics.widthPixels * 0.9f).toInt()
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newTitle = titleInput.text?.toString()?.trim().orEmpty()
                val newNote = noteInput.text?.toString()?.trim().orEmpty()

                if (newTitle.isBlank()) {
                    titleInput.error = getString(R.string.error_habit_title_required)
                    return@setOnClickListener
                }

                HabitRepository.updateHabit(habit.id, newTitle, newNote)
                refreshHabits()
                Toast.makeText(requireContext(), R.string.habit_updated, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(habit: Habit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_habit_title)
            .setMessage(R.string.delete_habit_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                HabitRepository.deleteHabit(habit.id)
                refreshHabits()
                Toast.makeText(requireContext(), R.string.habit_deleted, Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
