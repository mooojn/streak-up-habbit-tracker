package com.example.streakup_habbit_tracker.data

import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var body: String,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = createdAt
)
