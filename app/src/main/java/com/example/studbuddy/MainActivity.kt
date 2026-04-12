package com.example.studbuddy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.studbuddy.core.AppDataStore

/**
 * Main dashboard and navigation hub for StudBuddy.
 */
class MainActivity : AppCompatActivity() {

    // ── Section 1: View References ──────────────────────
    private lateinit var toolbarMain: Toolbar
    private lateinit var imageButtonMenuToggle: ImageButton
    private lateinit var layoutMainContent: LinearLayout
    private lateinit var layoutSidebar: LinearLayout
    private lateinit var viewSidebarOverlay: View
    private lateinit var imageButtonCloseSidebar: ImageButton
    
    // Dashboard cards
    private lateinit var cardTimetable: CardView
    private lateinit var cardAssignments: CardView
    private lateinit var cardAttendance: CardView
    private lateinit var cardExams: CardView
    private lateinit var cardGpa: CardView
    private lateinit var cardSettings: CardView

    // Sidebar nav items
    private lateinit var navItemTimetable: LinearLayout
    private lateinit var navItemAssignments: LinearLayout
    private lateinit var navItemAttendance: LinearLayout
    private lateinit var navItemExams: LinearLayout
    private lateinit var navItemGpa: LinearLayout
    private lateinit var navItemSettings: LinearLayout

    // ── Section 2: State ─────────────────────────────────
    private var isSidebarOpen = false

    // ── Section 3: Lifecycle ─────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize Core Components
        AppDataStore.initialize(applicationContext)
        // NotificationHelper.createChannels(this) // To be implemented in Phase 6
        
