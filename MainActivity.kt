package com.example.studbuddy

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import com.example.studbuddy.core.BaseActivity
import com.example.studbuddy.core.ViewModelFactory
import com.example.studbuddy.core.models.Semester
import com.example.studbuddy.home.DashboardUiState
import com.example.studbuddy.home.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity() {

    private lateinit var btnSetupSemester: Button
    private lateinit var cardSetupSemester: CardView
    private lateinit var dashboardItemsContainer: LinearLayout
    private lateinit var tvToolbarTitle: TextView
    private lateinit var btnEditSemester: ImageView

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((application as StudBuddyApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        setupSidebar()
        observeViewModel()
    }

    private fun setupViews() {
        btnSetupSemester = findViewById(R.id.btnSetupSemester)
        cardSetupSemester = findViewById(R.id.cardSetupSemester)
        dashboardItemsContainer = findViewById(R.id.dashboardItemsContainer)
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle)
        btnEditSemester = findViewById(R.id.btnEditSemester)

        btnSetupSemester.setOnClickListener { showSemesterDialog() }
        btnEditSemester.setOnClickListener { 
            viewModel.uiState.value?.semester?.let { showSemesterDialog(it) }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            updateUi(state)
        }
    }

    private fun updateUi(state: DashboardUiState) {
        val semester = state.semester
        if (semester != null) {
            cardSetupSemester.visibility = View.GONE
            btnEditSemester.visibility = View.VISIBLE
            tvToolbarTitle.text = semester.name
            updateDashboard(state)
        } else {
            cardSetupSemester.visibility = View.VISIBLE
            btnEditSemester.visibility = View.GONE
            tvToolbarTitle.text = getString(R.string.dashboard_title)
            dashboardItemsContainer.removeAllViews()
        }
    }

    private fun updateDashboard(state: DashboardUiState) {
        dashboardItemsContainer.removeAllViews()
        val semester = state.semester ?: return
        val courses = state.courses
        val timetable = state.timetable
        val attendance = state.attendance
        val assignments = state.assignments
        val exams = state.exams

        // 1. Next Lecture Card
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
        val dayIndex = if (currentDay == 1) 7 else currentDay - 1 // Adjust to 1=Mon...7=Sun
        val currentTime = String.format("%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))

        val nextLecture = timetable
            .filter { it.dayOfWeek == dayIndex && it.startTime > currentTime }
            .minByOrNull { it.startTime }
            ?: timetable
                .filter { it.dayOfWeek > dayIndex || (it.dayOfWeek < dayIndex) }
                .sortedWith(compareBy({ (it.dayOfWeek - dayIndex + 7) % 7 }, { it.startTime }))
                .firstOrNull()

        if (nextLecture != null) {
            val course = courses.find { it.id == nextLecture.courseId }
            val dayName = when(nextLecture.dayOfWeek) {
                1 -> "Monday"; 2 -> "Tuesday"; 3 -> "Wednesday"; 4 -> "Thursday"
                5 -> "Friday"; 6 -> "Saturday"; 7 -> "Sunday"; else -> ""
            }
            addDashboardCard(
                "Next Lecture",
                "${course?.name ?: "Unknown"}: ${nextLecture.startTime}",
                "Day: $dayName | Room: ${nextLecture.room}"
            )
        } else {
            addDashboardCard("Next Lecture", "No lectures scheduled", "Check timetable for full schedule")
        }

        // 2. Short Attendance Card
        val shortAttendanceDetails = mutableListOf<String>()
        val threshold = 75.0
        courses.forEach { course ->
            val records = attendance.filter { it.courseId == course.id }
            val percentage = if (records.isNotEmpty()) (records.count { it.status == "PRESENT" }.toDouble() / records.size) * 100 else 100.0
            if (percentage < threshold) {
                shortAttendanceDetails.add("${course.name} (${String.format("%.1f%%", percentage)})")
            }
        }
        if (shortAttendanceDetails.isNotEmpty()) {
            addDashboardCard(
                "Attendance Alert", 
                "Short attendance in: ${shortAttendanceDetails.joinToString(", ")}", 
                "Required Threshold: ${String.format("%.0f%%", threshold)}"
            )
        }

        // 3. Pending Assignment Card
        val pendingAssignments = assignments
            .filter { !it.isCompleted }
            .sortedBy { it.dueDate }
        
        if (pendingAssignments.isNotEmpty()) {
            val topAssignment = pendingAssignments.first()
            val course = courses.find { it.id == topAssignment.courseId }
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            addDashboardCard(
                "Pending Assignment",
                "${topAssignment.name} (${course?.name ?: "Unknown"})",
                "Due Date: ${sdf.format(Date(topAssignment.dueDate))}"
            )
        }

        // 4. Next Exam Card
        val nextExam = exams
            .filter { !it.isCompleted && it.date > System.currentTimeMillis() }
            .minByOrNull { it.date }
        
        if (nextExam != null) {
            val course = courses.find { it.id == nextExam.courseId }
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            addDashboardCard(
                "Next Exam",
                "${course?.name ?: "Unknown"}: ${nextExam.type}",
                "Date: ${sdf.format(Date(nextExam.date))} | Venue: ${nextExam.venue ?: "TBD"}"
            )
        }

        // 5. Semester Status / GPA Card
        val totalPoints = courses.sumOf { it.gradePoints }
        val totalCredits = courses.filter { it.grade != null }.sumOf { it.creditHours }.toDouble()
        val calculatedGpa = if (totalCredits > 0) totalPoints / totalCredits else 0.0
        
        // Note: GPA update logic should ideally be in ViewModel to avoid side effects during UI update,
        // but for now we keep it here to match existing logic.
        // Actually, let's move it to ViewModel eventually.

        addDashboardCard(
            "Semester GPA", 
            "Current GPA: ${String.format("%.2f", calculatedGpa)}", 
            if (semester.isActive) "Active Semester" else "Inactive Semester"
        )
    }

    private fun addDashboardCard(title: String, main: String, sub: String) {
        val cardView = LayoutInflater.from(this).inflate(R.layout.item_dashboard_card, dashboardItemsContainer, false)
        cardView.findViewById<TextView>(R.id.tvCardTitle).text = title
        cardView.findViewById<TextView>(R.id.tvCardMainText).text = main
        cardView.findViewById<TextView>(R.id.tvCardSubText).text = sub
        dashboardItemsContainer.addView(cardView)
    }

    private fun showSemesterDialog(existingSemester: Semester? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_semester, null)
        val etName = dialogView.findViewById<EditText>(R.id.etSemesterName)
        val btnStart = dialogView.findViewById<Button>(R.id.btnStartDate)
        val btnEnd = dialogView.findViewById<Button>(R.id.btnEndDate)
        val cbActive = dialogView.findViewById<CheckBox>(R.id.cbIsActive)

        var startMs = existingSemester?.startDate ?: 0L
        var endMs = existingSemester?.endDate ?: 0L

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        existingSemester?.let {
            etName.setText(it.name)
            cbActive.isChecked = it.isActive
            if (startMs > 0) btnStart.text = sdf.format(Date(startMs))
            if (endMs > 0) btnEnd.text = sdf.format(Date(endMs))
        }

        btnStart.setOnClickListener {
            showDatePicker { ms -> 
                startMs = ms
                btnStart.text = sdf.format(Date(ms))
            }
        }

        btnEnd.setOnClickListener {
            showDatePicker { ms -> 
                endMs = ms
                btnEnd.text = sdf.format(Date(ms))
            }
        }

        AlertDialog.Builder(this)
            .setTitle(if (existingSemester == null) "Setup Semester" else "Edit Semester")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter semester name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (startMs > 0 && endMs > startMs) {
                    val semester = Semester(
                        id = existingSemester?.id ?: UUID.randomUUID().toString(),
                        name = name,
                        startDate = startMs,
                        endDate = endMs,
                        isActive = cbActive.isChecked,
                        gpa = existingSemester?.gpa ?: 0.0
                    )
                    viewModel.saveSemester(semester)
                } else {
                    Toast.makeText(this, "Please select valid dates", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val selected = Calendar.getInstance()
            selected.set(y, m, d)
            onDateSelected(selected.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }
}
