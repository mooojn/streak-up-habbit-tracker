package com.example.streakup_habbit_tracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.Friend

class FriendAdapter(private val friends: List<Friend>) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val friendName: TextView = view.findViewById(R.id.friendName)
        val friendStreak: TextView = view.findViewById(R.id.friendStreak)
        val friendAvatar: ImageView = view.findViewById(R.id.friendAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.friendName.text = friend.name
        holder.friendStreak.text = "Max Streak: ${friend.currentStreak} days"
        // In a real app we'd load the avatar via Glide/Picasso
    }

    override fun getItemCount() = friends.size
}
