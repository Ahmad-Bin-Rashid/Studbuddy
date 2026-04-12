# FILE: MODULE_GPA.md

# Module: GPA Calculator

**Owner:** Dev-E  
**Package:** `com.studbuddy.gpa`  
**Activity:** `GpaActivity`  
**Adapter:** `GpaAdapter`  
**Layout (Activity):** `activity_gpa.xml`  
**Layout (Item):** `item_gpa.xml`  
**Layout (Dialog):** `dialog_add_course.xml`

---

## 1. Purpose

The GPA module manages the student's course list and calculates cumulative GPA. Each course has a name, credit hours, and grade. The module computes GPA using the standard 4.0 scale. Course names entered here are shared with other modules (Timetable, Assignments, Attendance, Exams) via `AppDataStore.getCourseNames()`.

---

## 2. Data Model

```kotlin
data class Course(
    val id: String,            // UUID
    val name: String,          // e.g. "CS101 — Data Structures"
    val creditHours: Int,      // 1–6
    val grade: String,         // "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F"
    val gradePoints: Double,   // derived from grade — never set directly by UI
    val semester: String       // e.g. "Fall 2024"
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("creditHours", creditHours)
        put("grade", grade)
        put("gradePoints", gradePoints)
        put("semester", semester)
    }

    companion object {
        val GRADE_POINTS_MAP = mapOf(
            "A"  to 4.0,
            "A-" to 3.7,
            "B+" to 3.3,
            "B"  to 3.0,
            "B-" to 2.7,
            "C+" to 2.3,
            "C"  to 2.0,
            "C-" to 1.7,
            "D+" to 1.3,
            "D"  to 1.0,
            "F"  to 0.0
        )

        val GRADE_OPTIONS: List<String> = GRADE_POINTS_MAP.keys.toList()

        val SEMESTER_OPTIONS = listOf(
            "Fall 2023", "Spring 2024", "Summer 2024",
            "Fall 2024", "Spring 2025", "Summer 2025",
            "Fall 2025", "Spring 2026"
        )

        fun gradeToPoints(grade: String): Double =
            GRADE_POINTS_MAP[grade] ?: 0.0

        fun fromJson(obj: JSONObject): Course = Course(
            id = obj.getString("id"),
            name = obj.getString("name"),
            creditHours = obj.getInt("creditHours"),
            grade = obj.getString("grade"),
            gradePoints = obj.getDouble("gradePoints"),
            semester = obj.optString("semester", "")
        )
    }
}
```

---

## 3. UI Screens

### Screen 1: GPA Dashboard

- Toolbar: "GPA Calculator"
- Summary card at top: Current GPA (large text), grade letter, total credit hours
- Semester filter Spinner: "All Semesters" + individual semesters from data
- RecyclerView of courses
- FAB: opens Add Course dialog
- Empty state text

---

## 4. XML Layout Structure

### `activity_gpa.xml`

```
CoordinatorLayout (match_parent × match_parent)
│
├── AppBarLayout
│   └── Toolbar (id: toolbarGpa)
│
├── LinearLayout (vertical)
│   │
│   ├── CardView (match_parent × wrap_content, margin: 12dp, elevation: 6dp)
│   │   └── LinearLayout (vertical, padding: 20dp, gravity: center)
│   │       ├── TextView (id: textViewGpaLabel, "Cumulative GPA", 14sp, colorTextSecondary)
│   │       ├── TextView (id: textViewGpaValue, "0.00", 48sp, bold, colorPrimary)
│   │       ├── TextView (id: textViewGpaLetterGrade, "N/A", 20sp, colorAccent)
│   │       └── TextView (id: textViewTotalCredits, "0 credit hours", 12sp, colorTextSecondary)
│   │
│   ├── Spinner (id: spinnerSemesterFilter, margin: 8dp)
│   │
│   └── RelativeLayout (match_parent × 0dp, weight: 1)
│       ├── RecyclerView (id: recyclerViewGpa)
│       └── TextView (id: textViewEmptyGpa, center, visibility: gone)
│           text: "No courses added. Tap + to get started."
│
└── FloatingActionButton (id: fabAddCourse)
```

### `item_gpa.xml`

