package com.example.studbuddy.core.models

import org.json.JSONObject

/**
 * Model representing an Assignment.
 */
data class Assignment(
    val id: String,
    val title: String,
    val courseName: String,
    val dueDate: Long,
    val priority: Priority,
    val description: String,
    val isCompleted: Boolean
) {
    enum class Priority {
        LOW, MEDIUM, HIGH
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("courseName", courseName)
        json.put("dueDate", dueDate)
        json.put("priority", priority.name)
        json.put("description", description)
        json.put("isCompleted", isCompleted)
        return json
    }

    companion object {
        fun fromJson(obj: JSONObject): Assignment {
            return Assignment(
                id = obj.getString("id"),
                title = obj.getString("title"),
                courseName = obj.getString("courseName"),
                dueDate = obj.getLong("dueDate"),
                priority = Priority.valueOf(obj.getString("priority")),
                description = obj.getString("description"),
                isCompleted = obj.getBoolean("isCompleted")
            )
        }
    }
}
