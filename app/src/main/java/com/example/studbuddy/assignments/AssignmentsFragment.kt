package com.example.studbuddy.assignments

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.StudBuddyApp
import com.example.studbuddy.core.ViewModelFactory
import com.example.studbuddy.core.models.Assignment
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AssignmentsFragment : Fragment() {

    private lateinit var rvPending: RecyclerView
    private lateinit var rvCompleted: RecyclerView
    private lateinit var pendingAdapter: AssignmentsAdapter
    private lateinit var completedAdapter: AssignmentsAdapter
    
    private val pendingList = mutableListOf<Assignment>()
    private val completedList = mutableListOf<Assignment>()

    private val viewModel: AssignmentsViewModel by viewModels {
        ViewModelFactory((requireActivity().application as StudBuddyApp).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_assignments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerViews(view)
        observeViewModel()
    }

    private fun setupViews(view: View) {
        view.findViewById<Button>(R.id.btnAddAssignment).setOnClickListener {
            if (viewModel.courses.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please add courses first", Toast.LENGTH_SHORT).show()
            } else {
                showAssignmentDialog(null)
            }
        }
    }

    private fun setupRecyclerViews(view: View) {
        rvPending = view.findViewById(R.id.rvPending)
        rvCompleted = view.findViewById(R.id.rvCompleted)

        pendingAdapter = AssignmentsAdapter(pendingList, { assignment, isChecked ->
            updateAssignmentStatus(assignment, isChecked)
        }, { assignment ->
            showAssignmentDialog(assignment)
        })

        completedAdapter = AssignmentsAdapter(completedList, { assignment, isChecked ->
            updateAssignmentStatus(assignment, isChecked)
        }, { assignment ->
            showAssignmentDialog(assignment)
        })

        rvPending.layoutManager = LinearLayoutManager(requireContext())
        rvPending.adapter = pendingAdapter

        rvCompleted.layoutManager = LinearLayoutManager(requireContext())
        rvCompleted.adapter = completedAdapter
    }

    private fun observeViewModel() {
        viewModel.assignments.observe(viewLifecycleOwner) { allAssignments ->
            updateAdapters(allAssignments, viewModel.courses.value ?: emptyList())
        }
        viewModel.courses.observe(viewLifecycleOwner) { allCourses ->
            updateAdapters(viewModel.assignments.value ?: emptyList(), allCourses)
        }
    }

    private fun updateAdapters(allAssignments: List<Assignment>, allCourses: List<com.example.studbuddy.core.models.Course>) {
        val pending = allAssignments.filter { !it.isCompleted }.sortedBy { it.dueDate }
        val completed = allAssignments.filter { it.isCompleted }.sortedByDescending { it.dueDate }
        
        pendingAdapter.updateData(pending, allCourses)
        completedAdapter.updateData(completed, allCourses)
    }

    private fun updateAssignmentStatus(assignment: Assignment, isCompleted: Boolean) {
        val updated = assignment.copy(isCompleted = isCompleted)
        viewModel.updateAssignment(updated)
        
        if (isCompleted) {
            cancelReminder(updated)
        } else {
            scheduleReminder(updated)
        }
    }

    private fun showAssignmentDialog(existing: Assignment?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_assignment, null)
        val spinnerCourses = dialogView.findViewById<Spinner>(R.id.spinnerCourses)
        val etName = dialogView.findViewById<EditText>(R.id.etAssignmentTitle)
        val etTotal = dialogView.findViewById<EditText>(R.id.etTotalMarks)
        val etWeight = dialogView.findViewById<EditText>(R.id.etWeightage)
        val btnDate = dialogView.findViewById<Button>(R.id.btnDueDate)
        val layoutObtained = dialogView.findViewById<View>(R.id.layoutObtainedMarks)
        val etObtained = dialogView.findViewById<EditText>(R.id.etObtainedMarks)

        val courses = viewModel.courses.value ?: emptyList()
        val courseNames = courses.map { it.name }
        spinnerCourses.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courseNames)

        var selectedDate = existing?.dueDate ?: System.currentTimeMillis()
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        if (existing != null) {
            etName.setText(existing.name)
            etTotal.setText(existing.totalMarks.toString())
            etWeight.setText(existing.weightage.toString())
            btnDate.text = sdf.format(Date(existing.dueDate))
            val courseIdx = courses.indexOfFirst { it.id == existing.courseId }
            if (courseIdx != -1) spinnerCourses.setSelection(courseIdx)
            
            if (existing.isCompleted) {
                layoutObtained.visibility = View.VISIBLE
                etObtained.setText(existing.obtainedMarks?.toString() ?: "")
            }
        }

        btnDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = selectedDate
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val selected = Calendar.getInstance()
                selected.set(y, m, d)
                selectedDate = selected.timeInMillis
                btnDate.text = sdf.format(selected.time)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) "Add Assignment" else "Edit Assignment")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
        
        if (existing != null) {
            dialogBuilder.setNeutralButton("Delete") { _, _ ->
                confirmDelete(existing)
            }
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val selectedIdx = spinnerCourses.selectedItemPosition
            if (selectedIdx == -1) return@setOnClickListener
            
            val courseId = courses[selectedIdx].id
            val name = etName.text.toString().trim()
            val total = etTotal.text.toString().toDoubleOrNull() ?: 0.0
            val weight = etWeight.text.toString().toDoubleOrNull() ?: 0.0
            val obtained = etObtained.text.toString().toDoubleOrNull()

            if (name.isNotEmpty()) {
                val assignment = Assignment(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    name = name,
                    courseId = courseId,
                    dueDate = selectedDate,
                    totalMarks = total,
                    obtainedMarks = obtained,
                    weightage = weight,
                    isCompleted = existing?.isCompleted ?: false
                )
                viewModel.updateAssignment(assignment)
                
                if (!assignment.isCompleted) {
                    scheduleReminder(assignment)
                }
                alertDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter assignment name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete(assignment: Assignment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Assignment")
            .setMessage("Are you sure you want to delete this assignment?")
            .setPositiveButton("Delete") { _, _ ->
                cancelReminder(assignment)
                viewModel.deleteAssignment(assignment)
                Toast.makeText(requireContext(), "Assignment deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun scheduleReminder(assignment: Assignment) {
        val triggerTime = assignment.dueDate - TimeUnit.HOURS.toMillis(24)
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent("com.example.studbuddy.ASSIGNMENT_REMINDER").apply {
            putExtra("assignment_id", assignment.id)
            putExtra("name", assignment.name)
            `package` = requireContext().packageName
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), assignment.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun cancelReminder(assignment: Assignment) {
        val intent = Intent("com.example.studbuddy.ASSIGNMENT_REMINDER").apply {
            `package` = requireContext().packageName
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), assignment.id.hashCode(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
        }
    }
}
