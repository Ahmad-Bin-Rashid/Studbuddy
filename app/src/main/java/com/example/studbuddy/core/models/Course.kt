package com.example.studbuddy.core.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(
            entity = Semester::class,
            parentColumns = ["id"],
            childColumns = ["semesterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("semesterId")]
)
data class Course(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val instructor: String?,
    val creditHours: Int,
    val semesterId: String,
    val marks: Double,
    val grade: String?,
    val gradePoints: Double
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("description", description ?: JSONObject.NULL)
        obj.put("instructor", instructor ?: JSONObject.NULL)
        obj.put("creditHours", creditHours)
        obj.put("semesterId", semesterId)
        obj.put("marks", marks)
        obj.put("grade", grade ?: JSONObject.NULL)
        obj.put("gradePoints", gradePoints)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): Course {
            return Course(
                obj.getString("id"),
                obj.getString("name"),
                if (obj.isNull("description")) null else obj.getString("description"),
                if (obj.isNull("instructor")) null else obj.getString("instructor"),
                obj.getInt("creditHours"),
                obj.getString("semesterId"),
                obj.getDouble("marks"),
                if (obj.isNull("grade")) null else obj.getString("grade"),
                obj.getDouble("gradePoints")
            )
        }
    }
}