```
CardView (match_parent × wrap_content, margin: 8dp)
└── LinearLayout (horizontal, padding: 12dp)
    ├── LinearLayout (vertical, weight: 1)
    │   ├── TextView (id: textViewCourseName, 15sp, bold)
    │   ├── TextView (id: textViewCourseSemester, 12sp, colorTextSecondary)
    │   └── TextView (id: textViewCreditHours, 12sp, colorTextSecondary)
    │       — "3 credit hours"
    └── LinearLayout (vertical, gravity: center, minWidth: 60dp)
        ├── TextView (id: textViewGradeLabel, 22sp, bold, colorPrimary)
        │   — "A", "B+", etc.
        └── TextView (id: textViewGradePoints, 12sp, colorTextSecondary)
            — "4.0 pts"
    └── LinearLayout (vertical, gravity: center_vertical)
        ├── ImageButton (id: imageButtonEditCourse)
        └── ImageButton (id: imageButtonDeleteCourse)
```

### `dialog_add_course.xml`

```
ScrollView
└── LinearLayout (vertical, padding: 16dp)
    ├── TextView ("Course Name *")
    ├── EditText (id: editTextCourseName, inputType: text)
    ├── TextView ("Semester")
    ├── Spinner (id: spinnerCourseSemester)
    ├── TextView ("Credit Hours *")
    ├── Spinner (id: spinnerCreditHours)
    │   options: "1", "2", "3", "4", "5", "6"
    ├── TextView ("Grade *")
    └── Spinner (id: spinnerGrade)
        options: "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F"
```

---

## 5. RecyclerView Design

- **Layout Manager:** `LinearLayoutManager(this)`
- **Sorted by:** `semester DESC` then `name ASC`
- **Filtered by:** selected semester from Spinner ("All Semesters" = no filter)
- **No swipe-to-delete** — delete via ImageButton only (prevents accidental deletion)

---

## 6. Adapter Responsibilities

```kotlin
class GpaAdapter(
    private val items: MutableList<Course>,
    private val onEditClick: (Course) -> Unit,
    private val onDeleteClick: (Course) -> Unit
) : RecyclerView.Adapter<GpaAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewCourseName: TextView = view.findViewById(R.id.textViewCourseName)
        val textViewCourseSemester: TextView = view.findViewById(R.id.textViewCourseSemester)
        val textViewCreditHours: TextView = view.findViewById(R.id.textViewCreditHours)
        val textViewGradeLabel: TextView = view.findViewById(R.id.textViewGradeLabel)
        val textViewGradePoints: TextView = view.findViewById(R.id.textViewGradePoints)
        val imageButtonEdit: ImageButton = view.findViewById(R.id.imageButtonEditCourse)
        val imageButtonDelete: ImageButton = view.findViewById(R.id.imageButtonDeleteCourse)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = items[position]
        holder.textViewCourseName.text = course.name
        holder.textViewCourseSemester.text = course.semester
        holder.textViewCreditHours.text = "${course.creditHours} credit hour${if (course.creditHours == 1) "" else "s"}"
        holder.textViewGradeLabel.text = course.grade
        holder.textViewGradePoints.text = String.format("%.1f pts", course.gradePoints)
        holder.imageButtonEdit.setOnClickListener { onEditClick(course) }
        holder.imageButtonDelete.setOnClickListener { onDeleteClick(course) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<Course>) {
        items.clear(); items.addAll(newItems); notifyDataSetChanged()
    }
}
```

---

## 7. User Interactions

| Interaction | Result |
|---|---|
| Tap FAB | `showAddCourseDialog()` |
| Tap Edit | `showEditCourseDialog(course)` |
| Tap Delete | `showDeleteConfirmation(course)` |
| Select semester in Spinner | Filter course list, recalculate GPA display |

---

## 8. Dialog Designs

