package com.example.studbuddy.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.studbuddy.StudBuddyApp
import com.example.studbuddy.core.models.Assignment
import com.example.studbuddy.core.models.Exam
import com.example.studbuddy.core.models.TimetableEntry
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleAssignmentReminder(context: Context, assignment: Assignment, courseName: String) {
        val settingsManager = (context.applicationContext as StudBuddyApp).settingsManager
        if (!settingsManager.assignmentRemindersEnabled) return

        val triggerTime = assignment.dueDate - TimeUnit.HOURS.toMillis(24)
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = createBaseIntent(context, NotificationType.ASSIGNMENT_DUE, assignment.id).apply {
            putExtra(AlarmReceiver.EXTRA_TITLE, "Assignment Due Soon")
            putExtra(AlarmReceiver.EXTRA_BODY, "${assignment.name} ($courseName) is due in 24 hours.")
        }

        setAlarm(context, triggerTime, intent, getRequestCode(NotificationType.ASSIGNMENT_DUE, assignment.id))
    }

    fun scheduleExamReminders(context: Context, exam: Exam, courseName: String) {
        val settingsManager = (context.applicationContext as StudBuddyApp).settingsManager
        if (!settingsManager.examRemindersEnabled) return

        // 24h before
        scheduleExamAlarm(context, exam, courseName, TimeUnit.HOURS.toMillis(24), "24h")
        // 1h before
        scheduleExamAlarm(context, exam, courseName, TimeUnit.HOURS.toMillis(1), "1h")
    }

    private fun scheduleExamAlarm(context: Context, exam: Exam, courseName: String, offsetMs: Long, suffix: String) {
        val triggerTime = exam.date - offsetMs
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = createBaseIntent(context, NotificationType.EXAM_REMINDER, "${exam.id}_$suffix").apply {
            putExtra(AlarmReceiver.EXTRA_TITLE, "Upcoming Exam")
            val leadTime = if (offsetMs == TimeUnit.HOURS.toMillis(24)) "24 hours" else "1 hour"
            putExtra(AlarmReceiver.EXTRA_BODY, "${exam.type} for $courseName in $leadTime.")
        }

        setAlarm(context, triggerTime, intent, getRequestCode(NotificationType.EXAM_REMINDER, "${exam.id}_$suffix"))
    }

    fun scheduleTimetableReminder(context: Context, entry: TimetableEntry, courseName: String) {
        val settingsManager = (context.applicationContext as StudBuddyApp).settingsManager
        if (!settingsManager.lectureRemindersEnabled) return

        val nextClassTime = getNextClassTime(entry.dayOfWeek, entry.startTime) ?: return
        val triggerTime = nextClassTime - TimeUnit.MINUTES.toMillis(15)
        
        // If 15m before class is already passed for TODAY, getNextClassTime will already return next week.
        // But double check
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = createBaseIntent(context, NotificationType.TIMETABLE_CLASS, entry.id).apply {
            putExtra(AlarmReceiver.EXTRA_TITLE, "Class Reminder")
            putExtra(AlarmReceiver.EXTRA_BODY, "$courseName starts in 15 minutes in room ${entry.room}.")
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(NotificationType.TIMETABLE_CLASS, entry.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context, type: NotificationType, itemId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(type, itemId),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun createBaseIntent(context: Context, type: NotificationType, itemId: String): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_TYPE, type.name)
            putExtra(AlarmReceiver.EXTRA_ITEM_ID, itemId)
        }
    }

    private fun setAlarm(context: Context, triggerTime: Long, intent: Intent, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun getRequestCode(type: NotificationType, itemId: String): Int {
        return (type.ordinal * 100_000 + itemId.hashCode()).and(0x7FFFFFFF)
    }

    private fun getNextClassTime(dayOfWeek: Int, startTime: String): Long? {
        return try {
            val parts = startTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                // Calendar.MONDAY = 2, SUNDAY = 1.
                // Our dayOfWeek: 1=Mon...7=Sun
                var calendarDay = dayOfWeek + 1
                if (calendarDay > 7) calendarDay = 1
                
                set(Calendar.DAY_OF_WEEK, calendarDay)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                if (before(now) || timeInMillis <= now.timeInMillis) {
                    add(Calendar.DAY_OF_YEAR, 7)
                }
            }
            target.timeInMillis
        } catch (e: Exception) {
            null
        }
    }
}
