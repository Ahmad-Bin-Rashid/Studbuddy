package com.example.studbuddy.core

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.studbuddy.MainActivity
import com.example.studbuddy.R
import com.example.studbuddy.assignments.AssignmentsActivity
import com.example.studbuddy.attendance.AttendanceActivity
import com.example.studbuddy.courses.CourseActivity
import com.example.studbuddy.exams.ExamsActivity
import com.example.studbuddy.gpa.GpaActivity
import com.example.studbuddy.settings.SettingsActivity
import com.example.studbuddy.timetable.TimetableActivity

abstract class BaseActivity : AppCompatActivity() {

    private var sidebarContainer: CardView? = null
    private var sidebarDim: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun setupSidebar() {
        sidebarContainer = findViewById(R.id.sidebarContainer)
        sidebarDim = findViewById(R.id.sidebarDim)
        val btnOpenSidebar = findViewById<ImageView>(R.id.btnOpenSidebar)

        btnOpenSidebar?.setOnClickListener { toggleSidebar(true) }
        sidebarDim?.setOnClickListener { toggleSidebar(false) }

        findViewById<TextView>(R.id.navHome)?.setOnClickListener { navigateTo(MainActivity::class.java) }
        findViewById<TextView>(R.id.navCourses)?.setOnClickListener { navigateTo(CourseActivity::class.java) }
        findViewById<TextView>(R.id.navAttendance)?.setOnClickListener { navigateTo(AttendanceActivity::class.java) }
        findViewById<TextView>(R.id.navTimetable)?.setOnClickListener { navigateTo(TimetableActivity::class.java) }
        findViewById<TextView>(R.id.navAssignments)?.setOnClickListener { navigateTo(AssignmentsActivity::class.java) }
        findViewById<TextView>(R.id.navExams)?.setOnClickListener { navigateTo(ExamsActivity::class.java) }
        findViewById<TextView>(R.id.navGpa)?.setOnClickListener { navigateTo(GpaActivity::class.java) }
        findViewById<TextView>(R.id.navSettings)?.setOnClickListener { navigateTo(SettingsActivity::class.java) }
    }

    protected fun navigateTo(cls: Class<*>) {
        if (this::class.java == cls) {
            toggleSidebar(false)
            return
        }
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
        toggleSidebar(false)
    }

    protected fun toggleSidebar(open: Boolean) {
        sidebarContainer?.visibility = if (open) View.VISIBLE else View.GONE
        sidebarDim?.visibility = if (open) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        if (sidebarContainer?.visibility == View.VISIBLE) {
            toggleSidebar(false)
        } else {
            super.onBackPressed()
        }
    }
}
