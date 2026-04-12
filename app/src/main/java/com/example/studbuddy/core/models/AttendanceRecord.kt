package com.example.studbuddy.core.models

import org.json.JSONObject

/**
 * Model representing an Attendance Record.
 */
data class AttendanceRecord(
    val id: String,
    val courseName: String,
    val date: Long,
    val status: AttendanceStatus,
    val notes: String
) {
    enum class AttendanceStatus {
        PRESENT, ABSENT, LATE
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("courseName", courseName)
        json.put("date", date)
        json.put("status", status.name)
        json.put("notes", notes)
        return json
    }

    companion object {
        fun fromJson(obj: JSONObject): AttendanceRecord {
            return AttendanceRecord(
                id = obj.getString("id"),
                courseName = obj.getString("courseName"),
                date = obj.getLong("date"),
                status = AttendanceStatus.valueOf(obj.getString("status")),
                notes = obj.getString("notes")
            )
        }
    }
}
