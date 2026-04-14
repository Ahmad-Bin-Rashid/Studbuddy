package com.example.studbuddy.core.models

import org.json.JSONObject

data class Exam(
    val id: String,
    val courseName: String,
    val examType: String,
    val examDate: Long,
    val venue: String,
    val durationMinutes: Int,
    val notes: String,
    val weightage: Double,      // 0.0–100.0
    val obtainedMarks: Double,  // -1.0 = not taken yet
    val totalMarks: Double
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("courseName", courseName)
        put("examType", examType)
        put("examDate", examDate)
        put("venue", venue)
        put("durationMinutes", durationMinutes)
        put("notes", notes)
        put("weightage", weightage)
        put("obtainedMarks", obtainedMarks)
        put("totalMarks", totalMarks)
    }

    companion object {
        val EXAM_TYPES = listOf("Quiz", "Midterm", "Final", "Lab", "Presentation")

        fun fromJson(obj: JSONObject): Exam = Exam(
            id = obj.getString("id"),
            courseName = obj.getString("courseName"),
            examType = obj.getString("examType"),
            examDate = obj.getLong("examDate"),
            venue = obj.optString("venue", ""),
            durationMinutes = obj.optInt("durationMinutes", 90),
            notes = obj.optString("notes", ""),
            weightage = obj.optDouble("weightage", 0.0),
            obtainedMarks = obj.optDouble("obtainedMarks", -1.0),
            totalMarks = obj.optDouble("totalMarks", 100.0)
        )
    }
}
