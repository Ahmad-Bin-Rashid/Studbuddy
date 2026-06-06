package com.example.studbuddy.gpa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class GpaFragment : Fragment() {

    private lateinit var tvSemesterGpa: TextView
    private lateinit var tvCgpa: TextView
    private lateinit var recyclerViewCourses: RecyclerView
    private lateinit var gpaAdapter: GpaAdapter

    private val viewModel: GpaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    gpaAdapter.updateList(state.courses)
                    tvSemesterGpa.text = String.format(Locale.getDefault(), "%.2f", state.calculatedGpa)
                }
            }
        }
    }
}
