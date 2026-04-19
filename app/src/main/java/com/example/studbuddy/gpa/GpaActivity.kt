package com.example.studbuddy.gpa

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.core.BaseActivity
import com.example.studbuddy.core.AppDataStore
import java.util.*

class GpaActivity : BaseActivity() {

    private lateinit var tvSemesterGpa: TextView
    private lateinit var tvCgpa: TextView
    private lateinit var recyclerViewCourses: RecyclerView
    private lateinit var gpaAdapter: GpaAdapter

    private val gpaUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            calculateAndDisplayGpa()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpa)

        setupSidebar()
        setupViews()
        setupRecyclerView()
        calculateAndDisplayGpa()

        val filter = IntentFilter("com.example.studbuddy.GPA_UPDATE")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(gpaUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(gpaUpdateReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(gpaUpdateReceiver)
    }

    private fun setupViews() {
        tvSemesterGpa = findViewById(R.id.tvSemesterGpa)
        tvCgpa = findViewById(R.id.tvCgpa)
    }

    private fun setupRecyclerView() {
        recyclerViewCourses = findViewById(R.id.recyclerViewGpaCourses)
        gpaAdapter = GpaAdapter(mutableListOf())
        recyclerViewCourses.layoutManager = LinearLayoutManager(this)
        recyclerViewCourses.adapter = gpaAdapter
    }

    private fun calculateAndDisplayGpa() {
        val courses = AppDataStore.getCourses()
        
        var totalPoints = 0.0
        var totalCredits = 0
        
        courses.forEach { course ->
            if (course.grade != null) {
                totalPoints += course.gradePoints
                totalCredits += course.creditHours
            }
        }
        
        val gpa = if (totalCredits > 0) totalPoints / totalCredits else 0.0
        tvSemesterGpa.text = String.format(Locale.getDefault(), "%.2f", gpa)

        gpaAdapter.updateList(courses)
    }
}
