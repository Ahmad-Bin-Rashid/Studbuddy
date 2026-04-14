package com.example.studbuddy.gpa

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.core.AppDataStore
import com.example.studbuddy.core.models.Course
import com.example.studbuddy.core.models.Semester
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.UUID

class CourseActivity : AppCompatActivity() {

    // ── Section 1: View References ─────────────────────
    private lateinit var toolbarCourse: Toolbar
    private lateinit var textViewGpaValue: TextView
    private lateinit var textViewTotalCreditsLabel: TextView
    private lateinit var textViewActiveSemesterName: TextView
    private lateinit var layoutSemesterChips: LinearLayout
    private lateinit var recyclerViewCourses: RecyclerView
    private lateinit var textViewEmptyCourses: TextView
    private lateinit var fabAddCourse: FloatingActionButton

    // ── Section 2: Adapter ─────────────────────────────
    private lateinit var courseAdapter: CourseAdapter

    // ── Section 3: Data ────────────────────────────────
    private val courseList = mutableListOf<Course>()
    private var selectedSemesterId: String? = null

    // ── Section 4: Lifecycle ───────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        toolbarCourse = findViewById(R.id.toolbarCourse)
        textViewGpaValue = findViewById(R.id.textViewGpaValue)
        textViewTotalCreditsLabel = findViewById(R.id.textViewTotalCreditsLabel)
        textViewActiveSemesterName = findViewById(R.id.textViewActiveSemesterName)
        layoutSemesterChips = findViewById(R.id.layoutSemesterChips)
        recyclerViewCourses = findViewById(R.id.recyclerViewCourses)
        textViewEmptyCourses = findViewById(R.id.textViewEmptyCourses)
        fabAddCourse = findViewById(R.id.fabAddCourse)

        setupToolbar()
        setupRecyclerView()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // ── Section 5: Setup ───────────────────────────────
    private fun setupToolbar() {
        setSupportActionBar(toolbarCourse)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter(
            items = courseList,
            onEditClick = { course -> showEditCourseDialog(course) },
            onDeleteClick = { course -> showDeleteConfirmation(course) }
        )
        recyclerViewCourses.layoutManager = LinearLayoutManager(this)
        recyclerViewCourses.adapter = courseAdapter
        recyclerViewCourses.setHasFixedSize(false)
    }

    private fun setupFab() {
        fabAddCourse.setOnClickListener { showAddCourseDialog() }
    }

    // ── Section 6: Data ────────────────────────────────
    private fun loadData() {
        val semesters = AppDataStore.getSemesterList()
        buildSemesterChips(semesters)
        if (selectedSemesterId == null) {
            selectedSemesterId = AppDataStore.getActiveSemester()?.id
        }
        selectSemester(selectedSemesterId)
    }

    private fun buildSemesterChips(semesters: List<Semester>) {
        layoutSemesterChips.removeAllViews()
        
        // Add "All" chip
        val allChip = LayoutInflater.from(this).inflate(R.layout.item_semester_chip, layoutSemesterChips, false) as TextView
        allChip.text = "All"
        styleChip(allChip, selectedSemesterId == null)
        allChip.setOnClickListener { selectSemester(null) }
        layoutSemesterChips.addView(allChip)

        semesters.forEach { semester ->
            val chip = LayoutInflater.from(this).inflate(R.layout.item_semester_chip, layoutSemesterChips, false) as TextView
            chip.text = semester.name
            styleChip(chip, semester.id == selectedSemesterId)
            chip.setOnClickListener { selectSemester(semester.id) }
            layoutSemesterChips.addView(chip)
        }
    }

