package com.example.studbuddy.core

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "studbuddy_settings"
        private const val KEY_LECTURE_REMINDERS = "lecture_reminders"
        private const val KEY_ASSIGNMENT_REMINDERS = "assignment_reminders"
        private const val KEY_EXAM_REMINDERS = "exam_reminders"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    var lectureRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_LECTURE_REMINDERS, true)
        set(value) = prefs.edit().putBoolean(KEY_LECTURE_REMINDERS, value).apply()

    var assignmentRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_ASSIGNMENT_REMINDERS, true)
        set(value) = prefs.edit().putBoolean(KEY_ASSIGNMENT_REMINDERS, value).apply()

    var examRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_EXAM_REMINDERS, true)
        set(value) = prefs.edit().putBoolean(KEY_EXAM_REMINDERS, value).apply()
        
    var darkModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
}
