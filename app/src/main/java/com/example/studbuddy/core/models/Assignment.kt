package com.example.studbuddy.core.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(
    tableName = "assignments",
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
data class Assignment(
    @PrimaryKey
    val id: String,
    val name: String,
    val courseId: String,
    val dueDate: Long,
    val totalMarks: Double,
    val obtainedMarks: Double?,
    val weightage: Double,
    val isCompleted: Boolean
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("courseId", courseId)
        obj.put("dueDate", dueDate)
        obj.put("totalMarks", totalMarks)
        obj.put("obtainedMarks", obtainedMarks ?: JSONObject.NULL)
        obj.put("weightage", weightage)
        obj.put("isCompleted", isCompleted)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): Assignment {
            return Assignment(
                obj.getString("id"),
                obj.optString("name", obj.optString("title", "")),
                obj.getString("courseId"),
                obj.getLong("dueDate"),
                obj.getDouble("totalMarks"),
                if (obj.isNull("obtainedMarks")) null else obj.getDouble("obtainedMarks"),
                obj.getDouble("weightage"),
                obj.getBoolean("isCompleted")
            )
        }
    }
}
