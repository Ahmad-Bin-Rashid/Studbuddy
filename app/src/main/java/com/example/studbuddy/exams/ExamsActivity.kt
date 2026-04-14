package com.example.studbuddy.exams

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.core.AppDataStore
import com.example.studbuddy.core.models.Course
import com.example.studbuddy.core.models.Exam
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class ExamsActivity : AppCompatActivity() {

    // ── Section 1: View References ─────────────────────
    private lateinit var toolbarExams: Toolbar
    private lateinit var radioGroupExamFilter: RadioGroup
    private lateinit var recyclerViewExams: RecyclerView
    private lateinit var textViewEmptyExams: TextView
    private lateinit var fabAddExam: FloatingActionButton

    // ── Section 2: Adapter & State ─────────────────────
    private lateinit var examsAdapter: ExamsAdapter
    private val examList = mutableListOf<Exam>()
    private var currentFilter = ExamFilter.UPCOMING

    enum class ExamFilter { UPCOMING, PAST }

    // ── Section 3: Lifecycle ───────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exams)

        toolbarExams = findViewById(R.id.toolbarExams)
        radioGroupExamFilter = findViewById(R.id.radioGroupExamFilter)
        recyclerViewExams = findViewById(R.id.recyclerViewExams)
        textViewEmptyExams = findViewById(R.id.textViewEmptyExams)
        fabAddExam = findViewById(R.id.fabAddExam)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupFilter()
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
        setSupportActionBar(toolbarExams)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        examsAdapter = ExamsAdapter(examList,
            onGradeClick = { showGradeDialog(it) },
            onEditClick = { showEditDialog(it) },
            onDeleteClick = { showDeleteConfirmation(it) })
        recyclerViewExams.layoutManager = LinearLayoutManager(this)
        recyclerViewExams.adapter = examsAdapter
    }

    private fun setupFab() {
        fabAddExam.setOnClickListener { showAddDialog() }
    }

    private fun setupFilter() {
        radioGroupExamFilter.setOnCheckedChangeListener { _, checkedId ->
            currentFilter = if (checkedId == R.id.radioExamPast) ExamFilter.PAST else ExamFilter.UPCOMING
            loadData()
        }
    }

    // ── Section 5: Data ────────────────────────────────
    private fun loadData() {
        val all = AppDataStore.getExamList()
        val now = System.currentTimeMillis()
        val filtered = when (currentFilter) {
            ExamFilter.UPCOMING -> all.filter { it.examDate > now }.sortedBy { it.examDate }
            ExamFilter.PAST -> all.filter { it.examDate <= now }.sortedByDescending { it.examDate }
        }
        examList.clear()
        examList.addAll(filtered)
        examsAdapter.updateList(examList)
        updateEmptyState()
    }

    private fun saveExam(e: Exam) {
        AppDataStore.addExam(e)
        loadData()
        Toast.makeText(this, "Exam added", Toast.LENGTH_SHORT).show()
    }

    private fun updateExam(e: Exam) {
        AppDataStore.updateExam(e)
        loadData()
        Toast.makeText(this, "Exam updated", Toast.LENGTH_SHORT).show()
    }

    private fun deleteExam(id: String) {
        AppDataStore.deleteExam(id)
        loadData()
        Toast.makeText(this, "Exam removed", Toast.LENGTH_SHORT).show()
    }

    private fun recordGrade(exam: Exam, obtainedMarks: Double) {
        val updated = exam.copy(obtainedMarks = obtainedMarks)
        AppDataStore.updateExam(updated)
        recalculateCourseGrade(exam.courseName)
        loadData()
        Toast.makeText(this, getString(R.string.exam_grade_recorded), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, getString(R.string.exam_error_no_courses_exist), Toast.LENGTH_LONG).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exam, null)
        val calendar = Calendar.getInstance()
        var selectedDate = 0L
        var selectedTime = ""

        val spinnerCourse = dialogView.findViewById<Spinner>(R.id.spinnerExamCourse)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerExamType)
        val buttonPickDate = dialogView.findViewById<Button>(R.id.buttonPickExamDate)
        val textViewDate = dialogView.findViewById<TextView>(R.id.textViewExamDateDisplay)
        val buttonPickTime = dialogView.findViewById<Button>(R.id.buttonPickExamTime)
        val textViewTime = dialogView.findViewById<TextView>(R.id.textViewExamTimeDisplay)
        val editTextWeightage = dialogView.findViewById<EditText>(R.id.editTextExamWeightage)
        val editTextTotalMarks = dialogView.findViewById<EditText>(R.id.editTextExamTotalMarks)
        val spinnerDuration = dialogView.findViewById<Spinner>(R.id.spinnerExamDuration)
        val editTextVenue = dialogView.findViewById<EditText>(R.id.editTextExamVenue)
        val editTextNotes = dialogView.findViewById<EditText>(R.id.editTextExamNotes)

        spinnerCourse.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Exam.EXAM_TYPES).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        buttonPickDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d)
                selectedDate = calendar.timeInMillis
                textViewDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        buttonPickTime.setOnClickListener {
            TimePickerDialog(this, { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, min)
                selectedTime = String.format("%02d:%02d", h, min)
                textViewTime.text = selectedTime
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        val durationOptions = (1..8).map { "${it * 30} minutes" }
        spinnerDuration.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durationOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerDuration.setSelection(2)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.exam_add_title)
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val courseName = spinnerCourse.selectedItem?.toString() ?: ""
                if (selectedDate == 0L || selectedTime.isEmpty()) {
                    Toast.makeText(this, getString(R.string.exam_error_no_date), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val weightage = editTextWeightage.text.toString().toDoubleOrNull() ?: 0.0
                val totalMarks = editTextTotalMarks.text.toString().toDoubleOrNull() ?: 100.0
                val duration = (spinnerDuration.selectedItemPosition + 1) * 30

                val exam = Exam(
                    id = UUID.randomUUID().toString(),
                    courseName = courseName,
                    examType = spinnerType.selectedItem.toString(),
                    examDate = calendar.timeInMillis,
                    venue = editTextVenue.text.toString().trim(),
                    durationMinutes = duration,
                    notes = editTextNotes.text.toString().trim(),
                    weightage = weightage,
                    obtainedMarks = -1.0,
                    totalMarks = totalMarks
                )
                saveExam(exam)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showEditDialog(exam: Exam) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exam, null)
        val calendar = Calendar.getInstance().apply { timeInMillis = exam.examDate }
        var selectedDate = exam.examDate
        var selectedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(exam.examDate))

        val spinnerCourse = dialogView.findViewById<Spinner>(R.id.spinnerExamCourse)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerExamType)
        val buttonPickDate = dialogView.findViewById<Button>(R.id.buttonPickExamDate)
        val textViewDate = dialogView.findViewById<TextView>(R.id.textViewExamDateDisplay)
        val buttonPickTime = dialogView.findViewById<Button>(R.id.buttonPickExamTime)
        val textViewTime = dialogView.findViewById<TextView>(R.id.textViewExamTimeDisplay)
        val editTextWeightage = dialogView.findViewById<EditText>(R.id.editTextExamWeightage)
        val editTextTotalMarks = dialogView.findViewById<EditText>(R.id.editTextExamTotalMarks)
        val spinnerDuration = dialogView.findViewById<Spinner>(R.id.spinnerExamDuration)
        val editTextVenue = dialogView.findViewById<EditText>(R.id.editTextExamVenue)
        val editTextNotes = dialogView.findViewById<EditText>(R.id.editTextExamNotes)

        val courses = AppDataStore.getCourseNames()
        spinnerCourse.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerCourse.setSelection(courses.indexOf(exam.courseName).coerceAtLeast(0))

        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Exam.EXAM_TYPES).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerType.setSelection(Exam.EXAM_TYPES.indexOf(exam.examType).coerceAtLeast(0))

        textViewDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(exam.examDate))
        textViewTime.text = selectedTime

        buttonPickDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d)
                selectedDate = calendar.timeInMillis
                textViewDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        buttonPickTime.setOnClickListener {
            TimePickerDialog(this, { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, min)
                selectedTime = String.format("%02d:%02d", h, min)
                textViewTime.text = selectedTime
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        val durationOptions = (1..8).map { "${it * 30} minutes" }
        spinnerDuration.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durationOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerDuration.setSelection((exam.durationMinutes / 30) - 1)

        editTextWeightage.setText(exam.weightage.toString())
        editTextTotalMarks.setText(exam.totalMarks.toString())
        editTextVenue.setText(exam.venue)
        editTextNotes.setText(exam.notes)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.exam_edit_title)
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val weightage = editTextWeightage.text.toString().toDoubleOrNull() ?: 0.0
                val totalMarks = editTextTotalMarks.text.toString().toDoubleOrNull() ?: 100.0
                
                updateExam(exam.copy(
                    courseName = spinnerCourse.selectedItem.toString(),
                    examType = spinnerType.selectedItem.toString(),
                    examDate = calendar.timeInMillis,
                    venue = editTextVenue.text.toString().trim(),
                    durationMinutes = (spinnerDuration.selectedItemPosition + 1) * 30,
                    notes = editTextNotes.text.toString().trim(),
                    weightage = weightage,
                    totalMarks = totalMarks
                ))
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showGradeDialog(exam: Exam) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_grade_assignment, null) // Reusing assignment grade layout
        val textViewName = dialogView.findViewById<TextView>(R.id.textViewGradeAssignmentName)
        val textViewCourse = dialogView.findViewById<TextView>(R.id.textViewGradeAssignmentCourse)
        val textViewTotal = dialogView.findViewById<TextView>(R.id.textViewGradeTotalMarks)
        val editTextObtained = dialogView.findViewById<EditText>(R.id.editTextObtainedMarks)
        val textViewPreview = dialogView.findViewById<TextView>(R.id.textViewGradePercentagePreview)

        textViewName.text = "${exam.examType} Exam"
        textViewCourse.text = exam.courseName
        textViewTotal.text = "${exam.totalMarks.toInt()} marks"
        if (exam.obtainedMarks >= 0) {
            editTextObtained.setText(exam.obtainedMarks.toInt().toString())
        }

        editTextObtained.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val obtained = s.toString().toDoubleOrNull()
                if (obtained != null && exam.totalMarks > 0) {
                    val pct = (obtained / exam.totalMarks) * 100.0
                    textViewPreview.text = String.format("%.1f%% — %s", pct, percentageToGrade(pct))
                } else {
                    textViewPreview.text = "– %"
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.exam_grade_title))
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val obtained = editTextObtained.text.toString().toDoubleOrNull()
                if (obtained == null || obtained < 0 || obtained > exam.totalMarks) {
                    Toast.makeText(this, "Invalid marks", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                recordGrade(exam, obtained)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(exam: Exam) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.exam_delete_title))
            .setMessage(getString(R.string.exam_delete_message, exam.courseName))
            .setPositiveButton("Delete") { _, _ -> deleteExam(exam.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateEmptyState() {
        val isEmpty = examList.isEmpty()
        textViewEmptyExams.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerViewExams.visibility = if (isEmpty) View.GONE else View.VISIBLE
        if (isEmpty) {
            textViewEmptyExams.text = if (currentFilter == ExamFilter.UPCOMING) getString(R.string.exam_empty_upcoming) else getString(R.string.exam_empty_past)
        }
    }

    // ── Section 8: Inner Adapter ───────────────────────
    private inner class ExamsAdapter(
        private val items: MutableList<Exam>,
        private val onGradeClick: (Exam) -> Unit,
        private val onEditClick: (Exam) -> Unit,
        private val onDeleteClick: (Exam) -> Unit
    ) : RecyclerView.Adapter<ExamsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textViewExamItemCourse: TextView = view.findViewById(R.id.textViewExamItemCourse)
            val textViewExamItemType: TextView = view.findViewById(R.id.textViewExamItemType)
            val textViewExamCountdown: TextView = view.findViewById(R.id.textViewExamCountdown)
            val textViewExamItemDate: TextView = view.findViewById(R.id.textViewExamItemDate)
            val layoutExamVenue: LinearLayout = view.findViewById(R.id.layoutExamVenue)
            val textViewExamItemVenue: TextView = view.findViewById(R.id.textViewExamItemVenue)
            val textViewExamWeightage: TextView = view.findViewById(R.id.textViewExamWeightage)
            val textViewExamMarks: TextView = view.findViewById(R.id.textViewExamMarks)
            val textViewExamDuration: TextView = view.findViewById(R.id.textViewExamDuration)
            val textViewExamNotes: TextView = view.findViewById(R.id.textViewExamNotes)
            val imageButtonGradeExam: ImageButton = view.findViewById(R.id.imageButtonGradeExam)
            val imageButtonEditExam: ImageButton = view.findViewById(R.id.imageButtonEditExam)
            val imageButtonDeleteExam: ImageButton = view.findViewById(R.id.imageButtonDeleteExam)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exam, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val exam = items[position]
            val now = System.currentTimeMillis()
            val diff = exam.examDate - now
            
            holder.textViewExamItemCourse.text = exam.courseName
            holder.textViewExamItemType.text = exam.examType
            
            val countdownText = when {
                diff < 0 -> getString(R.string.exam_countdown_past)
                diff < 3_600_000 -> "< 1 hr"
                diff < 86_400_000 -> "${diff / 3_600_000}h left"
                diff < 86_400_000 * 2 -> getString(R.string.exam_countdown_today)
                else -> "${diff / 86_400_000}d left"
            }
            holder.textViewExamCountdown.text = countdownText
            
            val countdownBg = when {
                diff < 0 -> R.color.colorStatusComplete
                diff < 86_400_000 -> R.color.colorStatusCritical
                diff < 259_200_000 -> R.color.colorStatusWarning
                else -> R.color.colorStatusNormal
            }
            val gd = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f.dpToPx().toFloat()
                setColor(ContextCompat.getColor(holder.itemView.context, countdownBg))
            }
            holder.textViewExamCountdown.background = gd

            val sdf = SimpleDateFormat("EEE, MMM dd yyyy 'at' HH:mm", Locale.getDefault())
            holder.textViewExamItemDate.text = sdf.format(Date(exam.examDate))
            
            if (exam.venue.isBlank()) {
                holder.layoutExamVenue.visibility = View.GONE
            } else {
                holder.layoutExamVenue.visibility = View.VISIBLE
                holder.textViewExamItemVenue.text = exam.venue
            }
            
            holder.textViewExamWeightage.text = "${exam.weightage.toInt()}% of grade"
            holder.textViewExamMarks.text = if (exam.obtainedMarks < 0) getString(R.string.exam_not_taken)
                                            else "${exam.obtainedMarks.toInt()}/${exam.totalMarks.toInt()}"
            holder.textViewExamDuration.text = "${exam.durationMinutes} min"
            
            if (exam.notes.isBlank()) {
                holder.textViewExamNotes.visibility = View.GONE
            } else {
                holder.textViewExamNotes.visibility = View.VISIBLE
                holder.textViewExamNotes.text = exam.notes
            }

            holder.imageButtonGradeExam.setOnClickListener { onGradeClick(exam) }
            holder.imageButtonEditExam.setOnClickListener { onEditClick(exam) }
            holder.imageButtonDeleteExam.setOnClickListener { onDeleteClick(exam) }
        }

        override fun getItemCount() = items.size

        fun updateList(newItems: List<Exam>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }
}

private fun Float.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
