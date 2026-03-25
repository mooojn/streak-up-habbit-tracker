package com.example.streakup_habbit_tracker.data

import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var note: String
)
