package com.example.streakup_habbit_tracker.data

import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var note: String,
    var streakCount: Int = 0,
    var lastCompletedDate: String = "",
    var previousStreakCount: Int = 0,
    var previousLastCompletedDate: String = "",
    var isFlexible: Boolean = false,
    var targetValue: Int = 1,
    var currentValue: Int = 0,
    var unit: String = ""
)