```kotlin
private fun showAddCourseDialog() {
    val dialogView = LayoutInflater.from(this)
        .inflate(R.layout.dialog_add_course, null)

    val editTextName = dialogView.findViewById<EditText>(R.id.editTextCourseName)
    val spinnerSemester = dialogView.findViewById<Spinner>(R.id.spinnerCourseSemester)
    val spinnerCredits = dialogView.findViewById<Spinner>(R.id.spinnerCreditHours)
    val spinnerGrade = dialogView.findViewById<Spinner>(R.id.spinnerGrade)

    spinnerSemester.adapter = ArrayAdapter(this,
        android.R.layout.simple_spinner_item, Course.SEMESTER_OPTIONS)
    spinnerCredits.adapter = ArrayAdapter(this,
        android.R.layout.simple_spinner_item, (1..6).map { it.toString() })
    spinnerGrade.adapter = ArrayAdapter(this,
        android.R.layout.simple_spinner_item, Course.GRADE_OPTIONS)

    // Default credit hours = 3 (index 2)
    spinnerCredits.setSelection(2)

    AlertDialog.Builder(this)
        .setTitle("Add Course")
        .setView(dialogView)
        .setPositiveButton("Save") { _, _ ->
            val name = editTextName.text.toString().trim()
            val semester = spinnerSemester.selectedItem.toString()
            val credits = spinnerCredits.selectedItemPosition + 1
            val grade = spinnerGrade.selectedItem.toString()

            if (!validateCourseInput(name)) return@setPositiveButton

            val course = Course(
                id = UUID.randomUUID().toString(),
                name = name,
                creditHours = credits,
                grade = grade,
                gradePoints = Course.gradeToPoints(grade),
                semester = semester
            )
            saveCourse(course)
        }
        .setNegativeButton("Cancel", null)
        .show()
}

private fun showEditCourseDialog(course: Course) {
    val dialogView = LayoutInflater.from(this)
        .inflate(R.layout.dialog_add_course, null)

    val editTextName = dialogView.findViewById<EditText>(R.id.editTextCourseName)
    val spinnerSemester = dialogView.findViewById<Spinner>(R.id.spinnerCourseSemester)
    val spinnerCredits = dialogView.findViewById<Spinner>(R.id.spinnerCreditHours)
    val spinnerGrade = dialogView.findViewById<Spinner>(R.id.spinnerGrade)

    // Pre-fill
    editTextName.setText(course.name)
    spinnerSemester.adapter = ArrayAdapter(this,
        android.R.layout.simple_spinner_item, Course.SEMESTER_OPTIONS)
    spinnerSemester.setSelection(Course.SEMESTER_OPTIONS.indexOf(course.semester).coerceAtLeast(0))
    spinnerCredits.adapter = ArrayAdapter(this,
        android.R.layout.simple_spinner_item, (1..6).map { it.toString() })
    spinnerCredits.setSelection(course.creditHours - 1)
    spinnerGrade.adapter = ArrayAdapter(this,
        android.R.layout.simple_spinner_item, Course.GRADE_OPTIONS)
    spinnerGrade.setSelection(Course.GRADE_OPTIONS.indexOf(course.grade).coerceAtLeast(0))

    AlertDialog.Builder(this)
        .setTitle("Edit Course")
        .setView(dialogView)
        .setPositiveButton("Save") { _, _ ->
            val updated = course.copy(
                name = editTextName.text.toString().trim(),
                semester = spinnerSemester.selectedItem.toString(),
                creditHours = spinnerCredits.selectedItemPosition + 1,
                grade = spinnerGrade.selectedItem.toString(),
                gradePoints = Course.gradeToPoints(spinnerGrade.selectedItem.toString())
            )
            if (!validateCourseInput(updated.name)) return@setPositiveButton
            updateCourse(updated)
        }
        .setNegativeButton("Cancel", null)
        .show()
}
```

---

## 9. Business Logic

```kotlin
// GPA Calculation — weighted average
// Formula: GPA = Σ(gradePoints × creditHours) / Σ(creditHours)
private fun calculateGpa(courses: List<Course>): Double {
    if (courses.isEmpty()) return 0.0
    val totalPoints = courses.sumOf { it.gradePoints * it.creditHours }
    val totalCredits = courses.sumOf { it.creditHours }
    return if (totalCredits == 0) 0.0 else totalPoints / totalCredits
}

// Convert numeric GPA to letter grade
private fun gpaToLetterGrade(gpa: Double): String = when {
    gpa >= 3.7  -> "A"
    gpa >= 3.3  -> "A-/B+"
    gpa >= 3.0  -> "B"
    gpa >= 2.7  -> "B-"
    gpa >= 2.3  -> "B-/C+"
    gpa >= 2.0  -> "C"
    gpa >= 1.0  -> "D"
    else        -> "F"
}

// Update summary card
private fun updateGpaSummaryCard(courses: List<Course>) {
    val gpa = calculateGpa(courses)
    textViewGpaValue.text = String.format("%.2f", gpa)
    textViewGpaLetterGrade.text = gpaToLetterGrade(gpa)
    val totalCredits = courses.sumOf { it.creditHours }
    textViewTotalCredits.text = "$totalCredits credit hour${if (totalCredits == 1) "" else "s"}"
}

// Validation
private fun validateCourseInput(name: String): Boolean {
    if (name.isBlank()) {
        Toast.makeText(this, "Course name cannot be empty", Toast.LENGTH_SHORT).show()
        return false
    }
    if (name.length > 100) {
        Toast.makeText(this, "Course name too long (max 100 chars)", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

// Load data
private fun loadData() {
    val all = AppDataStore.getCourseList()
    val filtered = when (currentSemesterFilter) {
        "All Semesters" -> all
        else -> all.filter { it.semester == currentSemesterFilter }
    }
    val sorted = filtered.sortedWith(compareByDescending<Course> {
        Course.SEMESTER_OPTIONS.indexOf(it.semester)
    }.thenBy { it.name })

    courseList.clear()
    courseList.addAll(sorted)
    gpaAdapter.updateList(courseList)
    updateGpaSummaryCard(filtered)
    updateEmptyState()
    updateSemesterSpinner(all)
}

// Build semester filter options from actual data
private fun updateSemesterSpinner(all: List<Course>) {
    val semesters = mutableListOf("All Semesters")
    semesters.addAll(all.map { it.semester }.distinct()
        .sortedByDescending { Course.SEMESTER_OPTIONS.indexOf(it) })
    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, semesters)
    spinnerSemesterFilter.adapter = adapter
}

private fun saveCourse(course: Course) {
    AppDataStore.addCourse(course)
    loadData()
    Toast.makeText(this, "Course added", Toast.LENGTH_SHORT).show()
}

private fun updateCourse(course: Course) {
    AppDataStore.updateCourse(course)
    loadData()
    Toast.makeText(this, "Course updated", Toast.LENGTH_SHORT).show()
}

private fun deleteCourse(id: String) {
    AppDataStore.deleteCourse(id)
    loadData()
    Toast.makeText(this, "Course removed", Toast.LENGTH_SHORT).show()
}
```

