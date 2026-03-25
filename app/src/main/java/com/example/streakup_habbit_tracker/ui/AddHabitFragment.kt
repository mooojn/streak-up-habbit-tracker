package com.example.streakup_habbit_tracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.streakup_habbit_tracker.DashboardActivity
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class AddHabitFragment : Fragment() {

    private var habitTitleInput: TextInputEditText? = null
    private var habitNoteInput: TextInputEditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_habit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        habitTitleInput = view.findViewById(R.id.habitTitleInput)
        habitNoteInput = view.findViewById(R.id.habitNoteInput)
        val addHabitButton: MaterialButton = view.findViewById(R.id.addHabitButton)

        addHabitButton.setOnClickListener { addHabit(view) }
    }

    private fun addHabit(rootView: View) {
        val title = habitTitleInput?.text?.toString()?.trim().orEmpty()
        val note = habitNoteInput?.text?.toString()?.trim().orEmpty()

        if (title.isBlank()) {
            Toast.makeText(requireContext(), R.string.error_habit_title_required, Toast.LENGTH_SHORT).show()
            return
        }

        HabitRepository.addHabit(title, note)
        habitTitleInput?.setText("")
        habitNoteInput?.setText("")

        Snackbar.make(rootView, R.string.habit_added, Snackbar.LENGTH_SHORT).show()

        (activity as? DashboardActivity)?.showHabitsTab()
    }
}
