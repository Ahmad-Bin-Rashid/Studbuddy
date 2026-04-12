# FILE: RECYCLER_VIEW_PATTERNS.md

# StudBuddy — RecyclerView Patterns

---

## 1. Universal Rules

These rules apply to ALL RecyclerViews in the project, without exception:

1. **ViewHolder pattern always.** Never access views directly in `onBindViewHolder` without going through `ViewHolder` fields.
2. **No business logic in Adapter.** Color logic, date formatting, and status determination happen in the Adapter (`onBindViewHolder`) but only for presentation. Calculations (GPA, %) happen in Activity.
3. **No data access in Adapter.** Adapters receive a `MutableList<T>` — they never call `AppDataStore`.
4. **Adapter always has `updateList()`** — called from Activity with a new list. Never mutate the adapter's list from outside.
5. **Use granular notify calls** for single-item updates (`notifyItemInserted`, `notifyItemRemoved`, `notifyItemChanged`). Use `notifyDataSetChanged()` only for full list replacement.

---

## 2. Standard Single-Type Adapter Template

```kotlin
class <Feature>Adapter(
    private val items: MutableList<<Model>>,
    private val on<Action>Click: (<Model>) -> Unit,
    private val onDeleteClick: (<Model>) -> Unit
) : RecyclerView.Adapter<<Feature>Adapter.ViewHolder>() {

    // ── ViewHolder ─────────────────────────────────────────────────
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Declare ALL views used in this item — no findViewById in onBindViewHolder
        val textView<Name>: TextView = view.findViewById(R.id.textView<Name>)
        val imageButtonDelete: ImageButton = view.findViewById(R.id.imageButtonDelete<Feature>)
        // ... all other views
    }

    // ── onCreateViewHolder ─────────────────────────────────────────
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_<feature>, parent, false)
        return ViewHolder(view)
    }

    // ── onBindViewHolder ───────────────────────────────────────────
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        // Bind data to views
        holder.textView<Name>.text = item.<field>
        // Set click listeners
        holder.imageButtonDelete.setOnClickListener { onDeleteClick(item) }
    }

    // ── getItemCount ───────────────────────────────────────────────
    override fun getItemCount(): Int = items.size

    // ── Public mutation methods ────────────────────────────────────
    fun updateList(newItems: List<<Model>>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: <Model>) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(id: String) {
        val position = items.indexOfFirst { it.id == id }
        if (position >= 0) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateItem(item: <Model>) {
        val position = items.indexOfFirst { it.id == item.id }
        if (position >= 0) {
            items[position] = item
            notifyItemChanged(position)
        }
    }
}
```

---

## 3. Multi-ViewType Adapter Template (Attendance Module)

