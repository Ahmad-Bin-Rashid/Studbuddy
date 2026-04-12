# FILE: MODULE_ATTENDANCE.md

# Module: Attendance

**Owner:** Dev-C  
**Package:** `com.studbuddy.attendance`  
**Activity:** `AttendanceActivity`  
**Adapter:** `AttendanceAdapter`  
**Layout (Activity):** `activity_attendance.xml`  
**Layout (Item):** `item_attendance.xml`  
**Layout (Dialog):** `dialog_mark_attendance.xml`

---

## 1. Purpose

The Attendance module tracks student attendance per course. It calculates the attendance percentage per course, warns when attendance falls below a configurable threshold (default 75%), and allows marking each class session as Present, Absent, or Late.

---

## 2. Data Model

```kotlin
enum class AttendanceStatus { PRESENT, ABSENT, LATE }

data class AttendanceRecord(
    val id: String,              // UUID
    val courseName: String,
    val date: Long,              // epoch ms — time stored as midnight (00:00:00) of that day
    val status: AttendanceStatus,
    val notes: String            // optional
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("courseName", courseName)
        put("date", date)
        put("status", status.name)
        put("notes", notes)
    }

    companion object {
        fun fromJson(obj: JSONObject): AttendanceRecord = AttendanceRecord(
            id = obj.getString("id"),
            courseName = obj.getString("courseName"),
            date = obj.getLong("date"),
            status = AttendanceStatus.valueOf(obj.getString("status")),
            notes = obj.optString("notes", "")
        )
    }
}

// Summary model — computed, not stored
data class AttendanceSummary(
    val courseName: String,
    val totalClasses: Int,
    val presentCount: Int,
    val absentCount: Int,
    val lateCount: Int,
    val percentage: Double   // (present + late) / total * 100
)
```

---

## 3. UI Screens

### Screen 1: Summary View (default)

- Toolbar: "Attendance"
- Tab-like ToggleGroup: "Summary" / "Records"
- RecyclerView showing one `AttendanceSummary` card per course
- Each card shows course name, percentage bar (ProgressBar), count breakdown
- FAB: opens Mark Attendance dialog

### Screen 2: Records View (toggled)

- Same RecyclerView, different adapter binding showing individual records
- Course filter Spinner at top
- Each item shows: date, status badge, course name, notes

---

## 4. XML Layout Structure

### `activity_attendance.xml`

```
CoordinatorLayout (match_parent × match_parent)
│
├── AppBarLayout
│   └── Toolbar (id: toolbarAttendance)
│
├── LinearLayout (vertical, padding: 8dp)
│   ├── RadioGroup (id: radioGroupView, horizontal)
│   │   ├── RadioButton (id: radioViewSummary, text: "Summary", checked)
│   │   └── RadioButton (id: radioViewRecords, text: "Records")
│   ├── Spinner (id: spinnerCourseFilter, visibility: gone initially)
│   └── RelativeLayout (match_parent × 0dp, weight: 1)
│       ├── RecyclerView (id: recyclerViewAttendance)
│       └── TextView (id: textViewEmptyAttendance, center, visibility: gone)
│
└── FloatingActionButton (id: fabMarkAttendance)
```

### `item_attendance_summary.xml`

```
CardView (match_parent × wrap_content, margin: 8dp)
└── LinearLayout (vertical, padding: 16dp)
    ├── LinearLayout (horizontal)
    │   ├── TextView (id: textViewSummaryCourse, 16sp, bold, weight: 1)
    │   └── TextView (id: textViewPercentage, 18sp, bold)
    │       — color: green ≥75%, orange 60–74%, red <60%
    ├── ProgressBar (id: progressBarAttendance, style: horizontal, max: 100)
    │   — progressTint: same color logic as percentage TextView
    └── LinearLayout (horizontal)
        ├── TextView (id: textViewPresentCount, green, 12sp)  "✓ N Present"
        ├── TextView (id: textViewAbsentCount, red, 12sp)     "✗ N Absent"
        └── TextView (id: textViewLateCount, orange, 12sp)    "~ N Late"
```

### `item_attendance_record.xml`

```
CardView (match_parent × wrap_content, margin: 6dp)
└── LinearLayout (horizontal, padding: 12dp)
    ├── TextView (id: textViewStatusBadge, 12sp, background: status color, padding: 4dp)
    │   — "P" / "A" / "L" with matching background
    └── LinearLayout (vertical, weight: 1, marginStart: 12dp)
        ├── TextView (id: textViewRecordDate, 14sp, bold)     "Mon, Apr 15 2024"
        ├── TextView (id: textViewRecordCourse, 13sp, colorTextSecondary)
        └── TextView (id: textViewRecordNotes, 12sp, colorTextSecondary, maxLines: 1)
            — GONE if notes is blank
    └── ImageButton (id: imageButtonDeleteRecord)
```

