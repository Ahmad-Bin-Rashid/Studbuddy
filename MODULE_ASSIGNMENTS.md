# FILE: MODULE_ASSIGNMENTS.md

# Module: Assignments

**Owner:** Dev-B  
**Package:** `com.studbuddy.assignments`  
**Activity:** `AssignmentsActivity`  
**Adapter:** `AssignmentsAdapter`  
**Layout (Activity):** `activity_assignments.xml`  
**Layout (Item):** `item_assignment.xml`  
**Layout (Dialog):** `dialog_add_assignment.xml`

---

## 1. Purpose

The Assignments module lets students track academic assignments with due dates, priorities, and completion status. It provides visual indicators for overdue and high-priority items, and triggers deadline notifications.

---

## 2. Data Model

```kotlin
enum class Priority { LOW, MEDIUM, HIGH }

data class Assignment(
    val id: String,             // UUID
    val title: String,          // e.g. "Lab Report #3"
    val courseName: String,     // e.g. "CS101"
    val dueDate: Long,          // epoch milliseconds
    val priority: Priority,
    val description: String,    // optional, may be empty
    val isCompleted: Boolean
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("courseName", courseName)
        put("dueDate", dueDate)
        put("priority", priority.name)
        put("description", description)
        put("isCompleted", isCompleted)
    }

    companion object {
        fun fromJson(obj: JSONObject): Assignment = Assignment(
            id = obj.getString("id"),
            title = obj.getString("title"),
            courseName = obj.getString("courseName"),
            dueDate = obj.getLong("dueDate"),
            priority = Priority.valueOf(obj.getString("priority")),
            description = obj.optString("description", ""),
            isCompleted = obj.getBoolean("isCompleted")
        )
    }
}
```

---

## 3. UI Screens

### Screen 1: Assignment List

- Toolbar: "Assignments"
- Filter row: RadioGroup with "All", "Pending", "Completed"
- Sort Spinner: "Due Date", "Priority", "Course"
- RecyclerView of assignments
- FAB: opens Add dialog
- Empty state TextView

### Screen 2: Add/Edit Assignment Dialog (AlertDialog)

- Fields: Title, Course (Spinner from `AppDataStore.getCourseNames()`), Due Date (date picker trigger), Priority RadioGroup, Description (optional)

---

## 4. XML Layout Structure

### `activity_assignments.xml`

```
CoordinatorLayout (match_parent × match_parent)
│
├── AppBarLayout
│   └── Toolbar (id: toolbarAssignments)
│
├── LinearLayout (vertical, padding: 8dp)
│   ├── RadioGroup (id: radioGroupFilter, horizontal)
│   │   ├── RadioButton (id: radioAll, text: "All")
│   │   ├── RadioButton (id: radioPending, text: "Pending")
│   │   └── RadioButton (id: radioCompleted, text: "Completed")
│   ├── Spinner (id: spinnerSort)
│   └── RelativeLayout (match_parent × 0dp, weight: 1)
│       ├── RecyclerView (id: recyclerViewAssignments)
│       └── TextView (id: textViewEmptyAssignments, center, visibility: gone)
│
└── FloatingActionButton (id: fabAddAssignment)
```

### `item_assignment.xml`

```
CardView (match_parent × wrap_content, margin: 8dp)
└── LinearLayout (horizontal, padding: 12dp)
    ├── CheckBox (id: checkBoxCompleted, width: wrap_content)
    └── LinearLayout (vertical, weight: 1, marginStart: 8dp)
        ├── TextView (id: textViewAssignmentTitle, 16sp, bold)
        ├── TextView (id: textViewAssignmentCourse, 13sp, colorTextSecondary)
        ├── LinearLayout (horizontal)
        │   ├── TextView (id: textViewDueDate, 12sp)
        │   │   — color: colorStatusCritical if overdue, else colorTextSecondary
        │   └── TextView (id: textViewPriorityBadge, 11sp, background: priority color)
        │       — "HIGH" / "MEDIUM" / "LOW"
        └── TextView (id: textViewAssignmentDescription, 12sp, maxLines: 2)
            — GONE if description is empty
    └── ImageButton (id: imageButtonDeleteAssignment)
```

### `dialog_add_assignment.xml`

