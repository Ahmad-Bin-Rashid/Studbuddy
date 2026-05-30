package com.example.studbuddy.core.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject
import java.util.UUID

@Entity(tableName = "semesters")
data class Semester(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean = true,
    val gpa: Double = 0.0
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("startDate", startDate)
        obj.put("endDate", endDate)
        obj.put("isActive", isActive)
        obj.put("gpa", gpa)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): Semester {
            return Semester(
                obj.optString("id", UUID.randomUUID().toString()),
                obj.optString("name", "Unnamed Semester"),
                obj.getLong("startDate"),
                obj.getLong("endDate"),
                obj.optBoolean("isActive", true),
                obj.optDouble("gpa", 0.0)
            )
        }
    }
}
