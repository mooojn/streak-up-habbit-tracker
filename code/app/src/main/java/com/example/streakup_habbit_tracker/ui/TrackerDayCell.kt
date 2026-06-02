package com.example.streakup_habbit_tracker.ui

data class TrackerDayCell(
    val dayLabel: String,
    val dateKey: String?,
    val completionCount: Int,
    val isToday: Boolean
)
