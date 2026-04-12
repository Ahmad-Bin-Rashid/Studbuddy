package com.example.studbuddy.core.models

import org.json.JSONObject

/**
 * Model representing an Exam.
 */
data class Exam(
    val id: String,
    val courseName: String,
    val examType: String,
    val examDate: Long,
    val venue: String,
    val durationMinutes: Int,
    val notes: String
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("courseName", courseName)
        json.put("examType", examType)
        json.put("examDate", examDate)
        json.put("venue", venue)
        json.put("durationMinutes", durationMinutes)
        json.put("notes", notes)
        return json
    }

    companion object {
        val EXAM_TYPES = listOf("Midterm", "Final", "Quiz", "Lab", "Assignment")

        fun fromJson(obj: JSONObject): Exam {
            return Exam(
                id = obj.getString("id"),
                courseName = obj.getString("courseName"),
                examType = obj.getString("examType"),
                examDate = obj.getLong("examDate"),
                venue = obj.getString("venue"),
                durationMinutes = obj.getInt("durationMinutes"),
                notes = obj.getString("notes")
            )
        }
    }
}
