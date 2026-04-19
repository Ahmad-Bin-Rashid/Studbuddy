package com.example.studbuddy.attendance

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.core.BaseActivity
import com.example.studbuddy.core.AppDataStore
import com.example.studbuddy.core.models.AttendanceRecord
import com.example.studbuddy.core.models.Course
import java.util.*

class AttendanceActivity : BaseActivity() {

    private lateinit var recyclerViewAttendance: RecyclerView
    private lateinit var attendanceAdapter: AttendanceAdapter
    private val courseList = mutableListOf<Course>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        setupViews()
        setupSidebar()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.btnMarkAttendance).setOnClickListener {
            if (AppDataStore.getCourses().isEmpty()) {
                Toast.makeText(this, "Please add courses first", Toast.LENGTH_SHORT).show()
            } else {
                showMarkAttendanceDialog(null)
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerViewAttendance = findViewById(R.id.recyclerViewAttendance)
        attendanceAdapter = AttendanceAdapter(courseList) { course ->
            showAttendanceHistoryDialog(course)
        }
        recyclerViewAttendance.layoutManager = LinearLayoutManager(this)
        recyclerViewAttendance.adapter = attendanceAdapter
    }

    private fun loadData() {
        val courses = AppDataStore.getCourses()
        courseList.clear()
        courseList.addAll(courses)
        attendanceAdapter.notifyDataSetChanged()
    }

    private fun showMarkAttendanceDialog(existingRecord: AttendanceRecord?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mark_attendance, null)
        val spinnerCourses = dialogView.findViewById<Spinner>(R.id.spinnerCourses)
        val rgStatus = dialogView.findViewById<RadioGroup>(R.id.rgStatus)

        val courses = AppDataStore.getCourses()
        spinnerCourses.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses.map { it.name })

        existingRecord?.let { record ->
            val courseIdx = courses.indexOfFirst { it.id == record.courseId }
            if (courseIdx != -1) {
                spinnerCourses.setSelection(courseIdx)
                spinnerCourses.isEnabled = false // Don't allow changing course during update
            }
            when (record.status) {
                "PRESENT" -> rgStatus.check(R.id.rbPresent)
                "ABSENT" -> rgStatus.check(R.id.rbAbsent)
                "LATE" -> rgStatus.check(R.id.rbLate)
            }
        }

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(if (existingRecord == null) "Mark Attendance" else "Update Attendance")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val courseId = courses[spinnerCourses.selectedItemPosition].id
                val status = when (rgStatus.checkedRadioButtonId) {
                    R.id.rbPresent -> "PRESENT"
                    R.id.rbAbsent -> "ABSENT"
                    R.id.rbLate -> "LATE"
                    else -> "PRESENT"
                }

                val record = AttendanceRecord(
                    id = existingRecord?.id ?: UUID.randomUUID().toString(),
                    courseId = courseId,
                    dateTime = existingRecord?.dateTime ?: System.currentTimeMillis(),
                    status = status
                )
                
                if (existingRecord == null) {
                    AppDataStore.addAttendanceRecord(record)
                } else {
                    AppDataStore.updateAttendanceRecord(record)
                }
                loadData()
            }
            .setNegativeButton("Cancel", null)

        if (existingRecord != null) {
            dialogBuilder.setNeutralButton("Delete") { _, _ ->
                AppDataStore.deleteAttendanceRecord(existingRecord.id)
                loadData()
                Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBuilder.show()
    }

    private fun showAttendanceHistoryDialog(course: Course) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_attendance_history, null)
        val rvRecords = dialogView.findViewById<RecyclerView>(R.id.rvAttendanceRecords)
        val tvCourseName = dialogView.findViewById<TextView>(R.id.tvHistoryCourseName)

        tvCourseName.text = "History for ${course.name}"
        
        val records = AppDataStore.getAttendance().filter { it.courseId == course.id }.sortedByDescending { it.dateTime }
        
        val historyDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Close", null)
            .create()

        val adapter = RecordAdapter(records) { record ->
            historyDialog.dismiss()
            showMarkAttendanceDialog(record)
        }
        
        rvRecords.layoutManager = LinearLayoutManager(this)
        rvRecords.adapter = adapter
        
        historyDialog.show()
    }
}
