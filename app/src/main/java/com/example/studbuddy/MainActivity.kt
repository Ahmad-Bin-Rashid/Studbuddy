package com.example.studbuddy

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.studbuddy.core.AppDataStore
import com.example.studbuddy.core.models.Semester
import com.example.studbuddy.gpa.CourseActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main dashboard and navigation hub for StudBuddy.
 * Uses a "Rail" style sidebar that pushes content instead of overlapping.
 */
class MainActivity : AppCompatActivity() {

    // ── Section 1: View References ──────────────────────
    private lateinit var toolbarMain: Toolbar
    private lateinit var imageButtonMenuToggle: ImageButton
    private lateinit var layoutMainContent: LinearLayout
    private lateinit var layoutSidebar: View
    private lateinit var imageButtonCloseSidebar: ImageButton
    private lateinit var imageButtonMinimizeSidebar: ImageButton
    
    private lateinit var textViewSidebarAppName: TextView
    private lateinit var textViewSidebarTagline: TextView
    private lateinit var textViewSidebarFooter: TextView

    private lateinit var cardTimetable: CardView
    private lateinit var cardAssignments: CardView
    private lateinit var cardAttendance: CardView
    private lateinit var cardExams: CardView
    private lateinit var cardCourses: CardView

    private lateinit var navItemTimetable: LinearLayout
    private lateinit var navItemAssignments: LinearLayout
    private lateinit var navItemAttendance: LinearLayout
    private lateinit var navItemExams: LinearLayout
    private lateinit var navItemGpa: LinearLayout
    private lateinit var navItemCourses: LinearLayout
    private lateinit var navItemSettings: LinearLayout

    private lateinit var textViewActiveSemesterBadge: TextView
    private lateinit var cardSemesterBanner: CardView
    private lateinit var textViewBannerSemesterName: TextView
    private lateinit var imageButtonManageSemesters: ImageButton
    private lateinit var fabMainAction: FloatingActionButton

    // ── Section 2: State ─────────────────────────────────
    private var isSidebarExpanded = true
    private var isSidebarVisible = true
    private var addDialogStartDate: Long = 0L
    private var addDialogEndDate: Long = 0L

    // ── Section 3: Lifecycle ─────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        AppDataStore.initialize(applicationContext)
        
