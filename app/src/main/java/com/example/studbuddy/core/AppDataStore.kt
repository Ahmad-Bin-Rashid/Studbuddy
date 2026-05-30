package com.example.studbuddy.core

import android.content.Context
import com.example.studbuddy.core.db.StudBuddyDatabase
import com.example.studbuddy.core.models.*
import com.example.studbuddy.core.repository.StudBuddyRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors

object AppDataStore {
    private lateinit var repository: StudBuddyRepository
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
        val db = StudBuddyDatabase.getDatabase(context)
        repository = StudBuddyRepository(db)
        migrateIfNeeded(context, db)
    }

    private fun migrateIfNeeded(context: Context, db: StudBuddyDatabase) {
        val prefs = SharedPrefManager(context)
        if (!prefs.getBoolean(KEY_MIGRATED, false)) {
            executor.execute {
                try {
                    // Migration logic remains the same, using DAOs directly for this one-time task
                    // to avoid potential issues with repository state during initialization.
                    
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
        repository.getSemester()
    }

    fun saveSemester(semester: Semester) = runBlocking {
        repository.saveSemester(semester)
    }

    // --- Course API ---
    fun getCourses(): List<Course> = runBlocking {
        repository.getCourses()
    }

    fun addCourse(course: Course) = runBlocking {
        repository.addCourse(course)
    }

    fun updateCourse(course: Course) = runBlocking {
        repository.updateCourse(course)
    }

    // --- Timetable API ---
    fun getTimetable(): List<TimetableEntry> = runBlocking {
        repository.getTimetable()
    }

    fun addTimetableEntry(entry: TimetableEntry) = runBlocking {
        repository.addTimetableEntry(entry)
    }

    fun updateTimetableEntry(entry: TimetableEntry) = runBlocking {
        repository.updateTimetableEntry(entry)
    }

    fun deleteTimetableEntry(entryId: String) = runBlocking {
        repository.deleteTimetableEntry(entryId)
    }

    // --- Attendance API ---
    fun getAttendance(): List<AttendanceRecord> = runBlocking {
        repository.getAttendance()
    }

    fun addAttendanceRecord(record: AttendanceRecord) = runBlocking {
        repository.addAttendanceRecord(record)
    }

    fun updateAttendanceRecord(record: AttendanceRecord) = runBlocking {
        repository.updateAttendanceRecord(record)
    }

    fun deleteAttendanceRecord(recordId: String) = runBlocking {
        repository.deleteAttendanceRecord(recordId)
    }

    // --- Assignment API ---
    fun getAssignments(): List<Assignment> = runBlocking {
        repository.getAssignments()
    }

    fun updateAssignment(assignment: Assignment) = runBlocking {
        repository.updateAssignment(assignment)
    }

    fun deleteAssignment(assignmentId: String) = runBlocking {
        repository.deleteAssignment(assignmentId)
    }

    // --- Exam API ---
    fun getExams(): List<Exam> = runBlocking {
        repository.getExams()
    }

    fun updateExam(exam: Exam) = runBlocking {
        repository.updateExam(exam)
    }

    fun deleteExam(examId: String) = runBlocking {
        repository.deleteExam(examId)
    }

    fun clearAll() = runBlocking {
        repository.clearAll()
    }
}