### `dialog_mark_attendance.xml`

```
LinearLayout (vertical, padding: 16dp)
├── TextView ("Course *")
├── Spinner (id: spinnerAttendanceCourse)
├── TextView ("Date *")
├── Button (id: buttonPickAttendanceDate, text: "Select Date")
├── TextView (id: textViewAttendanceDate) — shows selected date
├── TextView ("Status *")
├── RadioGroup (id: radioGroupStatus, horizontal)
│   ├── RadioButton (id: radioPresent, text: "Present", checked)
│   ├── RadioButton (id: radioAbsent, text: "Absent")
│   └── RadioButton (id: radioLate, text: "Late")
├── TextView ("Notes (optional)")
└── EditText (id: editTextAttendanceNotes, inputType: textMultiLine, lines: 2)
```

---

## 5. RecyclerView Design

The attendance RecyclerView uses **two different item layouts** depending on the view mode:

```kotlin
// Two view types
companion object {
    const val VIEW_TYPE_SUMMARY = 0
    const val VIEW_TYPE_RECORD  = 1
}

override fun getItemViewType(position: Int): Int = currentViewType

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
        VIEW_TYPE_SUMMARY -> SummaryViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_attendance_summary, parent, false))
        else -> RecordViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_attendance_record, parent, false))
    }
}
```

- **Summary mode:** One item per unique course name
- **Records mode:** One item per `AttendanceRecord`, sorted by date DESC

---

## 6. Adapter Responsibilities

```kotlin
class AttendanceAdapter(
    private val summaries: MutableList<AttendanceSummary>,
    private val records: MutableList<AttendanceRecord>,
    private var viewMode: ViewMode,
    private val onDeleteRecord: (AttendanceRecord) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ViewMode { SUMMARY, RECORDS }

    inner class SummaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewSummaryCourse: TextView = view.findViewById(R.id.textViewSummaryCourse)
        val textViewPercentage: TextView = view.findViewById(R.id.textViewPercentage)
        val progressBarAttendance: ProgressBar = view.findViewById(R.id.progressBarAttendance)
        val textViewPresentCount: TextView = view.findViewById(R.id.textViewPresentCount)
        val textViewAbsentCount: TextView = view.findViewById(R.id.textViewAbsentCount)
        val textViewLateCount: TextView = view.findViewById(R.id.textViewLateCount)
    }

    inner class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewStatusBadge: TextView = view.findViewById(R.id.textViewStatusBadge)
        val textViewRecordDate: TextView = view.findViewById(R.id.textViewRecordDate)
        val textViewRecordCourse: TextView = view.findViewById(R.id.textViewRecordCourse)
        val textViewRecordNotes: TextView = view.findViewById(R.id.textViewRecordNotes)
        val imageButtonDeleteRecord: ImageButton = view.findViewById(R.id.imageButtonDeleteRecord)
    }

    override fun getItemCount(): Int = when (viewMode) {
        ViewMode.SUMMARY -> summaries.size
        ViewMode.RECORDS -> records.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (viewMode) {
            ViewMode.SUMMARY -> bindSummary(holder as SummaryViewHolder, summaries[position])
            ViewMode.RECORDS -> bindRecord(holder as RecordViewHolder, records[position])
        }
    }

    private fun bindSummary(holder: SummaryViewHolder, summary: AttendanceSummary) {
        holder.textViewSummaryCourse.text = summary.courseName
        val pct = summary.percentage.toInt()
        holder.textViewPercentage.text = "$pct%"
        holder.progressBarAttendance.progress = pct
        val color = getAttendanceColor(holder.itemView.context, summary.percentage)
        holder.textViewPercentage.setTextColor(color)
        holder.progressBarAttendance.progressTintList = ColorStateList.valueOf(color)
        holder.textViewPresentCount.text = "✓ ${summary.presentCount} Present"
        holder.textViewAbsentCount.text = "✗ ${summary.absentCount} Absent"
        holder.textViewLateCount.text = "~ ${summary.lateCount} Late"
    }

    private fun bindRecord(holder: RecordViewHolder, record: AttendanceRecord) {
        val sdf = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())
        holder.textViewRecordDate.text = sdf.format(Date(record.date))
        holder.textViewRecordCourse.text = record.courseName
        holder.textViewRecordNotes.visibility =
            if (record.notes.isBlank()) View.GONE else View.VISIBLE
        holder.textViewRecordNotes.text = record.notes

        val (badgeText, badgeColor) = when (record.status) {
            AttendanceStatus.PRESENT -> Pair("P", R.color.colorStatusNormal)
            AttendanceStatus.ABSENT  -> Pair("A", R.color.colorStatusCritical)
            AttendanceStatus.LATE    -> Pair("L", R.color.colorStatusWarning)
        }
        holder.textViewStatusBadge.text = badgeText
        holder.textViewStatusBadge.setBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, badgeColor))
        holder.imageButtonDeleteRecord.setOnClickListener { onDeleteRecord(record) }
    }

    private fun getAttendanceColor(context: Context, percentage: Double): Int {
        return when {
            percentage >= 75.0 -> ContextCompat.getColor(context, R.color.colorStatusNormal)
            percentage >= 60.0 -> ContextCompat.getColor(context, R.color.colorStatusWarning)
            else               -> ContextCompat.getColor(context, R.color.colorStatusCritical)
        }
    }

    fun switchMode(mode: ViewMode) {
        viewMode = mode
        notifyDataSetChanged()
    }

    fun updateSummaries(newSummaries: List<AttendanceSummary>) {
        summaries.clear(); summaries.addAll(newSummaries); notifyDataSetChanged()
    }

    fun updateRecords(newRecords: List<AttendanceRecord>) {
        records.clear(); records.addAll(newRecords); notifyDataSetChanged()
    }
}
```

