package com.example.studbuddy.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.StudBuddyApp
import com.example.studbuddy.core.ViewModelFactory
import com.example.studbuddy.core.models.AttendanceRecord
import com.example.studbuddy.core.models.Course
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.*

class AttendanceFragment : Fragment() {

    private lateinit var recyclerViewAttendance: RecyclerView
    private lateinit var attendanceAdapter: AttendanceAdapter
    private val courseList = mutableListOf<Course>()

    private val viewModel: AttendanceViewModel by viewModels {
        ViewModelFactory((requireActivity().application as StudBuddyApp).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_attendance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView(view)
        observeViewModel()
    }

    private fun setupViews(view: View) {
        view.findViewById<Button>(R.id.btnMarkAttendance).setOnClickListener {
            if (viewModel.uiState.value.courses.isEmpty()) {
                Toast.makeText(requireContext(), "Please add courses first", Toast.LENGTH_SHORT).show()
            } else {
                showMarkAttendanceDialog(null)
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerViewAttendance = view.findViewById(R.id.recyclerViewAttendance)
        attendanceAdapter = AttendanceAdapter(courseList) { course ->
            showAttendanceHistoryDialog(course)
        }
        recyclerViewAttendance.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewAttendance.adapter = attendanceAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    attendanceAdapter.updateData(state.courses, state.attendance)
                }
            }
        }
    }

    private fun showMarkAttendanceDialog(existingRecord: AttendanceRecord?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_mark_attendance, null)
        val spinnerCourses = dialogView.findViewById<Spinner>(R.id.spinnerCourses)
        val rgStatus = dialogView.findViewById<RadioGroup>(R.id.rgStatus)

        val courses = viewModel.uiState.value.courses
        spinnerCourses.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses.map { it.name })

        existingRecord?.let { record ->
            val courseIdx = courses.indexOfFirst { it.id == record.courseId }
            if (courseIdx != -1) {
                spinnerCourses.setSelection(courseIdx)
                spinnerCourses.isEnabled = false
            }
            when (record.status) {
                "PRESENT" -> rgStatus.check(R.id.rbPresent)
                "ABSENT" -> rgStatus.check(R.id.rbAbsent)
                "LATE" -> rgStatus.check(R.id.rbLate)
            }
        }

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existingRecord == null) "Mark Attendance" else "Update Attendance")
            .setView(dialogView)
            .setPositiveButton(R.string.save_button) { _, _ ->
                val selectedIdx = spinnerCourses.selectedItemPosition
                if (selectedIdx == -1) return@setPositiveButton
                
                val courseId = courses[selectedIdx].id
                val status = when (rgStatus.checkedRadioButtonId) {
                    R.id.rbPresent -> "PRESENT"
                    R.id.rbAbsent -> "ABSENT"
                    R.id.rbLate -> "LATE"
                    else -> "PRESENT"
                }

                val record = AttendanceRecord(
                    id = existingRecord?.id ?: UUID.randomUUID().toString(),
                    courseId = courseId,
                    dateTime = existingRecord?.dateTime ?: System.currentTimeMillis(),
                    status = status
                )
                
                if (existingRecord == null) {
                    viewModel.addAttendanceRecord(record)
                } else {
                    viewModel.updateAttendanceRecord(record)
                }
            }
            .setNegativeButton(R.string.cancel_button, null)

        if (existingRecord != null) {
            dialogBuilder.setNeutralButton(R.string.delete_button) { _, _ ->
                viewModel.deleteAttendanceRecord(existingRecord.id)
                Toast.makeText(requireContext(), "Record deleted", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBuilder.show()
    }

    private fun showAttendanceHistoryDialog(course: Course) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_attendance_history, null)
        val rvRecords = dialogView.findViewById<RecyclerView>(R.id.rvAttendanceRecords)
        val tvCourseName = dialogView.findViewById<TextView>(R.id.tvHistoryCourseName)

        tvCourseName.text = String.format(Locale.getDefault(), "History for %s", course.name)
        
        val records = viewModel.uiState.value.attendance
            .filter { it.courseId == course.id }
            .sortedByDescending { it.dateTime }
        
        val historyDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setNegativeButton(R.string.cancel_button, null)
            .create()

        val adapter = RecordAdapter(records) { record ->
            historyDialog.dismiss()
            showMarkAttendanceDialog(record)
        }
        
        rvRecords.layoutManager = LinearLayoutManager(requireContext())
        rvRecords.adapter = adapter
        
        historyDialog.show()
    }
}
