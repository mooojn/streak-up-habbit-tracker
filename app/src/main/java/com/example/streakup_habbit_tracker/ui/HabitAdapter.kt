package com.example.streakup_habbit_tracker.ui

import android.content.res.ColorStateList
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.Habit
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class HabitAdapter(private val actionListener: HabitActionListener) :
    RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    interface HabitActionListener {
        fun onEdit(habit: Habit)
        fun onDelete(habit: Habit)
        fun onCompleteToday(habit: Habit)
        fun onSelectionChanged(selectedCount: Int)
    }

    private val habits = mutableListOf<Habit>()
    private val selectedHabitIds = linkedSetOf<String>()

    fun setHabits(updatedHabits: List<Habit>) {
        val validHabitIds = updatedHabits.mapTo(mutableSetOf()) { it.id }
        selectedHabitIds.retainAll(validHabitIds)
        habits.clear()
        habits.addAll(updatedHabits)
        notifyDataSetChanged()
        actionListener.onSelectionChanged(selectedHabitIds.size)
    }

    fun getSelectedHabitIds(): Set<String> = selectedHabitIds.toSet()

    fun clearSelection() {
        if (selectedHabitIds.isEmpty()) return
        selectedHabitIds.clear()
        notifyDataSetChanged()
        actionListener.onSelectionChanged(0)
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
        val isSelected = selectedHabitIds.contains(habit.id)

        val cardBackgroundColor = when {
            isSelected -> ContextCompat.getColor(context, R.color.surface_selected)
            completedToday -> ContextCompat.getColor(context, R.color.surface_success)
            else -> ContextCompat.getColor(context, R.color.surface_card)
        }
        val cardStrokeColor = when {
            isSelected -> ContextCompat.getColor(context, R.color.brand_primary)
            completedToday -> ContextCompat.getColor(context, R.color.success)
            else -> ContextCompat.getColor(context, R.color.stroke_soft)
        }
        holder.habitCard.setCardBackgroundColor(cardBackgroundColor)
        holder.habitCard.strokeColor = cardStrokeColor
        holder.habitCard.strokeWidth = if (isSelected) 3 else 1

        holder.completeHabitButton.text = if (completedToday) {
            context.getString(R.string.undo_complete)
        } else {
            context.getString(R.string.complete_today)
        }
        holder.completeHabitButton.isEnabled = true

        val buttonColor = if (completedToday) {
            ContextCompat.getColor(context, R.color.danger)
        } else {
            ContextCompat.getColor(context, R.color.brand_primary)
        }
        holder.completeHabitButton.backgroundTintList = ColorStateList.valueOf(buttonColor)
        holder.completeHabitButton.alpha = 1f

        holder.selectHabitCheckbox.setOnCheckedChangeListener(null)
        holder.selectHabitCheckbox.isChecked = isSelected
        holder.selectHabitCheckbox.setOnCheckedChangeListener { _, isChecked ->
            updateSelection(habit.id, isChecked)
        }

        holder.editHabitButton.setOnClickListener { actionListener.onEdit(habit) }
        holder.deleteHabitButton.setOnClickListener { actionListener.onDelete(habit) }
        holder.completeHabitButton.setOnClickListener { actionListener.onCompleteToday(habit) }
        holder.itemView.setOnClickListener {
            holder.selectHabitCheckbox.isChecked = !holder.selectHabitCheckbox.isChecked
        }
    }

    override fun getItemCount(): Int = habits.size

    private fun updateSelection(habitId: String, isSelected: Boolean) {
        if (isSelected) {
            selectedHabitIds.add(habitId)
        } else {
            selectedHabitIds.remove(habitId)
        }
        actionListener.onSelectionChanged(selectedHabitIds.size)
    }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitCard: MaterialCardView = itemView.findViewById(R.id.habitCard)
        val selectHabitCheckbox: CheckBox = itemView.findViewById(R.id.selectHabitCheckbox)
        val habitTitleText: TextView = itemView.findViewById(R.id.habitTitleText)
        val habitNoteText: TextView = itemView.findViewById(R.id.habitNoteText)
        val habitStreakText: TextView = itemView.findViewById(R.id.habitStreakText)
        val editHabitButton: ImageButton = itemView.findViewById(R.id.editHabitButton)
        val deleteHabitButton: ImageButton = itemView.findViewById(R.id.deleteHabitButton)
        val completeHabitButton: MaterialButton = itemView.findViewById(R.id.completeHabitButton)
    }
}
