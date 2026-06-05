package com.example.studbuddy.core.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.studbuddy.StudBuddyApp
import com.example.studbuddy.core.notifications.NotificationHelper
import com.example.studbuddy.core.notifications.NotificationScheduler
import com.example.studbuddy.core.notifications.NotificationType
import java.util.*

class DailyMaintenanceWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as StudBuddyApp
        val repository = app.repository
        val settingsManager = app.settingsManager

        // 1. Morning Briefing (Summary of today's lectures)
        if (settingsManager.morningBriefingEnabled) {
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val ourDayIndex = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
            
            val timetable = repository.getTimetable()
            val todayClasses = timetable.filter { it.dayOfWeek == ourDayIndex }
            
            if (todayClasses.isNotEmpty()) {
                val courses = repository.getCourses().associateBy { it.id }
                val summary = todayClasses.joinToString(", ") { 
                    "${courses[it.courseId]?.name ?: "Unknown"} (${it.startTime})" 
                }
                
                NotificationHelper.showNotification(
                    applicationContext,
                    NotificationType.TIMETABLE_CLASS,
                    "morning_briefing",
                    "Today's Schedule",
                    "You have classes: $summary"
                )
            }
        }

        // 2. Alarm Fail-safe (Ensure all upcoming reminders are scheduled)
        val courses = repository.getCourses().associateBy { it.id }
        
        repository.getAssignments()
            .filter { !it.isCompleted }
            .forEach { NotificationScheduler.scheduleAssignmentReminder(applicationContext, it, courses[it.courseId]?.name ?: "Unknown") }

        repository.getExams()
            .filter { !it.isCompleted && it.date > System.currentTimeMillis() }
            .forEach { NotificationScheduler.scheduleExamReminders(applicationContext, it, courses[it.courseId]?.name ?: "Unknown") }

        repository.getTimetable()
            .forEach { NotificationScheduler.scheduleTimetableReminder(applicationContext, it, courses[it.courseId]?.name ?: "Unknown") }

        return Result.success()
    }
}
