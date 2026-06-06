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
import com.example.streakup_habbit_tracker.data.Note
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.streakup_habbit_tracker.data.remote.OllamaRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitsFragment : Fragment() {

    private var habitAdapter: HabitAdapter? = null
    private var noteAdapter: NoteAdapter? = null
    private var emptyStateText: View? = null
    private var emptyNotesText: View? = null
    private var bulkCompleteButton: MaterialButton? = null
    private var habitsSummaryText: TextView? = null
    private var notesSummaryText: TextView? = null
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
        val notesRecyclerView: RecyclerView = view.findViewById(R.id.notesRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        emptyNotesText = view.findViewById(R.id.emptyNotesText)
        bulkCompleteButton = view.findViewById(R.id.bulkCompleteButton)
        habitsSummaryText = view.findViewById(R.id.habitsSummaryText)
        notesSummaryText = view.findViewById(R.id.notesSummaryText)

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

            override fun onProgressChanged(habit: Habit) {
                refreshHabits()
            }

            override fun onInsight(habit: Habit) {
                showInsightDialog(habit)
            }
        })

        habitsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        habitsRecyclerView.adapter = habitAdapter
        habitsRecyclerView.isNestedScrollingEnabled = false

        noteAdapter = NoteAdapter(object : NoteAdapter.NoteActionListener {
            override fun onAiHelp(note: Note) {
                showNoteAiHelpDialog(note)
            }

            override fun onEdit(note: Note) {
                showEditNoteDialog(note)
            }

            override fun onDelete(note: Note) {
                showDeleteNoteConfirmation(note)
            }
        })
        notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        notesRecyclerView.adapter = noteAdapter
        notesRecyclerView.isNestedScrollingEnabled = false

        bulkCompleteButton?.setOnClickListener {
            completeSelectedHabits()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHabits()
        refreshNotes()
    }

    private fun refreshHabits() {
        val habits = HabitRepository.getHabits()
        currentHabitCount = habits.size
        habitAdapter?.setHabits(habits)
        emptyStateText?.isVisible = habits.isEmpty()
        updateHeaderSummary(habitAdapter?.getSelectedHabitIds()?.size ?: 0)
    }

    private fun refreshNotes() {
        val notes = HabitRepository.getNotes()
        noteAdapter?.setNotes(notes)
        emptyNotesText?.isVisible = notes.isEmpty()
        notesSummaryText?.text = resources.getQuantityString(
            R.plurals.note_count_summary,
            notes.size,
            notes.size
        )
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
        val flexibleSwitch: SwitchMaterial = dialogView.findViewById(R.id.editHabitFlexibleSwitch)
        val flexibleOptionsLayout: LinearLayout = dialogView.findViewById(R.id.editFlexibleOptionsLayout)
        val targetInput: TextInputEditText = dialogView.findViewById(R.id.editHabitTargetInput)
        val unitInput: TextInputEditText = dialogView.findViewById(R.id.editHabitUnitInput)

        titleInput.setText(habit.title)
        noteInput.setText(habit.note)
        flexibleSwitch.isChecked = habit.isFlexible
        flexibleOptionsLayout.visibility = if (habit.isFlexible) View.VISIBLE else View.GONE
        targetInput.setText(habit.targetValue.toString())
        unitInput.setText(habit.unit)

        flexibleSwitch.setOnCheckedChangeListener { _, isChecked ->
            flexibleOptionsLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

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

                val isFlexible = flexibleSwitch.isChecked
                val targetValue = targetInput.text?.toString()?.toIntOrNull() ?: 1
                val unit = unitInput.text?.toString()?.trim().orEmpty()

                HabitRepository.updateHabit(habit.id, newTitle, newNote, isFlexible, targetValue, unit)
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

    private fun showEditNoteDialog(note: Note) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_note, null)
        val titleInput: TextInputEditText = dialogView.findViewById(R.id.editNoteTitleInput)
        val bodyInput: TextInputEditText = dialogView.findViewById(R.id.editNoteBodyInput)

        titleInput.setText(note.title)
        bodyInput.setText(note.body)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_note_title)
            .setView(dialogView)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save, null)
            .create()

        dialog.setOnShowListener {
            val width = (resources.displayMetrics.widthPixels * 0.9f).toInt()
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = titleInput.text?.toString()?.trim().orEmpty()
                val body = bodyInput.text?.toString()?.trim().orEmpty()

                if (title.isBlank()) {
                    titleInput.error = getString(R.string.error_note_title_required)
                    return@setOnClickListener
                }

                HabitRepository.updateNote(note.id, title, body)
                refreshNotes()
                Toast.makeText(requireContext(), R.string.note_updated, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteNoteConfirmation(note: Note) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_note_title)
            .setMessage(R.string.delete_note_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                HabitRepository.deleteNote(note.id)
                refreshNotes()
                Toast.makeText(requireContext(), R.string.note_deleted, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showInsightDialog(habit: Habit) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("AI Insight: ${habit.title}")
            .setMessage("Generating insight...")
            .setPositiveButton("OK", null)
            .show()

        viewLifecycleOwner.lifecycleScope.launch {
            val insight = OllamaRepository.getHabitSpecificInsight(habit)
            dialog.setMessage(insight)
        }
    }

    private fun showNoteAiHelpDialog(note: Note) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.note_ai_help_title, note.title))
            .setMessage(R.string.ai_generating)
            .setPositiveButton("OK", null)
            .show()

        viewLifecycleOwner.lifecycleScope.launch {
            val insight = OllamaRepository.getNoteHelp(note)
            dialog.setMessage(insight)
        }
    }

    private fun showDailyNoteDialog(habit: Habit) {
        val todayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val displayFmt = SimpleDateFormat("EEE, MMM dd yyyy", Locale.US)
        val today = todayFmt.format(Date())
        val displayDate = displayFmt.format(Date())

        val existingNote = HabitRepository.getDailyNote(habit.id, today)

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_daily_note, null)
        val noteInput: TextInputEditText = dialogView.findViewById(R.id.dailyNoteInput)
        val dateLabel: TextView = dialogView.findViewById(R.id.dailyNoteDateLabel)
        val historyHeader: TextView = dialogView.findViewById(R.id.dailyNoteHistoryHeader)
        val historyList: RecyclerView = dialogView.findViewById(R.id.dailyNoteHistoryList)

        dateLabel.text = displayDate
        noteInput.setText(existingNote)

        // Show past notes (excluding today) in reverse chronological order
        val pastNotes = habit.dailyNotes
            .filter { it.key != today }
            .entries
            .sortedByDescending { it.key }

        if (pastNotes.isNotEmpty()) {
            historyHeader.isVisible = true
            historyList.isVisible = true
            historyList.layoutManager = LinearLayoutManager(requireContext())
            historyList.adapter = DailyNoteHistoryAdapter(pastNotes)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(habit.title)
            .setView(dialogView)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val note = noteInput.text?.toString()?.trim().orEmpty()
                HabitRepository.saveDailyNote(habit.id, today, note)
                val msg = if (note.isBlank()) "Note cleared" else "Daily note saved!"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                refreshHabits()
            }
            .create()

        dialog.setOnShowListener {
            val width = (resources.displayMetrics.widthPixels * 0.92f).toInt()
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            // Move cursor to end of existing text
            noteInput.setSelection(noteInput.text?.length ?: 0)
        }

        dialog.show()
    }

    /**
     * Simple inline adapter for the past notes history list inside the daily note dialog.
     */
    private inner class DailyNoteHistoryAdapter(
        private val items: List<Map.Entry<String, String>>
    ) : RecyclerView.Adapter<DailyNoteHistoryAdapter.NoteVH>() {

        inner class NoteVH(view: View) : RecyclerView.ViewHolder(view) {
            val dateText: TextView = view.findViewById(android.R.id.text1)
            val noteText: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteVH {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return NoteVH(view)
        }

        override fun onBindViewHolder(holder: NoteVH, position: Int) {
            val entry = items[position]
            val displayFmt = SimpleDateFormat("EEE, MMM dd yyyy", Locale.US)
            val parseFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsedDate = runCatching { parseFmt.parse(entry.key) }.getOrNull()
            holder.dateText.text = if (parsedDate != null) displayFmt.format(parsedDate) else entry.key
            holder.noteText.text = entry.value
            holder.dateText.setTextColor(
                androidx.core.content.ContextCompat.getColor(holder.dateText.context, R.color.brand_primary)
            )
        }

        override fun getItemCount() = items.size
    }
}
