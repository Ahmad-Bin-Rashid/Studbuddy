package com.example.studbuddy

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.studbuddy.core.SettingsManager
import com.example.studbuddy.core.db.StudBuddyDatabase
import com.example.studbuddy.core.repository.StudBuddyRepository
import com.example.studbuddy.core.workers.DailyMaintenanceWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

class StudBuddyApp : Application() {
    
    val database by lazy { StudBuddyDatabase.getDatabase(this) }
    val repository by lazy { StudBuddyRepository(database) }
    val settingsManager by lazy { SettingsManager(this) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        scheduleDailyMaintenance()
    }

    fun scheduleDailyMaintenance() {
        applicationScope.launch {
            val summaryTime = settingsManager.dailySummaryTime.first()
            val maintenanceWork = PeriodicWorkRequestBuilder<DailyMaintenanceWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(calculateInitialDelay(summaryTime), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(this@StudBuddyApp).enqueueUniquePeriodicWork(
                "daily_maintenance",
                ExistingPeriodicWorkPolicy.KEEP,
                maintenanceWork
            )
        }
    }

    private fun calculateInitialDelay(summaryTime: String): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        val timeParts = summaryTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(calendar)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return target.timeInMillis - now
    }
}
