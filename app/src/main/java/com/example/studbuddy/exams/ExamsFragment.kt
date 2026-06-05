package com.example.studbuddy.exams

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
import com.example.studbuddy.core.models.Course
import com.example.studbuddy.core.models.Exam
import com.example.studbuddy.core.models.ExamType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ExamsFragment : Fragment() {

    private lateinit var rvPending: RecyclerView
    private lateinit var rvCompleted: RecyclerView
    private lateinit var pendingAdapter: ExamAdapter
    private lateinit var completedAdapter: ExamAdapter
    
    private val pendingList = mutableListOf<Exam>()
    private val completedList = mutableListOf<Exam>()

    private val viewModel: ExamsViewModel by viewModels {
        ViewModelFactory((requireActivity().application as StudBuddyApp).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_exams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerViews(view)
        observeViewModel()
    }

    private fun setupViews(view: View) {
        view.findViewById<Button>(R.id.btnAddExam).setOnClickListener {
            if (viewModel.uiState.value.courses.isEmpty()) {
                Toast.makeText(requireContext(), "Please add courses first", Toast.LENGTH_SHORT).show()
            } else {
                showExamDialog(null)
            }
        }
    }

    private fun setupRecyclerViews(view: View) {
        rvPending = view.findViewById(R.id.rvPendingExams)
        rvCompleted = view.findViewById(R.id.rvCompletedExams)

        pendingAdapter = ExamAdapter(pendingList) { exam -> showExamDialog(exam) }
        completedAdapter = ExamAdapter(completedList) { exam -> showExamDialog(exam) }

        rvPending.layoutManager = LinearLayoutManager(requireContext())
        rvPending.adapter = pendingAdapter

        rvCompleted.layoutManager = LinearLayoutManager(requireContext())
        rvCompleted.adapter = completedAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateAdapters(state.exams, state.courses)
                }
            }
        }
    }

    private fun updateAdapters(allExams: List<Exam>, allCourses: List<Course>) {
        val pending = allExams.filter { !it.isCompleted }.sortedBy { it.date }
        val completed = allExams.filter { it.isCompleted }.sortedByDescending { it.date }
        
        pendingAdapter.updateData(pending, allCourses)
        completedAdapter.updateData(completed, allCourses)
    }

    private fun showExamDialog(existing: Exam?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_exam, null)
        val spinnerCourses = dialogView.findViewById<Spinner>(R.id.spinnerCourses)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerExamType)
        val btnDate = dialogView.findViewById<Button>(R.id.btnExamDate)
        val etVenue = dialogView.findViewById<EditText>(R.id.etVenue)
        val etTotalMarks = dialogView.findViewById<EditText>(R.id.etTotalMarks)
        val etWeightage = dialogView.findViewById<EditText>(R.id.etWeightage)
        val cbCompleted = dialogView.findViewById<CheckBox>(R.id.cbExamCompleted)
        val layoutObtained = dialogView.findViewById<View>(R.id.layoutObtainedMarks)
        val etObtained = dialogView.findViewById<EditText>(R.id.etObtainedMarks)

        val courses = viewModel.uiState.value.courses
        if (courses.isEmpty()) return
        
        spinnerCourses.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses.map { it.name })
        spinnerType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ExamType.entries.map { it.name })

        val calendar = Calendar.getInstance()
        existing?.let { 
            calendar.timeInMillis = it.date
            etVenue.setText(it.venue)
            etTotalMarks.setText(it.totalMarks.toString())
            etWeightage.setText(it.weightage.toString())
            cbCompleted.isChecked = it.isCompleted
            etObtained.setText(it.obtainedMarks?.toString() ?: "")
            if (it.isCompleted) layoutObtained.visibility = View.VISIBLE
            
            val courseIdx = courses.indexOfFirst { c -> c.id == it.courseId }
            if (courseIdx != -1) spinnerCourses.setSelection(courseIdx)
            spinnerType.setSelection(it.type.ordinal)
        }

        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        btnDate.text = sdf.format(calendar.time)

        btnDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)
                TimePickerDialog(requireContext(), { _, hh, mm ->
                    calendar.set(Calendar.HOUR_OF_DAY, hh)
                    calendar.set(Calendar.MINUTE, mm)
                    btnDate.text = sdf.format(calendar.time)
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            layoutObtained.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) "Add Exam" else "Edit Exam")
            .setView(dialogView)
            .setPositiveButton(R.string.save_button, null)
            .setNegativeButton(R.string.cancel_button, null)

        if (existing != null) {
            dialogBuilder.setNeutralButton(R.string.delete_button) { _, _ -> confirmDelete(existing) }
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val selectedIdx = spinnerCourses.selectedItemPosition
            if (selectedIdx == -1) return@setOnClickListener
            
            val courseId = courses[selectedIdx].id
            val type = ExamType.entries[spinnerType.selectedItemPosition]
            val total = etTotalMarks.text.toString().toDoubleOrNull() ?: 0.0
            val weight = etWeightage.text.toString().toDoubleOrNull() ?: 0.0
            val isComp = cbCompleted.isChecked
            val obtained = if (isComp) etObtained.text.toString().toDoubleOrNull() else null

            val exam = Exam(
                id = existing?.id ?: UUID.randomUUID().toString(),
                courseId = courseId,
                type = type,
                date = calendar.timeInMillis,
                venue = etVenue.text.toString(),
                totalMarks = total,
                obtainedMarks = obtained,
                weightage = weight,
                isCompleted = isComp
            )
            viewModel.updateExam(exam)
            
            if (!exam.isCompleted) {
                scheduleExamReminder(exam)
            } else {
                cancelExamReminder(exam)
            }
            
            alertDialog.dismiss()
        }
    }

    private fun confirmDelete(exam: Exam) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Exam")
            .setMessage("Are you sure you want to delete this exam?")
            .setPositiveButton(R.string.delete_button) { _, _ ->
                cancelExamReminder(exam)
                viewModel.deleteExam(exam)
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }

    private fun scheduleExamReminder(exam: Exam) {
        val triggerTime = exam.date - TimeUnit.HOURS.toMillis(1)
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent("com.example.studbuddy.EXAM_REMINDER").apply {
            val course = viewModel.uiState.value.courses.find { it.id == exam.courseId }
            putExtra("exam_id", exam.id)
            putExtra("course_name", course?.name ?: "Unknown Course")
            putExtra("type", exam.type.name)
            putExtra("venue", exam.venue ?: "Not set")
            putExtra("time", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(exam.date)))
            `package` = requireContext().packageName
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), exam.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun cancelExamReminder(exam: Exam) {
        val intent = Intent("com.example.studbuddy.EXAM_REMINDER").apply {
            `package` = requireContext().packageName
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), exam.id.hashCode(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
        }
    }
}
