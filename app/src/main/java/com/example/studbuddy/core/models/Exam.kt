package com.example.studbuddy.core.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONObject

enum class ExamType { QUIZ, MIDTERM, FINAL }

@Entity(
    tableName = "exams",
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
data class Exam(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val type: ExamType,
    val date: Long,
    val venue: String?,
    val totalMarks: Double,
    val obtainedMarks: Double?,
    val weightage: Double,
    val isCompleted: Boolean
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("courseId", courseId)
        obj.put("type", type.name)
        obj.put("date", date)
        obj.put("venue", venue ?: JSONObject.NULL)
        obj.put("totalMarks", totalMarks)
        obj.put("obtainedMarks", obtainedMarks ?: JSONObject.NULL)
        obj.put("weightage", weightage)
        obj.put("isCompleted", isCompleted)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): Exam {
            return Exam(
                obj.getString("id"),
                obj.getString("courseId"),
                ExamType.valueOf(obj.getString("type")),
                obj.getLong("date"),
                if (obj.isNull("venue")) null else obj.getString("venue"),
                obj.getDouble("totalMarks"),
                if (obj.isNull("obtainedMarks")) null else obj.getDouble("obtainedMarks"),
                obj.getDouble("weightage"),
                obj.getBoolean("isCompleted")
            )
        }
    }
}
