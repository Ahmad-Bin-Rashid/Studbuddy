# FILE: MODULE_EXAMS.md

# Module: Exams

**Owner:** Dev-D  
**Package:** `com.studbuddy.exams`  
**Activity:** `ExamsActivity`  
**Adapter:** `ExamsAdapter`  
**Layout (Activity):** `activity_exams.xml`  
**Layout (Item):** `item_exam.xml`  
**Layout (Dialog):** `dialog_add_exam.xml`

---

## 1. Purpose

The Exams module allows students to schedule and track upcoming exams. It shows a countdown to each exam, allows setting reminders, and visually indicates upcoming vs. past exams. Students can add, edit, and delete exam entries.

---

## 2. Data Model

```kotlin
data class Exam(
    val id: String,             // UUID
    val courseName: String,
    val examType: String,       // "Midterm" | "Final" | "Quiz" | "Lab" | "Assignment"
    val examDate: Long,         // epoch ms — includes time
    val venue: String,          // optional, e.g. "Hall B, Room 204"
    val durationMinutes: Int,   // e.g. 90, 120, 180
    val notes: String           // optional study notes
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("courseName", courseName)
        put("examType", examType)
        put("examDate", examDate)
        put("venue", venue)
        put("durationMinutes", durationMinutes)
        put("notes", notes)
    }

    companion object {
        fun fromJson(obj: JSONObject): Exam = Exam(
            id = obj.getString("id"),
            courseName = obj.getString("courseName"),
            examType = obj.getString("examType"),
            examDate = obj.getLong("examDate"),
            venue = obj.optString("venue", ""),
            durationMinutes = obj.optInt("durationMinutes", 90),
            notes = obj.optString("notes", "")
        )

        val EXAM_TYPES = listOf("Midterm", "Final", "Quiz", "Lab", "Assignment")
    }
}
```

---

## 3. UI Screens

### Screen 1: Exam List

- Toolbar: "Exams"
- Filter ToggleGroup: "Upcoming" (default) / "Past"
- RecyclerView of exam cards sorted by date ASC (upcoming) or DESC (past)
- FAB: opens Add dialog
- Empty state text

### Screen 2: Add/Edit Dialog

- Course (Spinner), Exam Type (Spinner), Date+Time (pickers), Venue (EditText), Duration (Spinner: 30 min increments), Notes (EditText)

---

## 4. XML Layout Structure

### `activity_exams.xml`

```
CoordinatorLayout (match_parent × match_parent)
│
├── AppBarLayout
│   └── Toolbar (id: toolbarExams)
│
├── LinearLayout (vertical, padding: 8dp)
│   ├── RadioGroup (id: radioGroupExamFilter, horizontal)
│   │   ├── RadioButton (id: radioUpcoming, text: "Upcoming", checked)
│   │   └── RadioButton (id: radioPast, text: "Past")
│   └── RelativeLayout (match_parent × 0dp, weight: 1)
│       ├── RecyclerView (id: recyclerViewExams)
│       └── TextView (id: textViewEmptyExams, center, visibility: gone)
│
└── FloatingActionButton (id: fabAddExam)
```

### `item_exam.xml`

```
CardView (match_parent × wrap_content, margin: 8dp)
└── LinearLayout (vertical, padding: 16dp)
    ├── LinearLayout (horizontal)
    │   ├── LinearLayout (vertical, weight: 1)
    │   │   ├── TextView (id: textViewExamCourse, 16sp, bold)
    │   │   └── TextView (id: textViewExamType, 13sp, colorTextSecondary)
    │   │       — e.g. "Final Exam"
    │   └── TextView (id: textViewCountdown, 14sp, bold)
    │       — "In 3 days" / "In 5 hours" / "TODAY" / "Past"
    │       — color: red if ≤1 day, orange if ≤3 days, green otherwise
    ├── View (height: 1dp, background: colorDivider, marginVertical: 8dp)
    ├── LinearLayout (horizontal)
    │   ├── ImageView (src: ic_calendar, 16dp)
    │   └── TextView (id: textViewExamDate, 13sp, marginStart: 4dp)
    │       — "Mon, Apr 15 2024 at 09:00"
    ├── LinearLayout (horizontal, visibility: gone if venue blank)
    │   ├── ImageView (src: ic_location, 16dp)
    │   └── TextView (id: textViewExamVenue, 13sp)
    ├── LinearLayout (horizontal)
    │   ├── ImageView (src: ic_timer, 16dp)
    │   └── TextView (id: textViewExamDuration, 13sp)
    │       — "90 minutes"
    ├── TextView (id: textViewExamNotes, 12sp, colorTextSecondary, maxLines: 2)
    │   — GONE if notes blank
    └── LinearLayout (horizontal, gravity: end)
        ├── ImageButton (id: imageButtonEditExam)
        └── ImageButton (id: imageButtonDeleteExam)
```

