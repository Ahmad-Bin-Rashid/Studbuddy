package com.example.studbuddy.core.repository

import android.content.Context
import android.util.Log
import com.example.studbuddy.core.SettingsManager
import com.example.studbuddy.core.db.StudBuddyDatabase
import com.example.studbuddy.core.models.*
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

private const val TAG = "DriveBackupRepository"
private const val BACKUP_FILE_NAME = "studbuddy_backup.json"

class DriveBackupRepository(
    private val context: Context,
    private val db: StudBuddyDatabase,
    private val settingsManager: SettingsManager
) {

    private suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext null
            val scope = "oauth2:https://www.googleapis.com/auth/drive.appdata"
            GoogleAuthUtil.getToken(context, account.account ?: return@withContext null, scope)
        } catch (e: Exception) {
            Log.e(TAG, "getAccessToken: Failed to retrieve OAuth token", e)
            null
        }
    }

    private fun findBackupFileId(token: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val query = "name = '$BACKUP_FILE_NAME' and trashed = false"
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = URL("https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=$encodedQuery&fields=files(id,name)")

            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8))
                val response = reader.readText()
                val json = JSONObject(response)
                val files = json.optJSONArray("files")
                if (files != null && files.length() > 0) {
                    files.getJSONObject(0).getString("id")
                } else {
                    null
                }
            } else {
                Log.e(TAG, "findBackupFileId: API call failed with code ${conn.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "findBackupFileId: Exception during query", e)
            null
        } finally {
            conn?.disconnect()
        }
    }

    suspend fun backup(): Boolean = withContext(Dispatchers.IO) {
        val token = getAccessToken() ?: return@withContext false
        try {
            // 1. Serialize local database tables to JSON
            val backupJson = JSONObject().apply {
                put("semesters", semestersToJson())
                put("courses", coursesToJson())
                put("timetable", timetableToJson())
                put("attendance", attendanceToJson())
                put("assignments", assignmentsToJson())
                put("exams", examsToJson())
                put("backupTime", System.currentTimeMillis())
            }

            val jsonString = backupJson.toString()
            val fileId = findBackupFileId(token)

            if (fileId != null) {
                // Update content of existing file
                val success = updateFileContent(token, fileId, jsonString)
                if (success) {
                    settingsManager.setLastSyncTime(System.currentTimeMillis())
                }
                success
            } else {
                // Create file metadata first, then upload content
                val newId = createFileMetadata(token)
                if (newId != null) {
                    val success = updateFileContent(token, newId, jsonString)
                    if (success) {
                        settingsManager.setLastSyncTime(System.currentTimeMillis())
                    }
                    success
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "backup: Error uploading backup to Drive", e)
            false
        }
    }

    private fun createFileMetadata(token: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL("https://www.googleapis.com/drive/v3/files")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")

            val metadata = JSONObject().apply {
                put("name", BACKUP_FILE_NAME)
                put("parents", JSONArray().put("appDataFolder"))
            }

            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(metadata.toString())
                writer.flush()
            }

            if (conn.responseCode == 200 || conn.responseCode == 201) {
                val response = BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).readText()
                JSONObject(response).getString("id")
            } else {
                Log.e(TAG, "createFileMetadata: Failed with code ${conn.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "createFileMetadata: Exception during creation", e)
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun updateFileContent(token: String, fileId: String, content: String): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.doOutput = true
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")

            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(content)
                writer.flush()
            }

            if (conn.responseCode == 200) {
                true
            } else {
                Log.e(TAG, "updateFileContent: Failed with code ${conn.responseCode}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateFileContent: Exception during upload", e)
            false
        } finally {
            conn?.disconnect()
        }
    }

    suspend fun restore(): Boolean = withContext(Dispatchers.IO) {
        val token = getAccessToken() ?: return@withContext false
        var conn: HttpURLConnection? = null
        try {
            val fileId = findBackupFileId(token) ?: return@withContext false
            val url = URL("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")

            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8))
                val jsonString = reader.readText()
                val backupJson = JSONObject(jsonString)

                // Save notes in-memory before database clean
                val localNotes = db.noteDao().getAll()

                // Delete local data in Room
                db.courseDao().deleteAll()
                db.semesterDao().deleteAll()
                db.timetableDao().deleteAll()
                db.attendanceDao().deleteAll()
                db.assignmentDao().deleteAll()
                db.examDao().deleteAll()

                // Restore from JSON
                restoreFromJson(backupJson)

                // Re-insert local notes that belong to the restored courses
                val restoredCourses = db.courseDao().getAll().map { it.id }.toSet()
                val validNotes = localNotes.filter { it.courseId in restoredCourses }
                if (validNotes.isNotEmpty()) {
                    db.noteDao().insertAll(validNotes)
                }
                true
            } else {
                Log.e(TAG, "restore: Download API failed with code ${conn.responseCode}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "restore: Error downloading/restoring backup from Drive", e)
            false
        } finally {
            conn?.disconnect()
        }
    }

    // ─── JSON Serializers ──────────────────────────────────────────────────

    private suspend fun semestersToJson(): JSONArray {
        val list = db.semesterDao().getAll()
        return JSONArray().apply {
            list.forEach { item ->
                put(JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("startDate", item.startDate)
                    put("endDate", item.endDate)
                    put("isActive", item.isActive)
                    put("gpa", item.gpa ?: JSONObject.NULL)
                    put("createdAt", item.createdAt)
                    put("lastModified", item.lastModified)
                })
            }
        }
    }

    private suspend fun coursesToJson(): JSONArray {
        val list = db.courseDao().getAll()
        return JSONArray().apply {
            list.forEach { item ->
                put(JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("description", item.description ?: JSONObject.NULL)
                    put("instructor", item.instructor ?: JSONObject.NULL)
                    put("creditHours", item.creditHours)
                    put("semesterId", item.semesterId)
                    put("marks", item.marks)
                    put("grade", item.grade ?: JSONObject.NULL)
                    put("gradePoints", item.gradePoints)
                    put("createdAt", item.createdAt)
                    put("lastModified", item.lastModified)
                })
            }
        }
    }

    private suspend fun timetableToJson(): JSONArray {
        val list = db.timetableDao().getAll()
        return JSONArray().apply {
            list.forEach { item ->
                put(JSONObject().apply {
                    put("id", item.id)
                    put("courseId", item.courseId)
                    put("dayOfWeek", item.dayOfWeek)
                    put("startTime", item.startTime)
                    put("endTime", item.endTime)
                    put("room", item.room)
                    put("color", item.color)
                    put("lastModified", item.lastModified)
                })
            }
        }
    }

    private suspend fun attendanceToJson(): JSONArray {
        val list = db.attendanceDao().getAll()
        return JSONArray().apply {
            list.forEach { item ->
                put(JSONObject().apply {
                    put("id", item.id)
                    put("courseId", item.courseId)
                    put("dateTime", item.dateTime)
                    put("status", item.status)
                    put("lastModified", item.lastModified)
                })
            }
        }
    }

    private suspend fun assignmentsToJson(): JSONArray {
        val list = db.assignmentDao().getAll()
        return JSONArray().apply {
            list.forEach { item ->
                put(JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("courseId", item.courseId)
                    put("dueDate", item.dueDate)
                    put("totalMarks", item.totalMarks)
                    put("obtainedMarks", item.obtainedMarks ?: JSONObject.NULL)
                    put("weightage", item.weightage)
                    put("isCompleted", item.isCompleted)
                    put("lastModified", item.lastModified)
                })
            }
        }
    }

    private suspend fun examsToJson(): JSONArray {
        val list = db.examDao().getAll()
        return JSONArray().apply {
            list.forEach { item ->
                put(JSONObject().apply {
                    put("id", item.id)
                    put("courseId", item.courseId)
                    put("type", item.type.name)
                    put("date", item.date)
                    put("venue", item.venue ?: JSONObject.NULL)
                    put("totalMarks", item.totalMarks)
                    put("obtainedMarks", item.obtainedMarks ?: JSONObject.NULL)
                    put("weightage", item.weightage)
                    put("isCompleted", item.isCompleted)
                    put("lastModified", item.lastModified)
                })
            }
        }
    }

    // ─── JSON Deserializers / Database Restorers ───────────────────────────

    private suspend fun restoreFromJson(backupJson: JSONObject) {
        val semestersArray = backupJson.optJSONArray("semesters")
        if (semestersArray != null) {
            for (i in 0 until semestersArray.length()) {
                val obj = semestersArray.getJSONObject(i)
                val semester = Semester(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    startDate = obj.getLong("startDate"),
                    endDate = obj.getLong("endDate"),
                    isActive = obj.getBoolean("isActive"),
                    gpa = if (obj.isNull("gpa")) null else obj.getDouble("gpa"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    lastModified = obj.optLong("lastModified", System.currentTimeMillis())
                )
                db.semesterDao().insert(semester)
            }
        }

        val coursesArray = backupJson.optJSONArray("courses")
        if (coursesArray != null) {
            for (i in 0 until coursesArray.length()) {
                val obj = coursesArray.getJSONObject(i)
                val course = Course(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    description = if (obj.isNull("description")) null else obj.getString("description"),
                    instructor = if (obj.isNull("instructor")) null else obj.getString("instructor"),
                    creditHours = obj.getInt("creditHours"),
                    semesterId = obj.getString("semesterId"),
                    marks = obj.optDouble("marks", 0.0),
                    grade = if (obj.isNull("grade")) null else obj.getString("grade"),
                    gradePoints = obj.optDouble("gradePoints", 0.0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    lastModified = obj.optLong("lastModified", System.currentTimeMillis())
                )
                db.courseDao().insert(course)
            }
        }

        val timetableArray = backupJson.optJSONArray("timetable")
        if (timetableArray != null) {
            for (i in 0 until timetableArray.length()) {
                val obj = timetableArray.getJSONObject(i)
                val entry = TimetableEntry(
                    id = obj.getString("id"),
                    courseId = obj.getString("courseId"),
                    dayOfWeek = obj.getInt("dayOfWeek"),
                    startTime = obj.getString("startTime"),
                    endTime = obj.getString("endTime"),
                    room = obj.optString("room", ""),
                    color = obj.optString("color", "#FF5722"),
                    lastModified = obj.optLong("lastModified", System.currentTimeMillis())
                )
                db.timetableDao().insert(entry)
            }
        }

        val attendanceArray = backupJson.optJSONArray("attendance")
        if (attendanceArray != null) {
            for (i in 0 until attendanceArray.length()) {
                val obj = attendanceArray.getJSONObject(i)
                val record = AttendanceRecord(
                    id = obj.getString("id"),
                    courseId = obj.getString("courseId"),
                    dateTime = obj.getLong("dateTime"),
                    status = obj.getString("status"),
                    lastModified = obj.optLong("lastModified", System.currentTimeMillis())
                )
                db.attendanceDao().insert(record)
            }
        }

        val assignmentsArray = backupJson.optJSONArray("assignments")
        if (assignmentsArray != null) {
            for (i in 0 until assignmentsArray.length()) {
                val obj = assignmentsArray.getJSONObject(i)
                val assignment = Assignment(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    courseId = obj.getString("courseId"),
                    dueDate = obj.getLong("dueDate"),
                    totalMarks = obj.optDouble("totalMarks", 0.0),
                    obtainedMarks = if (obj.isNull("obtainedMarks")) null else obj.getDouble("obtainedMarks"),
                    weightage = obj.optDouble("weightage", 0.0),
                    isCompleted = obj.getBoolean("isCompleted"),
                    lastModified = obj.optLong("lastModified", System.currentTimeMillis())
                )
                db.assignmentDao().insert(assignment)
            }
        }

        val examsArray = backupJson.optJSONArray("exams")
        if (examsArray != null) {
            for (i in 0 until examsArray.length()) {
                val obj = examsArray.getJSONObject(i)
                val exam = Exam(
                    id = obj.getString("id"),
                    courseId = obj.getString("courseId"),
                    type = ExamType.valueOf(obj.getString("type")),
                    date = obj.getLong("date"),
                    venue = if (obj.isNull("venue")) null else obj.getString("venue"),
                    totalMarks = obj.optDouble("totalMarks", 0.0),
                    obtainedMarks = if (obj.isNull("obtainedMarks")) null else obj.getDouble("obtainedMarks"),
                    weightage = obj.optDouble("weightage", 0.0),
                    isCompleted = obj.getBoolean("isCompleted"),
                    lastModified = obj.optLong("lastModified", System.currentTimeMillis())
                )
                db.examDao().insert(exam)
            }
        }
    }
}
