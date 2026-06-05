package com.example.studbuddy

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.studbuddy.core.SettingsManager
import com.example.studbuddy.core.db.StudBuddyDatabase
import com.example.studbuddy.core.repository.StudBuddyRepository
import com.example.studbuddy.core.workers.DailyMaintenanceWorker
import java.util.*
import java.util.concurrent.TimeUnit

class StudBuddyApp : Application() {
    
    val database by lazy { StudBuddyDatabase.getDatabase(this) }
    val repository by lazy { StudBuddyRepository(database) }
    val settingsManager by lazy { SettingsManager(this) }

    override fun onCreate() {
        super.onCreate()
        scheduleDailyMaintenance()
    }

    private fun scheduleDailyMaintenance() {
        val maintenanceWork = PeriodicWorkRequestBuilder<DailyMaintenanceWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_maintenance",
            ExistingPeriodicWorkPolicy.KEEP,
            maintenanceWork
        )
    }

    private fun calculateInitialDelay(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        // Target: 8:00 AM
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(calendar)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return target.timeInMillis - now
    }
}
