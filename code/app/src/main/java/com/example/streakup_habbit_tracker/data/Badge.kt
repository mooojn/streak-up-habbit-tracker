package com.example.streakup_habbit_tracker.data

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val requiredStreak: Int,
    var isUnlocked: Boolean = false
)
