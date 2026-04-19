package com.example.studbuddy.assignments

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.core.BaseActivity
import com.example.studbuddy.core.AppDataStore
import com.example.studbuddy.core.models.Assignment
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AssignmentsActivity : BaseActivity() {

    private lateinit var rvPending: RecyclerView
    private lateinit var rvCompleted: RecyclerView
    private lateinit var pendingAdapter: AssignmentsAdapter
    private lateinit var completedAdapter: AssignmentsAdapter
    
    private val pendingList = mutableListOf<Assignment>()
    private val completedList = mutableListOf<Assignment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignments)

        setupSidebar()
        setupViews()
        setupRecyclerViews()
    }

    override fun onResume() {
        super.onResume()
        loadAssignments()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.btnAddAssignment).setOnClickListener {
            if (AppDataStore.getCourses().isEmpty()) {
                Toast.makeText(this, "Please add courses first", Toast.LENGTH_SHORT).show()
            } else {
                showAssignmentDialog(null)
            }
        }
    }

    private fun setupRecyclerViews() {
        rvPending = findViewById(R.id.rvPending)
        rvCompleted = findViewById(R.id.rvCompleted)

        pendingAdapter = AssignmentsAdapter(pendingList, { assignment, isChecked ->
            updateAssignmentStatus(assignment, isChecked)
        }, { assignment ->
            showAssignmentDialog(assignment)
        })

        completedAdapter = AssignmentsAdapter(completedList, { assignment, isChecked ->
            updateAssignmentStatus(assignment, isChecked)
        }, { assignment ->
            showAssignmentDialog(assignment)
        })

        rvPending.layoutManager = LinearLayoutManager(this)
        rvPending.adapter = pendingAdapter

        rvCompleted.layoutManager = LinearLayoutManager(this)
        rvCompleted.adapter = completedAdapter
    }

    private fun loadAssignments() {
        val allAssignments = AppDataStore.getAssignments()
        
        pendingList.clear()
        pendingList.addAll(allAssignments.filter { !it.isCompleted }.sortedBy { it.dueDate })
        
        completedList.clear()
        completedList.addAll(allAssignments.filter { it.isCompleted }.sortedByDescending { it.dueDate })
        
        pendingAdapter.notifyDataSetChanged()
        completedAdapter.notifyDataSetChanged()
    }

    private fun updateAssignmentStatus(assignment: Assignment, isCompleted: Boolean) {
        val updated = assignment.copy(isCompleted = isCompleted)
        AppDataStore.updateAssignment(updated)
        
        if (isCompleted) {
            cancelReminder(updated)
        } else {
            scheduleReminder(updated)
        }
        
        updateCourseMarks(updated.courseId)
        loadAssignments()
    }

    private fun showAssignmentDialog(existing: Assignment?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_assignment, null)
        val spinnerCourses = dialogView.findViewById<Spinner>(R.id.spinnerCourses)
        val etName = dialogView.findViewById<EditText>(R.id.etAssignmentTitle)
        val etTotal = dialogView.findViewById<EditText>(R.id.etTotalMarks)
        val etWeight = dialogView.findViewById<EditText>(R.id.etWeightage)
        val btnDate = dialogView.findViewById<Button>(R.id.btnDueDate)
        val layoutObtained = dialogView.findViewById<View>(R.id.layoutObtainedMarks)
        val etObtained = dialogView.findViewById<EditText>(R.id.etObtainedMarks)

        val courses = AppDataStore.getCourses()
        val courseNames = courses.map { it.name }
        spinnerCourses.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courseNames)

        var selectedDate = existing?.dueDate ?: System.currentTimeMillis()
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        if (existing != null) {
            etName.setText(existing.name)
            etTotal.setText(existing.totalMarks.toString())
            etWeight.setText(existing.weightage.toString())
            btnDate.text = sdf.format(Date(existing.dueDate))
            val courseIdx = courses.indexOfFirst { it.id == existing.courseId }
            if (courseIdx != -1) spinnerCourses.setSelection(courseIdx)
            
            if (existing.isCompleted) {
                layoutObtained.visibility = View.VISIBLE
                etObtained.setText(existing.obtainedMarks?.toString() ?: "")
            }
        }

        btnDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = selectedDate
            DatePickerDialog(this, { _, y, m, d ->
                val selected = Calendar.getInstance()
                selected.set(y, m, d)
                selectedDate = selected.timeInMillis
                btnDate.text = sdf.format(selected.time)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add Assignment" else "Edit Assignment")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
        
        if (existing != null) {
            dialogBuilder.setNeutralButton("Delete") { _, _ ->
                confirmDelete(existing)
            }
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val courseId = courses[spinnerCourses.selectedItemPosition].id
            val name = etName.text.toString().trim()
            val total = etTotal.text.toString().toDoubleOrNull() ?: 0.0
            val weight = etWeight.text.toString().toDoubleOrNull() ?: 0.0
            val obtained = etObtained.text.toString().toDoubleOrNull()

            if (name.isNotEmpty()) {
                val assignment = Assignment(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    name = name,
                    courseId = courseId,
                    dueDate = selectedDate,
                    totalMarks = total,
                    obtainedMarks = obtained,
                    weightage = weight,
                    isCompleted = existing?.isCompleted ?: false
                )
                AppDataStore.updateAssignment(assignment)
                
                if (!assignment.isCompleted) {
                    scheduleReminder(assignment)
                }
                
                updateCourseMarks(courseId)
                loadAssignments()
                alertDialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter assignment name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete(assignment: Assignment) {
        AlertDialog.Builder(this)
            .setTitle("Delete Assignment")
            .setMessage("Are you sure you want to delete this assignment?")
            .setPositiveButton("Delete") { _, _ ->
                cancelReminder(assignment)
                AppDataStore.deleteAssignment(assignment.id)
                updateCourseMarks(assignment.courseId)
                loadAssignments()
                Toast.makeText(this, "Assignment deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateCourseMarks(courseId: String) {
        val course = AppDataStore.getCourses().find { it.id == courseId } ?: return
        val assignments = AppDataStore.getAssignments().filter { it.courseId == courseId && it.isCompleted }
        
        val totalMarksFromAssignments = assignments.sumOf { 
            if (it.totalMarks > 0) (it.obtainedMarks ?: 0.0) / it.totalMarks * it.weightage else 0.0 
        }
        
        val updatedCourse = course.copy(marks = totalMarksFromAssignments)
        AppDataStore.updateCourse(updatedCourse)
    }

    private fun scheduleReminder(assignment: Assignment) {
        val triggerTime = assignment.dueDate - TimeUnit.HOURS.toMillis(24)
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent("com.example.studbuddy.ASSIGNMENT_REMINDER").apply {
            putExtra("assignment_id", assignment.id)
            putExtra("name", assignment.name)
            `package` = packageName
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, assignment.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun cancelReminder(assignment: Assignment) {
        val intent = Intent("com.example.studbuddy.ASSIGNMENT_REMINDER").apply {
            `package` = packageName
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, assignment.id.hashCode(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
        }
    }
}
