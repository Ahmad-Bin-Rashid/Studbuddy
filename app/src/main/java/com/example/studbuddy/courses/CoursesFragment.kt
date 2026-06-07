package com.example.studbuddy.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.core.models.Course
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class CoursesFragment : Fragment() {

    private lateinit var recyclerViewCourses: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmpty: View
    private val courseList = mutableListOf<Course>()

    private val viewModel: CourseViewModel by viewModels()

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
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_courses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        
        setupViews(view)
        setupRecyclerView(view)
        observeViewModel()
    }

    private fun setupViews(view: View) {
        view.findViewById<Button>(R.id.btnAddCourse).setOnClickListener {
            val semester = viewModel.uiState.value.semester
            if (semester == null) {
                // If no semester context (e.g. accessed from sidebar without active semester)
                Toast.makeText(requireContext(), "Please setup or select a semester first", Toast.LENGTH_SHORT).show()
                // Optionally navigate to semester page
                findNavController().navigate(R.id.semesterFragment)
            } else {
                showCourseDialog(null)
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerViewCourses = view.findViewById(R.id.recyclerViewCourses)
        courseAdapter = CourseAdapter(courseList) { course ->
            showCourseDialog(course)
        }
        recyclerViewCourses.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCourses.adapter = courseAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    
                    if (!state.isLoading) {
                        courseAdapter.updateData(state.courses, state.semester)
                        updateEmptyState(state.courses.isEmpty())
                    }
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            if (layoutEmpty.visibility != View.VISIBLE) {
                layoutEmpty.visibility = View.VISIBLE
                layoutEmpty.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
                
                // Customize empty state for courses
                layoutEmpty.findViewById<ImageView>(R.id.imgEmptyState).setImageResource(android.R.drawable.ic_menu_agenda)
                layoutEmpty.findViewById<TextView>(R.id.tvEmptyTitle).setText(R.string.empty_courses_title)
                layoutEmpty.findViewById<TextView>(R.id.tvEmptyDescription).setText(R.string.empty_courses_desc)
            }
            recyclerViewCourses.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            recyclerViewCourses.visibility = View.VISIBLE
        }
    }

    private fun showCourseDialog(existingCourse: Course?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_course, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCourseName)
        val etInstructor = dialogView.findViewById<EditText>(R.id.etInstructor)
        val etCredits = dialogView.findViewById<EditText>(R.id.etCredits)
        val spinnerGrade = dialogView.findViewById<Spinner>(R.id.spinnerGrade)
        val tvGpDisplay = dialogView.findViewById<TextView>(R.id.tvGradePointsDisplay)

        val grades = gradeMap.keys.toList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, grades)
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

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existingCourse == null) "Add Course" else "Edit Course")
            .setView(dialogView)
            .setPositiveButton(R.string.save_button) { _, _ ->
                val name = etName.text.toString().trim()
                val instructor = etInstructor.text.toString().trim()
                val credits = etCredits.text.toString().toIntOrNull() ?: 0
                val selectedGrade = spinnerGrade.selectedItem.toString()
                val basePoints = gradeMap[selectedGrade] ?: -1.0
                
                val semester = viewModel.uiState.value.semester
                
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
                        viewModel.addCourse(course)
                    } else {
                        viewModel.updateCourse(course)
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter valid details", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }
}
