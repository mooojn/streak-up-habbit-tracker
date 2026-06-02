package com.example.streakup_habbit_tracker.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.Badge
import com.google.android.material.card.MaterialCardView

class BadgeAdapter(private val badges: List<Badge>) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val badgeCard: MaterialCardView = view.findViewById(R.id.badgeCard)
        val badgeIcon: ImageView = view.findViewById(R.id.badgeIcon)
        val badgeTitle: TextView = view.findViewById(R.id.badgeTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        holder.badgeTitle.text = badge.title

        if (badge.isUnlocked) {
            holder.badgeCard.setCardBackgroundColor(Color.parseColor("#FFD700")) // Gold
            holder.badgeIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
            holder.badgeTitle.alpha = 1.0f
        } else {
            holder.badgeCard.setCardBackgroundColor(Color.parseColor("#E0E0E0")) // Gray
            holder.badgeIcon.imageTintList = ColorStateList.valueOf(Color.GRAY)
            holder.badgeTitle.alpha = 0.5f
        }
    }

    override fun getItemCount() = badges.size
}