### `dialog_add_exam.xml`

```
ScrollView
└── LinearLayout (vertical, padding: 16dp)
    ├── TextView ("Course *")
    ├── Spinner (id: spinnerExamCourse)
    ├── TextView ("Exam Type *")
    ├── Spinner (id: spinnerExamType)
    ├── TextView ("Date *")
    ├── Button (id: buttonPickExamDate, text: "Select Date")
    ├── TextView (id: textViewExamDateDisplay)
    ├── TextView ("Time *")
    ├── Button (id: buttonPickExamTime, text: "Select Time")
    ├── TextView (id: textViewExamTimeDisplay)
    ├── TextView ("Duration")
    ├── Spinner (id: spinnerExamDuration)
    ├── TextView ("Venue (optional)")
    ├── EditText (id: editTextExamVenue, inputType: text)
    ├── TextView ("Notes (optional)")
    └── EditText (id: editTextExamNotes, inputType: textMultiLine, lines: 3)
```

---

## 5. RecyclerView Design

- **Layout Manager:** `LinearLayoutManager(this)`
- **Upcoming:** sorted by `examDate ASC` — soonest first
- **Past:** sorted by `examDate DESC` — most recent first
- **Filtering:** `examDate > now` for upcoming, `examDate <= now` for past

---

## 6. Adapter Responsibilities

```kotlin
class ExamsAdapter(
    private val items: MutableList<Exam>,
    private val onEditClick: (Exam) -> Unit,
    private val onDeleteClick: (Exam) -> Unit
) : RecyclerView.Adapter<ExamsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewExamCourse: TextView = view.findViewById(R.id.textViewExamCourse)
        val textViewExamType: TextView = view.findViewById(R.id.textViewExamType)
        val textViewCountdown: TextView = view.findViewById(R.id.textViewCountdown)
        val textViewExamDate: TextView = view.findViewById(R.id.textViewExamDate)
        val textViewExamVenue: TextView = view.findViewById(R.id.textViewExamVenue)
        val layoutVenue: LinearLayout = view.findViewById(R.id.layoutVenue)
        val textViewExamDuration: TextView = view.findViewById(R.id.textViewExamDuration)
        val textViewExamNotes: TextView = view.findViewById(R.id.textViewExamNotes)
        val imageButtonEdit: ImageButton = view.findViewById(R.id.imageButtonEditExam)
        val imageButtonDelete: ImageButton = view.findViewById(R.id.imageButtonDeleteExam)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exam = items[position]
        holder.textViewExamCourse.text = exam.courseName
        holder.textViewExamType.text = exam.examType

        // Countdown
        val now = System.currentTimeMillis()
        val diff = exam.examDate - now
        holder.textViewCountdown.text = formatCountdown(diff)
        holder.textViewCountdown.setTextColor(getCountdownColor(holder.itemView.context, diff))

        // Date
        val sdf = SimpleDateFormat("EEE, MMM dd yyyy 'at' HH:mm", Locale.getDefault())
        holder.textViewExamDate.text = sdf.format(Date(exam.examDate))

        // Venue
        holder.layoutVenue.visibility = if (exam.venue.isBlank()) View.GONE else View.VISIBLE
        holder.textViewExamVenue.text = exam.venue

        // Duration
        holder.textViewExamDuration.text = "${exam.durationMinutes} minutes"

        // Notes
        holder.textViewExamNotes.visibility =
            if (exam.notes.isBlank()) View.GONE else View.VISIBLE
        holder.textViewExamNotes.text = exam.notes

        holder.imageButtonEdit.setOnClickListener { onEditClick(exam) }
        holder.imageButtonDelete.setOnClickListener { onDeleteClick(exam) }
    }

    private fun formatCountdown(diffMs: Long): String {
        return when {
            diffMs < 0          -> "Past"
            diffMs < 3_600_000  -> "In ${diffMs / 60_000} min"
            diffMs < 86_400_000 -> {
                val hours = diffMs / 3_600_000
                "In $hours hour${if (hours == 1L) "" else "s"}"
            }
            else -> {
                val days = diffMs / 86_400_000
                if (days == 0L) "TODAY" else "In $days day${if (days == 1L) "" else "s"}"
            }
        }
    }

    private fun getCountdownColor(context: Context, diffMs: Long): Int {
        return when {
            diffMs < 0             -> ContextCompat.getColor(context, R.color.colorTextSecondary)
            diffMs < 86_400_000    -> ContextCompat.getColor(context, R.color.colorStatusCritical)
            diffMs < 259_200_000   -> ContextCompat.getColor(context, R.color.colorStatusWarning)
            else                   -> ContextCompat.getColor(context, R.color.colorStatusNormal)
        }
    }

    fun updateList(newItems: List<Exam>) {
        items.clear(); items.addAll(newItems); notifyDataSetChanged()
    }
}
```

