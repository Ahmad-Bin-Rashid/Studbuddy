package com.example.studbuddy.core.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(
    tableName = "timetable_entries",
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
data class TimetableEntry(
    @PrimaryKey
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
