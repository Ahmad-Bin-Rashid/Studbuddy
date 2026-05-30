package com.example.studbuddy.core.db

import androidx.room.*
import com.example.studbuddy.core.models.*

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters LIMIT 1")
    fun getSemester(): Semester?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(semester: Semester)

    @Query("DELETE FROM semesters")
    fun deleteAll()
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAll(): List<Course>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(course: Course)

    @Update
    fun update(course: Course)

    @Delete
    fun delete(course: Course)

    @Query("DELETE FROM courses")
    fun deleteAll()
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_entries")
    fun getAll(): List<TimetableEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entry: TimetableEntry)

    @Update
    fun update(entry: TimetableEntry)

    @Query("DELETE FROM timetable_entries WHERE id = :entryId")
    fun deleteById(entryId: String)

    @Query("DELETE FROM timetable_entries")
    fun deleteAll()
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records")
    fun getAll(): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: AttendanceRecord)

    @Update
    fun update(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records WHERE id = :recordId")
    fun deleteById(recordId: String)

    @Query("DELETE FROM attendance_records")
    fun deleteAll()
}

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments")
    fun getAll(): List<Assignment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(assignment: Assignment)

    @Query("DELETE FROM assignments WHERE id = :assignmentId")
    fun deleteById(assignmentId: String)

    @Query("DELETE FROM assignments")
    fun deleteAll()
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams")
    fun getAll(): List<Exam>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(exam: Exam)

    @Query("DELETE FROM exams WHERE id = :examId")
    fun deleteById(examId: String)

    @Query("DELETE FROM exams")
    fun deleteAll()
}