---

## 7. User Interactions

| Interaction | Result |
|---|---|
| Tap FAB | `showAddDialog()` |
| Tap Edit | `showEditDialog(exam)` |
| Tap Delete | `showDeleteConfirmation(exam)` |
| Select Upcoming filter | Load upcoming exams sorted ASC |
| Select Past filter | Load past exams sorted DESC |

---

## 8. Dialog Designs

```kotlin
private fun showAddDialog() {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exam, null)
    var selectedDateTimeMs: Long = 0L

    val spinnerCourse = dialogView.findViewById<Spinner>(R.id.spinnerExamCourse)
    val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerExamType)
    val buttonPickDate = dialogView.findViewById<Button>(R.id.buttonPickExamDate)
    val textViewDate = dialogView.findViewById<TextView>(R.id.textViewExamDateDisplay)
    val buttonPickTime = dialogView.findViewById<Button>(R.id.buttonPickExamTime)
    val textViewTime = dialogView.findViewById<TextView>(R.id.textViewExamTimeDisplay)
    val spinnerDuration = dialogView.findViewById<Spinner>(R.id.spinnerExamDuration)
    val editTextVenue = dialogView.findViewById<EditText>(R.id.editTextExamVenue)
    val editTextNotes = dialogView.findViewById<EditText>(R.id.editTextExamNotes)

    // Course spinner
    spinnerCourse.adapter = ArrayAdapter(this,
        android.R.layout.simple_spinner_item, AppDataStore.getCourseNames())

    // Exam type spinner
    spinnerType.adapter = ArrayAdapter(this,
        android.R.layout.simple_spinner_item, Exam.EXAM_TYPES)

    // Duration spinner: 30, 60, 90, ... 240 minutes
    val durations = (1..8).map { "${it * 30} minutes" }
    spinnerDuration.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durations)
    spinnerDuration.setSelection(2) // default: 90 minutes

    // Date/time pickers — store date and time separately, combine on save
    var selectedYear = 0; var selectedMonth = 0; var selectedDay = 0
    var selectedHour = 9; var selectedMinute = 0

    buttonPickDate.setOnClickListener {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            selectedYear = y; selectedMonth = m; selectedDay = d
            textViewDate.text = String.format("%04d-%02d-%02d", y, m + 1, d)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
           cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    buttonPickTime.setOnClickListener {
        TimePickerDialog(this, { _, h, m ->
            selectedHour = h; selectedMinute = m
            textViewTime.text = String.format("%02d:%02d", h, m)
        }, selectedHour, selectedMinute, true).show()
    }

    AlertDialog.Builder(this)
        .setTitle("Add Exam")
        .setView(dialogView)
        .setPositiveButton("Save") { _, _ ->
            if (selectedYear == 0) {
                Toast.makeText(this, "Select a date", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            val cal = Calendar.getInstance()
            cal.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0)
            val exam = Exam(
                id = UUID.randomUUID().toString(),
                courseName = spinnerCourse.selectedItem?.toString() ?: "",
                examType = spinnerType.selectedItem.toString(),
                examDate = cal.timeInMillis,
                venue = editTextVenue.text.toString().trim(),
                durationMinutes = (spinnerDuration.selectedItemPosition + 1) * 30,
                notes = editTextNotes.text.toString().trim()
            )
            if (!validateExamInput(exam)) return@setPositiveButton
            saveExam(exam)
        }
        .setNegativeButton("Cancel", null)
        .show()
}
```

