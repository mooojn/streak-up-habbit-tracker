package com.example.streakup_habbit_tracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.HabitRepository

class ProfileFragment : Fragment() {

    private var profileGreetingText: TextView? = null
    private var profileHabitCountText: TextView? = null

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
}
