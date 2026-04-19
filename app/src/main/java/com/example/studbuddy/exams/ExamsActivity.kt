package com.example.studbuddy.exams

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
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
import com.example.studbuddy.core.models.Exam
import com.example.studbuddy.core.models.ExamType
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ExamsActivity : BaseActivity() {

    private lateinit var rvPending: RecyclerView
    private lateinit var rvCompleted: RecyclerView
    private lateinit var pendingAdapter: ExamAdapter
    private lateinit var completedAdapter: ExamAdapter
    
    private val pendingList = mutableListOf<Exam>()
    private val completedList = mutableListOf<Exam>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exams)

        setupSidebar()
        setupViews()
        setupRecyclerViews()
    }

    override fun onResume() {
        super.onResume()
        loadExams()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.btnAddExam).setOnClickListener {
            if (AppDataStore.getCourses().isEmpty()) {
                Toast.makeText(this, "Please add courses first", Toast.LENGTH_SHORT).show()
            } else {
                showExamDialog(null)
            }
        }
    }

    private fun setupRecyclerViews() {
        rvPending = findViewById(R.id.rvPendingExams)
        rvCompleted = findViewById(R.id.rvCompletedExams)

        pendingAdapter = ExamAdapter(pendingList) { exam -> showExamDialog(exam) }
        completedAdapter = ExamAdapter(completedList) { exam -> showExamDialog(exam) }

        rvPending.layoutManager = LinearLayoutManager(this)
        rvPending.adapter = pendingAdapter

        rvCompleted.layoutManager = LinearLayoutManager(this)
        rvCompleted.adapter = completedAdapter
    }

    private fun loadExams() {
        val allExams = AppDataStore.getExams()
        
        pendingList.clear()
        pendingList.addAll(allExams.filter { !it.isCompleted }.sortedBy { it.date })
        
        completedList.clear()
        completedList.addAll(allExams.filter { it.isCompleted }.sortedByDescending { it.date })
        
        pendingAdapter.notifyDataSetChanged()
        completedAdapter.notifyDataSetChanged()
    }

    private fun showExamDialog(existing: Exam?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exam, null)
        val spinnerCourses = dialogView.findViewById<Spinner>(R.id.spinnerCourses)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerExamType)
        val btnDate = dialogView.findViewById<Button>(R.id.btnExamDate)
        val etVenue = dialogView.findViewById<EditText>(R.id.etVenue)
        val etTotalMarks = dialogView.findViewById<EditText>(R.id.etTotalMarks)
        val etWeightage = dialogView.findViewById<EditText>(R.id.etWeightage)
        val cbCompleted = dialogView.findViewById<CheckBox>(R.id.cbExamCompleted)
        val layoutObtained = dialogView.findViewById<View>(R.id.layoutObtainedMarks)
        val etObtained = dialogView.findViewById<EditText>(R.id.etObtainedMarks)

        val courses = AppDataStore.getCourses()
        if (courses.isEmpty()) return
        
        spinnerCourses.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses.map { it.name })
        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ExamType.values().map { it.name })

        val calendar = Calendar.getInstance()
        existing?.let { 
            calendar.timeInMillis = it.date
            etVenue.setText(it.venue)
            etTotalMarks.setText(it.totalMarks.toString())
            etWeightage.setText(it.weightage.toString())
            cbCompleted.isChecked = it.isCompleted
            etObtained.setText(it.obtainedMarks?.toString() ?: "")
            if (it.isCompleted) layoutObtained.visibility = View.VISIBLE
            
            val courseIdx = courses.indexOfFirst { c -> c.id == it.courseId }
            if (courseIdx != -1) spinnerCourses.setSelection(courseIdx)
            spinnerType.setSelection(it.type.ordinal)
        }

        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        btnDate.text = sdf.format(calendar.time)

        btnDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)
                TimePickerDialog(this, { _, hh, mm ->
                    calendar.set(Calendar.HOUR_OF_DAY, hh)
                    calendar.set(Calendar.MINUTE, mm)
                    btnDate.text = sdf.format(calendar.time)
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            layoutObtained.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add Exam" else "Edit Exam")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)

        if (existing != null) {
            dialogBuilder.setNeutralButton("Delete") { _, _ -> confirmDelete(existing) }
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val courseId = courses[spinnerCourses.selectedItemPosition].id
            val type = ExamType.values()[spinnerType.selectedItemPosition]
            val total = etTotalMarks.text.toString().toDoubleOrNull() ?: 0.0
            val weight = etWeightage.text.toString().toDoubleOrNull() ?: 0.0
            val isComp = cbCompleted.isChecked
            val obtained = if (isComp) etObtained.text.toString().toDoubleOrNull() else null

            val exam = Exam(
                id = existing?.id ?: UUID.randomUUID().toString(),
                courseId = courseId,
                type = type,
                date = calendar.timeInMillis,
                venue = etVenue.text.toString(),
                totalMarks = total,
                obtainedMarks = obtained,
                weightage = weight,
                isCompleted = isComp
            )
            AppDataStore.updateExam(exam)
            
            if (!exam.isCompleted) {
                scheduleExamReminder(exam)
            } else {
                cancelExamReminder(exam)
            }
            
            updateCourseMarks(courseId)
            loadExams()
            alertDialog.dismiss()
        }
    }

    private fun confirmDelete(exam: Exam) {
        AlertDialog.Builder(this)
            .setTitle("Delete Exam")
            .setMessage("Are you sure you want to delete this exam?")
            .setPositiveButton("Delete") { _, _ ->
                cancelExamReminder(exam)
                AppDataStore.deleteExam(exam.id)
                updateCourseMarks(exam.courseId)
                loadExams()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateCourseMarks(courseId: String) {
        val course = AppDataStore.getCourses().find { it.id == courseId } ?: return
        val assignments = AppDataStore.getAssignments().filter { it.courseId == courseId && it.isCompleted }
        val exams = AppDataStore.getExams().filter { it.courseId == courseId && it.isCompleted }
        
        val assignmentScore = assignments.sumOf { 
            if (it.totalMarks > 0) (it.obtainedMarks ?: 0.0) / it.totalMarks * it.weightage else 0.0 
        }
        val examScore = exams.sumOf {
            if (it.totalMarks > 0) (it.obtainedMarks ?: 0.0) / it.totalMarks * it.weightage else 0.0
        }
        
        val updatedCourse = course.copy(marks = assignmentScore + examScore)
        AppDataStore.updateCourse(updatedCourse)
    }

    private fun scheduleExamReminder(exam: Exam) {
        val triggerTime = exam.date - TimeUnit.HOURS.toMillis(1)
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent("com.example.studbuddy.EXAM_REMINDER").apply {
            val course = AppDataStore.getCourses().find { it.id == exam.courseId }
            putExtra("exam_id", exam.id)
            putExtra("course_name", course?.name ?: "Unknown Course")
            putExtra("type", exam.type.name)
            putExtra("venue", exam.venue ?: "Not set")
            putExtra("time", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(exam.date)))
            `package` = packageName
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, exam.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun cancelExamReminder(exam: Exam) {
        val intent = Intent("com.example.studbuddy.EXAM_REMINDER").apply {
            `package` = packageName
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, exam.id.hashCode(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
        }
    }
}
