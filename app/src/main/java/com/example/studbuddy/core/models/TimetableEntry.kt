package com.example.studbuddy.core.models

import org.json.JSONObject

data class TimetableEntry(
    val id: String,             // UUID
    val courseName: String,     // e.g. "CS101"
    val dayOfWeek: Int,         // 1=Monday ... 7=Sunday
    val startTime: String,      // "HH:mm" 24-hour format
    val endTime: String,        // "HH:mm" 24-hour format
    val room: String,           // e.g. "Room 204" — optional, can be empty
    val color: String           // hex color string e.g. "#1565C0"
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("courseName", courseName)
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
                obj.getString("courseName"),
                obj.getInt("dayOfWeek"),
                obj.getString("startTime"),
                obj.getString("endTime"),
                obj.getString("room"),
                obj.getString("color")
            )
        }
    }
}
