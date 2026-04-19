package com.example.studbuddy.core

import android.content.Context
import android.content.SharedPreferences

class SharedPrefManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_FILE_NAME = "studbuddy_prefs"
    }

    fun getString(key: String): String? = prefs.getString(key, null)

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        prefs.getBoolean(key, default)

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getInt(key: String, default: Int = 0): Int =
        prefs.getInt(key, default)

    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
