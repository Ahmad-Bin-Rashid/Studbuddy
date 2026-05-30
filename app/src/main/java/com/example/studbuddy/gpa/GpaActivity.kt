package com.example.studbuddy.gpa

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.StudBuddyApp
import com.example.studbuddy.core.BaseActivity
import com.example.studbuddy.core.ViewModelFactory
import java.util.*

class GpaActivity : BaseActivity() {

    private lateinit var tvSemesterGpa: TextView
    private lateinit var tvCgpa: TextView
    private lateinit var recyclerViewCourses: RecyclerView
    private lateinit var gpaAdapter: GpaAdapter

    private val viewModel: GpaViewModel by viewModels {
        ViewModelFactory((application as StudBuddyApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpa)

        setupSidebar()
        setupViews()
        setupRecyclerView()
        observeViewModel()
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

    private fun observeViewModel() {
        viewModel.courses.observe(this) { courses ->
            gpaAdapter.updateList(courses)
        }
        
        viewModel.calculatedGpa.observe(this) { gpa ->
            tvSemesterGpa.text = String.format(Locale.getDefault(), "%.2f", gpa)
        }
    }
}
