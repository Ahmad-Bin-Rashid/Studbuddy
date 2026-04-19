package com.example.studbuddy.core.models

import org.json.JSONObject

enum class ExamType { QUIZ, MIDTERM, FINAL }

data class Exam(
    val id: String,
    val courseId: String,
    val type: ExamType,
    val date: Long,
    val venue: String?,
    val totalMarks: Double,
    val obtainedMarks: Double?,
    val weightage: Double,
    val isCompleted: Boolean
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("courseId", courseId)
        obj.put("type", type.name)
        obj.put("date", date)
        obj.put("venue", venue ?: JSONObject.NULL)
        obj.put("totalMarks", totalMarks)
        obj.put("obtainedMarks", obtainedMarks ?: JSONObject.NULL)
        obj.put("weightage", weightage)
        obj.put("isCompleted", isCompleted)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): Exam {
            return Exam(
                obj.getString("id"),
                obj.getString("courseId"),
                ExamType.valueOf(obj.getString("type")),
                obj.getLong("date"),
                if (obj.isNull("venue")) null else obj.getString("venue"),
                obj.getDouble("totalMarks"),
                if (obj.isNull("obtainedMarks")) null else obj.getDouble("obtainedMarks"),
                obj.getDouble("weightage"),
                obj.getBoolean("isCompleted")
            )
        }
    }
}
