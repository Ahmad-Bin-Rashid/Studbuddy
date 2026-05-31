package com.example.studbuddy

import android.app.Application
import com.example.studbuddy.core.db.StudBuddyDatabase
import com.example.studbuddy.core.repository.StudBuddyRepository

class StudBuddyApp : Application() {
    
    val database by lazy { StudBuddyDatabase.getDatabase(this) }
    val repository by lazy { StudBuddyRepository(database) }

    override fun onCreate() {
        super.onCreate()
    }
}
