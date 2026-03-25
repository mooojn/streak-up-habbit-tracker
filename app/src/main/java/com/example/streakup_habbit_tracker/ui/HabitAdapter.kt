package com.example.streakup_habbit_tracker.ui

import android.content.res.ColorStateList
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.Habit
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.google.android.material.button.MaterialButton

class HabitAdapter(private val actionListener: HabitActionListener) :
    RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    interface HabitActionListener {
        fun onEdit(habit: Habit)
        fun onDelete(habit: Habit)
        fun onCompleteToday(habit: Habit)
    }

    private val habits = mutableListOf<Habit>()

    fun setHabits(updatedHabits: List<Habit>) {
        habits.clear()
        habits.addAll(updatedHabits)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        val context = holder.itemView.context

        holder.habitTitleText.text = habit.title
        holder.habitStreakText.text = context.resources.getQuantityString(
            R.plurals.streak_days,
            habit.streakCount,
            habit.streakCount
        )

        if (TextUtils.isEmpty(habit.note)) {
            holder.habitNoteText.visibility = View.GONE
        } else {
            holder.habitNoteText.visibility = View.VISIBLE
            holder.habitNoteText.text = habit.note
        }

        val completedToday = HabitRepository.hasCompletedToday(habit)
        holder.completeHabitButton.text = if (completedToday) {
            context.getString(R.string.completed_today)
        } else {
            context.getString(R.string.complete_today)
        }
        holder.completeHabitButton.isEnabled = !completedToday

        val buttonColor = if (completedToday) {
            ContextCompat.getColor(context, R.color.brand_secondary)
        } else {
            ContextCompat.getColor(context, R.color.brand_primary)
        }
        holder.completeHabitButton.backgroundTintList = ColorStateList.valueOf(buttonColor)
        holder.completeHabitButton.alpha = if (completedToday) 0.82f else 1f

        holder.editHabitButton.setOnClickListener { actionListener.onEdit(habit) }
        holder.deleteHabitButton.setOnClickListener { actionListener.onDelete(habit) }
        holder.completeHabitButton.setOnClickListener { actionListener.onCompleteToday(habit) }
    }

    override fun getItemCount(): Int = habits.size

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitTitleText: TextView = itemView.findViewById(R.id.habitTitleText)
        val habitNoteText: TextView = itemView.findViewById(R.id.habitNoteText)
        val habitStreakText: TextView = itemView.findViewById(R.id.habitStreakText)
        val editHabitButton: ImageButton = itemView.findViewById(R.id.editHabitButton)
        val deleteHabitButton: ImageButton = itemView.findViewById(R.id.deleteHabitButton)
        val completeHabitButton: MaterialButton = itemView.findViewById(R.id.completeHabitButton)
    }
}
