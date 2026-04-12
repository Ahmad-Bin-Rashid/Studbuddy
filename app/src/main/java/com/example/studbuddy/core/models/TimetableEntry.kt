package com.example.studbuddy.core.models

import org.json.JSONObject

/**
 * Model representing a Timetable entry.
 */
data class TimetableEntry(
    val id: String,
    val courseName: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val room: String,
    val colorHex: String
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("courseName", courseName)
        json.put("dayOfWeek", dayOfWeek)
        json.put("startTime", startTime)
        json.put("endTime", endTime)
        json.put("room", room)
        json.put("colorHex", colorHex)
        return json
    }

    companion object {
        val DAY_NAMES = listOf("", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        fun fromJson(obj: JSONObject): TimetableEntry {
            return TimetableEntry(
                id = obj.getString("id"),
                courseName = obj.getString("courseName"),
                dayOfWeek = obj.getInt("dayOfWeek"),
                startTime = obj.getString("startTime"),
                endTime = obj.getString("endTime"),
                room = obj.getString("room"),
                colorHex = obj.getString("colorHex")
            )
        }
    }
}
