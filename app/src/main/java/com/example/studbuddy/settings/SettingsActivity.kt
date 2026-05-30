package com.example.studbuddy.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import com.example.studbuddy.MainActivity
import com.example.studbuddy.R
import com.example.studbuddy.attendance.AttendanceActivity
import com.example.studbuddy.courses.CourseActivity
import com.example.studbuddy.exams.ExamsActivity
import com.example.studbuddy.gpa.GpaActivity
import com.example.studbuddy.timetable.TimetableActivity
import com.example.studbuddy.assignments.AssignmentsActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var sidebarContainer: CardView
    private lateinit var sidebarDim: View
    private lateinit var btnOpenSidebar: ImageView
    private lateinit var cbDarkMode: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupViews()
        setupSidebarNavigation()
    }

    private fun setupViews() {
        sidebarContainer = findViewById(R.id.sidebarContainer)
        sidebarDim = findViewById(R.id.sidebarDim)
        btnOpenSidebar = findViewById(R.id.btnOpenSidebar)
        cbDarkMode = findViewById(R.id.cbDarkMode)

        btnOpenSidebar.setOnClickListener { toggleSidebar(true) }
        sidebarDim.setOnClickListener { toggleSidebar(false) }

        // Dark Mode Logic
        val isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        cbDarkMode.isChecked = isDark

        cbDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setupSidebarNavigation() {
        findViewById<TextView>(R.id.navHome).setOnClickListener { navigateTo(MainActivity::class.java) }
        findViewById<TextView>(R.id.navCourses).setOnClickListener { navigateTo(CourseActivity::class.java) }
        findViewById<TextView>(R.id.navAttendance).setOnClickListener { navigateTo(AttendanceActivity::class.java) }
        findViewById<TextView>(R.id.navTimetable).setOnClickListener { navigateTo(TimetableActivity::class.java) }
        findViewById<TextView>(R.id.navAssignments).setOnClickListener { navigateTo(AssignmentsActivity::class.java) }
        findViewById<TextView>(R.id.navExams).setOnClickListener { navigateTo(ExamsActivity::class.java) }
        findViewById<TextView>(R.id.navGpa).setOnClickListener { navigateTo(GpaActivity::class.java) }
        findViewById<TextView>(R.id.navSettings).setOnClickListener { toggleSidebar(false) }
    }

    private fun navigateTo(cls: Class<*>) {
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
        finish()
    }

    private fun toggleSidebar(open: Boolean) {
        sidebarContainer.visibility = if (open) View.VISIBLE else View.GONE
        sidebarDim.visibility = if (open) View.VISIBLE else View.GONE
    }
}
