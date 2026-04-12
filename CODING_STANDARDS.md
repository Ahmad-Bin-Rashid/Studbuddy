# FILE: CODING_STANDARDS.md

# StudBuddy — Coding Standards

---

## 1. Naming Conventions

### 1.1 Classes

| Type | Pattern | Example |
|---|---|---|
| Activity | `<Feature>Activity` | `TimetableActivity` |
| Adapter | `<Feature>Adapter` | `TimetableAdapter` |
| Model | `<Entity>` (noun) | `Assignment`, `TimetableEntry` |
| Helper | `<Feature>Helper` | `NotificationHelper` |
| Manager | `<Feature>Manager` | `SharedPrefManager` |
| Receiver | `<Feature>Receiver` | `AlarmReceiver` |
| Scheduler | `<Feature>Scheduler` | `NotificationScheduler` |

### 1.2 Layout Files

| Type | Pattern | Example |
|---|---|---|
| Activity layout | `activity_<feature>.xml` | `activity_timetable.xml` |
| RecyclerView item | `item_<feature>.xml` | `item_timetable.xml` |
| Dialog layout | `dialog_<purpose>.xml` | `dialog_add_assignment.xml` |
| Custom view | `view_<name>.xml` | `view_empty_state.xml` |

### 1.3 View IDs (camelCase, always)

```xml
<!-- Activities -->
android:id="@+id/recyclerViewTimetable"
android:id="@+id/fabAddTimetable"
android:id="@+id/textViewEmptyTimetable"
android:id="@+id/toolbarTimetable"

<!-- Item layouts -->
android:id="@+id/cardViewTimetableItem"
android:id="@+id/textViewCourseName"
android:id="@+id/textViewTimeSlot"
android:id="@+id/textViewDayLabel"

<!-- Dialogs -->
android:id="@+id/editTextCourseName"
android:id="@+id/spinnerDay"
android:id="@+id/spinnerStartTime"
android:id="@+id/buttonSave"
android:id="@+id/buttonCancel"
```

### 1.4 Kotlin Variables

```kotlin
// Private fields — camelCase with backing property if needed
private lateinit var recyclerViewTimetable: RecyclerView
private lateinit var timetableAdapter: TimetableAdapter
private val timetableList = mutableListOf<TimetableEntry>()

// Constants — SCREAMING_SNAKE_CASE in companion object
companion object {
    const val EXTRA_ENTRY_ID = "extra_entry_id"
    const val EXTRA_COURSE_NAME = "extra_course_name"
    const val REQUEST_CODE_ADD = 1001
}

// Local variables — camelCase
val courseName = editTextCourseName.text.toString().trim()
```

### 1.5 String Resource Keys

Pattern: `<screen>_<element>_<descriptor>`

```xml
<string name="timetable_label_course">Course</string>
<string name="timetable_hint_course_name">Enter course name</string>
<string name="timetable_error_empty_name">Course name cannot be empty</string>
<string name="assignment_label_due_date">Due Date</string>
<string name="common_button_save">Save</string>
<string name="common_button_cancel">Cancel</string>
<string name="common_empty_state">No items yet. Tap + to add.</string>
```

### 1.6 Color Resource Names

```xml
<!-- State colors — defined in colors.xml, never hardcoded -->
<color name="colorPrimary">#1565C0</color>
<color name="colorPrimaryDark">#003c8f</color>
<color name="colorAccent">#42A5F5</color>
<color name="colorStatusNormal">#4CAF50</color>
<color name="colorStatusWarning">#FF9800</color>
<color name="colorStatusCritical">#F44336</color>
<color name="colorStatusOverdue">#B71C1C</color>
<color name="colorCardBackground">#FFFFFF</color>
<color name="colorDivider">#E0E0E0</color>
<color name="colorTextPrimary">#212121</color>
<color name="colorTextSecondary">#757575</color>
<color name="colorEmptyState">#BDBDBD</color>
```

---

## 2. Code Formatting

### 2.1 Kotlin Style

- Indentation: 4 spaces (no tabs)
- Max line length: 120 characters
- Opening braces on same line
- One blank line between functions
- No trailing whitespace

```kotlin
// CORRECT
fun calculateGpa(courses: List<Course>): Double {
    if (courses.isEmpty()) return 0.0
    val totalPoints = courses.sumOf { it.gradePoints * it.creditHours }
    val totalCredits = courses.sumOf { it.creditHours }
    return if (totalCredits == 0) 0.0 else totalPoints / totalCredits
}

// WRONG — no blank lines, inconsistent formatting
fun calculateGpa(courses: List<Course>): Double {
if (courses.isEmpty()) return 0.0
val totalPoints = courses.sumOf { it.gradePoints * it.creditHours }; return totalPoints }
```

### 2.2 XML Layout Style

- Indentation: 4 spaces
- One attribute per line for elements with 3+ attributes
- Always define `android:id` first
- Always define `android:layout_width` and `android:layout_height` second and third

```xml
<!-- CORRECT -->
<TextView
    android:id="@+id/textViewCourseName"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:textColor="@color/colorTextPrimary"
    android:textSize="16sp"
    android:fontFamily="sans-serif-medium" />

<!-- WRONG -->
<TextView android:id="@+id/textViewCourseName" android:layout_width="0dp" android:layout_height="wrap_content"/>
```

---

## 3. Activity Structure (Mandatory Order)

Every Activity must follow this exact section order:

