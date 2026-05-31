package com.example.studbuddy.gpa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.StudBuddyApp
import com.example.studbuddy.core.ViewModelFactory
import java.util.*

class GpaFragment : Fragment() {

    private lateinit var tvSemesterGpa: TextView
    private lateinit var tvCgpa: TextView
    private lateinit var recyclerViewCourses: RecyclerView
    private lateinit var gpaAdapter: GpaAdapter

    private val viewModel: GpaViewModel by viewModels {
        ViewModelFactory((requireActivity().application as StudBuddyApp).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gpa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView(view)
        observeViewModel()
    }

    private fun setupViews(view: View) {
        tvSemesterGpa = view.findViewById(R.id.tvSemesterGpa)
        tvCgpa = view.findViewById(R.id.tvCgpa)
    }

    private fun setupRecyclerView(view: View) {
        recyclerViewCourses = view.findViewById(R.id.recyclerViewGpaCourses)
        gpaAdapter = GpaAdapter(mutableListOf())
        recyclerViewCourses.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCourses.adapter = gpaAdapter
    }

    private fun observeViewModel() {
        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            gpaAdapter.updateList(courses)
        }
        
        viewModel.calculatedGpa.observe(viewLifecycleOwner) { gpa ->
            tvSemesterGpa.text = String.format(Locale.getDefault(), "%.2f", gpa)
        }
    }
}
