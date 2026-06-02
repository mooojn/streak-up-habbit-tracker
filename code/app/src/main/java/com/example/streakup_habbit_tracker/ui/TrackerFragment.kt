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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import androidx.core.content.ContextCompat

class TrackerFragment : Fragment() {

    private val monthTitleFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dateKeyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private var currentMonth = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }

    private var monthTitleText: TextView? = null
    private var trackerRecyclerView: RecyclerView? = null
    private var badgesRecyclerView: RecyclerView? = null
    private var completionBarChart: BarChart? = null
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
        badgesRecyclerView = view.findViewById(R.id.badgesRecyclerView)
        completionBarChart = view.findViewById(R.id.completionBarChart)
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
        renderBadges()
        renderChart()
    }

    private fun renderBadges() {
        val badges = HabitRepository.getBadges()
        badgesRecyclerView?.adapter = BadgeAdapter(badges)
    }

    private fun renderChart() {
        val chart = completionBarChart ?: return
        
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        
        val displayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
        
        for (i in 0..6) {
            val dateKey = dateKeyFormatter.format(calendar.time)
            val count = HabitRepository.getCompletionCountByDate(dateKey)
            entries.add(BarEntry(i.toFloat(), count.toFloat()))
            labels.add(displayFormatter.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val dataSet = BarDataSet(entries, "Completions")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.brand_primary)
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
        dataSet.valueTextSize = 10f
        
        val barData = BarData(dataSet)
        barData.barWidth = 0.6f
        
        chart.data = barData
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.setTouchEnabled(false)
        
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        
        val yAxis = chart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.granularity = 1f
        yAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        yAxis.setDrawGridLines(true)
        yAxis.gridColor = ContextCompat.getColor(requireContext(), R.color.stroke_soft)
        
        chart.invalidate()
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
