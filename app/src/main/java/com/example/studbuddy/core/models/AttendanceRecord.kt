package com.example.studbuddy.core.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId")]
)
data class AttendanceRecord(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val dateTime: Long,
    val status: String // PRESENT, ABSENT, LATE
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("courseId", courseId)
        obj.put("dateTime", dateTime)
        obj.put("status", status)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): AttendanceRecord {
            return AttendanceRecord(
                obj.getString("id"),
                obj.getString("courseId"),
                obj.getLong("dateTime"),
                obj.getString("status")
            )
        }
    }
}
