package com.example.studbuddy.core

import android.content.Context
import com.example.studbuddy.core.models.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/**
 * Single source of truth for application data.
 * Handles in-memory caching and persistence via SharedPrefManager.
 */
object AppDataStore {

    private lateinit var prefs: SharedPrefManager

    // ── Cache ──────────────────────────────────────────────────────
    private var semesterList: MutableList<Semester>? = null
    private var courseList: MutableList<Course>? = null
    private var assignmentList: MutableList<Assignment>? = null
    private var attendanceList: MutableList<AttendanceRecord>? = null
    private var examList: MutableList<Exam>? = null
    private var timetableList: MutableList<TimetableEntry>? = null

    // ── Keys ───────────────────────────────────────────────────────
    private const val KEY_SEMESTERS = "studbuddy_semesters"
    private const val KEY_COURSES = "studbuddy_courses"
    private const val KEY_ASSIGNMENTS = "studbuddy_assignments"
    private const val KEY_ATTENDANCE = "studbuddy_attendance"
    private const val KEY_EXAMS = "studbuddy_exams"
    private const val KEY_TIMETABLE = "studbuddy_timetable"

    fun initialize(context: Context) {
        prefs = SharedPrefManager(context)
    }

    // ── Semester API ───────────────────────────────────────────────
    fun getSemesterList(): List<Semester> {
        if (semesterList == null) {
            semesterList = loadList(KEY_SEMESTERS) { Semester.fromJson(it) }
        }
        return semesterList!!.toList()
    }

    fun addSemester(s: Semester) {
        getSemesterList() // Ensure loaded
        semesterList?.add(s)
        saveList(KEY_SEMESTERS, semesterList)
    }

    fun updateSemester(s: Semester) {
        getSemesterList()
        val index = semesterList?.indexOfFirst { it.id == s.id } ?: -1
        if (index != -1) {
            semesterList?.set(index, s)
            saveList(KEY_SEMESTERS, semesterList)
        }
    }

    fun deleteSemester(id: String) {
        getSemesterList()
        semesterList?.removeAll { it.id == id }
        saveList(KEY_SEMESTERS, semesterList)
    }

    fun getActiveSemester(): Semester? {
        return getSemesterList().find { it.isActive }
    }

    // ── Course API ──────────────────────────────────────────────────
    fun getCourseList(): List<Course> {
        if (courseList == null) {
            courseList = loadList(KEY_COURSES) { Course.fromJson(it) }
        }
        return courseList!!.toList()
    }

    fun getCoursesBySemester(semesterId: String): List<Course> {
        return getCourseList().filter { it.semesterId == semesterId }
    }

    fun getCourseNames(): List<String> {
        return getCourseList().map { it.name }.distinct().sorted()
    }

    fun addCourse(c: Course) {
        getCourseList()
        courseList?.add(c)
        saveList(KEY_COURSES, courseList)
    }

    fun updateCourse(c: Course) {
        getCourseList()
        val index = courseList?.indexOfFirst { it.id == c.id } ?: -1
        if (index != -1) {
            courseList?.set(index, c)
            saveList(KEY_COURSES, courseList)
        }
    }

    fun deleteCourse(id: String) {
        getCourseList()
        courseList?.removeAll { it.id == id }
        saveList(KEY_COURSES, courseList)
    }

    fun calculateGpa(semesterId: String? = null): Double {
        val courses = if (semesterId != null) getCoursesBySemester(semesterId) else getCourseList()
        if (courses.isEmpty()) return 0.0
        
        var totalPoints = 0.0
        var totalCredits = 0
        
        for (course in courses) {
            totalPoints += (course.gradePoints * course.creditHours)
            totalCredits += course.creditHours
        }
        
        return if (totalCredits == 0) 0.0 else totalPoints / totalCredits
    }

    // ── Assignment API ──────────────────────────────────────────────
    fun getAssignmentList(): List<Assignment> {
        if (assignmentList == null) {
            assignmentList = loadList(KEY_ASSIGNMENTS) { Assignment.fromJson(it) }
        }
        return assignmentList!!.toList()
    }

    fun addAssignment(a: Assignment) {
        getAssignmentList()
        assignmentList?.add(a)
        saveList(KEY_ASSIGNMENTS, assignmentList)
    }

    fun updateAssignment(a: Assignment) {
        getAssignmentList()
        val index = assignmentList?.indexOfFirst { it.id == a.id } ?: -1
        if (index != -1) {
            assignmentList?.set(index, a)
            saveList(KEY_ASSIGNMENTS, assignmentList)
        }
    }

    fun deleteAssignment(id: String) {
        getAssignmentList()
        assignmentList?.removeAll { it.id == id }
        saveList(KEY_ASSIGNMENTS, assignmentList)
    }

    fun markAssignmentComplete(id: String, isComplete: Boolean) {
        getAssignmentList()
        val index = assignmentList?.indexOfFirst { it.id == id } ?: -1
        if (index != -1) {
            val a = assignmentList!![index]
            assignmentList?.set(index, a.copy(isCompleted = isComplete))
            saveList(KEY_ASSIGNMENTS, assignmentList)
        }
    }

