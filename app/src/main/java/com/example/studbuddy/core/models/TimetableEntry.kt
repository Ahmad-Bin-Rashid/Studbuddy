package com.example.studbuddy.core.models

import org.json.JSONObject

data class TimetableEntry(
    val id: String,
    val courseId: String,       // Reference to Course
    val dayOfWeek: Int,         // 1=Monday ... 7=Sunday
    val startTime: String,      // "HH:mm"
    val endTime: String,        // "HH:mm"
    val room: String,
    val color: String
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("courseId", courseId)
        obj.put("dayOfWeek", dayOfWeek)
        obj.put("startTime", startTime)
        obj.put("endTime", endTime)
        obj.put("room", room)
        obj.put("color", color)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): TimetableEntry {
            return TimetableEntry(
                obj.getString("id"),
                obj.getString("courseId"),
                obj.getInt("dayOfWeek"),
                obj.getString("startTime"),
                obj.getString("endTime"),
                obj.getString("room"),
                obj.getString("color")
            )
        }
    }
}