        initViews()
        setupToolbar()
        setupSidebar()
        setupDashboardCards()
        setupBackPressed()
    }

    override fun onResume() {
        super.onResume()
        updateSemesterBanner()
    }

    // ── Section 4: Setup Functions ───────────────────────
    private fun initViews() {
        toolbarMain = findViewById(R.id.toolbarMain)
        imageButtonMenuToggle = findViewById(R.id.imageButtonMenuToggle)
        layoutMainContent = findViewById(R.id.layoutMainContent)
        layoutSidebar = findViewById(R.id.layoutSidebar)
        
        val sidebarRoot = layoutSidebar
        imageButtonCloseSidebar = sidebarRoot.findViewById(R.id.imageButtonCloseSidebar)
        imageButtonMinimizeSidebar = sidebarRoot.findViewById(R.id.imageButtonMinimizeSidebar)
        textViewSidebarAppName = sidebarRoot.findViewById(R.id.textViewSidebarAppName)
        textViewSidebarTagline = sidebarRoot.findViewById(R.id.textViewSidebarTagline)
        textViewSidebarFooter = sidebarRoot.findViewById(R.id.textViewSidebarFooter)

        cardTimetable = findViewById(R.id.cardTimetable)
        cardAssignments = findViewById(R.id.cardAssignments)
        cardAttendance = findViewById(R.id.cardAttendance)
        cardExams = findViewById(R.id.cardExams)
        cardCourses = findViewById(R.id.cardCourses)

        navItemTimetable = sidebarRoot.findViewById(R.id.navItemTimetable)
        navItemAssignments = sidebarRoot.findViewById(R.id.navItemAssignments)
        navItemAttendance = sidebarRoot.findViewById(R.id.navItemAttendance)
        navItemExams = sidebarRoot.findViewById(R.id.navItemExams)
        navItemGpa = sidebarRoot.findViewById(R.id.navItemGpa)
        navItemCourses = sidebarRoot.findViewById(R.id.navItemCourses)
        navItemSettings = sidebarRoot.findViewById(R.id.navItemSettings)

        textViewActiveSemesterBadge = findViewById(R.id.textViewActiveSemesterBadge)
        cardSemesterBanner = findViewById(R.id.cardSemesterBanner)
        textViewBannerSemesterName = findViewById(R.id.textViewBannerSemesterName)
        imageButtonManageSemesters = findViewById(R.id.imageButtonManageSemesters)
        fabMainAction = findViewById(R.id.fabMainAction)

        setupNavItem(navItemTimetable, R.string.nav_timetable, R.drawable.ic_schedule)
        setupNavItem(navItemAssignments, R.string.nav_assignments, R.drawable.ic_assignment)
        setupNavItem(navItemAttendance, R.string.nav_attendance, R.drawable.ic_attendance)
        setupNavItem(navItemExams, R.string.nav_exams, R.drawable.ic_exam)
        setupNavItem(navItemGpa, R.string.nav_gpa, R.drawable.ic_school)
        setupNavItem(navItemCourses, R.string.course_screen_title, R.drawable.ic_school)
        setupNavItem(navItemSettings, R.string.nav_settings, R.drawable.ic_settings)
    }

    private fun setupNavItem(item: LinearLayout, labelRes: Int, iconRes: Int) {
        item.findViewById<TextView>(R.id.textViewNavLabel).text = getString(labelRes)
        item.findViewById<ImageView>(R.id.imageViewNavIcon).setImageResource(iconRes)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbarMain)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        imageButtonMenuToggle.setOnClickListener { toggleSidebarVisibility() }
    }

    private fun setupSidebar() {
        imageButtonCloseSidebar.setOnClickListener { toggleSidebarVisibility() }
        imageButtonMinimizeSidebar.setOnClickListener { toggleSidebarSize() }

        navItemTimetable.setOnClickListener { 
            setActiveSidebarItem(navItemTimetable)
            navigateTo(com.example.studbuddy.timetable.TimetableActivity::class.java) 
        }
        navItemAssignments.setOnClickListener { 
            setActiveSidebarItem(navItemAssignments)
            navigateTo(com.example.studbuddy.assignments.AssignmentsActivity::class.java) 
        }
        navItemAttendance.setOnClickListener { 
            setActiveSidebarItem(navItemAttendance)
            navigateTo(com.example.studbuddy.attendance.AttendanceActivity::class.java) 
        }
        navItemExams.setOnClickListener { 
            setActiveSidebarItem(navItemExams)
            navigateTo(com.example.studbuddy.exams.ExamsActivity::class.java) 
        }
        navItemGpa.setOnClickListener { setActiveSidebarItem(navItemGpa) }
        navItemCourses.setOnClickListener { 
            setActiveSidebarItem(navItemCourses)
            navigateTo(CourseActivity::class.java) 
        }
        navItemSettings.setOnClickListener { setActiveSidebarItem(navItemSettings) }
    }

    private fun setupDashboardCards() {
        cardTimetable.setOnClickListener   { navigateTo(com.example.studbuddy.timetable.TimetableActivity::class.java) }
        cardAssignments.setOnClickListener { navigateTo(com.example.studbuddy.assignments.AssignmentsActivity::class.java) }
        cardAttendance.setOnClickListener  { navigateTo(com.example.studbuddy.attendance.AttendanceActivity::class.java) }
        cardExams.setOnClickListener       { navigateTo(com.example.studbuddy.exams.ExamsActivity::class.java) }
        cardCourses.setOnClickListener     { navigateTo(CourseActivity::class.java) }

        imageButtonManageSemesters.setOnClickListener { showManageSemestersDialog() }
        fabMainAction.visibility = View.VISIBLE
        fabMainAction.setOnClickListener { showAddSemesterDialog() }
    }

    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isSidebarVisible && isSidebarExpanded) {
                    toggleSidebarSize()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    // ── Section 5: Sidebar Logic ─────────────────────────
    
    private fun toggleSidebarVisibility() {
        isSidebarVisible = !isSidebarVisible
        animateSidebarWidth(if (isSidebarVisible) {
            if (isSidebarExpanded) R.dimen.sidebar_width else R.dimen.sidebar_width_minimized
        } else {
            null // 0dp
        })
    }

    private fun toggleSidebarSize() {
        if (!isSidebarVisible) {
            isSidebarVisible = true
            isSidebarExpanded = true
        } else {
            isSidebarExpanded = !isSidebarExpanded
        }
        
        animateSidebarWidth(if (isSidebarExpanded) R.dimen.sidebar_width else R.dimen.sidebar_width_minimized)
        updateSidebarUI()
    }

    private fun animateSidebarWidth(widthRes: Int?) {
        val targetWidth = if (widthRes != null) resources.getDimensionPixelSize(widthRes) else 0
        val startWidth = layoutSidebar.width
        val params = layoutSidebar.layoutParams

        val animation = object : android.view.animation.Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: android.view.animation.Transformation?) {
                params.width = (startWidth + (targetWidth - startWidth) * interpolatedTime).toInt()
                layoutSidebar.layoutParams = params
            }
        }
        animation.duration = SIDEBAR_ANIM_DURATION
        animation.interpolator = DECELERATE
        layoutSidebar.startAnimation(animation)
    }

    private fun updateSidebarUI() {
        val visibility = if (isSidebarExpanded) View.VISIBLE else View.GONE
        textViewSidebarAppName.visibility = visibility
        textViewSidebarTagline.visibility = visibility
        textViewSidebarFooter.visibility = visibility
        imageButtonCloseSidebar.visibility = visibility
        
        val navItems = listOf(navItemTimetable, navItemAssignments, navItemAttendance, 
                              navItemExams, navItemGpa, navItemCourses, navItemSettings)
        
        navItems.forEach { item ->
            item.findViewById<TextView>(R.id.textViewNavLabel).visibility = visibility
        }

        imageButtonMinimizeSidebar.setImageResource(
            if (isSidebarExpanded) R.drawable.ic_arrow_back else R.drawable.ic_menu
        )
    }

    // ── Section 6: Navigation ────────────────────────────
    private fun navigateTo(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    private fun setActiveSidebarItem(activeItem: LinearLayout?) {
        val allItems = listOf(navItemTimetable, navItemAssignments, navItemAttendance, navItemExams, navItemGpa, navItemCourses, navItemSettings)
        allItems.forEach { item ->
            item.isSelected = (item == activeItem)
            item.findViewById<View>(R.id.viewActiveIndicator).visibility =
                if (item == activeItem) View.VISIBLE else View.INVISIBLE
            
            val label = item.findViewById<TextView>(R.id.textViewNavLabel)
            label.setTextColor(getColor(if (item == activeItem) R.color.colorSidebarItemActive else R.color.colorSidebarItem))
        }
    }

    // ── Section 7: Semester Management ───────────────────
    private fun updateSemesterBanner() {
        val active = AppDataStore.getActiveSemester()
        if (active != null) {
            textViewBannerSemesterName.text = active.name
            textViewActiveSemesterBadge.visibility = View.VISIBLE
            textViewActiveSemesterBadge.text = getString(R.string.semester_active_badge)
        } else {
            textViewBannerSemesterName.text = getString(R.string.dashboard_no_semester)
            textViewActiveSemesterBadge.visibility = View.GONE
        }
    }

    private fun showAddSemesterDialog() {
        addDialogStartDate = 0L
        addDialogEndDate = 0L
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_semester, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextSemesterName)
        val buttonPickStart = dialogView.findViewById<Button>(R.id.buttonPickStartDate)
        val textViewStart = dialogView.findViewById<TextView>(R.id.textViewStartDateDisplay)
        val buttonPickEnd = dialogView.findViewById<Button>(R.id.buttonPickEndDate)
        val textViewEnd = dialogView.findViewById<TextView>(R.id.textViewEndDateDisplay)
        val checkBoxActive = dialogView.findViewById<CheckBox>(R.id.checkBoxSetActive)
        val dateFmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        buttonPickStart.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val c = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
                addDialogStartDate = c.timeInMillis
                textViewStart.text = dateFmt.format(Date(addDialogStartDate))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        buttonPickEnd.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val c = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }
                addDialogEndDate = c.timeInMillis
                textViewEnd.text = dateFmt.format(Date(addDialogEndDate))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.semester_add_title))
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = editTextName.text.toString().trim()
                if (!validateSemesterInput(name, addDialogStartDate, addDialogEndDate)) return@setOnClickListener
                
                val semester = Semester(UUID.randomUUID().toString(), name, addDialogStartDate, addDialogEndDate, checkBoxActive.isChecked)
                if (semester.isActive) {
                    AppDataStore.getSemesterList().filter { it.isActive }.forEach {
                        AppDataStore.updateSemester(it.copy(isActive = false))
                    }
                }
                AppDataStore.addSemester(semester)
                updateSemesterBanner()
                Toast.makeText(this, "Semester added", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showManageSemestersDialog() {
        val semesters = AppDataStore.getSemesterList()
        if (semesters.isEmpty()) { showAddSemesterDialog(); return }
        
        val names = semesters.joinToString("\n") { s -> "- ${s.name}${if(s.isActive) " ★" else ""}" }
        AlertDialog.Builder(this).setTitle("Manage Semesters").setMessage(names)
            .setPositiveButton("Add New") { _, _ -> showAddSemesterDialog() }
            .setNeutralButton("Done", null).show()
    }

    private fun validateSemesterInput(name: String, start: Long, end: Long): Boolean {
        if (name.isBlank()) { Toast.makeText(this, R.string.semester_error_empty_name, Toast.LENGTH_SHORT).show(); return false }
        if (start == 0L || end == 0L) { Toast.makeText(this, R.string.semester_error_no_date, Toast.LENGTH_SHORT).show(); return false }
        if (end <= start) { Toast.makeText(this, R.string.semester_error_date_range, Toast.LENGTH_SHORT).show(); return false }
        return true
    }

    companion object {
        private const val SIDEBAR_ANIM_DURATION = 250L
        private val DECELERATE = DecelerateInterpolator()
    }
}
