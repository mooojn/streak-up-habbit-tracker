package com.example.streakup_habbit_tracker.ui

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.Habit

class HabitAdapter(private val actionListener: HabitActionListener) :
    RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    interface HabitActionListener {
        fun onEdit(habit: Habit)
        fun onDelete(habit: Habit)
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
        holder.habitTitleText.text = habit.title

        if (TextUtils.isEmpty(habit.note)) {
            holder.habitNoteText.visibility = View.GONE
        } else {
            holder.habitNoteText.visibility = View.VISIBLE
            holder.habitNoteText.text = habit.note
        }

        holder.editHabitButton.setOnClickListener { actionListener.onEdit(habit) }
        holder.deleteHabitButton.setOnClickListener { actionListener.onDelete(habit) }
    }

    override fun getItemCount(): Int = habits.size

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitTitleText: TextView = itemView.findViewById(R.id.habitTitleText)
        val habitNoteText: TextView = itemView.findViewById(R.id.habitNoteText)
        val editHabitButton: ImageButton = itemView.findViewById(R.id.editHabitButton)
        val deleteHabitButton: ImageButton = itemView.findViewById(R.id.deleteHabitButton)
    }
}
