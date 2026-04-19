package com.example.studbuddy.timetable

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.core.BaseActivity
import com.example.studbuddy.core.AppDataStore
import com.example.studbuddy.core.models.TimetableEntry
import java.util.*

class TimetableActivity : BaseActivity() {

    private lateinit var recyclerViewTimetable: RecyclerView
    private lateinit var dayTimetableAdapter: DayTimetableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        setupViews()
        setupSidebar()
    }

    override fun onResume() {
        super.onResume()
        loadTimetable()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.btnAddTimetableEntry).setOnClickListener {
            if (AppDataStore.getCourses().isEmpty()) {
                Toast.makeText(this, "Please add courses first", Toast.LENGTH_SHORT).show()
            } else {
                showTimetableDialog(null)
            }
        }
        
        recyclerViewTimetable = findViewById(R.id.recyclerViewTimetable)
        recyclerViewTimetable.layoutManager = LinearLayoutManager(this)
    }

    private fun loadTimetable() {
        val entries = AppDataStore.getTimetable()
        val dayMap = entries.groupBy { it.dayOfWeek }
        
        dayTimetableAdapter = DayTimetableAdapter(dayMap) { entry ->
            showTimetableDialog(entry)
        }
        recyclerViewTimetable.adapter = dayTimetableAdapter
    }

    private fun showTimetableDialog(existing: TimetableEntry?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_timetable, null)
        val spinnerCourses = dialogView.findViewById<Spinner>(R.id.spinnerCourses)
        val spinnerDay = dialogView.findViewById<Spinner>(R.id.spinnerDay)
        val btnStart = dialogView.findViewById<Button>(R.id.btnStartTime)
        val btnEnd = dialogView.findViewById<Button>(R.id.btnEndTime)
        val etRoom = dialogView.findViewById<EditText>(R.id.etRoom)

        val courses = AppDataStore.getCourses()
        spinnerCourses.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses.map { it.name })

        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        spinnerDay.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)

        var startTime = existing?.startTime ?: "09:00"
        var endTime = existing?.endTime ?: "10:00"

        btnStart.text = startTime
        btnEnd.text = endTime

        if (existing != null) {
            val courseIdx = courses.indexOfFirst { it.id == existing.courseId }
            if (courseIdx != -1) spinnerCourses.setSelection(courseIdx)
            spinnerDay.setSelection(existing.dayOfWeek - 1)
            etRoom.setText(existing.room)
        }

        btnStart.setOnClickListener {
            val parts = startTime.split(":")
            TimePickerDialog(this, { _, h, m ->
                startTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                btnStart.text = startTime
            }, parts[0].toInt(), parts[1].toInt(), true).show()
        }

        btnEnd.setOnClickListener {
            val parts = endTime.split(":")
            TimePickerDialog(this, { _, h, m ->
                endTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                btnEnd.text = endTime
            }, parts[0].toInt(), parts[1].toInt(), true).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add Class" else "Edit Class")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)

        if (existing != null) {
            dialog.setNeutralButton("Delete") { _, _ ->
                AppDataStore.deleteTimetableEntry(existing.id)
                loadTimetable()
                Toast.makeText(this, "Class deleted", Toast.LENGTH_SHORT).show()
            }
        }

        val alertDialog = dialog.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val courseId = courses[spinnerCourses.selectedItemPosition].id
            val day = spinnerDay.selectedItemPosition + 1
            val room = etRoom.text.toString().trim()

            if (room.isNotEmpty()) {
                val entry = TimetableEntry(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    courseId = courseId,
                    dayOfWeek = day,
                    startTime = startTime,
                    endTime = endTime,
                    room = room,
                    color = existing?.color ?: "#1565C0"
                )
                
                if (existing == null) {
                    AppDataStore.addTimetableEntry(entry)
                } else {
                    AppDataStore.updateTimetableEntry(entry)
                }
                
                loadTimetable()
                alertDialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a room/place", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
