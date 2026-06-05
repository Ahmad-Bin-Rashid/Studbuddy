package com.example.studbuddy.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.studbuddy.StudBuddyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val app = context.applicationContext as StudBuddyApp
        val repository = app.repository

        scope.launch {
            val courses = repository.getCourses()
            val coursesMap = courses.associateBy { it.id }

            // Reschedule assignments
            repository.getAssignments()
                .filter { !it.isCompleted }
                .forEach { NotificationScheduler.scheduleAssignmentReminder(context, it, coursesMap[it.courseId]?.name ?: "Unknown") }

            // Reschedule exams
            repository.getExams()
                .filter { !it.isCompleted && it.date > System.currentTimeMillis() }
                .forEach { NotificationScheduler.scheduleExamReminders(context, it, coursesMap[it.courseId]?.name ?: "Unknown") }

            // Reschedule timetable
            repository.getTimetable()
                .forEach { NotificationScheduler.scheduleTimetableReminder(context, it, coursesMap[it.courseId]?.name ?: "Unknown") }
        }
    }
}