---

## 7. User Interactions

| Interaction | Result |
|---|---|
| Select "Summary" radio | Switch adapter to SUMMARY mode, hide course filter Spinner |
| Select "Records" radio | Switch adapter to RECORDS mode, show course filter Spinner |
| Select course in filter Spinner | Filter records to that course |
| Tap FAB | `showMarkAttendanceDialog()` |
| Tap Delete on record | `showDeleteConfirmation(record)` |

---

## 8. Dialog Designs

### Mark Attendance Dialog

```kotlin
private fun showMarkAttendanceDialog() {
    val dialogView = LayoutInflater.from(this)
        .inflate(R.layout.dialog_mark_attendance, null)
    var selectedDate: Long = getTodayMidnightEpoch()

    val spinnerCourse = dialogView.findViewById<Spinner>(R.id.spinnerAttendanceCourse)
    val buttonPickDate = dialogView.findViewById<Button>(R.id.buttonPickAttendanceDate)
    val textViewDate = dialogView.findViewById<TextView>(R.id.textViewAttendanceDate)
    val radioGroupStatus = dialogView.findViewById<RadioGroup>(R.id.radioGroupStatus)
    val editTextNotes = dialogView.findViewById<EditText>(R.id.editTextAttendanceNotes)

    // Populate courses from AppDataStore
    val courses = AppDataStore.getCourseNames()
    spinnerCourse.adapter = ArrayAdapter(this,
        android.R.layout.simple_spinner_item, courses)

    // Default date = today
    textViewDate.text = formatDate(selectedDate)

    buttonPickDate.setOnClickListener {
        showDatePicker { date ->
            selectedDate = date
            textViewDate.text = formatDate(date)
        }
    }

    AlertDialog.Builder(this)
        .setTitle("Mark Attendance")
        .setView(dialogView)
        .setPositiveButton("Save") { _, _ ->
            val course = spinnerCourse.selectedItem?.toString() ?: ""
            val status = when (radioGroupStatus.checkedRadioButtonId) {
                R.id.radioAbsent -> AttendanceStatus.ABSENT
                R.id.radioLate   -> AttendanceStatus.LATE
                else             -> AttendanceStatus.PRESENT
            }
            val notes = editTextNotes.text.toString().trim()
            if (course.isBlank()) {
                Toast.makeText(this, "Select a course", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            val record = AttendanceRecord(
                id = UUID.randomUUID().toString(),
                courseName = course,
                date = selectedDate,
                status = status,
                notes = notes
            )
            saveAttendanceRecord(record)
        }
        .setNegativeButton("Cancel", null)
        .show()
}
```

---

## 9. Business Logic