```kotlin
class TimetableActivity : AppCompatActivity() {

    // ── 1. View References ──────────────────────────────────────────
    private lateinit var recyclerViewTimetable: RecyclerView
    private lateinit var fabAddTimetable: FloatingActionButton
    private lateinit var textViewEmptyTimetable: TextView

    // ── 2. Adapter ─────────────────────────────────────────────────
    private lateinit var timetableAdapter: TimetableAdapter

    // ── 3. Data ────────────────────────────────────────────────────
    private val timetableList = mutableListOf<TimetableEntry>()

    // ── 4. Lifecycle ───────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) { }
    override fun onResume() { }

    // ── 5. Setup Functions ─────────────────────────────────────────
    private fun setupRecyclerView() { }
    private fun setupClickListeners() { }

    // ── 6. Data Functions ──────────────────────────────────────────
    private fun loadData() { }
    private fun saveEntry(entry: TimetableEntry) { }
    private fun deleteEntry(entryId: String) { }

    // ── 7. UI State Functions ──────────────────────────────────────
    private fun updateEmptyState() { }

    // ── 8. Dialog Functions ────────────────────────────────────────
    private fun showAddDialog() { }
    private fun showEditDialog(entry: TimetableEntry) { }
    private fun showDeleteConfirmation(entry: TimetableEntry) { }

    // ── 9. Validation ──────────────────────────────────────────────
    private fun validateInput(name: String): Boolean { }

    // ── 10. Companion Object ───────────────────────────────────────
    companion object {
        const val EXTRA_ENTRY_ID = "extra_entry_id"
    }
}
```

---

## 4. Adapter Structure (Mandatory Order)

```kotlin
class TimetableAdapter(
    private val items: MutableList<TimetableEntry>,
    private val onEditClick: (TimetableEntry) -> Unit,
    private val onDeleteClick: (TimetableEntry) -> Unit
) : RecyclerView.Adapter<TimetableAdapter.ViewHolder>() {

    // ── 1. ViewHolder (inner class) ────────────────────────────────
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewCourseName: TextView = itemView.findViewById(R.id.textViewCourseName)
        val textViewTimeSlot: TextView = itemView.findViewById(R.id.textViewTimeSlot)
        // ... other views
    }

    // ── 2. Adapter Methods ─────────────────────────────────────────
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) { }
    override fun getItemCount(): Int = items.size

    // ── 3. Public Update Methods ───────────────────────────────────
    fun updateList(newItems: List<TimetableEntry>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: TimetableEntry) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateItem(position: Int, item: TimetableEntry) {
        items[position] = item
        notifyItemChanged(position)
    }
}
```

---

## 5. Forbidden Patterns

| Anti-Pattern | Rule | Correct Alternative |
|---|---|---|
| `SharedPreferences` accessed directly in Activity | FORBIDDEN | Use `AppDataStore` only |
| Business logic inside Adapter `onBindViewHolder` | FORBIDDEN | Move to Activity or model |
| Inline `AlertDialog.Builder.setView(View(...))` | FORBIDDEN | Use `LayoutInflater` |
| Fragment usage | FORBIDDEN | Use Activities |
| External libraries | FORBIDDEN without approval | Use Android SDK only |
| Hardcoded color strings in XML | FORBIDDEN | Use `@color/colorName` |
| Hardcoded string literals in XML | FORBIDDEN | Use `@string/key` |
| Hardcoded dimension values in XML | FORBIDDEN | Use `@dimen/name` |
| `notifyDataSetChanged()` for single item changes | AVOID | Use `notifyItemChanged(pos)` |
| Coroutines or threads | FORBIDDEN | Main thread only |
| `startActivityForResult` | DEPRECATED | Use `ActivityResultLauncher` |

---

## 6. Dimension Standards

All dimensions must be defined in `res/values/dimens.xml`:

```xml
<!-- Spacing -->
<dimen name="spacing_xsmall">4dp</dimen>
<dimen name="spacing_small">8dp</dimen>
<dimen name="spacing_medium">16dp</dimen>
<dimen name="spacing_large">24dp</dimen>
<dimen name="spacing_xlarge">32dp</dimen>

<!-- Card -->
<dimen name="card_corner_radius">8dp</dimen>
<dimen name="card_elevation">4dp</dimen>
<dimen name="card_margin">8dp</dimen>
<dimen name="card_padding">16dp</dimen>

<!-- Text sizes -->
<dimen name="text_size_small">12sp</dimen>
<dimen name="text_size_body">14sp</dimen>
<dimen name="text_size_subtitle">16sp</dimen>
<dimen name="text_size_title">18sp</dimen>
<dimen name="text_size_headline">20sp</dimen>
```

---

## 7. Comment Standards

```kotlin
// Use single-line comments for brief explanations
val gpa = calculateGpa(courses) // Returns 0.0 if no courses

/*
 * Use block comments for complex logic explanations.
 * Formula: GPA = Σ(gradePoints × creditHours) / Σ(creditHours)
 * gradePoints: A=4.0, B=3.0, C=2.0, D=1.0, F=0.0
 */
fun calculateGpa(courses: List<Course>): Double { ... }

// TODO: Brief description — assigned to Dev-X by YYYY-MM-DD
// FIXME: Description of the bug

// DO NOT use @param/@return in Kotlin — use readable function names instead
```

---

## 8. AppCompatActivity Usage

```kotlin
// CORRECT — always extend AppCompatActivity
class TimetableActivity : AppCompatActivity() {

// WRONG — never use Activity directly
class TimetableActivity : Activity() {
```

---

## 9. Toast Standards

```kotlin
// Always use application context for Toasts
Toast.makeText(this, "Entry added successfully", Toast.LENGTH_SHORT).show()
Toast.makeText(this, getString(R.string.timetable_error_empty_name), Toast.LENGTH_SHORT).show()

// For errors: LENGTH_LONG
Toast.makeText(this, "Failed to save. Please try again.", Toast.LENGTH_LONG).show()
```