```kotlin
class AttendanceAdapter(
    private val summaries: MutableList<AttendanceSummary>,
    private val records: MutableList<AttendanceRecord>,
    private var viewMode: ViewMode,
    private val onDeleteRecord: (AttendanceRecord) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ViewMode { SUMMARY, RECORDS }

    companion object {
        private const val TYPE_SUMMARY = 0
        private const val TYPE_RECORD = 1
    }

    // ── ViewHolders ────────────────────────────────────────────────
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

    // ── ViewType routing ───────────────────────────────────────────
    override fun getItemViewType(position: Int): Int = when (viewMode) {
        ViewMode.SUMMARY -> TYPE_SUMMARY
        ViewMode.RECORDS -> TYPE_RECORD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SUMMARY -> SummaryViewHolder(
                inflater.inflate(R.layout.item_attendance_summary, parent, false))
            else -> RecordViewHolder(
                inflater.inflate(R.layout.item_attendance_record, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SummaryViewHolder -> bindSummary(holder, summaries[position])
            is RecordViewHolder  -> bindRecord(holder, records[position])
        }
    }

    override fun getItemCount(): Int = when (viewMode) {
        ViewMode.SUMMARY -> summaries.size
        ViewMode.RECORDS -> records.size
    }

    // ── Bind functions ─────────────────────────────────────────────
    private fun bindSummary(holder: SummaryViewHolder, summary: AttendanceSummary) {
        holder.textViewSummaryCourse.text = summary.courseName
        val pct = summary.percentage.toInt()
        holder.textViewPercentage.text = "$pct%"
        holder.progressBarAttendance.progress = pct
        val color = getAttendanceColor(holder.itemView.context, summary.percentage)
        holder.textViewPercentage.setTextColor(color)
        holder.progressBarAttendance.progressTintList = ColorStateList.valueOf(color)
        holder.textViewPresentCount.text = "✓ ${summary.presentCount} Present"
        holder.textViewAbsentCount.text  = "✗ ${summary.absentCount} Absent"
        holder.textViewLateCount.text    = "~ ${summary.lateCount} Late"
    }

    private fun bindRecord(holder: RecordViewHolder, record: AttendanceRecord) {
        val sdf = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())
        holder.textViewRecordDate.text = sdf.format(Date(record.date))
        holder.textViewRecordCourse.text = record.courseName
        holder.textViewRecordNotes.visibility =
            if (record.notes.isBlank()) View.GONE else View.VISIBLE
        holder.textViewRecordNotes.text = record.notes
        val (badge, colorRes) = when (record.status) {
            AttendanceStatus.PRESENT -> "P" to R.color.colorStatusNormal
            AttendanceStatus.ABSENT  -> "A" to R.color.colorStatusCritical
            AttendanceStatus.LATE    -> "L" to R.color.colorStatusWarning
        }
        holder.textViewStatusBadge.text = badge
        holder.textViewStatusBadge.setBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, colorRes))
        holder.imageButtonDeleteRecord.setOnClickListener { onDeleteRecord(record) }
    }

    // ── Mode switch ────────────────────────────────────────────────
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

    private fun getAttendanceColor(context: Context, percentage: Double): Int =
        ContextCompat.getColor(context, when {
            percentage >= 75.0 -> R.color.colorAttendanceGood
            percentage >= 60.0 -> R.color.colorAttendanceMid
            else               -> R.color.colorAttendanceLow
        })
}
```

---

## 4. Activity-Side RecyclerView Setup

```kotlin
// Always do this in onCreate() — not onResume()
private fun setupRecyclerView() {
    timetableAdapter = TimetableAdapter(
        items = timetableList,
        onEditClick = { entry -> showEditDialog(entry) },
        onDeleteClick = { entry -> showDeleteConfirmation(entry) }
    )
    recyclerViewTimetable.apply {
        layoutManager = LinearLayoutManager(this@TimetableActivity)
        adapter = timetableAdapter
        setHasFixedSize(false) // false because list can change size
    }
}

// Always reload in onResume() — NOT onCreate()
override fun onResume() {
    super.onResume()
    loadData()
}
```

---

## 5. Forbidden Patterns in Adapters

| Anti-Pattern | Why Forbidden |
|---|---|
| `adapter.items.add(item)` from Activity | Violates encapsulation — use `adapter.addItem()` |
| `AppDataStore.xxx()` inside Adapter | Adapters must not access storage |
| `this.context` inside ViewHolder (without itemView) | Use `holder.itemView.context` |
| `notifyDataSetChanged()` for single-item operations | Performance — use granular notify |
| Click listeners set in `onCreateViewHolder` | Must be set in `onBindViewHolder` — position can change |
| `holder.adapterPosition` in async callbacks | Position may be stale — capture in lambda instead |

---

## 6. RecyclerView in XML (Authoritative Template)

```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView<Feature>"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:clipToPadding="false"
    android:paddingBottom="72dp"
    android:scrollbars="vertical"
    tools:listitem="@layout/item_<feature>" />
```

**Never** set `android:nestedScrollingEnabled="false"` unless RecyclerView is inside a `NestedScrollView` (which is not used in this project).

---

## 7. Item Layout Root Rules

The root element of every `item_<feature>.xml` must be a `CardView`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="@dimen/card_margin"
    app:cardCornerRadius="@dimen/card_corner_radius"
    app:cardElevation="@dimen/card_elevation"
    app:cardBackgroundColor="@color/colorCardBackground">

    <!-- Single child only -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="<horizontal|vertical>"
        android:padding="@dimen/card_padding">

        <!-- Item content here -->

    </LinearLayout>
</androidx.cardview.widget.CardView>
```

Constraints:
- Root = `CardView` always
- Exactly one direct child of `CardView`
- No nested `CardView` elements
