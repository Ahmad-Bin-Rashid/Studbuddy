package com.example.studbuddy.core.models

import org.json.JSONObject

/**
 * Model representing a Semester.
 */
data class Semester(
    val id: String,
    val name: String,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name)
        json.put("startDate", startDate)
        json.put("endDate", endDate)
        json.put("isActive", isActive)
        return json
    }

    companion object {
        fun fromJson(obj: JSONObject): Semester {
            return Semester(
                id = obj.getString("id"),
                name = obj.getString("name"),
                startDate = obj.getLong("startDate"),
                endDate = obj.getLong("endDate"),
                isActive = obj.getBoolean("isActive")
            )
        }
    }
}
