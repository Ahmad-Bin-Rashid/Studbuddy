package com.example.studbuddy.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.studbuddy.R
import com.example.studbuddy.StudBuddyApp

class SettingsFragment : Fragment() {

    private lateinit var cbDarkMode: CheckBox
    private lateinit var cbLectureReminders: CheckBox
    private lateinit var cbAssignmentReminders: CheckBox
    private lateinit var cbExamReminders: CheckBox

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsManager = (requireActivity().application as StudBuddyApp).settingsManager

        cbDarkMode = view.findViewById(R.id.cbDarkMode)
        cbLectureReminders = view.findViewById(R.id.cbLectureReminders)
        cbAssignmentReminders = view.findViewById(R.id.cbAssignmentReminders)
        cbExamReminders = view.findViewById(R.id.cbExamReminders)

        // Initialize checkboxes from SettingsManager
        cbDarkMode.isChecked = settingsManager.darkModeEnabled
        cbLectureReminders.isChecked = settingsManager.lectureRemindersEnabled
        cbAssignmentReminders.isChecked = settingsManager.assignmentRemindersEnabled
        cbExamReminders.isChecked = settingsManager.examRemindersEnabled

        cbDarkMode.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.darkModeEnabled = isChecked
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        cbLectureReminders.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.lectureRemindersEnabled = isChecked
        }

        cbAssignmentReminders.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.assignmentRemindersEnabled = isChecked
        }

        cbExamReminders.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.examRemindersEnabled = isChecked
        }
    }
}
