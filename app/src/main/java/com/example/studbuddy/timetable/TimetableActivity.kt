package com.example.studbuddy.timetable

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
import com.example.studbuddy.core.models.TimetableEntry
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class TimetableActivity : AppCompatActivity() {

    // ── Section 1: View References ─────────────────────
    private lateinit var toolbarTimetable: Toolbar
    private lateinit var cardUpcomingClasses: CardView
    private lateinit var textViewUpcomingHeader: TextView
    private lateinit var textViewUpcomingDetails: TextView
    private lateinit var layoutDayTabs: LinearLayout
    private lateinit var recyclerViewTimetable: RecyclerView
    private lateinit var textViewEmptyTimetable: TextView
    private lateinit var fabAddTimetable: FloatingActionButton

    // ── Section 2: Adapter & Data ──────────────────────
    private lateinit var timetableAdapter: TimetableAdapter
    private val timetableList = mutableListOf<TimetableEntry>()
    private var selectedDayFilter = 0   // 0 = All, 1–7 = Mon–Sun
    private var selectedColor = TIMETABLE_COLORS[0]

    // ── Section 3: Lifecycle ───────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        toolbarTimetable = findViewById(R.id.toolbarTimetable)
        cardUpcomingClasses = findViewById(R.id.cardUpcomingClasses)
        textViewUpcomingHeader = findViewById(R.id.textViewUpcomingHeader)
        textViewUpcomingDetails = findViewById(R.id.textViewUpcomingDetails)
        layoutDayTabs = findViewById(R.id.layoutDayTabs)
        recyclerViewTimetable = findViewById(R.id.recyclerViewTimetable)
        textViewEmptyTimetable = findViewById(R.id.textViewEmptyTimetable)
        fabAddTimetable = findViewById(R.id.fabAddTimetable)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        buildDayTabs()
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
        setSupportActionBar(toolbarTimetable)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        timetableAdapter = TimetableAdapter(timetableList,
            onEditClick = { showEditDialog(it) },
            onDeleteClick = { showDeleteConfirmation(it) })
        recyclerViewTimetable.layoutManager = LinearLayoutManager(this)
        recyclerViewTimetable.adapter = timetableAdapter
    }

    private fun setupFab() {
        fabAddTimetable.setOnClickListener { showAddDialog() }
    }

    private fun buildDayTabs() {
        layoutDayTabs.removeAllViews()
        DAY_TABS.forEachIndexed { index, label ->
            val tab = TextView(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            params.marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_small)
            tab.layoutParams = params
            tab.text = label
            tab.textSize = 13f
            tab.setPadding(
                resources.getDimensionPixelSize(R.dimen.spacing_medium), 8.dpToPx(),
                resources.getDimensionPixelSize(R.dimen.spacing_medium), 8.dpToPx())
            tab.setOnClickListener { applyFilter(index) }
            tab.tag = index
            layoutDayTabs.addView(tab)
        }
        updateDayTabStyles()
    }

    // ── Section 5: Data ────────────────────────────────
    private fun loadData() {
        val all = AppDataStore.getTimetableList()
        checkUpcomingClasses(all)
        val filtered = if (selectedDayFilter == 0) all
        else all.filter { it.dayOfWeek == selectedDayFilter }
        val sorted = filtered.sortedWith(
            compareBy({ it.dayOfWeek }, { it.startTime }))
        timetableList.clear()
        timetableList.addAll(sorted)
        timetableAdapter.updateList(timetableList)
        updateEmptyState()
    }

    private fun applyFilter(day: Int) {
        selectedDayFilter = day
        updateDayTabStyles()
        loadData()
    }

    private fun checkUpcomingClasses(allEntries: List<TimetableEntry>) {
        val now = System.currentTimeMillis()
        val in24h = now + 24 * 60 * 60 * 1000L
        val upcoming = allEntries.filter {
            val nextMs = getNextOccurrenceMs(it)
            nextMs in now..in24h
        }.sortedBy { getNextOccurrenceMs(it) }
        
        if (upcoming.isEmpty()) {
            cardUpcomingClasses.visibility = View.GONE
        } else {
            cardUpcomingClasses.visibility = View.VISIBLE
            val next = upcoming.first()
            textViewUpcomingDetails.text = "${next.courseName} at ${next.startTime}"
            if (upcoming.size > 1) {
                textViewUpcomingHeader.text = "${upcoming.size} classes in the next 24 hours"
            } else {
                textViewUpcomingHeader.text = getString(R.string.timetable_upcoming_header)
            }
        }
    }

    private fun getNextOccurrenceMs(entry: TimetableEntry): Long {
        val parts = entry.startTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val now = Calendar.getInstance()
        val target = Calendar.getInstance()
        val calDow = if (entry.dayOfWeek == 7) 1 else entry.dayOfWeek + 1
        target.set(Calendar.DAY_OF_WEEK, calDow)
        target.set(Calendar.HOUR_OF_DAY, hour)
        target.set(Calendar.MINUTE, minute)
        target.set(Calendar.SECOND, 0)
        target.set(Calendar.MILLISECOND, 0)
        if (target.before(now)) target.add(Calendar.DAY_OF_YEAR, 7)
        return target.timeInMillis
    }

    private fun saveTimetableEntry(entry: TimetableEntry) {
        AppDataStore.addTimetableEntry(entry)
        loadData()
        Toast.makeText(this, "Class added", Toast.LENGTH_SHORT).show()
    }

    private fun updateTimetableEntry(entry: TimetableEntry) {
        AppDataStore.updateTimetableEntry(entry)
        loadData()
        Toast.makeText(this, "Class updated", Toast.LENGTH_SHORT).show()
    }

    private fun deleteTimetableEntry(id: String) {
        AppDataStore.deleteTimetableEntry(id)
        loadData()
        Toast.makeText(this, "Class removed", Toast.LENGTH_SHORT).show()
    }

    // ── Section 6: Dialogs ─────────────────────────────
    private fun showAddDialog() {
        val courses = AppDataStore.getCourseNames()
        if (courses.isEmpty()) {
            Toast.makeText(this, getString(R.string.timetable_error_no_courses_exist), Toast.LENGTH_LONG).show()
            return
        }

        selectedColor = TIMETABLE_COLORS[0]
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_timetable, null)
        
        val spinnerCourse = dialogView.findViewById<Spinner>(R.id.spinnerTimetableCourse)
        val spinnerDay = dialogView.findViewById<Spinner>(R.id.spinnerTimetableDay)
        val spinnerStart = dialogView.findViewById<Spinner>(R.id.spinnerStartTime)
        val spinnerEnd = dialogView.findViewById<Spinner>(R.id.spinnerEndTime)
        val editTextRoom = dialogView.findViewById<EditText>(R.id.editTextTimetableRoom)
        val layoutColorPicker = dialogView.findViewById<LinearLayout>(R.id.layoutTimetableColorPicker)

        spinnerCourse.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val dayNames = TimetableEntry.DAY_NAMES.subList(1, 8)
        spinnerDay.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dayNames).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val timeSlots = generateTimeSlots()
        spinnerStart.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerEnd.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerEnd.setSelection(3) // 1.5h default

        buildColorPicker(layoutColorPicker, { color -> selectedColor = color }, selectedColor)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.timetable_add_title)
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val courseName = spinnerCourse.selectedItem?.toString() ?: ""
                val day = spinnerDay.selectedItemPosition + 1
                val startTime = spinnerStart.selectedItem.toString()
                val endTime = spinnerEnd.selectedItem.toString()
                val room = editTextRoom.text.toString().trim()
                
                if (!validateTimetableInput(courseName, startTime, endTime)) return@setOnClickListener
                
                val entry = TimetableEntry(
                    id = UUID.randomUUID().toString(),
                    courseName = courseName,
                    dayOfWeek = day,
                    startTime = startTime,
                    endTime = endTime,
                    room = room,
                    colorHex = selectedColor
                )
                saveTimetableEntry(entry)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showEditDialog(entry: TimetableEntry) {
        selectedColor = entry.colorHex
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_timetable, null)
        
        val spinnerCourse = dialogView.findViewById<Spinner>(R.id.spinnerTimetableCourse)
        val spinnerDay = dialogView.findViewById<Spinner>(R.id.spinnerTimetableDay)
        val spinnerStart = dialogView.findViewById<Spinner>(R.id.spinnerStartTime)
        val spinnerEnd = dialogView.findViewById<Spinner>(R.id.spinnerEndTime)
        val editTextRoom = dialogView.findViewById<EditText>(R.id.editTextTimetableRoom)
        val layoutColorPicker = dialogView.findViewById<LinearLayout>(R.id.layoutTimetableColorPicker)

        val courses = AppDataStore.getCourseNames()
        spinnerCourse.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerCourse.setSelection(courses.indexOf(entry.courseName).coerceAtLeast(0))

        val dayNames = TimetableEntry.DAY_NAMES.subList(1, 8)
        spinnerDay.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dayNames).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerDay.setSelection(entry.dayOfWeek - 1)

        val timeSlots = generateTimeSlots()
        spinnerStart.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerEnd.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerStart.setSelection(timeSlots.indexOf(entry.startTime).coerceAtLeast(0))
        spinnerEnd.setSelection(timeSlots.indexOf(entry.endTime).coerceAtLeast(0))

        editTextRoom.setText(entry.room)
        buildColorPicker(layoutColorPicker, { color -> selectedColor = color }, selectedColor)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.timetable_edit_title)
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val courseName = spinnerCourse.selectedItem?.toString() ?: ""
                val day = spinnerDay.selectedItemPosition + 1
                val startTime = spinnerStart.selectedItem.toString()
                val endTime = spinnerEnd.selectedItem.toString()
                val room = editTextRoom.text.toString().trim()
                
                if (!validateTimetableInput(courseName, startTime, endTime)) return@setOnClickListener
                
                updateTimetableEntry(entry.copy(
                    courseName = courseName, dayOfWeek = day,
                    startTime = startTime, endTime = endTime,
                    room = room, colorHex = selectedColor))
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showDeleteConfirmation(entry: TimetableEntry) {
        AlertDialog.Builder(this)
            .setTitle(R.string.timetable_delete_title)
            .setMessage(getString(R.string.timetable_delete_message, entry.courseName))
            .setPositiveButton("Delete") { _, _ -> deleteTimetableEntry(entry.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Section 7: Helpers ─────────────────────────────
    private fun updateEmptyState() {
        val isEmpty = timetableList.isEmpty()
        textViewEmptyTimetable.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerViewTimetable.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun updateDayTabStyles() {
        for (i in 0 until layoutDayTabs.childCount) {
            val tab = layoutDayTabs.getChildAt(i) as TextView
            if (tab.tag as Int == selectedDayFilter) {
                tab.background = ContextCompat.getDrawable(this, R.drawable.timetable_day_tab_selected)
                tab.setTextColor(getColor(R.color.colorTextOnPrimary))
            } else {
                tab.background = ContextCompat.getDrawable(this, R.drawable.timetable_day_tab_unselected)
                tab.setTextColor(getColor(R.color.colorTextSecondary))
            }
        }
    }

    private fun validateTimetableInput(courseName: String, startTime: String, endTime: String): Boolean {
        if (courseName.isBlank()) {
            Toast.makeText(this, R.string.timetable_error_no_course, Toast.LENGTH_SHORT).show()
            return false
        }
        if (startTime >= endTime) {
            Toast.makeText(this, R.string.timetable_error_time_range, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun buildColorPicker(container: LinearLayout, onColorSelected: (String) -> Unit, currentColor: String) {
        container.removeAllViews()
        TIMETABLE_COLORS.forEach { hex ->
            val circle = View(this)
            val size = 40.dpToPx()
            val params = LinearLayout.LayoutParams(size, size)
            params.marginEnd = 8.dpToPx()
            circle.layoutParams = params
            val gd = GradientDrawable()
            gd.shape = GradientDrawable.OVAL
            gd.setColor(Color.parseColor(hex))
            if (hex == currentColor) gd.setStroke(4, Color.WHITE)
            circle.background = gd
            circle.setOnClickListener {
                onColorSelected(hex)
                buildColorPicker(container, onColorSelected, hex)
            }
            container.addView(circle)
        }
    }

    // ── Section 8: Inner Adapter ───────────────────────
    private inner class TimetableAdapter(
        private val items: MutableList<TimetableEntry>,
        private val onEditClick: (TimetableEntry) -> Unit,
        private val onDeleteClick: (TimetableEntry) -> Unit
    ) : RecyclerView.Adapter<TimetableAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val viewTimetableColorStripe: View = view.findViewById(R.id.viewTimetableColorStripe)
            val textViewTimetableCourseName: TextView = view.findViewById(R.id.textViewTimetableCourseName)
            val textViewTimetableTimeSlot: TextView = view.findViewById(R.id.textViewTimetableTimeSlot)
            val layoutTimetableRoom: LinearLayout = view.findViewById(R.id.layoutTimetableRoom)
            val textViewTimetableRoom: TextView = view.findViewById(R.id.textViewTimetableRoom)
            val textViewTimetableDayLabel: TextView = view.findViewById(R.id.textViewTimetableDayLabel)
            val textViewTodayBadge: TextView = view.findViewById(R.id.textViewTodayBadge)
            val imageButtonEditTimetable: ImageButton = view.findViewById(R.id.imageButtonEditTimetable)
            val imageButtonDeleteTimetable: ImageButton = view.findViewById(R.id.imageButtonDeleteTimetable)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timetable, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = items[position]
            holder.viewTimetableColorStripe.setBackgroundColor(Color.parseColor(entry.colorHex))
            holder.textViewTimetableCourseName.text = entry.courseName
            holder.textViewTimetableTimeSlot.text = "${entry.startTime} – ${entry.endTime}"
            
            if (entry.room.isBlank()) {
                holder.layoutTimetableRoom.visibility = View.GONE
            } else {
                holder.layoutTimetableRoom.visibility = View.VISIBLE
                holder.textViewTimetableRoom.text = entry.room
            }
            
            holder.textViewTimetableDayLabel.text = TimetableEntry.DAY_NAMES[entry.dayOfWeek]
            
            val todayDow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val entryDow = if (entry.dayOfWeek == 7) 1 else entry.dayOfWeek + 1
            holder.textViewTodayBadge.visibility = if (todayDow == entryDow) View.VISIBLE else View.GONE
            
            holder.imageButtonEditTimetable.setOnClickListener { onEditClick(entry) }
            holder.imageButtonDeleteTimetable.setOnClickListener { onDeleteClick(entry) }
        }

        override fun getItemCount() = items.size

        fun updateList(newItems: List<TimetableEntry>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    // ── Section 9: Companion ───────────────────────────
    companion object {
        val TIMETABLE_COLORS = listOf(
            "#1565C0", "#6A1B9A", "#00695C",
            "#E65100", "#AD1457", "#283593",
            "#558B2F", "#4E342E"
        )
        val DAY_TABS = listOf("All", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        
        private fun generateTimeSlots(): List<String> {
            val slots = mutableListOf<String>()
            for (h in 6..22) for (m in listOf(0, 30)) {
                slots.add(String.format("%02d:%02d", h, m))
            }
            return slots
        }
    }
}

private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
