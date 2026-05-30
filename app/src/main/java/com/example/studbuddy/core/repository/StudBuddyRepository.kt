package com.example.studbuddy.core.repository

import com.example.studbuddy.core.db.StudBuddyDatabase
import com.example.studbuddy.core.models.*
import kotlinx.coroutines.flow.Flow

class StudBuddyRepository(private val db: StudBuddyDatabase) {

    // --- Semester ---
    fun getSemesterFlow(): Flow<Semester?> = db.semesterDao().getSemesterFlow()
    suspend fun getSemester(): Semester? = db.semesterDao().getSemester()
    suspend fun saveSemester(semester: Semester) = db.semesterDao().insert(semester)
    suspend fun clearAll() {
        db.semesterDao().deleteAll()
        db.courseDao().deleteAll()
        db.timetableDao().deleteAll()
        db.attendanceDao().deleteAll()
        db.assignmentDao().deleteAll()
        db.examDao().deleteAll()
    }

    // --- Courses ---
    fun getCoursesFlow(): Flow<List<Course>> = db.courseDao().getAllFlow()
    suspend fun getCourses(): List<Course> = db.courseDao().getAll()
    suspend fun addCourse(course: Course) = db.courseDao().insert(course)
    suspend fun updateCourse(course: Course) = db.courseDao().update(course)
    suspend fun deleteCourse(course: Course) = db.courseDao().delete(course)

    // --- Timetable ---
    fun getTimetableFlow(): Flow<List<TimetableEntry>> = db.timetableDao().getAllFlow()
    suspend fun getTimetable(): List<TimetableEntry> = db.timetableDao().getAll()
    suspend fun addTimetableEntry(entry: TimetableEntry) = db.timetableDao().insert(entry)
    suspend fun updateTimetableEntry(entry: TimetableEntry) = db.timetableDao().update(entry)
    suspend fun deleteTimetableEntry(entryId: String) = db.timetableDao().deleteById(entryId)

    // --- Attendance ---
    fun getAttendanceFlow(): Flow<List<AttendanceRecord>> = db.attendanceDao().getAllFlow()
    suspend fun getAttendance(): List<AttendanceRecord> = db.attendanceDao().getAll()
    suspend fun addAttendanceRecord(record: AttendanceRecord) = db.attendanceDao().insert(record)
    suspend fun updateAttendanceRecord(record: AttendanceRecord) = db.attendanceDao().update(record)
    suspend fun deleteAttendanceRecord(recordId: String) = db.attendanceDao().deleteById(recordId)

    // --- Assignments ---
    fun getAssignmentsFlow(): Flow<List<Assignment>> = db.assignmentDao().getAllFlow()
    suspend fun getAssignments(): List<Assignment> = db.assignmentDao().getAll()
    suspend fun updateAssignment(assignment: Assignment) = db.assignmentDao().insert(assignment)
    suspend fun deleteAssignment(assignmentId: String) = db.assignmentDao().deleteById(assignmentId)

    // --- Exams ---
    fun getExamsFlow(): Flow<List<Exam>> = db.examDao().getAllFlow()
    suspend fun getExams(): List<Exam> = db.examDao().getAll()
    suspend fun updateExam(exam: Exam) = db.examDao().insert(exam)
    suspend fun deleteExam(examId: String) = db.examDao().deleteById(examId)
}
