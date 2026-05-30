package com.example.studbuddy.core.repository

import com.example.studbuddy.core.db.StudBuddyDatabase
import com.example.studbuddy.core.models.*

class StudBuddyRepository(private val db: StudBuddyDatabase) {

    // --- Semester ---
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
    suspend fun getCourses(): List<Course> = db.courseDao().getAll()
    suspend fun addCourse(course: Course) = db.courseDao().insert(course)
    suspend fun updateCourse(course: Course) = db.courseDao().update(course)
    suspend fun deleteCourse(course: Course) = db.courseDao().delete(course)

    // --- Timetable ---
    suspend fun getTimetable(): List<TimetableEntry> = db.timetableDao().getAll()
    suspend fun addTimetableEntry(entry: TimetableEntry) = db.timetableDao().insert(entry)
    suspend fun updateTimetableEntry(entry: TimetableEntry) = db.timetableDao().update(entry)
    suspend fun deleteTimetableEntry(entryId: String) = db.timetableDao().deleteById(entryId)

    // --- Attendance ---
    suspend fun getAttendance(): List<AttendanceRecord> = db.attendanceDao().getAll()
    suspend fun addAttendanceRecord(record: AttendanceRecord) = db.attendanceDao().insert(record)
    suspend fun updateAttendanceRecord(record: AttendanceRecord) = db.attendanceDao().update(record)
    suspend fun deleteAttendanceRecord(recordId: String) = db.attendanceDao().deleteById(recordId)

    // --- Assignments ---
    suspend fun getAssignments(): List<Assignment> = db.assignmentDao().getAll()
    suspend fun updateAssignment(assignment: Assignment) = db.assignmentDao().insert(assignment)
    suspend fun deleteAssignment(assignmentId: String) = db.assignmentDao().deleteById(assignmentId)

    // --- Exams ---
    suspend fun getExams(): List<Exam> = db.examDao().getAll()
    suspend fun updateExam(exam: Exam) = db.examDao().insert(exam)
    suspend fun deleteExam(examId: String) = db.examDao().deleteById(examId)
}