    fun getOverdueAssignments(): List<Assignment> {
        val now = System.currentTimeMillis()
        return getAssignmentList().filter { !it.isCompleted && it.dueDate < now }
    }

    fun getUpcomingAssignments(withinMs: Long): List<Assignment> {
        val now = System.currentTimeMillis()
        val limit = now + withinMs
        return getAssignmentList().filter { !it.isCompleted && it.dueDate in now..limit }
    }

    // ── AttendanceRecord API ────────────────────────────────────────
    fun getAttendanceList(): List<AttendanceRecord> {
        if (attendanceList == null) {
            attendanceList = loadList(KEY_ATTENDANCE) { AttendanceRecord.fromJson(it) }
        }
        return attendanceList!!.toList()
    }

    fun addAttendanceRecord(r: AttendanceRecord) {
        getAttendanceList()
        attendanceList?.add(r)
        saveList(KEY_ATTENDANCE, attendanceList)
    }

    fun updateAttendanceRecord(r: AttendanceRecord) {
        getAttendanceList()
        val index = attendanceList?.indexOfFirst { it.id == r.id } ?: -1
        if (index != -1) {
            attendanceList?.set(index, r)
            saveList(KEY_ATTENDANCE, attendanceList)
        }
    }

    fun deleteAttendanceRecord(id: String) {
        getAttendanceList()
        attendanceList?.removeAll { it.id == id }
        saveList(KEY_ATTENDANCE, attendanceList)
    }

    fun getAttendanceForCourse(courseName: String): List<AttendanceRecord> {
        return getAttendanceList().filter { it.courseName == courseName }
    }

    fun getAttendancePercentage(courseName: String): Double {
        val records = getAttendanceForCourse(courseName)
        if (records.isEmpty()) return 0.0
        
        var presentCount = 0.0
        for (r in records) {
            when (r.status) {
                AttendanceRecord.AttendanceStatus.PRESENT -> presentCount += 1.0
                AttendanceRecord.AttendanceStatus.LATE -> presentCount += 0.5
                AttendanceRecord.AttendanceStatus.ABSENT -> { /* 0.0 */ }
            }
        }
        return (presentCount / records.size) * 100.0
    }

    // ── Exam API ────────────────────────────────────────────────────
    fun getExamList(): List<Exam> {
        if (examList == null) {
            examList = loadList(KEY_EXAMS) { Exam.fromJson(it) }
        }
        return examList!!.toList()
    }

    fun addExam(e: Exam) {
        getExamList()
        examList?.add(e)
        saveList(KEY_EXAMS, examList)
    }

    fun updateExam(e: Exam) {
        getExamList()
        val index = examList?.indexOfFirst { it.id == e.id } ?: -1
        if (index != -1) {
            examList?.set(index, e)
            saveList(KEY_EXAMS, examList)
        }
    }

    fun deleteExam(id: String) {
        getExamList()
        examList?.removeAll { it.id == id }
        saveList(KEY_EXAMS, examList)
    }

    fun getUpcomingExams(withinMs: Long): List<Exam> {
        val now = System.currentTimeMillis()
        val limit = now + withinMs
        return getExamList().filter { it.examDate in now..limit }
    }

    // ── Timetable API ───────────────────────────────────────────────
    fun getTimetableList(): List<TimetableEntry> {
        if (timetableList == null) {
            timetableList = loadList(KEY_TIMETABLE) { TimetableEntry.fromJson(it) }
        }
        return timetableList!!.toList()
    }

    fun addTimetableEntry(e: TimetableEntry) {
        getTimetableList()
        timetableList?.add(e)
        saveList(KEY_TIMETABLE, timetableList)
    }

    fun updateTimetableEntry(e: TimetableEntry) {
        getTimetableList()
        val index = timetableList?.indexOfFirst { it.id == e.id } ?: -1
        if (index != -1) {
            timetableList?.set(index, e)
            saveList(KEY_TIMETABLE, timetableList)
        }
    }

    fun deleteTimetableEntry(id: String) {
        getTimetableList()
        timetableList?.removeAll { it.id == id }
        saveList(KEY_TIMETABLE, timetableList)
    }

    // ── Global ──────────────────────────────────────────────────────
    fun clearAll() {
        prefs.clearAll()
        semesterList = null
        courseList = null
        assignmentList = null
        attendanceList = null
        examList = null
        timetableList = null
    }

    // ── Internal Helpers ───────────────────────────────────────────
    private fun <T> loadList(key: String, parser: (JSONObject) -> T): MutableList<T> {
        val jsonString = prefs.getString(key) ?: return mutableListOf()
        val list = mutableListOf<T>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                list.add(parser(array.getJSONObject(i)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun <T> saveList(key: String, list: List<T>?) {
        if (list == null) return
        val array = JSONArray()
        try {
            for (item in list) {
                // Use reflection or specific check if needed, but per prompt
                // each model has a toJson() method.
                val method = item!!::class.java.getMethod("toJson")
                array.put(method.invoke(item) as JSONObject)
            }
            prefs.putString(key, array.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