---

## 10. Edge Cases

| Case | Handling |
|---|---|
| No courses | GPA shows "0.00", letter grade shows "N/A" |
| All F grades | GPA = 0.00 — valid calculation |
| Single course, 1 credit | GPA = gradePoints of that course |
| Duplicate course name | Allowed — same course can appear in different semesters |
| Course name > 100 chars | Show Toast, block save |
| All credits sum to 0 | Return GPA = 0.00 |
| Semester filter with no courses | Show empty state "No courses for this semester" |

---

## 11. Data Flow

```
onResume()
    → AppDataStore.getCourseList()
    → filter by semester
    → sort by semester desc, name asc
    → gpaAdapter.updateList()
    → updateGpaSummaryCard()
    → updateSemesterSpinner()

Add: dialog → validate → Course(id=UUID, gradePoints=GRADE_POINTS_MAP[grade])
     → AppDataStore.addCourse() → loadData()

Edit: dialog pre-filled → validate → course.copy(gradePoints=recalculated)
     → AppDataStore.updateCourse() → loadData()

Delete: confirm → AppDataStore.deleteCourse(id) → loadData()
```

---

## 12. Navigation

```kotlin
startActivity(Intent(this, GpaActivity::class.java))

// No EXTRA_HIGHLIGHT_ID needed — GPA module has no notification-driven deep links
```

---

## 13. UI States

| State | UI |
|---|---|
| No courses | Empty RecyclerView + empty state text; GPA card shows "0.00" / "N/A" |
| Semester filter, no results | "No courses for this semester." |
| Normal | RecyclerView VISIBLE, GPA card populated |

```kotlin
private fun updateEmptyState() {
    val isEmpty = courseList.isEmpty()
    textViewEmptyGpa.visibility = if (isEmpty) View.VISIBLE else View.GONE
    recyclerViewGpa.visibility = if (isEmpty) View.GONE else View.VISIBLE
}
```

---

## 14. Naming Conventions

| View | ID |
|---|---|
| Toolbar | `toolbarGpa` |
| GPA label | `textViewGpaLabel` |
| GPA value | `textViewGpaValue` |
| Letter grade | `textViewGpaLetterGrade` |
| Total credits | `textViewTotalCredits` |
| Semester filter | `spinnerSemesterFilter` |
| RecyclerView | `recyclerViewGpa` |
| Empty text | `textViewEmptyGpa` |
| FAB | `fabAddCourse` |
| Item: course name | `textViewCourseName` |
| Item: semester | `textViewCourseSemester` |
| Item: credit hours | `textViewCreditHours` |
| Item: grade label | `textViewGradeLabel` |
| Item: grade points | `textViewGradePoints` |
| Item: edit | `imageButtonEditCourse` |
| Item: delete | `imageButtonDeleteCourse` |
| Dialog: name | `editTextCourseName` |
| Dialog: semester | `spinnerCourseSemester` |
| Dialog: credits | `spinnerCreditHours` |
| Dialog: grade | `spinnerGrade` |
