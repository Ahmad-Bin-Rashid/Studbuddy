package com.example.studbuddy.assignments

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.studbuddy.core.models.Assignment
import com.example.studbuddy.core.models.Course
import com.example.studbuddy.core.models.Priority
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class AssignmentsActivity : AppCompatActivity() {

    // ── Section 1: View References ─────────────────────
    private lateinit var toolbarAssignments: Toolbar
    private lateinit var textViewStatTotal: TextView
    private lateinit var textViewStatPending: TextView
    private lateinit var textViewStatOverdue: TextView
    private lateinit var textViewStatCompleted: TextView
    private lateinit var radioGroupAssignmentFilter: RadioGroup
    private lateinit var spinnerAssignmentSort: Spinner
    private lateinit var recyclerViewAssignments: RecyclerView
    private lateinit var textViewEmptyAssignments: TextView
    private lateinit var fabAddAssignment: FloatingActionButton

    // ── Section 2: Adapter & State ─────────────────────
    private lateinit var assignmentsAdapter: AssignmentsAdapter
    private val assignmentList = mutableListOf<Assignment>()
    private var currentFilter = AssignmentFilter.ALL
    private var currentSort = AssignmentSort.DUE_DATE

    enum class AssignmentFilter { ALL, PENDING, COMPLETED }
    enum class AssignmentSort { DUE_DATE, PRIORITY, COURSE }

    // ── Section 3: Lifecycle ───────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignments)

        toolbarAssignments = findViewById(R.id.toolbarAssignments)
        textViewStatTotal = findViewById(R.id.textViewStatTotal)
        textViewStatPending = findViewById(R.id.textViewStatPending)
        textViewStatOverdue = findViewById(R.id.textViewStatOverdue)
        textViewStatCompleted = findViewById(R.id.textViewStatCompleted)
        radioGroupAssignmentFilter = findViewById(R.id.radioGroupAssignmentFilter)
        spinnerAssignmentSort = findViewById(R.id.spinnerAssignmentSort)
        recyclerViewAssignments = findViewById(R.id.recyclerViewAssignments)
        textViewEmptyAssignments = findViewById(R.id.textViewEmptyAssignments)
        fabAddAssignment = findViewById(R.id.fabAddAssignment)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupFilterAndSort()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // ── Section 4: Setup ───────────────────────────────
    private fun setupToolbar() {
        setSupportActionBar(toolbarAssignments)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        assignmentsAdapter = AssignmentsAdapter(assignmentList,
            onCheckChanged = { assignment, isChecked ->
                val updated = assignment.copy(isCompleted = isChecked)
                AppDataStore.updateAssignment(updated)
                loadData()
            },
            onGradeClick = { showGradeDialog(it) },
            onEditClick = { showEditDialog(it) },
            onDeleteClick = { showDeleteConfirmation(it) })
        recyclerViewAssignments.layoutManager = LinearLayoutManager(this)
        recyclerViewAssignments.adapter = assignmentsAdapter
    }

    private fun setupFab() {
        fabAddAssignment.setOnClickListener { showAddDialog() }
    }

    private fun setupFilterAndSort() {
        radioGroupAssignmentFilter.setOnCheckedChangeListener { _, checkedId ->
            currentFilter = when (checkedId) {
                R.id.radioAssignmentPending -> AssignmentFilter.PENDING
                R.id.radioAssignmentCompleted -> AssignmentFilter.COMPLETED
                else -> AssignmentFilter.ALL
            }
            loadData()
        }

        spinnerAssignmentSort.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, SORT_OPTIONS).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerAssignmentSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSort = AssignmentSort.values()[position]
                loadData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ── Section 5: Data ────────────────────────────────
    private fun loadData() {
        val all = AppDataStore.getAssignmentList()
        updateStats(all)
        val filtered = applyFilterAndSort(all)
        assignmentList.clear()
        assignmentList.addAll(filtered)
        assignmentsAdapter.updateList(assignmentList)
        updateEmptyState()
    }

    private fun updateStats(all: List<Assignment>) {
        val now = System.currentTimeMillis()
        textViewStatTotal.text = all.size.toString()
        textViewStatPending.text = all.count { !it.isCompleted }.toString()
        textViewStatOverdue.text = all.count { !it.isCompleted && it.dueDate < now }.toString()
        textViewStatCompleted.text = all.count { it.isCompleted }.toString()
    }

    private fun applyFilterAndSort(all: List<Assignment>): List<Assignment> {
        val filtered = when (currentFilter) {
            AssignmentFilter.PENDING -> all.filter { !it.isCompleted }
            AssignmentFilter.COMPLETED -> all.filter { it.isCompleted }
            else -> all
        }
        return when (currentSort) {
            AssignmentSort.DUE_DATE -> filtered.sortedBy { it.dueDate }
            AssignmentSort.PRIORITY -> filtered.sortedByDescending { it.priority.ordinal }
            AssignmentSort.COURSE -> filtered.sortedBy { it.courseName }
        }
    }

    private fun saveAssignment(a: Assignment) {
        AppDataStore.addAssignment(a)
        loadData()
        Toast.makeText(this, "Assignment added", Toast.LENGTH_SHORT).show()
    }

    private fun updateAssignment(a: Assignment) {
        AppDataStore.updateAssignment(a)
        loadData()
        Toast.makeText(this, "Assignment updated", Toast.LENGTH_SHORT).show()
    }

    private fun deleteAssignment(id: String) {
        AppDataStore.deleteAssignment(id)
        loadData()
        Toast.makeText(this, "Assignment removed", Toast.LENGTH_SHORT).show()
    }

    private fun recordGrade(assignment: Assignment, obtainedMarks: Double) {
        val updated = assignment.copy(obtainedMarks = obtainedMarks)
        AppDataStore.updateAssignment(updated)
        recalculateCourseGrade(assignment.courseName)
        loadData()
        Toast.makeText(this, getString(R.string.assignment_grade_recorded), Toast.LENGTH_SHORT).show()
    }

    private fun recalculateCourseGrade(courseName: String) {
        val assignments = AppDataStore.getAssignmentList()
            .filter { it.courseName == courseName && it.obtainedMarks >= 0 && it.totalMarks > 0 }
        val exams = AppDataStore.getExamList()
            .filter { it.courseName == courseName && it.obtainedMarks >= 0 && it.totalMarks > 0 }
        
        val totalWeightage = assignments.sumOf { it.weightage } + exams.sumOf { it.weightage }
        if (totalWeightage <= 0.0) return

        val earnedWeightage = assignments.sumOf { (it.obtainedMarks / it.totalMarks) * it.weightage } +
                             exams.sumOf { (it.obtainedMarks / it.totalMarks) * it.weightage }
        
        val effectivePct = (earnedWeightage / totalWeightage) * 100.0
        val newGrade = percentageToGrade(effectivePct)
        val course = AppDataStore.getCourseList().firstOrNull { it.name == courseName } ?: return
        
        AppDataStore.updateCourse(course.copy(
            grade = newGrade,
            gradePoints = Course.gradeToPoints(newGrade)
        ))
    }

    private fun percentageToGrade(pct: Double): String {
        return when {
            pct >= 93.0 -> "A"
            pct >= 90.0 -> "A-"
            pct >= 87.0 -> "B+"
            pct >= 83.0 -> "B"
            pct >= 80.0 -> "B-"
            pct >= 77.0 -> "C+"
            pct >= 73.0 -> "C"
            pct >= 70.0 -> "C-"
            pct >= 67.0 -> "D+"
            pct >= 60.0 -> "D"
            else -> "F"
        }
    }

    // ── Section 6: Dialogs ─────────────────────────────
    private fun showAddDialog() {
        val courses = AppDataStore.getCourseNames()
        if (courses.isEmpty()) {
            Toast.makeText(this, getString(R.string.assignment_error_no_courses_exist), Toast.LENGTH_LONG).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_assignment, null)
        var selectedDate = 0L
        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextAssignmentTitle)
        val spinnerCourse = dialogView.findViewById<Spinner>(R.id.spinnerAssignmentCourse)
        val buttonPickDate = dialogView.findViewById<Button>(R.id.buttonPickAssignmentDate)
        val textViewDate = dialogView.findViewById<TextView>(R.id.textViewAssignmentDateDisplay)
        val radioGroupPriority = dialogView.findViewById<RadioGroup>(R.id.radioGroupPriority)
        val editTextWeightage = dialogView.findViewById<EditText>(R.id.editTextAssignmentWeightage)
        val editTextTotalMarks = dialogView.findViewById<EditText>(R.id.editTextAssignmentTotalMarks)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.editTextAssignmentDescription)

        spinnerCourse.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        buttonPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val c = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }
                selectedDate = c.timeInMillis
                textViewDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.assignment_add_title)
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = editTextTitle.text.toString().trim()
                val course = spinnerCourse.selectedItem?.toString() ?: ""
                val weightage = editTextWeightage.text.toString().toDoubleOrNull() ?: 0.0
                val totalMarks = editTextTotalMarks.text.toString().toDoubleOrNull() ?: 100.0
                
                if (!validateAssignmentInput(title, course, selectedDate)) return@setOnClickListener
                
                val priority = when (radioGroupPriority.checkedRadioButtonId) {
                    R.id.radioPriorityHigh -> Priority.HIGH
                    R.id.radioPriorityLow -> Priority.LOW
                    else -> Priority.MEDIUM
                }

                val assignment = Assignment(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    courseName = course,
                    dueDate = selectedDate,
                    priority = priority,
                    description = editTextDescription.text.toString().trim(),
                    isCompleted = false,
                    weightage = weightage,
                    obtainedMarks = -1.0,
                    totalMarks = totalMarks
                )
                saveAssignment(assignment)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showEditDialog(assignment: Assignment) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_assignment, null)
        var selectedDate = assignment.dueDate
        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextAssignmentTitle)
        val spinnerCourse = dialogView.findViewById<Spinner>(R.id.spinnerAssignmentCourse)
        val buttonPickDate = dialogView.findViewById<Button>(R.id.buttonPickAssignmentDate)
        val textViewDate = dialogView.findViewById<TextView>(R.id.textViewAssignmentDateDisplay)
        val radioGroupPriority = dialogView.findViewById<RadioGroup>(R.id.radioGroupPriority)
        val editTextWeightage = dialogView.findViewById<EditText>(R.id.editTextAssignmentWeightage)
        val editTextTotalMarks = dialogView.findViewById<EditText>(R.id.editTextAssignmentTotalMarks)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.editTextAssignmentDescription)

        editTextTitle.setText(assignment.title)
        val courses = AppDataStore.getCourseNames()
        spinnerCourse.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerCourse.setSelection(courses.indexOf(assignment.courseName).coerceAtLeast(0))
        
        textViewDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate))
        buttonPickDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
            DatePickerDialog(this, { _, y, m, d ->
                val c = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }
                selectedDate = c.timeInMillis
                textViewDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        radioGroupPriority.check(when (assignment.priority) {
            Priority.HIGH -> R.id.radioPriorityHigh
            Priority.LOW -> R.id.radioPriorityLow
            else -> R.id.radioPriorityMedium
        })

        editTextWeightage.setText(assignment.weightage.toString())
        editTextTotalMarks.setText(assignment.totalMarks.toString())
        editTextDescription.setText(assignment.description)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.assignment_edit_title)
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = editTextTitle.text.toString().trim()
                val course = spinnerCourse.selectedItem?.toString() ?: ""
                val weightage = editTextWeightage.text.toString().toDoubleOrNull() ?: 0.0
                val totalMarks = editTextTotalMarks.text.toString().toDoubleOrNull() ?: 100.0
                
                if (!validateAssignmentInput(title, course, selectedDate)) return@setOnClickListener
                
                val priority = when (radioGroupPriority.checkedRadioButtonId) {
                    R.id.radioPriorityHigh -> Priority.HIGH
                    R.id.radioPriorityLow -> Priority.LOW
                    else -> Priority.MEDIUM
                }

                updateAssignment(assignment.copy(
                    title = title,
                    courseName = course,
                    dueDate = selectedDate,
                    priority = priority,
                    weightage = weightage,
                    totalMarks = totalMarks,
                    description = editTextDescription.text.toString().trim()
                ))
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showGradeDialog(assignment: Assignment) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_grade_assignment, null)
        val textViewName = dialogView.findViewById<TextView>(R.id.textViewGradeAssignmentName)
        val textViewCourse = dialogView.findViewById<TextView>(R.id.textViewGradeAssignmentCourse)
        val textViewTotal = dialogView.findViewById<TextView>(R.id.textViewGradeTotalMarks)
        val editTextObtained = dialogView.findViewById<EditText>(R.id.editTextObtainedMarks)
        val textViewPreview = dialogView.findViewById<TextView>(R.id.textViewGradePercentagePreview)

        textViewName.text = assignment.title
        textViewCourse.text = assignment.courseName
        textViewTotal.text = "${assignment.totalMarks.toInt()} marks"
        if (assignment.obtainedMarks >= 0) {
            editTextObtained.setText(assignment.obtainedMarks.toInt().toString())
        }

        editTextObtained.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val obtained = s.toString().toDoubleOrNull()
                if (obtained != null && assignment.totalMarks > 0) {
                    val pct = (obtained / assignment.totalMarks) * 100.0
                    textViewPreview.text = String.format("%.1f%% — %s", pct, percentageToGrade(pct))
                } else {
                    textViewPreview.text = "– %"
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.assignment_grade_title))
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val obtained = editTextObtained.text.toString().toDoubleOrNull()
                if (obtained == null || obtained < 0 || obtained > assignment.totalMarks) {
                    Toast.makeText(this, getString(R.string.assignment_error_invalid_marks), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                recordGrade(assignment, obtained)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(assignment: Assignment) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.assignment_delete_title))
            .setMessage(getString(R.string.assignment_delete_message, assignment.title))
            .setPositiveButton("Delete") { _, _ -> deleteAssignment(assignment.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Section 7: Helpers ─────────────────────────────
    private fun validateAssignmentInput(title: String, course: String, dueDate: Long): Boolean {
        if (title.isBlank()) {
            Toast.makeText(this, getString(R.string.assignment_error_empty_title), Toast.LENGTH_SHORT).show()
            return false
        }
        if (course.isBlank()) {
            Toast.makeText(this, getString(R.string.assignment_error_no_course), Toast.LENGTH_SHORT).show()
            return false
        }
        if (dueDate == 0L) {
            Toast.makeText(this, getString(R.string.assignment_error_no_date), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun updateEmptyState() {
        val isEmpty = assignmentList.isEmpty()
        textViewEmptyAssignments.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerViewAssignments.visibility = if (isEmpty) View.GONE else View.VISIBLE
        
        if (isEmpty) {
            textViewEmptyAssignments.text = when (currentFilter) {
                AssignmentFilter.PENDING -> getString(R.string.assignment_empty_pending)
                AssignmentFilter.COMPLETED -> getString(R.string.assignment_empty_completed)
                else -> getString(R.string.assignment_empty_state)
            }
        }
    }

    // ── Section 8: Inner Adapter ───────────────────────
    private inner class AssignmentsAdapter(
        private val items: MutableList<Assignment>,
        private val onCheckChanged: (Assignment, Boolean) -> Unit,
        private val onGradeClick: (Assignment) -> Unit,
        private val onEditClick: (Assignment) -> Unit,
        private val onDeleteClick: (Assignment) -> Unit
    ) : RecyclerView.Adapter<AssignmentsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val cardAssignmentItem: CardView = view.findViewById(R.id.cardAssignmentItem)
            val checkBoxAssignmentComplete: CheckBox = view.findViewById(R.id.checkBoxAssignmentComplete)
            val textViewAssignmentTitle: TextView = view.findViewById(R.id.textViewAssignmentTitle)
            val textViewPriorityBadge: TextView = view.findViewById(R.id.textViewPriorityBadge)
            val textViewAssignmentCourse: TextView = view.findViewById(R.id.textViewAssignmentCourse)
            val textViewAssignmentDueDate: TextView = view.findViewById(R.id.textViewAssignmentDueDate)
            val textViewAssignmentDescription: TextView = view.findViewById(R.id.textViewAssignmentDescription)
            val textViewAssignmentWeightage: TextView = view.findViewById(R.id.textViewAssignmentWeightage)
            val textViewAssignmentMarks: TextView = view.findViewById(R.id.textViewAssignmentMarks)
            val imageButtonGradeAssignment: ImageButton = view.findViewById(R.id.imageButtonGradeAssignment)
            val imageButtonEditAssignment: ImageButton = view.findViewById(R.id.imageButtonEditAssignment)
            val imageButtonDeleteAssignment: ImageButton = view.findViewById(R.id.imageButtonDeleteAssignment)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_assignment, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val a = items[position]
            val now = System.currentTimeMillis()
            val isOverdue = !a.isCompleted && a.dueDate < now

            holder.textViewAssignmentTitle.text = a.title
            holder.textViewAssignmentTitle.paintFlags = if (a.isCompleted) {
                holder.textViewAssignmentTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                holder.textViewAssignmentTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            holder.cardAssignmentItem.alpha = if (a.isCompleted) 0.65f else 1.0f

            holder.textViewPriorityBadge.text = a.priority.name
            val badgeColor = when (a.priority) {
                Priority.HIGH -> R.color.colorStatusCritical
                Priority.MEDIUM -> R.color.colorStatusWarning
                Priority.LOW -> R.color.colorStatusNormal
            }
            holder.textViewPriorityBadge.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, badgeColor))

            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dueDateStr = when {
                isOverdue -> getString(R.string.assignment_overdue_label)
                a.dueDate - now < 86_400_000L -> getString(R.string.assignment_due_today)
                else -> sdf.format(Date(a.dueDate))
            }
            holder.textViewAssignmentDueDate.text = dueDateStr
            holder.textViewAssignmentDueDate.setTextColor(ContextCompat.getColor(holder.itemView.context,
                if (isOverdue) R.color.colorStatusCritical
                else if (a.dueDate - now < 86_400_000L) R.color.colorStatusWarning
                else R.color.colorTextSecondary))

            holder.textViewAssignmentDescription.visibility = if (a.description.isBlank()) View.GONE else View.VISIBLE
            holder.textViewAssignmentDescription.text = a.description
            holder.textViewAssignmentWeightage.text = "${a.weightage.toInt()}% of grade"
            holder.textViewAssignmentMarks.text = if (a.obtainedMarks < 0) getString(R.string.assignment_not_graded)
                                                 else "${a.obtainedMarks.toInt()}/${a.totalMarks.toInt()}"
            holder.textViewAssignmentMarks.setTextColor(ContextCompat.getColor(holder.itemView.context,
                if (a.obtainedMarks < 0) R.color.colorTextSecondary else R.color.colorStatusNormal))

            holder.textViewAssignmentCourse.text = a.courseName

            holder.checkBoxAssignmentComplete.setOnCheckedChangeListener(null)
            holder.checkBoxAssignmentComplete.isChecked = a.isCompleted
            holder.checkBoxAssignmentComplete.setOnCheckedChangeListener { _, checked -> onCheckChanged(a, checked) }

            holder.imageButtonGradeAssignment.setOnClickListener { onGradeClick(a) }
            holder.imageButtonEditAssignment.setOnClickListener { onEditClick(a) }
            holder.imageButtonDeleteAssignment.setOnClickListener { onDeleteClick(a) }
        }

        override fun getItemCount() = items.size

        fun updateList(newItems: List<Assignment>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    companion object {
        val SORT_OPTIONS = listOf("Due Date", "Priority", "Course")
    }
}
