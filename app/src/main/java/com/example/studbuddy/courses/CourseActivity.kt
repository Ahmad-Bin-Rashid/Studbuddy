package com.example.studbuddy.courses

import android.app.AlertDialog
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
import com.example.studbuddy.core.models.Course
import java.util.*

class CourseActivity : BaseActivity() {

    private lateinit var recyclerViewCourses: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    private val courseList = mutableListOf<Course>()

    private val gradeMap = mapOf(
        "Select Grade" to -1.0,
        "A" to 4.0,
        "A-" to 3.7,
        "B+" to 3.3,
        "B" to 3.0,
        "B-" to 2.7,
        "C+" to 2.3,
        "C" to 2.0,
        "C-" to 1.7,
        "D+" to 1.3,
        "D" to 1.0,
        "F" to 0.0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        setupViews()
        setupSidebar()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadCourses()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.btnAddCourse).setOnClickListener {
            val semester = AppDataStore.getSemester()
            if (semester == null) {
                Toast.makeText(this, "Please setup a semester first", Toast.LENGTH_SHORT).show()
            } else {
                showCourseDialog(null)
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerViewCourses = findViewById(R.id.recyclerViewCourses)
        courseAdapter = CourseAdapter(courseList) { course ->
            showCourseDialog(course)
        }
        recyclerViewCourses.layoutManager = LinearLayoutManager(this)
        recyclerViewCourses.adapter = courseAdapter
    }

    private fun loadCourses() {
        val courses = AppDataStore.getCourses()
        courseList.clear()
        courseList.addAll(courses)
        courseAdapter.notifyDataSetChanged()
    }

    private fun showCourseDialog(existingCourse: Course?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCourseName)
        val etInstructor = dialogView.findViewById<EditText>(R.id.etInstructor)
        val etCredits = dialogView.findViewById<EditText>(R.id.etCredits)
        val spinnerGrade = dialogView.findViewById<Spinner>(R.id.spinnerGrade)
        val tvGpDisplay = dialogView.findViewById<TextView>(R.id.tvGradePointsDisplay)

        val grades = gradeMap.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGrade.adapter = adapter

        existingCourse?.let {
            etName.setText(it.name)
            etInstructor.setText(it.instructor)
            etCredits.setText(it.creditHours.toString())
            val gradeIndex = grades.indexOf(it.grade ?: "Select Grade")
            if (gradeIndex != -1) spinnerGrade.setSelection(gradeIndex)
            tvGpDisplay.text = "Grade Points: ${String.format(Locale.getDefault(), "%.2f", it.gradePoints)}"
        }

        spinnerGrade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGrade = grades[position]
                val basePoints = gradeMap[selectedGrade] ?: -1.0
                val credits = etCredits.text.toString().toIntOrNull() ?: 0
                
                if (basePoints >= 0 && credits > 0) {
                    tvGpDisplay.text = "Grade Points: ${String.format(Locale.getDefault(), "%.2f", basePoints * credits)}"
                } else {
                    tvGpDisplay.text = "Grade Points: 0.00"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        AlertDialog.Builder(this)
            .setTitle(if (existingCourse == null) "Add Course" else "Edit Course")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val instructor = etInstructor.text.toString().trim()
                val credits = etCredits.text.toString().toIntOrNull() ?: 0
                val selectedGrade = spinnerGrade.selectedItem.toString()
                val basePoints = gradeMap[selectedGrade] ?: -1.0
                
                val semester = AppDataStore.getSemester()
                
                if (name.isNotEmpty() && credits > 0 && semester != null) {
                    val gradePoints = if (basePoints >= 0) basePoints * credits else 0.0
                    
                    val course = Course(
                        id = existingCourse?.id ?: UUID.randomUUID().toString(),
                        name = name,
                        description = existingCourse?.description,
                        instructor = if (instructor.isEmpty()) null else instructor,
                        creditHours = credits,
                        semesterId = semester.id,
                        marks = existingCourse?.marks ?: 0.0,
                        grade = if (basePoints >= 0) selectedGrade else null,
                        gradePoints = gradePoints
                    )
                    
                    if (existingCourse == null) {
                        AppDataStore.addCourse(course)
                    } else {
                        AppDataStore.updateCourse(course)
                    }
                    
                    updateSemesterGpa()
                    
                    // Send broadcast to update GPA Activity
                    val intent = Intent("com.example.studbuddy.GPA_UPDATE")
                    intent.`package` = packageName
                    sendBroadcast(intent)

                    loadCourses()
                } else {
                    Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateSemesterGpa() {
        val semester = AppDataStore.getSemester() ?: return
        val courses = AppDataStore.getCourses()
        
        val coursesWithGrades = courses.filter { it.grade != null }
        val totalPoints = coursesWithGrades.sumOf { it.gradePoints }
        val totalCredits = coursesWithGrades.sumOf { it.creditHours }
        
        val calculatedGpa = if (totalCredits > 0) totalPoints / totalCredits else 0.0
        
        val updatedSemester = semester.copy(gpa = calculatedGpa)
        AppDataStore.saveSemester(updatedSemester)
    }
}
