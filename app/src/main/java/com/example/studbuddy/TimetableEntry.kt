package com.example.studbuddy

data class TimetableEntry(
    val id: String,
    val subject: String,
    val teacher: String,
    val room: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val colorTag: Int
)