---

## 9. Business Logic

```kotlin
private fun validateExamInput(exam: Exam): Boolean {
    if (exam.courseName.isBlank()) {
        Toast.makeText(this, "Select a course", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

private fun saveExam(exam: Exam) {
    AppDataStore.addExam(exam)
    NotificationScheduler.scheduleExamNotification(this, exam)
    loadData()
    Toast.makeText(this, "Exam added", Toast.LENGTH_SHORT).show()
}

private fun loadData() {
    val all = AppDataStore.getExamList()
    val now = System.currentTimeMillis()
    val filtered = if (showingUpcoming) {
        all.filter { it.examDate > now }.sortedBy { it.examDate }
    } else {
        all.filter { it.examDate <= now }.sortedByDescending { it.examDate }
    }
    examList.clear()
    examList.addAll(filtered)
    examsAdapter.updateList(examList)
    updateEmptyState()
}
```

---

## 10. Edge Cases

| Case | Handling |
|---|---|
| No courses added | Block save, show Toast "Add courses in GPA module first" |
| Exam date in past (on add) | Warn "This exam date is in the past", allow save |
| Course name blank | Block save |
| Upcoming filter, no exams | "No upcoming exams 🎉" |
| Past filter, no exams | "No past exams recorded" |
| Exam date = today | Countdown shows "TODAY" in red |
| Duration = 0 | Spinner starts at 30 min — cannot select 0 |

---

## 11. Data Flow

```
onResume()
    → AppDataStore.getExamList()
    → filter by upcoming/past
    → sort
    → examsAdapter.updateList()

Add: dialog → validate → Exam(id=UUID) → AppDataStore.addExam()
     → NotificationScheduler.scheduleExamNotification()
     → loadData()

Edit: dialog pre-filled → validate → exam.copy() → AppDataStore.updateExam()
     → reschedule notification → loadData()

Delete: confirm → AppDataStore.deleteExam(id)
     → NotificationScheduler.cancelExamNotification(id) → loadData()
```

---

## 12. Navigation

```kotlin
startActivity(Intent(this, ExamsActivity::class.java))

companion object {
    const val EXTRA_HIGHLIGHT_ID = "extra_exam_highlight_id"
}
```

---

## 13. UI States

| State | UI |
|---|---|
| Upcoming, empty | "No upcoming exams. Enjoy your free time! 🎉" |
| Past, empty | "No past exams on record." |
| Non-empty | RecyclerView VISIBLE |

---

## 14. Naming Conventions

| View | ID |
|---|---|
| Toolbar | `toolbarExams` |
| Filter group | `radioGroupExamFilter` |
| Upcoming radio | `radioUpcoming` |
| Past radio | `radioPast` |
| RecyclerView | `recyclerViewExams` |
| Empty text | `textViewEmptyExams` |
| FAB | `fabAddExam` |
| Item: course | `textViewExamCourse` |
| Item: type | `textViewExamType` |
| Item: countdown | `textViewCountdown` |
| Item: date | `textViewExamDate` |
| Item: venue layout | `layoutVenue` |
| Item: venue text | `textViewExamVenue` |
| Item: duration | `textViewExamDuration` |
| Item: notes | `textViewExamNotes` |
| Item: edit | `imageButtonEditExam` |
| Item: delete | `imageButtonDeleteExam` |
| Dialog: course | `spinnerExamCourse` |
| Dialog: type | `spinnerExamType` |
| Dialog: date button | `buttonPickExamDate` |
| Dialog: date text | `textViewExamDateDisplay` |
| Dialog: time button | `buttonPickExamTime` |
| Dialog: time text | `textViewExamTimeDisplay` |
| Dialog: duration | `spinnerExamDuration` |
| Dialog: venue | `editTextExamVenue` |
| Dialog: notes | `editTextExamNotes` |