```
ScrollView
└── LinearLayout (vertical, padding: 16dp)
    ├── TextView ("Title *")
    ├── EditText (id: editTextAssignmentTitle, inputType: text)
    ├── TextView ("Course *")
    ├── Spinner (id: spinnerAssignmentCourse)
    ├── TextView ("Due Date *")
    ├── Button (id: buttonPickDate, text: "Select Date")
    ├── TextView (id: textViewSelectedDate, 14sp) — shows chosen date
    ├── TextView ("Priority")
    ├── RadioGroup (id: radioGroupPriority, horizontal)
    │   ├── RadioButton (id: radioLow, text: "Low")
    │   ├── RadioButton (id: radioMedium, text: "Medium", checked by default)
    │   └── RadioButton (id: radioHigh, text: "High")
    ├── TextView ("Description (optional)")
    └── EditText (id: editTextDescription, inputType: textMultiLine, lines: 3)
```

---

## 5. RecyclerView Design

- **Layout Manager:** `LinearLayoutManager(this)`
- **Sorting:** Applied in Activity before passing to adapter
  - By Due Date: `sortedBy { it.dueDate }`
  - By Priority: `sortedByDescending { it.priority.ordinal }`
  - By Course: `sortedBy { it.courseName }`
- **Filtering:** Applied in Activity by completion status
- Card background changes when `isCompleted = true`: alpha 0.5, strikethrough on title

---

## 6. Adapter Responsibilities

```kotlin
class AssignmentsAdapter(
    private val items: MutableList<Assignment>,
    private val onCheckChanged: (Assignment, Boolean) -> Unit,
    private val onDeleteClick: (Assignment) -> Unit,
    private val onItemClick: (Assignment) -> Unit   // opens edit dialog
) : RecyclerView.Adapter<AssignmentsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBoxCompleted: CheckBox = view.findViewById(R.id.checkBoxCompleted)
        val textViewAssignmentTitle: TextView = view.findViewById(R.id.textViewAssignmentTitle)
        val textViewAssignmentCourse: TextView = view.findViewById(R.id.textViewAssignmentCourse)
        val textViewDueDate: TextView = view.findViewById(R.id.textViewDueDate)
        val textViewPriorityBadge: TextView = view.findViewById(R.id.textViewPriorityBadge)
        val textViewDescription: TextView = view.findViewById(R.id.textViewAssignmentDescription)
        val imageButtonDelete: ImageButton = view.findViewById(R.id.imageButtonDeleteAssignment)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val assignment = items[position]

        holder.textViewAssignmentTitle.text = assignment.title
        holder.textViewAssignmentCourse.text = assignment.courseName

        // Due date display + color
        val now = System.currentTimeMillis()
        val isOverdue = !assignment.isCompleted && assignment.dueDate < now
        holder.textViewDueDate.text = formatDate(assignment.dueDate)
        holder.textViewDueDate.setTextColor(
            if (isOverdue) ContextCompat.getColor(holder.itemView.context, R.color.colorStatusCritical)
            else ContextCompat.getColor(holder.itemView.context, R.color.colorTextSecondary)
        )

        // Priority badge
        holder.textViewPriorityBadge.text = assignment.priority.name
        val badgeColor = when (assignment.priority) {
            Priority.HIGH   -> R.color.colorStatusCritical
            Priority.MEDIUM -> R.color.colorStatusWarning
            Priority.LOW    -> R.color.colorStatusNormal
        }
        holder.textViewPriorityBadge.setBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, badgeColor))

        // Completed visual
        holder.itemView.alpha = if (assignment.isCompleted) 0.5f else 1.0f
        holder.textViewAssignmentTitle.paintFlags = if (assignment.isCompleted)
            holder.textViewAssignmentTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else
            holder.textViewAssignmentTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        // Description
        holder.textViewDescription.visibility =
            if (assignment.description.isBlank()) View.GONE else View.VISIBLE
        holder.textViewDescription.text = assignment.description

        // Listeners (suppress CheckBox change during binding)
        holder.checkBoxCompleted.setOnCheckedChangeListener(null)
        holder.checkBoxCompleted.isChecked = assignment.isCompleted
        holder.checkBoxCompleted.setOnCheckedChangeListener { _, checked ->
            onCheckChanged(assignment, checked)
        }
        holder.imageButtonDelete.setOnClickListener { onDeleteClick(assignment) }
        holder.itemView.setOnClickListener { onItemClick(assignment) }
    }

    // Helper — format epoch ms to "MMM dd, yyyy"
    private fun formatDate(epochMs: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(epochMs))
    }

    fun updateList(newItems: List<Assignment>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
```

