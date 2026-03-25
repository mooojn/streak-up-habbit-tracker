package com.example.streakup_habbit_tracker.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.google.android.material.card.MaterialCardView

class TrackerHeatmapAdapter : RecyclerView.Adapter<TrackerHeatmapAdapter.TrackerViewHolder>() {

    private val cells = mutableListOf<TrackerDayCell>()
    private var maxCountInView: Int = 0

    fun submitData(updatedCells: List<TrackerDayCell>, maxCount: Int) {
        cells.clear()
        cells.addAll(updatedCells)
        maxCountInView = maxCount
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tracker_day, parent, false)
        return TrackerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackerViewHolder, position: Int) {
        val cell = cells[position]
        val context = holder.itemView.context

        if (cell.dateKey == null) {
            holder.dayText.text = ""
            holder.dayText.visibility = View.GONE
            holder.dayCard.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, android.R.color.transparent)
            )
            holder.dayCard.strokeWidth = 0
            holder.dayCard.isClickable = false
            return
        }

        holder.dayText.visibility = View.VISIBLE
        holder.dayText.text = cell.dayLabel

        val levelColorRes = resolveLevelColor(cell.completionCount)
        holder.dayCard.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, levelColorRes))

        if (cell.isToday) {
            holder.dayCard.strokeWidth = 2
            holder.dayCard.strokeColor = ContextCompat.getColor(context, R.color.brand_secondary)
        } else {
            holder.dayCard.strokeWidth = 1
            holder.dayCard.strokeColor = ContextCompat.getColor(context, R.color.stroke_soft)
        }

        val dayNumber = cell.dayLabel.toIntOrNull() ?: 0
        val description = context.resources.getQuantityString(
            R.plurals.tracker_day_completion_content,
            cell.completionCount,
            dayNumber,
            cell.completionCount
        )
        holder.dayCard.contentDescription = description
    }

    override fun getItemCount(): Int = cells.size

    private fun resolveLevelColor(count: Int): Int {
        if (count <= 0) return R.color.tracker_level_0

        if (maxCountInView <= 1) {
            return R.color.tracker_level_2
        }

        val ratio = count.toFloat() / maxCountInView.toFloat()
        return when {
            ratio >= 0.85f -> R.color.tracker_level_4
            ratio >= 0.60f -> R.color.tracker_level_3
            ratio >= 0.35f -> R.color.tracker_level_2
            else -> R.color.tracker_level_1
        }
    }

    class TrackerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayCard: MaterialCardView = itemView.findViewById(R.id.trackerDayCard)
        val dayText: TextView = itemView.findViewById(R.id.trackerDayText)
    }
}
