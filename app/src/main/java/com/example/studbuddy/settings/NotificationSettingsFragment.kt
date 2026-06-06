package com.example.studbuddy.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.studbuddy.R
import com.example.studbuddy.StudBuddyApp
import com.example.studbuddy.core.SettingsManager
import com.example.studbuddy.core.notifications.NotificationScheduler
import kotlinx.coroutines.launch
import java.util.*

class NotificationSettingsFragment : Fragment() {

    private lateinit var rgClassReminderMode: RadioGroup
    private lateinit var rbEachLecture: RadioButton
    private lateinit var rbDailySummary: RadioButton
    private lateinit var layoutEachLecture: View
    private lateinit var layoutDailySummary: View
    private lateinit var btnLectureLeadTime: Button
    private lateinit var btnDailySummaryTime: Button
    
    private lateinit var cbAssignmentReminders: CheckBox
    private lateinit var layoutAssignmentLeadTime: View
    private lateinit var btnAssignmentLeadTime: Button
    
    private lateinit var cbExamReminders: CheckBox
    private lateinit var layoutExamLeadTime: View
    private lateinit var btnExamLeadTime: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notification_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsManager = (requireActivity().application as StudBuddyApp).settingsManager

        rgClassReminderMode = view.findViewById(R.id.rgClassReminderMode)
        rbEachLecture = view.findViewById(R.id.rbEachLecture)
        rbDailySummary = view.findViewById(R.id.rbDailySummary)
        layoutEachLecture = view.findViewById(R.id.layoutEachLectureSettings)
        layoutDailySummary = view.findViewById(R.id.layoutDailySummarySettings)
        btnLectureLeadTime = view.findViewById(R.id.btnLectureLeadTime)
        btnDailySummaryTime = view.findViewById(R.id.btnDailySummaryTime)
        
        cbAssignmentReminders = view.findViewById(R.id.cbAssignmentReminders)
        layoutAssignmentLeadTime = view.findViewById(R.id.layoutAssignmentLeadTime)
        btnAssignmentLeadTime = view.findViewById(R.id.btnAssignmentLeadTime)
        
        cbExamReminders = view.findViewById(R.id.cbExamReminders)
        layoutExamLeadTime = view.findViewById(R.id.layoutExamLeadTime)
        btnExamLeadTime = view.findViewById(R.id.btnExamLeadTime)

        // Initialize from SettingsManager
        if (settingsManager.classReminderMode == SettingsManager.MODE_EACH_LECTURE) {
            rbEachLecture.isChecked = true
            layoutEachLecture.visibility = View.VISIBLE
            layoutDailySummary.visibility = View.GONE
        } else {
            rbDailySummary.isChecked = true
            layoutEachLecture.visibility = View.GONE
            layoutDailySummary.visibility = View.VISIBLE
        }
        btnLectureLeadTime.text = "${settingsManager.lectureLeadTime} minutes"
        btnDailySummaryTime.text = "At ${settingsManager.dailySummaryTime}"
        
        cbAssignmentReminders.isChecked = settingsManager.assignmentRemindersEnabled
        layoutAssignmentLeadTime.visibility = if (settingsManager.assignmentRemindersEnabled) View.VISIBLE else View.GONE
        btnAssignmentLeadTime.text = formatLeadTime(settingsManager.assignmentLeadTime)
        
        cbExamReminders.isChecked = settingsManager.examRemindersEnabled
        layoutExamLeadTime.visibility = if (settingsManager.examRemindersEnabled) View.VISIBLE else View.GONE
        btnExamLeadTime.text = formatLeadTime(settingsManager.examLeadTime)

        rgClassReminderMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbEachLecture -> {
                    settingsManager.classReminderMode = SettingsManager.MODE_EACH_LECTURE
                    layoutEachLecture.visibility = View.VISIBLE
                    layoutDailySummary.visibility = View.GONE
                    rescheduleClassReminders()
                }
                R.id.rbDailySummary -> {
                    settingsManager.classReminderMode = SettingsManager.MODE_DAILY_SUMMARY
                    layoutEachLecture.visibility = View.GONE
                    layoutDailySummary.visibility = View.VISIBLE
                    rescheduleClassReminders()
                }
            }
        }

        btnLectureLeadTime.setOnClickListener {
            showLeadTimeDialog(settingsManager)
        }

        btnDailySummaryTime.setOnClickListener {
            showSummaryTimeDialog(settingsManager)
        }

        cbAssignmentReminders.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.assignmentRemindersEnabled = isChecked
            layoutAssignmentLeadTime.visibility = if (isChecked) View.VISIBLE else View.GONE
            rescheduleAssignmentReminders()
        }

        btnAssignmentLeadTime.setOnClickListener {
            showAssignmentLeadTimeDialog(settingsManager)
        }

        cbExamReminders.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.examRemindersEnabled = isChecked
            layoutExamLeadTime.visibility = if (isChecked) View.VISIBLE else View.GONE
            rescheduleExamReminders()
        }

        btnExamLeadTime.setOnClickListener {
            showExamLeadTimeDialog(settingsManager)
        }
    }

    private fun formatLeadTime(hours: Int): String {
        return when {
            hours >= 168 -> "${hours / 168} week"
            hours >= 24 -> "${hours / 24} day"
            else -> "$hours hours"
        }
    }

    private fun showLeadTimeDialog(settingsManager: SettingsManager) {
        val options = arrayOf("5 minutes", "10 minutes", "15 minutes", "30 minutes", "1 hour")
        val values = intArrayOf(5, 10, 15, 30, 60)
        
        var currentSelection = values.indexOf(settingsManager.lectureLeadTime)
        if (currentSelection == -1) currentSelection = 2 // Default to 15

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Set Lead Time")
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                settingsManager.lectureLeadTime = values[which]
                btnLectureLeadTime.text = options[which]
                rescheduleClassReminders()
                dialog.dismiss()
            }
            .show()
    }

    private fun showAssignmentLeadTimeDialog(settingsManager: SettingsManager) {
        val options = arrayOf("12 hours", "1 day", "2 days", "3 days", "1 week")
        val values = intArrayOf(12, 24, 48, 72, 168)
        
        var currentSelection = values.indexOf(settingsManager.assignmentLeadTime)
        if (currentSelection == -1) currentSelection = 1 // Default to 24

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Set Lead Time")
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                settingsManager.assignmentLeadTime = values[which]
                btnAssignmentLeadTime.text = options[which]
                rescheduleAssignmentReminders()
                dialog.dismiss()
            }
            .show()
    }

    private fun showExamLeadTimeDialog(settingsManager: SettingsManager) {
        val options = arrayOf("1 hour", "6 hours", "12 hours", "1 day", "2 days")
        val values = intArrayOf(1, 6, 12, 24, 48)
        
        var currentSelection = values.indexOf(settingsManager.examLeadTime)
        if (currentSelection == -1) currentSelection = 3 // Default to 24

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Set Lead Time")
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                settingsManager.examLeadTime = values[which]
                btnExamLeadTime.text = options[which]
                rescheduleExamReminders()
                dialog.dismiss()
            }
            .show()
    }

    private fun showSummaryTimeDialog(settingsManager: SettingsManager) {
        val currentTime = settingsManager.dailySummaryTime.split(":")
        val h = currentTime[0].toInt()
        val m = currentTime[1].toInt()

        TimePickerDialog(requireContext(), { _, hour, minute ->
            val timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            settingsManager.dailySummaryTime = timeStr
            btnDailySummaryTime.text = "At $timeStr"
            (requireActivity().application as StudBuddyApp).scheduleDailyMaintenance()
        }, h, m, true).show()
    }

    private fun rescheduleClassReminders() {
        lifecycleScope.launch {
            NotificationScheduler.rescheduleAllClassReminders(requireContext())
        }
    }

    private fun rescheduleAssignmentReminders() {
        lifecycleScope.launch {
            NotificationScheduler.rescheduleAllAssignmentReminders(requireContext())
        }
    }

    private fun rescheduleExamReminders() {
        lifecycleScope.launch {
            NotificationScheduler.rescheduleAllExamReminders(requireContext())
        }
    }
}
