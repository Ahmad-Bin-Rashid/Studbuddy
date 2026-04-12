package com.example.studbuddy.core.models

import org.json.JSONObject

/**
 * Model representing a Course.
 */
data class Course(
    val id: String,
    val name: String,
    val creditHours: Int,
    val grade: String,
    val gradePoints: Double,
    val semesterId: String,
    val colorHex: String
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name)
        json.put("creditHours", creditHours)
        json.put("grade", grade)
        json.put("gradePoints", gradePoints)
        json.put("semesterId", semesterId)
        json.put("colorHex", colorHex)
        return json
    }

    companion object {
        val GRADE_OPTIONS = listOf("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F")
        
        val GRADE_POINTS_MAP = mapOf(
            "A" to 4.0, "A-" to 3.7, "B+" to 3.3, "B" to 3.0, "B-" to 2.7,
            "C+" to 2.3, "C" to 2.0, "C-" to 1.7, "D+" to 1.3, "D" to 1.0, "F" to 0.0
        )

        fun gradeToPoints(grade: String): Double {
            return GRADE_POINTS_MAP[grade] ?: 0.0
        }

        fun fromJson(obj: JSONObject): Course {
            return Course(
                id = obj.getString("id"),
                name = obj.getString("name"),
                creditHours = obj.getInt("creditHours"),
                grade = obj.getString("grade"),
                gradePoints = obj.getDouble("gradePoints"),
                semesterId = obj.getString("semesterId"),
                colorHex = obj.getString("colorHex")
            )
        }
    }
}
