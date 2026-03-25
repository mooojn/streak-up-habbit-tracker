package com.example.streakup_habbit_tracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.streakup_habbit_tracker.R
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TrackerFragment : Fragment() {

    private val monthTitleFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dateKeyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private var currentMonth = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }

    private var monthTitleText: TextView? = null
    private var trackerRecyclerView: RecyclerView? = null
    private lateinit var trackerHeatmapAdapter: TrackerHeatmapAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tracker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        monthTitleText = view.findViewById(R.id.trackerMonthTitle)
        trackerRecyclerView = view.findViewById(R.id.trackerRecyclerView)
        val previousButton: MaterialButton = view.findViewById(R.id.previousMonthButton)
        val nextButton: MaterialButton = view.findViewById(R.id.nextMonthButton)

        trackerHeatmapAdapter = TrackerHeatmapAdapter()
        trackerRecyclerView?.layoutManager = GridLayoutManager(requireContext(), 7)
        trackerRecyclerView?.adapter = trackerHeatmapAdapter

        previousButton.setOnClickListener {
            currentMonth.add(Calendar.MONTH, -1)
            currentMonth.set(Calendar.DAY_OF_MONTH, 1)
            renderMonth()
        }

        nextButton.setOnClickListener {
            currentMonth.add(Calendar.MONTH, 1)
            currentMonth.set(Calendar.DAY_OF_MONTH, 1)
            renderMonth()
        }

        renderMonth()
    }

    override fun onResume() {
        super.onResume()
        renderMonth()
    }

    private fun renderMonth() {
        monthTitleText?.text = monthTitleFormatter.format(currentMonth.time)

        val cells = buildMonthCells(currentMonth)
        val maxCount = cells.maxOfOrNull { it.completionCount } ?: 0

        trackerHeatmapAdapter.submitData(cells, maxCount)
    }

    private fun buildMonthCells(month: Calendar): List<TrackerDayCell> {
        val cells = mutableListOf<TrackerDayCell>()

        val monthStart = month.clone() as Calendar
        monthStart.set(Calendar.DAY_OF_MONTH, 1)

        val offset = monthStart.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        repeat(offset) {
            cells.add(TrackerDayCell(dayLabel = "", dateKey = null, completionCount = 0, isToday = false))
        }

        val totalDays = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH)
        val todayKey = dateKeyFormatter.format(Calendar.getInstance().time)

        for (day in 1..totalDays) {
            val dayCalendar = monthStart.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, day)

            val dateKey = dateKeyFormatter.format(dayCalendar.time)
            val count = HabitRepository.getCompletionCountByDate(dateKey)

            cells.add(
                TrackerDayCell(
                    dayLabel = day.toString(),
                    dateKey = dateKey,
                    completionCount = count,
                    isToday = dateKey == todayKey
                )
            )
        }

        val trailing = (7 - (cells.size % 7)) % 7
        repeat(trailing) {
            cells.add(TrackerDayCell(dayLabel = "", dateKey = null, completionCount = 0, isToday = false))
        }

        return cells
    }
}
