package com.example.studbuddy

import android.app.Application
import com.example.studbuddy.core.AppDataStore

class StudBuddyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Data Store globally
        AppDataStore.initialize(this)
    }
}
