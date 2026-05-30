package com.example.studbuddy.core.db

import androidx.room.*
import com.example.studbuddy.core.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters LIMIT 1")
    fun getSemesterFlow(): Flow<Semester?>

    @Query("SELECT * FROM semesters LIMIT 1")
    suspend fun getSemester(): Semester?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(semester: Semester)

    @Query("DELETE FROM semesters")
    suspend fun deleteAll()
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllFlow(): Flow<List<Course>>

    @Query("SELECT * FROM courses")
    suspend fun getAll(): List<Course>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course)

    @Update
    suspend fun update(course: Course)

    @Delete
    suspend fun delete(course: Course)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_entries")
    fun getAllFlow(): Flow<List<TimetableEntry>>

    @Query("SELECT * FROM timetable_entries")
    suspend fun getAll(): List<TimetableEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TimetableEntry)

    @Update
    suspend fun update(entry: TimetableEntry)

    @Query("DELETE FROM timetable_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: String)

    @Query("DELETE FROM timetable_entries")
    suspend fun deleteAll()
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records")
    fun getAllFlow(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAll(): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceRecord)

    @Update
    suspend fun update(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records WHERE id = :recordId")
    suspend fun deleteById(recordId: String)

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAll()
}

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments")
    fun getAllFlow(): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments")
    suspend fun getAll(): List<Assignment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignment: Assignment)

    @Query("DELETE FROM assignments WHERE id = :assignmentId")
    suspend fun deleteById(assignmentId: String)

    @Query("DELETE FROM assignments")
    suspend fun deleteAll()
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams")
    fun getAllFlow(): Flow<List<Exam>>

    @Query("SELECT * FROM exams")
    suspend fun getAll(): List<Exam>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exam: Exam)

    @Query("DELETE FROM exams WHERE id = :examId")
    suspend fun deleteById(examId: String)

    @Query("DELETE FROM exams")
    suspend fun deleteAll()
}
