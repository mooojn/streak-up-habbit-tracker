package com.example.streakup_habbit_tracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.Friend
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class FriendsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val friendsRecyclerView: RecyclerView = view.findViewById(R.id.friendsRecyclerView)
        val addFriendFab: ExtendedFloatingActionButton = view.findViewById(R.id.addFriendFab)

        val maxStreak = HabitRepository.getHabits().maxOfOrNull { it.streakCount } ?: 0

        val mockFriends = listOf(
            Friend("1", HabitRepository.userName.ifBlank { "You" }, maxStreak, 0),
            Friend("2", "Alex", 12, 0),
            Friend("3", "Jordan", 5, 0),
            Friend("4", "Taylor", 45, 0)
        ).sortedByDescending { it.currentStreak }

        friendsRecyclerView.adapter = FriendAdapter(mockFriends)

        addFriendFab.setOnClickListener {
            Toast.makeText(requireContext(), "Backend needed to add real friends!", Toast.LENGTH_SHORT).show()
        }
    }
}
