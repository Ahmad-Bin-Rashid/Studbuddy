package com.example.studbuddy.core

import android.content.Context
import com.example.studbuddy.core.db.StudBuddyDatabase
import com.example.studbuddy.core.models.*
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors

object AppDataStore {
    private lateinit var db: StudBuddyDatabase
    private val executor = Executors.newSingleThreadExecutor()

    // Legacy Keys for Migration
    private const val KEY_SEMESTER = "studbuddy_semester"
    private const val KEY_COURSES = "studbuddy_courses"
    private const val KEY_TIMETABLE = "studbuddy_timetable"
    private const val KEY_ATTENDANCE = "studbuddy_attendance"
    private const val KEY_ASSIGNMENTS = "studbuddy_assignments"
    private const val KEY_EXAMS = "studbuddy_exams"
    private const val KEY_MIGRATED = "studbuddy_room_migrated"

    fun initialize(context: Context) {
        db = StudBuddyDatabase.getDatabase(context)
        migrateIfNeeded(context)
    }

    private fun migrateIfNeeded(context: Context) {
        val prefs = SharedPrefManager(context)
        if (!prefs.getBoolean(KEY_MIGRATED, false)) {
            executor.execute {
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

    // --- Semester API ---
    fun getSemester(): Semester? = runBlocking {
        db.semesterDao().getSemester()
    }

    fun saveSemester(semester: Semester) = runBlocking {
        db.semesterDao().insert(semester)
    }

    // --- Course API ---
    fun getCourses(): List<Course> = runBlocking {
        db.courseDao().getAll()
    }

    fun addCourse(course: Course) = runBlocking {
        db.courseDao().insert(course)
    }

    fun updateCourse(course: Course) = runBlocking {
        db.courseDao().update(course)
    }

    // --- Timetable API ---
    fun getTimetable(): List<TimetableEntry> = runBlocking {
        db.timetableDao().getAll()
    }

    fun addTimetableEntry(entry: TimetableEntry) = runBlocking {
        db.timetableDao().insert(entry)
    }

    fun updateTimetableEntry(entry: TimetableEntry) = runBlocking {
        db.timetableDao().update(entry)
    }

    fun deleteTimetableEntry(entryId: String) = runBlocking {
        db.timetableDao().deleteById(entryId)
    }

    // --- Attendance API ---
    fun getAttendance(): List<AttendanceRecord> = runBlocking {
        db.attendanceDao().getAll()
    }

    fun addAttendanceRecord(record: AttendanceRecord) = runBlocking {
        db.attendanceDao().insert(record)
    }

    fun updateAttendanceRecord(record: AttendanceRecord) = runBlocking {
        db.attendanceDao().update(record)
    }

    fun deleteAttendanceRecord(recordId: String) = runBlocking {
        db.attendanceDao().deleteById(recordId)
    }

    // --- Assignment API ---
    fun getAssignments(): List<Assignment> = runBlocking {
        db.assignmentDao().getAll()
    }

    fun updateAssignment(assignment: Assignment) = runBlocking {
        db.assignmentDao().insert(assignment)
    }

    fun deleteAssignment(assignmentId: String) = runBlocking {
        db.assignmentDao().deleteById(assignmentId)
    }

    // --- Exam API ---
    fun getExams(): List<Exam> = runBlocking {
        db.examDao().getAll()
    }

    fun updateExam(exam: Exam) = runBlocking {
        db.examDao().insert(exam)
    }

    fun deleteExam(examId: String) = runBlocking {
        db.examDao().deleteById(examId)
    }

    fun clearAll() = runBlocking {
        db.semesterDao().deleteAll()
        db.courseDao().deleteAll()
        db.timetableDao().deleteAll()
        db.attendanceDao().deleteAll()
        db.assignmentDao().deleteAll()
        db.examDao().deleteAll()
    }
}
