package com.example.studbuddy.core.models

import org.json.JSONObject

enum class Priority { LOW, MEDIUM, HIGH }

data class Assignment(
    val id: String,
    val title: String,
    val courseName: String,
    val dueDate: Long,
    val priority: Priority,
    val description: String,
    val isCompleted: Boolean,
    val weightage: Double,      // 0.0–100.0
    val obtainedMarks: Double,  // -1.0 = not graded
    val totalMarks: Double      // max marks
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("courseName", courseName)
        put("dueDate", dueDate)
        put("priority", priority.name)
        put("description", description)
        put("isCompleted", isCompleted)
        put("weightage", weightage)
        put("obtainedMarks", obtainedMarks)
        put("totalMarks", totalMarks)
    }

    companion object {
        fun fromJson(obj: JSONObject): Assignment = Assignment(
            id = obj.getString("id"),
            title = obj.getString("title"),
            courseName = obj.getString("courseName"),
            dueDate = obj.getLong("dueDate"),
            priority = Priority.valueOf(obj.getString("priority")),
            description = obj.optString("description", ""),
            isCompleted = obj.getBoolean("isCompleted"),
            weightage = obj.optDouble("weightage", 0.0),
            obtainedMarks = obj.optDouble("obtainedMarks", -1.0),
            totalMarks = obj.optDouble("totalMarks", 100.0)
        )
    }
}
