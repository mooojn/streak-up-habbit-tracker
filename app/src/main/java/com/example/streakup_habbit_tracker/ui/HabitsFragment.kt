package com.example.streakup_habbit_tracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.Habit
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class HabitsFragment : Fragment() {

    private var habitAdapter: HabitAdapter? = null
    private var emptyStateText: View? = null
    private var bulkCompleteButton: MaterialButton? = null
    private var habitsSummaryText: TextView? = null
    private var currentHabitCount: Int = 0

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
        bulkCompleteButton = view.findViewById(R.id.bulkCompleteButton)
        habitsSummaryText = view.findViewById(R.id.habitsSummaryText)

        habitAdapter = HabitAdapter(object : HabitAdapter.HabitActionListener {
            override fun onEdit(habit: Habit) {
                showEditHabitDialog(habit)
            }

            override fun onDelete(habit: Habit) {
                showDeleteConfirmation(habit)
            }

            override fun onCompleteToday(habit: Habit) {
                toggleHabitCompletion(habit)
            }

            override fun onSelectionChanged(selectedCount: Int) {
                updateBulkCompleteButton(selectedCount)
            }
        })

        habitsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        habitsRecyclerView.adapter = habitAdapter
        bulkCompleteButton?.setOnClickListener {
            completeSelectedHabits()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHabits()
    }

    private fun refreshHabits() {
        val habits = HabitRepository.getHabits()
        currentHabitCount = habits.size
        habitAdapter?.setHabits(habits)
        emptyStateText?.isVisible = habits.isEmpty()
        updateHeaderSummary(habitAdapter?.getSelectedHabitIds()?.size ?: 0)
    }

    private fun updateBulkCompleteButton(selectedCount: Int) {
        val button = bulkCompleteButton ?: return
        button.isVisible = selectedCount > 0
        button.text = resources.getQuantityString(
            R.plurals.complete_selected_habits,
            selectedCount,
            selectedCount
        )
        updateHeaderSummary(selectedCount)
    }

    private fun updateHeaderSummary(selectedCount: Int) {
        val summaryText = habitsSummaryText ?: return
        summaryText.text = if (selectedCount > 0) {
            resources.getQuantityString(
                R.plurals.habits_selection_summary,
                selectedCount,
                selectedCount
            )
        } else {
            resources.getQuantityString(
                R.plurals.habit_count_summary,
                currentHabitCount,
                currentHabitCount
            )
        }
    }

    private fun toggleHabitCompletion(habit: Habit) {
        when (HabitRepository.toggleHabitForToday(habit.id)) {
            HabitRepository.HabitToggleResult.COMPLETED -> {
                refreshHabits()
                Toast.makeText(requireContext(), R.string.habit_completed_today, Toast.LENGTH_SHORT).show()
            }

            HabitRepository.HabitToggleResult.UNCOMPLETED -> {
                refreshHabits()
                Toast.makeText(requireContext(), R.string.habit_uncompleted_today, Toast.LENGTH_SHORT).show()
            }

            HabitRepository.HabitToggleResult.NOT_FOUND -> {
                refreshHabits()
            }
        }
    }

    private fun completeSelectedHabits() {
        val selectedHabitIds = habitAdapter?.getSelectedHabitIds().orEmpty()
        val completedCount = HabitRepository.completeHabitsForToday(selectedHabitIds)
        refreshHabits()
        habitAdapter?.clearSelection()

        val messageRes = if (completedCount > 0) {
            R.plurals.habits_completed_today
        } else {
            R.plurals.habits_already_completed_today
        }
        val quantity = if (completedCount > 0) completedCount else selectedHabitIds.size
        Toast.makeText(
            requireContext(),
            resources.getQuantityString(messageRes, quantity, quantity),
            Toast.LENGTH_SHORT
        ).show()
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
