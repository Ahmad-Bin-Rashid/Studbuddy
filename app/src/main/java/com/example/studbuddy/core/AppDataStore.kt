package com.example.studbuddy.core

import android.content.Context
import com.example.studbuddy.core.models.*
import org.json.JSONArray
import org.json.JSONObject

object AppDataStore {
    private lateinit var prefs: SharedPrefManager

    // Keys
    private const val KEY_SEMESTER = "studbuddy_semester"
    private const val KEY_COURSES = "studbuddy_courses"
    private const val KEY_TIMETABLE = "studbuddy_timetable"
    private const val KEY_ATTENDANCE = "studbuddy_attendance"
    private const val KEY_ASSIGNMENTS = "studbuddy_assignments"
    private const val KEY_EXAMS = "studbuddy_exams"

    // Caches
    private var semesterCache: Semester? = null
    private var coursesCache: MutableList<Course>? = null
    private var timetableCache: MutableList<TimetableEntry>? = null
    private var attendanceCache: MutableList<AttendanceRecord>? = null
    private var assignmentsCache: MutableList<Assignment>? = null
    private var examsCache: MutableList<Exam>? = null

    fun initialize(context: Context) {
        prefs = SharedPrefManager(context.applicationContext)
    }

    // --- Semester API ---
    fun getSemester(): Semester? {
        if (semesterCache == null) {
            val json = prefs.getString(KEY_SEMESTER)
            if (json != null) {
                try {
                    semesterCache = Semester.fromJson(JSONObject(json))
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
        return semesterCache
    }

    fun saveSemester(semester: Semester) {
        semesterCache = semester
        prefs.putString(KEY_SEMESTER, semester.toJson().toString())
    }

    // --- Course API ---
    fun getCourses(): List<Course> {
        if (coursesCache == null) {
            val json = prefs.getString(KEY_COURSES)
            coursesCache = if (json != null) {
                try {
                    val array = JSONArray(json)
                    (0 until array.length()).map { Course.fromJson(array.getJSONObject(it)) }.toMutableList()
                } catch (e: Exception) { mutableListOf() }
            } else {
                mutableListOf()
            }
        }
        return coursesCache!!
    }

    fun addCourse(course: Course) {
        if (coursesCache == null) getCourses()
        coursesCache!!.add(course)
        saveCourses()
    }

    fun updateCourse(course: Course) {
        if (coursesCache == null) getCourses()
        val index = coursesCache!!.indexOfFirst { it.id == course.id }
        if (index != -1) {
            coursesCache!![index] = course
            saveCourses()
        }
    }

    private fun saveCourses() {
        val array = JSONArray()
        coursesCache?.forEach { array.put(it.toJson()) }
        prefs.putString(KEY_COURSES, array.toString())
    }

    // --- Timetable API ---
    fun getTimetable(): List<TimetableEntry> {
        if (timetableCache == null) {
            val json = prefs.getString(KEY_TIMETABLE)
            timetableCache = if (json != null) {
                try {
                    val array = JSONArray(json)
                    (0 until array.length()).map { TimetableEntry.fromJson(array.getJSONObject(it)) }.toMutableList()
                } catch (e: Exception) { mutableListOf() }
            } else {
                mutableListOf()
            }
        }
        return timetableCache!!
    }

    fun addTimetableEntry(entry: TimetableEntry) {
        if (timetableCache == null) getTimetable()
        timetableCache!!.add(entry)
        saveTimetable()
    }

    fun updateTimetableEntry(entry: TimetableEntry) {
        if (timetableCache == null) getTimetable()
        val index = timetableCache!!.indexOfFirst { it.id == entry.id }
        if (index != -1) {
            timetableCache!![index] = entry
            saveTimetable()
        }
    }

    fun deleteTimetableEntry(entryId: String) {
        if (timetableCache == null) getTimetable()
        timetableCache!!.removeAll { it.id == entryId }
        saveTimetable()
    }

    private fun saveTimetable() {
        val array = JSONArray()
        timetableCache?.forEach { array.put(it.toJson()) }
        prefs.putString(KEY_TIMETABLE, array.toString())
    }

    // --- Attendance API ---
    fun getAttendance(): List<AttendanceRecord> {
        if (attendanceCache == null) {
            val json = prefs.getString(KEY_ATTENDANCE)
            attendanceCache = if (json != null) {
                try {
                    val array = JSONArray(json)
                    (0 until array.length()).map { AttendanceRecord.fromJson(array.getJSONObject(it)) }.toMutableList()
                } catch (e: Exception) { mutableListOf() }
            } else {
                mutableListOf()
            }
        }
        return attendanceCache!!
    }

    fun addAttendanceRecord(record: AttendanceRecord) {
        if (attendanceCache == null) getAttendance()
        attendanceCache!!.add(record)
        saveAttendance()
    }

    fun updateAttendanceRecord(record: AttendanceRecord) {
        if (attendanceCache == null) getAttendance()
        val index = attendanceCache!!.indexOfFirst { it.id == record.id }
        if (index != -1) {
            attendanceCache!![index] = record
            saveAttendance()
        }
    }

    fun deleteAttendanceRecord(recordId: String) {
        if (attendanceCache == null) getAttendance()
        attendanceCache!!.removeAll { it.id == recordId }
        saveAttendance()
    }

    private fun saveAttendance() {
        val array = JSONArray()
        attendanceCache?.forEach { array.put(it.toJson()) }
        prefs.putString(KEY_ATTENDANCE, array.toString())
    }

    // --- Assignment API ---
    fun getAssignments(): List<Assignment> {
        if (assignmentsCache == null) {
            val json = prefs.getString(KEY_ASSIGNMENTS)
            assignmentsCache = if (json != null) {
                try {
                    val array = JSONArray(json)
                    (0 until array.length()).map { Assignment.fromJson(array.getJSONObject(it)) }.toMutableList()
                } catch (e: Exception) { mutableListOf() }
            } else {
                mutableListOf()
            }
        }
        return assignmentsCache!!
    }

    fun updateAssignment(assignment: Assignment) {
        if (assignmentsCache == null) getAssignments()
        val index = assignmentsCache!!.indexOfFirst { it.id == assignment.id }
        if (index != -1) {
            assignmentsCache!![index] = assignment
        } else {
            assignmentsCache!!.add(assignment)
        }
        saveAssignments()
    }

    fun deleteAssignment(assignmentId: String) {
        if (assignmentsCache == null) getAssignments()
        assignmentsCache!!.removeAll { it.id == assignmentId }
        saveAssignments()
    }

    private fun saveAssignments() {
        val array = JSONArray()
        assignmentsCache?.forEach { array.put(it.toJson()) }
        prefs.putString(KEY_ASSIGNMENTS, array.toString())
    }

    // --- Exam API ---
    fun getExams(): List<Exam> {
        if (examsCache == null) {
            val json = prefs.getString(KEY_EXAMS)
            examsCache = if (json != null) {
                try {
                    val array = JSONArray(json)
                    (0 until array.length()).map { Exam.fromJson(array.getJSONObject(it)) }.toMutableList()
                } catch (e: Exception) { mutableListOf() }
            } else {
                mutableListOf()
            }
        }
        return examsCache!!
    }

    fun updateExam(exam: Exam) {
        if (examsCache == null) getExams()
        val index = examsCache!!.indexOfFirst { it.id == exam.id }
        if (index != -1) {
            examsCache!![index] = exam
        } else {
            examsCache!!.add(exam)
        }
        saveExams()
    }

    fun deleteExam(examId: String) {
        if (examsCache == null) getExams()
        examsCache!!.removeAll { it.id == examId }
        saveExams()
    }

    private fun saveExams() {
        val array = JSONArray()
        examsCache?.forEach { array.put(it.toJson()) }
        prefs.putString(KEY_EXAMS, array.toString())
    }

    fun clearAll() {
        semesterCache = null
        coursesCache = null
        timetableCache = null
        attendanceCache = null
        assignmentsCache = null
        examsCache = null
        prefs.clearAll()
    }
}
