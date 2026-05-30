package com.example.studbuddy.core.migration

import android.content.Context
import com.example.studbuddy.core.SharedPrefManager
import com.example.studbuddy.core.db.StudBuddyDatabase
import com.example.studbuddy.core.models.*
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors

object LegacyMigrationHelper {
    private val executor = Executors.newSingleThreadExecutor()

    private const val KEY_SEMESTER = "studbuddy_semester"
    private const val KEY_COURSES = "studbuddy_courses"
    private const val KEY_TIMETABLE = "studbuddy_timetable"
    private const val KEY_ATTENDANCE = "studbuddy_attendance"
    private const val KEY_ASSIGNMENTS = "studbuddy_assignments"
    private const val KEY_EXAMS = "studbuddy_exams"
    private const val KEY_MIGRATED = "studbuddy_room_migrated"

    fun migrateIfNeeded(context: Context, db: StudBuddyDatabase) {
        val prefs = SharedPrefManager(context)
        if (!prefs.getBoolean(KEY_MIGRATED, false)) {
            executor.execute {
                runBlocking {
                    try {
                        // Migrate Semester
                        prefs.getString(KEY_SEMESTER)?.let {
                            db.semesterDao().insert(Semester.fromJson(JSONObject(it)))
                        }

                        // Migrate Courses
                        prefs.getString(KEY_COURSES)?.let {
                            val array = JSONArray(it)
                            (0 until array.length()).forEach { i ->
                                db.courseDao().insert(Course.fromJson(array.getJSONObject(i)))
                            }
                        }

                        // Migrate Timetable
                        prefs.getString(KEY_TIMETABLE)?.let {
                            val array = JSONArray(it)
                            (0 until array.length()).forEach { i ->
                                db.timetableDao().insert(TimetableEntry.fromJson(array.getJSONObject(i)))
                            }
                        }

                        // Migrate Attendance
                        prefs.getString(KEY_ATTENDANCE)?.let {
                            val array = JSONArray(it)
                            (0 until array.length()).forEach { i ->
                                db.attendanceDao().insert(AttendanceRecord.fromJson(array.getJSONObject(i)))
                            }
                        }

                        // Migrate Assignments
                        prefs.getString(KEY_ASSIGNMENTS)?.let {
                            val array = JSONArray(it)
                            (0 until array.length()).forEach { i ->
                                db.assignmentDao().insert(Assignment.fromJson(array.getJSONObject(i)))
                            }
                        }

                        // Migrate Exams
                        prefs.getString(KEY_EXAMS)?.let {
                            val array = JSONArray(it)
                            (0 until array.length()).forEach { i ->
                                db.examDao().insert(Exam.fromJson(array.getJSONObject(i)))
                            }
                        }

                        prefs.putBoolean(KEY_MIGRATED, true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
