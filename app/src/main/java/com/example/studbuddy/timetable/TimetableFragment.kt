package com.example.studbuddy.timetable

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.StudBuddyApp
import com.example.studbuddy.core.ViewModelFactory
import com.example.studbuddy.core.models.TimetableEntry
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class TimetableFragment : Fragment() {

    private lateinit var recyclerViewTimetable: RecyclerView
    private lateinit var dayTimetableAdapter: DayTimetableAdapter

    private val viewModel: TimetableViewModel by viewModels {
        ViewModelFactory((requireActivity().application as StudBuddyApp).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timetable, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        observeViewModel()
    }

    private fun setupViews(view: View) {
        view.findViewById<Button>(R.id.btnAddTimetableEntry).setOnClickListener {
            if (viewModel.courses.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please add courses first", Toast.LENGTH_SHORT).show()
            } else {
                showTimetableDialog(null)
            }
        }
        
        recyclerViewTimetable = view.findViewById(R.id.recyclerViewTimetable)
        recyclerViewTimetable.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewModel.timetable.observe(viewLifecycleOwner) { entries ->
            updateAdapter(entries, viewModel.courses.value ?: emptyList())
        }
        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            updateAdapter(viewModel.timetable.value ?: emptyList(), courses)
        }
    }

    private fun updateAdapter(entries: List<TimetableEntry>, courses: List<com.example.studbuddy.core.models.Course>) {
        val dayMap = entries.groupBy { it.dayOfWeek }
        dayTimetableAdapter = DayTimetableAdapter(dayMap, courses) { entry ->
            showTimetableDialog(entry)
        }
        recyclerViewTimetable.adapter = dayTimetableAdapter
    }

    private fun showTimetableDialog(existing: TimetableEntry?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_timetable, null)
        val spinnerCourses = dialogView.findViewById<Spinner>(R.id.spinnerCourses)
        val spinnerDay = dialogView.findViewById<Spinner>(R.id.spinnerDay)
        val btnStart = dialogView.findViewById<Button>(R.id.btnStartTime)
        val btnEnd = dialogView.findViewById<Button>(R.id.btnEndTime)
        val etRoom = dialogView.findViewById<EditText>(R.id.etRoom)

        val courses = viewModel.courses.value ?: emptyList()
        spinnerCourses.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses.map { it.name })

        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        spinnerDay.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, days)

        var startTime = existing?.startTime ?: "09:00"
        var endTime = existing?.endTime ?: "10:00"

        btnStart.text = startTime
        btnEnd.text = endTime

        if (existing != null) {
            val courseIdx = courses.indexOfFirst { it.id == existing.courseId }
            if (courseIdx != -1) spinnerCourses.setSelection(courseIdx)
            spinnerDay.setSelection(existing.dayOfWeek - 1)
            etRoom.setText(existing.room)
        }

        btnStart.setOnClickListener {
            val parts = startTime.split(":")
            TimePickerDialog(requireContext(), { _, h, m ->
                startTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                btnStart.text = startTime
            }, parts[0].toInt(), parts[1].toInt(), true).show()
        }

        btnEnd.setOnClickListener {
            val parts = endTime.split(":")
            TimePickerDialog(requireContext(), { _, h, m ->
                endTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                btnEnd.text = endTime
            }, parts[0].toInt(), parts[1].toInt(), true).show()
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) "Add Class" else "Edit Class")
            .setView(dialogView)
            .setPositiveButton(R.string.save_button, null)
            .setNegativeButton(R.string.cancel_button, null)

        if (existing != null) {
            dialog.setNeutralButton(R.string.delete_button) { _, _ ->
                viewModel.deleteTimetableEntry(existing.id)
                Toast.makeText(requireContext(), "Class deleted", Toast.LENGTH_SHORT).show()
            }
        }

        val alertDialog = dialog.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val selectedCourseIndex = spinnerCourses.selectedItemPosition
            if (selectedCourseIndex == -1) return@setOnClickListener
            
            val courseId = courses[selectedCourseIndex].id
            val day = spinnerDay.selectedItemPosition + 1
            val room = etRoom.text.toString().trim()

            if (room.isNotEmpty()) {
                val entry = TimetableEntry(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    courseId = courseId,
                    dayOfWeek = day,
                    startTime = startTime,
                    endTime = endTime,
                    room = room,
                    color = existing?.color ?: "#1565C0"
                )
                
                if (existing == null) {
                    viewModel.addTimetableEntry(entry)
                } else {
                    viewModel.updateTimetableEntry(entry)
                }
                alertDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a room/place", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