```kotlin
// Get midnight epoch for today
private fun getTodayMidnightEpoch(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

// Build summaries from raw records
private fun buildSummaries(records: List<AttendanceRecord>): List<AttendanceSummary> {
    val grouped = records.groupBy { it.courseName }
    return grouped.map { (course, courseRecords) ->
        val presentCount = courseRecords.count { it.status == AttendanceStatus.PRESENT }
        val absentCount  = courseRecords.count { it.status == AttendanceStatus.ABSENT }
        val lateCount    = courseRecords.count { it.status == AttendanceStatus.LATE }
        val total        = courseRecords.size
        // Late counts as half-present for percentage
        val effectivePresent = presentCount + (lateCount * 0.5)
        val percentage   = if (total == 0) 100.0 else (effectivePresent / total) * 100.0
        AttendanceSummary(course, total, presentCount, absentCount, lateCount, percentage)
    }.sortedBy { it.courseName }
}

// Check and warn if any course is below threshold
private fun checkAttendanceWarnings() {
    val threshold = 75.0
    val lowCourses = buildSummaries(AppDataStore.getAttendanceList())
        .filter { it.percentage < threshold }
    if (lowCourses.isNotEmpty()) {
        val names = lowCourses.joinToString(", ") { it.courseName }
        Toast.makeText(this, "⚠ Low attendance: $names", Toast.LENGTH_LONG).show()
    }
}

// Save record
private fun saveAttendanceRecord(record: AttendanceRecord) {
    AppDataStore.addAttendanceRecord(record)
    loadData()
    checkAttendanceWarnings()
}
```

**Percentage Formula:**
```
percentage = (presentCount + lateCount × 0.5) / totalClasses × 100
```

---

## 10. Edge Cases

| Case | Handling |
|---|---|
| No courses added | Spinner empty — block save with Toast "Add courses in GPA module first" |
| Duplicate record (same course + date) | Allowed — user may have multiple sessions per day |
| Date in future | Allowed — pre-marking is valid |
| No attendance records | Summary shows empty state |
| Course filtered, no records | Show "No records for this course" in empty TextView |
| All courses at 100% | No warning triggered |

---

## 11. Data Flow

```
onResume()
    → AppDataStore.getAttendanceList()
    → buildSummaries(records)
    → adapter.updateSummaries(summaries)
    → adapter.updateRecords(records)
    → checkAttendanceWarnings()

Mark: dialog → validate → AttendanceRecord(id=UUID) → AppDataStore.addAttendanceRecord()
     → loadData() → checkAttendanceWarnings()

Delete: confirm → AppDataStore.deleteAttendanceRecord(id) → loadData()
```

---

## 12. Navigation

```kotlin
// From MainActivity
startActivity(Intent(this, AttendanceActivity::class.java))

companion object {
    const val EXTRA_HIGHLIGHT_ID = "extra_attendance_highlight_id"
}
```

---

## 13. UI States

| State | UI |
|---|---|
| No records | `textViewEmptyAttendance` VISIBLE |
| Summary mode, no courses | "No courses added. Go to GPA module to add courses." |
| Records mode, filter active, no results | "No records for [CourseName]" |
| Normal | RecyclerView VISIBLE |

---

## 14. Naming Conventions

| View | ID |
|---|---|
| Toolbar | `toolbarAttendance` |
| View mode RadioGroup | `radioGroupView` |
| Summary radio | `radioViewSummary` |
| Records radio | `radioViewRecords` |
| Course filter | `spinnerCourseFilter` |
| RecyclerView | `recyclerViewAttendance` |
| Empty text | `textViewEmptyAttendance` |
| FAB | `fabMarkAttendance` |
| Summary: course | `textViewSummaryCourse` |
| Summary: percentage | `textViewPercentage` |
| Summary: progress bar | `progressBarAttendance` |
| Summary: present | `textViewPresentCount` |
| Summary: absent | `textViewAbsentCount` |
| Summary: late | `textViewLateCount` |
| Record: status badge | `textViewStatusBadge` |
| Record: date | `textViewRecordDate` |
| Record: course | `textViewRecordCourse` |
| Record: notes | `textViewRecordNotes` |
| Record: delete | `imageButtonDeleteRecord` |
| Dialog: course | `spinnerAttendanceCourse` |
| Dialog: date button | `buttonPickAttendanceDate` |
| Dialog: date text | `textViewAttendanceDate` |
| Dialog: status group | `radioGroupStatus` |
| Dialog: present | `radioPresent` |
| Dialog: absent | `radioAbsent` |
| Dialog: late | `radioLate` |
| Dialog: notes | `editTextAttendanceNotes` |
