package com.example.studbuddy.attendance

import android.app.DatePickerDialog
import android.content.res.ColorStateList
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
import com.example.studbuddy.core.models.AttendanceRecord
import com.example.studbuddy.core.models.AttendanceRecord.AttendanceStatus
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class AttendanceActivity : AppCompatActivity() {

    // ── Section 1: View References ─────────────────────
    private lateinit var toolbarAttendance: Toolbar
    private lateinit var cardAttendanceWarning: CardView
    private lateinit var textViewAttendanceWarningMessage: TextView
    private lateinit var radioGroupAttendanceView: RadioGroup
    private lateinit var spinnerAttendanceCourseFilter: Spinner
    private lateinit var recyclerViewAttendance: RecyclerView
    private lateinit var textViewEmptyAttendance: TextView
    private lateinit var fabMarkAttendance: FloatingActionButton

    // ── Section 2: Adapter & State ─────────────────────
    private lateinit var attendanceAdapter: AttendanceAdapter
    private val displayList = mutableListOf<Any>()
    private var currentView = AttendanceView.SUMMARY
    private var selectedCourseFilter = "All"

    enum class AttendanceView { SUMMARY, RECORDS }

    data class AttendanceSummary(
        val courseName: String,
        val totalClasses: Int,
        val presentCount: Int,
        val absentCount: Int,
        val lateCount: Int,
        val percentage: Double
    )

    // ── Section 3: Lifecycle ───────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        toolbarAttendance = findViewById(R.id.toolbarAttendance)
        cardAttendanceWarning = findViewById(R.id.cardAttendanceWarning)
        textViewAttendanceWarningMessage = findViewById(R.id.textViewAttendanceWarningMessage)
        radioGroupAttendanceView = findViewById(R.id.radioGroupAttendanceView)
        spinnerAttendanceCourseFilter = findViewById(R.id.spinnerAttendanceCourseFilter)
        recyclerViewAttendance = findViewById(R.id.recyclerViewAttendance)
        textViewEmptyAttendance = findViewById(R.id.textViewEmptyAttendance)
        fabMarkAttendance = findViewById(R.id.fabMarkAttendance)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupViewToggle()
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
        setSupportActionBar(toolbarAttendance)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceAdapter(displayList,
            onDeleteClick = { showDeleteConfirmation(it) })
        recyclerViewAttendance.layoutManager = LinearLayoutManager(this)
        recyclerViewAttendance.adapter = attendanceAdapter
    }

    private fun setupFab() {
        fabMarkAttendance.setOnClickListener { showMarkAttendanceDialog() }
    }

    private fun setupViewToggle() {
        radioGroupAttendanceView.setOnCheckedChangeListener { _, checkedId ->
            currentView = if (checkedId == R.id.radioAttendanceRecords) AttendanceView.RECORDS else AttendanceView.SUMMARY
            loadData()
        }
    }

    // ── Section 5: Data ────────────────────────────────
    private fun loadData() {
        val allRecords = AppDataStore.getAttendanceList()
        when (currentView) {
            AttendanceView.SUMMARY -> {
                spinnerAttendanceCourseFilter.visibility = View.GONE
                val summaries = buildSummaries(allRecords)
                displayList.clear()
                displayList.addAll(summaries)
                checkAttendanceWarnings(summaries)
            }
            AttendanceView.RECORDS -> {
                spinnerAttendanceCourseFilter.visibility = View.VISIBLE
                val courseNames = allRecords.map { it.courseName }.distinct().sorted()
                buildCourseFilterSpinner(listOf("All") + courseNames)
                val filtered = if (selectedCourseFilter == "All") allRecords
                else allRecords.filter { it.courseName == selectedCourseFilter }
                val sorted = filtered.sortedByDescending { it.date }
                displayList.clear()
                displayList.addAll(sorted)
                cardAttendanceWarning.visibility = View.GONE
            }
        }
        attendanceAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun buildSummaries(records: List<AttendanceRecord>): List<AttendanceSummary> {
        return records
            .groupBy { it.courseName }
            .map { (course, recs) ->
                val present = recs.count { it.status == AttendanceStatus.PRESENT }
                val absent  = recs.count { it.status == AttendanceStatus.ABSENT }
                val late    = recs.count { it.status == AttendanceStatus.LATE }
                val total   = recs.size
                val effective = present + (late * 0.5)
                val pct = if (total == 0) 100.0 else (effective / total) * 100.0
                AttendanceSummary(course, total, present, absent, late, pct)
            }
            .sortedBy { it.courseName }
    }

    private fun checkAttendanceWarnings(summaries: List<AttendanceSummary>) {
        val criticalCourses = summaries.filter { it.percentage < ATTENDANCE_THRESHOLD }
        if (criticalCourses.isEmpty()) {
            cardAttendanceWarning.visibility = View.GONE
            return
        }
        cardAttendanceWarning.visibility = View.VISIBLE
        val worst = criticalCourses.minByOrNull { it.percentage }!!
        val messageRes = if (worst.percentage < ATTENDANCE_CRITICAL)
            R.string.attendance_threshold_critical
        else
            R.string.attendance_threshold_warning
        
        textViewAttendanceWarningMessage.text = getString(messageRes, worst.courseName, worst.percentage)
        
        cardAttendanceWarning.setCardBackgroundColor(
            ContextCompat.getColor(this,
                if (worst.percentage < ATTENDANCE_CRITICAL) R.color.colorStatusCritical
                else R.color.colorStatusWarning))
    }

    private fun buildCourseFilterSpinner(courses: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAttendanceCourseFilter.adapter = adapter
        spinnerAttendanceCourseFilter.setSelection(courses.indexOf(selectedCourseFilter).coerceAtLeast(0))
        spinnerAttendanceCourseFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (selectedCourseFilter != courses[position]) {
                    selectedCourseFilter = courses[position]
                    loadData()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun saveRecord(record: AttendanceRecord) {
        AppDataStore.addAttendanceRecord(record)
        loadData()
        Toast.makeText(this, getString(R.string.attendance_record_added), Toast.LENGTH_SHORT).show()
    }

    private fun deleteRecord(id: String) {
        AppDataStore.deleteAttendanceRecord(id)
        loadData()
        Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show()
    }

    // ── Section 6: Dialogs ─────────────────────────────
    private fun showMarkAttendanceDialog() {
        val courses = AppDataStore.getCourseNames()
        if (courses.isEmpty()) {
            Toast.makeText(this, getString(R.string.attendance_error_no_courses_exist), Toast.LENGTH_LONG).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mark_attendance, null)
        var selectedDate = getTodayMidnightEpoch()

        val spinnerCourse = dialogView.findViewById<Spinner>(R.id.spinnerMarkAttendanceCourse)
        val buttonPickDate = dialogView.findViewById<Button>(R.id.buttonPickAttendanceDate)
        val textViewDate = dialogView.findViewById<TextView>(R.id.textViewAttendanceDateDisplay)
        val radioGroupStatus = dialogView.findViewById<RadioGroup>(R.id.radioGroupAttendanceStatus)
        val editTextNotes = dialogView.findViewById<EditText>(R.id.editTextAttendanceNotes)

        spinnerCourse.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val dateFmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        textViewDate.text = "Today (${dateFmt.format(Date(selectedDate))})"

        buttonPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val c = Calendar.getInstance().apply {
                    set(y, m, d, 0, 0, 0); set(Calendar.MILLISECOND, 0)
                }
                selectedDate = c.timeInMillis
                textViewDate.text = dateFmt.format(Date(selectedDate))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.attendance_mark_title))
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val course = spinnerCourse.selectedItem?.toString() ?: ""
                if (course.isBlank()) {
                    Toast.makeText(this, getString(R.string.attendance_error_no_course), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val status = when (radioGroupStatus.checkedRadioButtonId) {
                    R.id.radioStatusAbsent -> AttendanceStatus.ABSENT
                    R.id.radioStatusLate   -> AttendanceStatus.LATE
                    else                   -> AttendanceStatus.PRESENT
                }
                val notes = editTextNotes.text.toString().trim()
                val record = AttendanceRecord(
                    id = UUID.randomUUID().toString(),
                    courseName = course,
                    date = selectedDate,
                    status = status,
                    notes = notes
                )
                saveRecord(record)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showDeleteConfirmation(record: AttendanceRecord) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.attendance_delete_title))
            .setMessage(getString(R.string.attendance_delete_message))
            .setPositiveButton("Delete") { _, _ -> deleteRecord(record.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Section 7: Helpers ─────────────────────────────
    private fun updateEmptyState() {
        val isEmpty = displayList.isEmpty()
        textViewEmptyAttendance.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerViewAttendance.visibility = if (isEmpty) View.GONE else View.VISIBLE
        if (isEmpty) {
            textViewEmptyAttendance.text = getString(R.string.attendance_empty_state)
        }
    }

    private fun getTodayMidnightEpoch(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // ── Section 8: Inner Adapter ───────────────────────
    private inner class AttendanceAdapter(
        private val items: List<Any>,
        private val onDeleteClick: (AttendanceRecord) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        inner class SummaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val cardAttendanceSummaryItem: CardView = view.findViewById(R.id.cardAttendanceSummaryItem)
            val textViewSummaryCourseName: TextView = view.findViewById(R.id.textViewSummaryCourseName)
            val textViewSummaryPercentage: TextView = view.findViewById(R.id.textViewSummaryPercentage)
            val progressBarAttendance: ProgressBar = view.findViewById(R.id.progressBarAttendance)
            val textViewSummaryPresent: TextView = view.findViewById(R.id.textViewSummaryPresent)
            val textViewSummaryAbsent: TextView = view.findViewById(R.id.textViewSummaryAbsent)
            val textViewSummaryLate: TextView = view.findViewById(R.id.textViewSummaryLate)
            val textViewThresholdStatus: TextView = view.findViewById(R.id.textViewThresholdStatus)
            val viewCriticalOverlay: View = view.findViewById(R.id.viewCriticalOverlay)
        }

        inner class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val viewAttendanceStatusDot: View = view.findViewById(R.id.viewAttendanceStatusDot)
            val textViewRecordStatus: TextView = view.findViewById(R.id.textViewRecordStatus)
            val textViewRecordCourseName: TextView = view.findViewById(R.id.textViewRecordCourseName)
            val textViewRecordDate: TextView = view.findViewById(R.id.textViewRecordDate)
            val textViewRecordNotes: TextView = view.findViewById(R.id.textViewRecordNotes)
            val imageButtonDeleteRecord: ImageButton = view.findViewById(R.id.imageButtonDeleteRecord)
        }

        override fun getItemViewType(position: Int) =
            if (items[position] is AttendanceSummary) VIEW_TYPE_SUMMARY else VIEW_TYPE_RECORD

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == VIEW_TYPE_SUMMARY) {
                SummaryViewHolder(inflater.inflate(R.layout.item_attendance_summary, parent, false))
            } else {
                RecordViewHolder(inflater.inflate(R.layout.item_attendance_record, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val context = holder.itemView.context
            if (holder is SummaryViewHolder) {
                val summary = items[position] as AttendanceSummary
                holder.textViewSummaryCourseName.text = summary.courseName
                val pct = summary.percentage
                holder.textViewSummaryPercentage.text = String.format("%.0f%%", pct)
                
                val pctColor = when {
                    pct >= ATTENDANCE_THRESHOLD -> R.color.colorStatusNormal
                    pct >= ATTENDANCE_CRITICAL  -> R.color.colorStatusWarning
                    else                        -> R.color.colorStatusCritical
                }
                holder.textViewSummaryPercentage.setTextColor(ContextCompat.getColor(context, pctColor))
                holder.progressBarAttendance.progress = pct.toInt()
                holder.progressBarAttendance.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context, pctColor))
                
                holder.textViewSummaryPresent.text = "✓ ${summary.presentCount} Present"
                holder.textViewSummaryAbsent.text  = "✗ ${summary.absentCount} Absent"
                holder.textViewSummaryLate.text    = "~ ${summary.lateCount} Late"
                
                val (statusText, statusColor) = when {
                    pct >= ATTENDANCE_THRESHOLD -> Pair("✓ Safe", R.color.colorStatusNormal)
                    pct >= ATTENDANCE_CRITICAL  -> Pair("⚠ Warning", R.color.colorStatusWarning)
                    else -> Pair("🚨 Critical", R.color.colorStatusCritical)
                }
                holder.textViewThresholdStatus.text = statusText
                holder.textViewThresholdStatus.setTextColor(ContextCompat.getColor(context, statusColor))
                holder.viewCriticalOverlay.visibility = if (pct < ATTENDANCE_CRITICAL) View.VISIBLE else View.GONE
                
            } else if (holder is RecordViewHolder) {
                val record = items[position] as AttendanceRecord
                val (statusText, statusColor, dotDrawable) = when (record.status) {
                    AttendanceStatus.PRESENT -> Triple("Present", R.color.colorStatusNormal, R.drawable.attendance_status_present)
                    AttendanceStatus.ABSENT  -> Triple("Absent", R.color.colorStatusCritical, R.drawable.attendance_status_absent)
                    AttendanceStatus.LATE    -> Triple("Late", R.color.colorStatusWarning, R.drawable.attendance_status_late)
                }
                holder.viewAttendanceStatusDot.setBackgroundResource(dotDrawable)
                holder.textViewRecordStatus.text = statusText
                holder.textViewRecordStatus.setTextColor(ContextCompat.getColor(context, statusColor))
                holder.textViewRecordCourseName.text = record.courseName
                
                val sdf = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())
                holder.textViewRecordDate.text = sdf.format(Date(record.date))
                
                if (record.notes.isBlank()) {
                    holder.textViewRecordNotes.visibility = View.GONE
                } else {
                    holder.textViewRecordNotes.visibility = View.VISIBLE
                    holder.textViewRecordNotes.text = record.notes
                }
                holder.imageButtonDeleteRecord.setOnClickListener { onDeleteClick(record) }
            }
        }

        override fun getItemCount() = items.size
    }

    companion object {
        private const val VIEW_TYPE_SUMMARY = 0
        private const val VIEW_TYPE_RECORD  = 1
        private const val ATTENDANCE_THRESHOLD = 75.0
        private const val ATTENDANCE_CRITICAL  = 60.0
    }
}