---

## 7. User Interactions

| Interaction | Result |
|---|---|
| Tap FAB | `showAddDialog()` |
| Tap item | `showEditDialog(assignment)` |
| Tap CheckBox | `onCheckChanged` → `AppDataStore.markAssignmentComplete(id, checked)` |
| Tap Delete | `showDeleteConfirmation(assignment)` |
| Select filter (RadioGroup) | Re-filter and reload adapter |
| Select sort (Spinner) | Re-sort and reload adapter |
| Tap "Select Date" in dialog | Show `DatePickerDialog` |

---

## 8. Dialog Designs

### Date Picker

```kotlin
private fun showDatePicker(onDateSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(this, { _, year, month, day ->
        val cal = Calendar.getInstance()
        cal.set(year, month, day, 23, 59, 0)
        onDateSelected(cal.timeInMillis)
    }, calendar.get(Calendar.YEAR),
       calendar.get(Calendar.MONTH),
       calendar.get(Calendar.DAY_OF_MONTH)).show()
}
```

### Add Assignment Dialog

```kotlin
private fun showAddDialog() {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_assignment, null)
    var selectedDueDate: Long = 0L

    val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextAssignmentTitle)
    val spinnerCourse = dialogView.findViewById<Spinner>(R.id.spinnerAssignmentCourse)
    val buttonPickDate = dialogView.findViewById<Button>(R.id.buttonPickDate)
    val textViewSelectedDate = dialogView.findViewById<TextView>(R.id.textViewSelectedDate)
    val radioGroupPriority = dialogView.findViewById<RadioGroup>(R.id.radioGroupPriority)
    val editTextDescription = dialogView.findViewById<EditText>(R.id.editTextDescription)

    // Populate course Spinner
    val courseNames = AppDataStore.getCourseNames()
    val courseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courseNames)
    spinnerCourse.adapter = courseAdapter

    buttonPickDate.setOnClickListener {
        showDatePicker { date ->
            selectedDueDate = date
            textViewSelectedDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(date))
        }
    }

    val dialog = AlertDialog.Builder(this)
        .setTitle("Add Assignment")
        .setView(dialogView)
        .setPositiveButton("Save", null) // override to prevent auto-dismiss
        .setNegativeButton("Cancel", null)
        .create()

    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val title = editTextTitle.text.toString().trim()
            val course = spinnerCourse.selectedItem?.toString() ?: ""
            val priority = when (radioGroupPriority.checkedRadioButtonId) {
                R.id.radioHigh -> Priority.HIGH
                R.id.radioLow  -> Priority.LOW
                else           -> Priority.MEDIUM
            }
            val description = editTextDescription.text.toString().trim()

            if (!validateAssignmentInput(title, course, selectedDueDate)) return@setOnClickListener

            val assignment = Assignment(
                id = UUID.randomUUID().toString(),
                title = title,
                courseName = course,
                dueDate = selectedDueDate,
                priority = priority,
                description = description,
                isCompleted = false
            )
            saveAssignment(assignment)
            dialog.dismiss()
        }
    }

    dialog.show()
}
```

---

## 9. Business Logic

