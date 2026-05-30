package com.example.studbuddy.timetable

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.StudBuddyApp
import com.example.studbuddy.core.BaseActivity
import com.example.studbuddy.core.ViewModelFactory
import com.example.studbuddy.core.models.TimetableEntry
import java.util.*

class TimetableActivity : BaseActivity() {

    private lateinit var recyclerViewTimetable: RecyclerView
    private lateinit var dayTimetableAdapter: DayTimetableAdapter

    private val viewModel: TimetableViewModel by viewModels {
        ViewModelFactory((application as StudBuddyApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        setupViews()
        setupSidebar()
        observeViewModel()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.btnAddTimetableEntry).setOnClickListener {
            if (viewModel.courses.value.isNullOrEmpty()) {
                Toast.makeText(this, "Please add courses first", Toast.LENGTH_SHORT).show()
            } else {
                showTimetableDialog(null)
            }
        }
        
        recyclerViewTimetable = findViewById(R.id.recyclerViewTimetable)
        recyclerViewTimetable.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        viewModel.timetable.observe(this) { entries ->
            updateAdapter(entries, viewModel.courses.value ?: emptyList())
        }
        viewModel.courses.observe(this) { courses ->
            updateAdapter(viewModel.timetable.value ?: emptyList(), courses)
        }
    }

    private fun updateAdapter(entries: List<TimetableEntry>, courses: List<com.example.studbuddy.core.models.Course>) {
        val dayMap = entries.groupBy { it.dayOfWeek }
        dayTimetableAdapter = DayTimetableAdapter(dayMap, courses) { entry ->
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

        val courses = viewModel.courses.value ?: emptyList()
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
                viewModel.deleteTimetableEntry(existing.id)
                Toast.makeText(this, "Class deleted", Toast.LENGTH_SHORT).show()
            }
        }

        val alertDialog = dialog.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val selectedCourseIndex = spinnerCourses.selectedItemPosition
            if (selectedCourseIndex == -1) return@setOnClickListener
            
            val courseId = courses[selectedCourseIndex].id
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
                    viewModel.addTimetableEntry(entry)
                } else {
                    viewModel.updateTimetableEntry(entry)
                }
                alertDialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a room/place", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