    private fun styleChip(chip: TextView, isSelected: Boolean) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.chip_semester_active)
            chip.setTextColor(ContextCompat.getColor(this, R.color.colorTextOnPrimary))
        } else {
            chip.setBackgroundResource(R.drawable.chip_semester_inactive)
            chip.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary))
        }
    }

    private fun selectSemester(semesterId: String?) {
        selectedSemesterId = semesterId
        val courses = if (semesterId == null) {
            AppDataStore.getCourseList()
        } else {
            AppDataStore.getCoursesBySemester(semesterId)
        }
        courseList.clear()
        courseList.addAll(courses.sortedBy { it.name })
        courseAdapter.updateList(courseList)
        updateGpaSummary(courses)
        updateEmptyState()
        
        // Update chip visuals
        for (i in 0 until layoutSemesterChips.childCount) {
            val view = layoutSemesterChips.getChildAt(i) as TextView
            val isAllChip = i == 0
            val isSelected = if (isAllChip) semesterId == null else {
                val semester = AppDataStore.getSemesterList()[i - 1]
                semester.id == semesterId
            }
            styleChip(view, isSelected)
        }
    }

    private fun updateGpaSummary(courses: List<Course>) {
        val gpa = AppDataStore.calculateGpa(selectedSemesterId)
        textViewGpaValue.text = String.format("%.2f", gpa)
        val totalCredits = courses.sumOf { it.creditHours }
        textViewTotalCreditsLabel.text = "$totalCredits credit${if (totalCredits == 1) "" else "s"}"
        textViewActiveSemesterName.text = AppDataStore.getActiveSemester()?.name ?: getString(R.string.semester_none_active)
    }

    private fun saveCourse(course: Course) {
        AppDataStore.addCourse(course)
        if (selectedSemesterId == null || course.semesterId == selectedSemesterId) {
            courseList.add(course)
            courseList.sortBy { it.name }
            courseAdapter.updateList(courseList)
        }
        val currentCourses = if (selectedSemesterId == null) AppDataStore.getCourseList() else AppDataStore.getCoursesBySemester(selectedSemesterId!!)
        updateGpaSummary(currentCourses)
        updateEmptyState()
        Toast.makeText(this, "Course added", Toast.LENGTH_SHORT).show()
    }

    private fun updateCourse(course: Course) {
        AppDataStore.updateCourse(course)
        courseAdapter.updateItem(course)
        val currentCourses = if (selectedSemesterId == null) AppDataStore.getCourseList() else AppDataStore.getCoursesBySemester(selectedSemesterId!!)
        updateGpaSummary(currentCourses)
        Toast.makeText(this, "Course updated", Toast.LENGTH_SHORT).show()
    }

    private fun deleteCourse(id: String) {
        AppDataStore.deleteCourse(id)
        courseAdapter.removeItem(id)
        val currentCourses = if (selectedSemesterId == null) AppDataStore.getCourseList() else AppDataStore.getCoursesBySemester(selectedSemesterId!!)
        updateGpaSummary(currentCourses)
        updateEmptyState()
        Toast.makeText(this, "Course removed", Toast.LENGTH_SHORT).show()
    }

    // ── Section 7: Dialogs ─────────────────────────────
    private fun showAddCourseDialog() {
        val semesters = AppDataStore.getSemesterList()
        if (semesters.isEmpty()) {
            Toast.makeText(this, getString(R.string.course_error_no_semester), Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null)
        var selectedColor = COURSE_COLORS[0]
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextCourseName)
        val spinnerSemester = dialogView.findViewById<Spinner>(R.id.spinnerCourseSemester)
        val spinnerCredits = dialogView.findViewById<Spinner>(R.id.spinnerCreditHours)
        val spinnerGrade = dialogView.findViewById<Spinner>(R.id.spinnerGrade)
        val layoutColorPicker = dialogView.findViewById<LinearLayout>(R.id.layoutColorPicker)

        // Semester Spinner
        val semesterNames = semesters.map { it.name }
        spinnerSemester.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, semesterNames).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        val activeSemester = AppDataStore.getActiveSemester()
        if (activeSemester != null) {
            val index = semesters.indexOfFirst { it.id == activeSemester.id }
            if (index != -1) spinnerSemester.setSelection(index)
        }

        // Credits Spinner
        val creditOptions = (1..6).map { it.toString() }
        spinnerCredits.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, creditOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerCredits.setSelection(2) // Default 3

        // Grade Spinner
        spinnerGrade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Course.GRADE_OPTIONS).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        buildColorPicker(layoutColorPicker, { color -> selectedColor = color })

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.course_add_title))
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = editTextName.text.toString().trim()
                if (!validateCourseInput(name)) return@setOnClickListener
                
                val semester = semesters[spinnerSemester.selectedItemPosition]
                val credits = spinnerCredits.selectedItemPosition + 1
                val grade = spinnerGrade.selectedItem.toString()
                
                val course = Course(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    creditHours = credits,
                    grade = grade,
                    gradePoints = Course.gradeToPoints(grade),
                    semesterId = semester.id,
                    colorHex = selectedColor
                )
                saveCourse(course)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showEditCourseDialog(course: Course) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_course, null)
        var selectedColor = course.colorHex
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextCourseName)
        val spinnerSemester = dialogView.findViewById<Spinner>(R.id.spinnerCourseSemester)
        val spinnerCredits = dialogView.findViewById<Spinner>(R.id.spinnerCreditHours)
        val spinnerGrade = dialogView.findViewById<Spinner>(R.id.spinnerGrade)
        val layoutColorPicker = dialogView.findViewById<LinearLayout>(R.id.layoutColorPicker)

        editTextName.setText(course.name)

        // Semester Spinner
        val semesters = AppDataStore.getSemesterList()
        val semesterNames = semesters.map { it.name }
        spinnerSemester.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, semesterNames).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        val semesterIndex = semesters.indexOfFirst { it.id == course.semesterId }
        if (semesterIndex != -1) spinnerSemester.setSelection(semesterIndex)

        // Credits Spinner
        val creditOptions = (1..6).map { it.toString() }
        spinnerCredits.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, creditOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerCredits.setSelection(course.creditHours - 1)

        // Grade Spinner
        spinnerGrade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Course.GRADE_OPTIONS).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        val gradeIndex = Course.GRADE_OPTIONS.indexOf(course.grade)
        if (gradeIndex != -1) spinnerGrade.setSelection(gradeIndex)

        buildColorPicker(layoutColorPicker, { c -> selectedColor = c }, currentColor = course.colorHex)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.course_edit_title))
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = editTextName.text.toString().trim()
                if (!validateCourseInput(name)) return@setOnClickListener
                
                val semester = semesters[spinnerSemester.selectedItemPosition]
                val grade = spinnerGrade.selectedItem.toString()
                
                val updated = course.copy(
                    name = name,
                    creditHours = spinnerCredits.selectedItemPosition + 1,
                    grade = grade,
                    gradePoints = Course.gradeToPoints(grade),
                    semesterId = semester.id,
                    colorHex = selectedColor
                )
                updateCourse(updated)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showDeleteConfirmation(course: Course) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.course_delete_title))
            .setMessage(getString(R.string.course_delete_message, course.name))
            .setPositiveButton("Delete") { _, _ -> deleteCourse(course.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Section 8: Helpers ─────────────────────────────
    private fun validateCourseInput(name: String): Boolean {
        if (name.isBlank()) {
            Toast.makeText(this, getString(R.string.course_error_empty_name), Toast.LENGTH_SHORT).show()
            return false
        }
        if (name.length > 100) {
            Toast.makeText(this, getString(R.string.course_error_name_too_long), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun buildColorPicker(container: LinearLayout, onColorSelected: (String) -> Unit, currentColor: String = COURSE_COLORS[0]) {
        container.removeAllViews()
        COURSE_COLORS.forEach { colorHex ->
            val circle = View(this)
            val size = resources.getDimensionPixelSize(R.dimen.spacing_xlarge)
            val params = LinearLayout.LayoutParams(size, size)
            params.marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_small)
            circle.layoutParams = params
            
            val gd = GradientDrawable()
            gd.shape = GradientDrawable.OVAL
            gd.setColor(Color.parseColor(colorHex))
            if (colorHex == currentColor) {
                gd.setStroke(4, Color.WHITE)
            }
            circle.background = gd
            
            circle.setOnClickListener {
                onColorSelected(colorHex)
                buildColorPicker(container, onColorSelected, colorHex)
            }
            container.addView(circle)
        }
    }

    private fun updateEmptyState() {
        val isEmpty = courseList.isEmpty()
        val hasSemesters = AppDataStore.getSemesterList().isNotEmpty()
        textViewEmptyCourses.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerViewCourses.visibility = if (isEmpty) View.GONE else View.VISIBLE
        textViewEmptyCourses.text = if (!hasSemesters) {
            getString(R.string.course_empty_no_semester)
        } else {
            getString(R.string.course_empty_state)
        }
    }

    // ── Section 9: Companion ───────────────────────────
    companion object {
        val COURSE_COLORS = listOf(
            "#1565C0", "#6A1B9A", "#00695C",
            "#E65100", "#AD1457", "#283593",
            "#558B2F", "#4E342E"
        )
    }

    // ── Adapter ────────────────────────────────────────
    private inner class CourseAdapter(
        private val items: MutableList<Course>,
        private val onEditClick: (Course) -> Unit,
        private val onDeleteClick: (Course) -> Unit
    ) : RecyclerView.Adapter<CourseAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val viewCourseColorStripe: View = view.findViewById(R.id.viewCourseColorStripe)
            val textViewCourseItemName: TextView = view.findViewById(R.id.textViewCourseItemName)
            val textViewCourseItemCredits: TextView = view.findViewById(R.id.textViewCourseItemCredits)
            val textViewCourseItemSemester: TextView = view.findViewById(R.id.textViewCourseItemSemester)
            val textViewCourseItemGrade: TextView = view.findViewById(R.id.textViewCourseItemGrade)
            val textViewCourseItemGradePoints: TextView = view.findViewById(R.id.textViewCourseItemGradePoints)
            val imageButtonEditCourse: ImageButton = view.findViewById(R.id.imageButtonEditCourse)
            val imageButtonDeleteCourse: ImageButton = view.findViewById(R.id.imageButtonDeleteCourse)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val course = items[position]
            holder.viewCourseColorStripe.setBackgroundColor(Color.parseColor(course.colorHex))
            holder.textViewCourseItemName.text = course.name
            holder.textViewCourseItemCredits.text = "${course.creditHours} credit${if (course.creditHours == 1) "" else "s"}"
            
            val semesterName = AppDataStore.getSemesterList().find { it.id == course.semesterId }?.name ?: "Unknown"
            holder.textViewCourseItemSemester.text = semesterName
            
            holder.textViewCourseItemGrade.text = course.grade
            holder.textViewCourseItemGradePoints.text = String.format("%.1f pts", course.gradePoints)
            
            holder.imageButtonEditCourse.setOnClickListener { onEditClick(course) }
            holder.imageButtonDeleteCourse.setOnClickListener { onDeleteClick(course) }
        }

        override fun getItemCount(): Int = items.size

        fun updateList(newItems: List<Course>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        fun addItem(item: Course) {
            items.add(item)
            notifyItemInserted(items.size - 1)
        }

        fun removeItem(id: String) {
            val pos = items.indexOfFirst { it.id == id }
            if (pos >= 0) {
                items.removeAt(pos)
                notifyItemRemoved(pos)
            }
        }

        fun updateItem(item: Course) {
            val pos = items.indexOfFirst { it.id == item.id }
            if (pos >= 0) {
                items[pos] = item
                notifyItemChanged(pos)
            }
        }
    }
}
