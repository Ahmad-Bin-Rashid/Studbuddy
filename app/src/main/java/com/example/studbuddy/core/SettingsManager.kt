package com.example.studbuddy.core

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "studbuddy_settings"
        const val MODE_EACH_LECTURE = "EACH_LECTURE"
        const val MODE_DAILY_SUMMARY = "DAILY_SUMMARY"

        private const val KEY_CLASS_REMINDER_MODE = "class_reminder_mode"
        private const val KEY_LECTURE_LEAD_TIME = "lecture_lead_time"
        private const val KEY_DAILY_SUMMARY_TIME = "daily_summary_time"
        private const val KEY_ASSIGNMENT_REMINDERS = "assignment_reminders"
        private const val KEY_ASSIGNMENT_LEAD_TIME = "assignment_lead_time"
        private const val KEY_EXAM_REMINDERS = "exam_reminders"
        private const val KEY_EXAM_LEAD_TIME = "exam_lead_time"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    var classReminderMode: String
        get() = prefs.getString(KEY_CLASS_REMINDER_MODE, MODE_EACH_LECTURE) ?: MODE_EACH_LECTURE
        set(value) = prefs.edit().putString(KEY_CLASS_REMINDER_MODE, value).apply()

    var lectureLeadTime: Int
        get() = prefs.getInt(KEY_LECTURE_LEAD_TIME, 15)
        set(value) = prefs.edit().putInt(KEY_LECTURE_LEAD_TIME, value).apply()

    var dailySummaryTime: String
        get() = prefs.getString(KEY_DAILY_SUMMARY_TIME, "08:00") ?: "08:00"
        set(value) = prefs.edit().putString(KEY_DAILY_SUMMARY_TIME, value).apply()

    var assignmentRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_ASSIGNMENT_REMINDERS, true)
        set(value) = prefs.edit().putBoolean(KEY_ASSIGNMENT_REMINDERS, value).apply()

    var assignmentLeadTime: Int
        get() = prefs.getInt(KEY_ASSIGNMENT_LEAD_TIME, 24)
        set(value) = prefs.edit().putInt(KEY_ASSIGNMENT_LEAD_TIME, value).apply()

    var examRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_EXAM_REMINDERS, true)
        set(value) = prefs.edit().putBoolean(KEY_EXAM_REMINDERS, value).apply()

    var examLeadTime: Int
        get() = prefs.getInt(KEY_EXAM_LEAD_TIME, 24)
        set(value) = prefs.edit().putInt(KEY_EXAM_LEAD_TIME, value).apply()

    var darkModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
}