        initViews()
        setupToolbar()
        setupSidebar()
        setupDashboardCards()
    }

    // ── Section 4: Setup Functions ───────────────────────
    private fun initViews() {
        toolbarMain = findViewById(R.id.toolbarMain)
        imageButtonMenuToggle = findViewById(R.id.imageButtonMenuToggle)
        layoutMainContent = findViewById(R.id.layoutMainContent)
        layoutSidebar = findViewById(R.id.layoutSidebar)
        viewSidebarOverlay = findViewById(R.id.viewSidebarOverlay)
        
        val sidebarRoot = layoutSidebar
        imageButtonCloseSidebar = sidebarRoot.findViewById(R.id.imageButtonCloseSidebar)

        // Dashboard cards
        cardTimetable = findViewById(R.id.cardTimetable)
        cardAssignments = findViewById(R.id.cardAssignments)
        cardAttendance = findViewById(R.id.cardAttendance)
        cardExams = findViewById(R.id.cardExams)
        cardGpa = findViewById(R.id.cardGpa)
        cardSettings = findViewById(R.id.cardSettings)

        // Sidebar nav items
        navItemTimetable = sidebarRoot.findViewById(R.id.navItemTimetable)
        navItemAssignments = sidebarRoot.findViewById(R.id.navItemAssignments)
        navItemAttendance = sidebarRoot.findViewById(R.id.navItemAttendance)
        navItemExams = sidebarRoot.findViewById(R.id.navItemExams)
        navItemGpa = sidebarRoot.findViewById(R.id.navItemGpa)
        navItemSettings = sidebarRoot.findViewById(R.id.navItemSettings)

        // Set Nav Labels
        navItemTimetable.findViewById<TextView>(R.id.textViewNavLabel).text = getString(R.string.nav_timetable)
        navItemAssignments.findViewById<TextView>(R.id.textViewNavLabel).text = getString(R.string.nav_assignments)
        navItemAttendance.findViewById<TextView>(R.id.textViewNavLabel).text = getString(R.string.nav_attendance)
        navItemExams.findViewById<TextView>(R.id.textViewNavLabel).text = getString(R.string.nav_exams)
        navItemGpa.findViewById<TextView>(R.id.textViewNavLabel).text = getString(R.string.nav_gpa)
        navItemSettings.findViewById<TextView>(R.id.textViewNavLabel).text = getString(R.string.nav_settings)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbarMain)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        imageButtonMenuToggle.setOnClickListener { toggleSidebar() }
    }

    private fun setupSidebar() {
        imageButtonCloseSidebar.setOnClickListener { closeSidebar() }
        viewSidebarOverlay.setOnClickListener { closeSidebar() }

        navItemTimetable.setOnClickListener {
            setActiveSidebarItem(navItemTimetable)
            closeSidebar()
            // navigateTo(TimetableActivity::class.java)
        }
        navItemAssignments.setOnClickListener {
            setActiveSidebarItem(navItemAssignments)
            closeSidebar()
            // navigateTo(AssignmentsActivity::class.java)
        }
        navItemAttendance.setOnClickListener {
            setActiveSidebarItem(navItemAttendance)
            closeSidebar()
            // navigateTo(AttendanceActivity::class.java)
        }
        navItemExams.setOnClickListener {
            setActiveSidebarItem(navItemExams)
            closeSidebar()
            // navigateTo(ExamsActivity::class.java)
        }
        navItemGpa.setOnClickListener {
            setActiveSidebarItem(navItemGpa)
            closeSidebar()
            // navigateTo(GpaActivity::class.java)
        }
        navItemSettings.setOnClickListener {
            setActiveSidebarItem(navItemSettings)
            closeSidebar()
        }
    }

    private fun setupDashboardCards() {
        cardTimetable.setOnClickListener   { /* navigateTo(TimetableActivity::class.java) */ }
        cardAssignments.setOnClickListener { /* navigateTo(AssignmentsActivity::class.java) */ }
        cardAttendance.setOnClickListener  { /* navigateTo(AttendanceActivity::class.java) */ }
        cardExams.setOnClickListener       { /* navigateTo(ExamsActivity::class.java) */ }
        cardGpa.setOnClickListener         { /* navigateTo(GpaActivity::class.java) */ }
        cardSettings.setOnClickListener    { /* Open Settings */ }
    }

    // ── Section 5: Sidebar Logic ─────────────────────────
    private fun openSidebar() {
        if (isSidebarOpen) return
        isSidebarOpen = true
        
        viewSidebarOverlay.visibility = View.VISIBLE
        viewSidebarOverlay.alpha = 0f
        viewSidebarOverlay.animate()
            .alpha(1f)
            .setDuration(SIDEBAR_ANIM_DURATION)
            .setInterpolator(DECELERATE)
            .start()

        layoutSidebar.animate()
            .translationX(0f)
            .setDuration(SIDEBAR_ANIM_DURATION)
            .setInterpolator(DECELERATE)
            .start()
    }

    private fun closeSidebar() {
        if (!isSidebarOpen) return
        isSidebarOpen = false
        
        viewSidebarOverlay.animate()
            .alpha(0f)
            .setDuration(SIDEBAR_ANIM_DURATION)
            .setInterpolator(DECELERATE)
            .withEndAction { viewSidebarOverlay.visibility = View.GONE }
            .start()

        val sidebarWidth = resources.getDimension(R.dimen.sidebar_width)
        layoutSidebar.animate()
            .translationX(-sidebarWidth)
            .setDuration(SIDEBAR_ANIM_DURATION)
            .setInterpolator(DECELERATE)
            .start()
    }

    private fun toggleSidebar() {
        if (isSidebarOpen) closeSidebar() else openSidebar()
    }

    // ── Section 6: Navigation ────────────────────────────
    private fun navigateTo(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    private fun setActiveSidebarItem(activeItem: LinearLayout?) {
        val allItems = listOf(navItemTimetable, navItemAssignments, navItemAttendance, navItemExams, navItemGpa, navItemSettings)
        allItems.forEach { item ->
            item.isSelected = (item == activeItem)
            item.findViewById<View>(R.id.viewActiveIndicator).visibility =
                if (item == activeItem) View.VISIBLE else View.INVISIBLE
            
            val label = item.findViewById<TextView>(R.id.textViewNavLabel)
            label.setTextColor(
                if (item == activeItem)
                    getColor(R.color.colorSidebarItemActive)
                else
                    getColor(R.color.colorSidebarItem)
            )
        }
    }

    override fun onBackPressed() {
        if (isSidebarOpen) {
            closeSidebar()
            return
        }
        super.onBackPressed()
    }

    // ── Section 7: Companion ─────────────────────────────
    companion object {
        private const val SIDEBAR_ANIM_DURATION = 250L
        private val DECELERATE = DecelerateInterpolator()
    }
}