```kotlin
// Validation
private fun validateAssignmentInput(title: String, course: String, dueDate: Long): Boolean {
    if (title.isBlank()) {
        Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
        return false
    }
    if (course.isBlank()) {
        Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show()
        return false
    }
    if (dueDate == 0L) {
        Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

// Save
private fun saveAssignment(assignment: Assignment) {
    AppDataStore.addAssignment(assignment)
    NotificationScheduler.scheduleAssignmentNotification(this, assignment)
    loadData()
    Toast.makeText(this, "Assignment added", Toast.LENGTH_SHORT).show()
}

// Mark complete
private fun onAssignmentCheckChanged(assignment: Assignment, isComplete: Boolean) {
    AppDataStore.markAssignmentComplete(assignment.id, isComplete)
    if (isComplete) {
        NotificationScheduler.cancelAssignmentNotification(this, assignment.id)
    } else {
        if (assignment.dueDate > System.currentTimeMillis()) {
            NotificationScheduler.scheduleAssignmentNotification(this, assignment.copy(isCompleted = false))
        }
    }
    loadData()
}

// Load and apply filter + sort
private fun loadData() {
    val all = AppDataStore.getAssignmentList()
    val filtered = when (currentFilter) {
        Filter.PENDING   -> all.filter { !it.isCompleted }
        Filter.COMPLETED -> all.filter { it.isCompleted }
        else             -> all
    }
    val sorted = when (currentSort) {
        Sort.DUE_DATE -> filtered.sortedBy { it.dueDate }
        Sort.PRIORITY -> filtered.sortedByDescending { it.priority.ordinal }
        Sort.COURSE   -> filtered.sortedBy { it.courseName }
    }
    assignmentList.clear()
    assignmentList.addAll(sorted)
    assignmentsAdapter.updateList(assignmentList)
    updateEmptyState()
}

enum class Filter { ALL, PENDING, COMPLETED }
enum class Sort { DUE_DATE, PRIORITY, COURSE }
```

---

## 10. Edge Cases

| Case | Handling |
|---|---|
| No courses in AppDataStore | Spinner shows "No courses added" — Save blocked with Toast "Add a course in GPA module first" |
| Due date in past (on add) | Warn with Toast "Due date is in the past" — allow save |
| Title > 200 chars | `maxLength="200"` on EditText |
| Description > 500 chars | `maxLength="500"` on EditText |
| All assignments completed | Show "All done! 🎉" in empty state TextView |
| Filter = Pending, 0 results | Show "No pending assignments" |

---

## 11. Data Flow

```
onResume() → AppDataStore.getAssignmentList() → filter + sort → adapter.updateList()

Add: dialog → validate → Assignment(id=UUID) → AppDataStore.addAssignment()
     → NotificationScheduler.scheduleAssignmentNotification()
     → loadData()

Edit: dialog pre-filled → validate → assignment.copy(...) → AppDataStore.updateAssignment()
     → reschedule notification → loadData()

Toggle complete: CheckBox → AppDataStore.markAssignmentComplete(id, checked)
     → cancel/reschedule notification → loadData()

Delete: confirm → AppDataStore.deleteAssignment(id)
     → NotificationScheduler.cancelAssignmentNotification(id) → loadData()
```

---

## 12. Navigation

```kotlin
// From MainActivity
startActivity(Intent(this, AssignmentsActivity::class.java))

// Deep link from notification
companion object {
    const val EXTRA_HIGHLIGHT_ID = "extra_assignment_highlight_id"
}
```

---

## 13. UI States

| State | UI |
|---|---|
| Empty (no assignments) | `textViewEmptyAssignments` VISIBLE |
| Empty (filter active) | "No [Pending/Completed] assignments" in empty TextView |
| All completed | "All assignments completed! 🎉" |
| Normal list | RecyclerView VISIBLE |

---

## 14. Naming Conventions

| View | ID |
|---|---|
| Toolbar | `toolbarAssignments` |
| Filter RadioGroup | `radioGroupFilter` |
| All radio | `radioAll` |
| Pending radio | `radioPending` |
| Completed radio | `radioCompleted` |
| Sort Spinner | `spinnerSort` |
| RecyclerView | `recyclerViewAssignments` |
| Empty text | `textViewEmptyAssignments` |
| FAB | `fabAddAssignment` |
| Item: checkbox | `checkBoxCompleted` |
| Item: title | `textViewAssignmentTitle` |
| Item: course | `textViewAssignmentCourse` |
| Item: due date | `textViewDueDate` |
| Item: priority badge | `textViewPriorityBadge` |
| Item: description | `textViewAssignmentDescription` |
| Item: delete | `imageButtonDeleteAssignment` |
| Dialog: title | `editTextAssignmentTitle` |
| Dialog: course | `spinnerAssignmentCourse` |
| Dialog: date button | `buttonPickDate` |
| Dialog: date display | `textViewSelectedDate` |
| Dialog: priority group | `radioGroupPriority` |
| Dialog: description | `editTextDescription` |
