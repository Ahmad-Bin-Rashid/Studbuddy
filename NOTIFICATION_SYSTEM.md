# FILE: NOTIFICATION_SYSTEM.md

# StudBuddy — Notification System

---

## 1. Overview

The notification system uses `AlarmManager` to schedule time-based alerts. When an alarm fires, the `AlarmReceiver` BroadcastReceiver is triggered, which builds and posts a notification via `NotificationHelper`.

```
Module Activity
    → NotificationScheduler.schedule<Type>Notification(context, item)
        → AlarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, triggerTime, pendingIntent)
            → [time passes]
        → AlarmReceiver.onReceive(context, intent)
            → NotificationHelper.showNotification(context, type, extras)
                → NotificationCompat.Builder
                → NotificationManager.notify()
```

---

## 2. Notification Types Enum

```kotlin
// File: com/studbuddy/notifications/NotificationHelper.kt
enum class NotificationType(val channelId: String, val channelName: String) {
    ASSIGNMENT_DUE(
        channelId   = "channel_assignments",
        channelName = "Assignment Deadlines"
    ),
    EXAM_REMINDER(
        channelId   = "channel_exams",
        channelName = "Exam Reminders"
    ),
    TIMETABLE_CLASS(
        channelId   = "channel_timetable",
        channelName = "Class Reminders"
    ),
    ATTENDANCE_WARNING(
        channelId   = "channel_attendance",
        channelName = "Attendance Warnings"
    )
}
```

---

## 3. Notification Channels

Channels are created in `NotificationHelper.createChannels(context)`. This must be called once in `MainActivity.onCreate()`.

```kotlin
object NotificationHelper {

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            NotificationType.values().forEach { type ->
                val importance = when (type) {
                    NotificationType.ASSIGNMENT_DUE   -> NotificationManager.IMPORTANCE_HIGH
                    NotificationType.EXAM_REMINDER    -> NotificationManager.IMPORTANCE_HIGH
                    NotificationType.TIMETABLE_CLASS  -> NotificationManager.IMPORTANCE_DEFAULT
                    NotificationType.ATTENDANCE_WARNING -> NotificationManager.IMPORTANCE_HIGH
                }
                val channel = NotificationChannel(
                    type.channelId,
                    type.channelName,
                    importance
                ).apply {
                    description = "StudBuddy ${type.channelName}"
                    enableVibration(true)
                    enableLights(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}
```

---

## 4. Intent Extras Contract

All Intent extras passed to/from `AlarmReceiver` use these constants — defined in `AlarmReceiver.kt`:

```kotlin
// File: com/studbuddy/notifications/AlarmReceiver.kt
object AlarmReceiver {
    // Required in ALL alarm intents
    const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"  // String: NotificationType.name
    const val EXTRA_ITEM_ID           = "extra_item_id"            // String: UUID of the item

    // Required for ASSIGNMENT_DUE
    const val EXTRA_ASSIGNMENT_TITLE  = "extra_assignment_title"   // String
    const val EXTRA_ASSIGNMENT_COURSE = "extra_assignment_course"  // String
    const val EXTRA_DUE_DATE          = "extra_due_date"           // Long: epoch ms

    // Required for EXAM_REMINDER
    const val EXTRA_EXAM_COURSE       = "extra_exam_course"        // String
    const val EXTRA_EXAM_TYPE         = "extra_exam_type"          // String
    const val EXTRA_EXAM_DATE         = "extra_exam_date"          // Long: epoch ms

    // Required for TIMETABLE_CLASS
    const val EXTRA_CLASS_COURSE      = "extra_class_course"       // String
    const val EXTRA_CLASS_ROOM        = "extra_class_room"         // String
    const val EXTRA_CLASS_TIME        = "extra_class_time"         // String: "HH:mm"

    // Required for ATTENDANCE_WARNING
    const val EXTRA_COURSE_NAME       = "extra_course_name"        // String
    const val EXTRA_ATTENDANCE_PCT    = "extra_attendance_pct"     // Double (as String)
}
```

---

## 5. NotificationScheduler

```kotlin
// File: com/studbuddy/notifications/NotificationScheduler.kt
object NotificationScheduler {

    // ── Assignment Due Reminder ────────────────────────────────────
    // Fires 24 hours before due date
    fun scheduleAssignmentNotification(context: Context, assignment: Assignment) {
        if (assignment.isCompleted) return
        val triggerTime = assignment.dueDate - TimeUnit.HOURS.toMillis(24)
        if (triggerTime <= System.currentTimeMillis()) return  // Already past

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_TYPE, NotificationType.ASSIGNMENT_DUE.name)
            putExtra(AlarmReceiver.EXTRA_ITEM_ID, assignment.id)
            putExtra(AlarmReceiver.EXTRA_ASSIGNMENT_TITLE, assignment.title)
            putExtra(AlarmReceiver.EXTRA_ASSIGNMENT_COURSE, assignment.courseName)
            putExtra(AlarmReceiver.EXTRA_DUE_DATE, assignment.dueDate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(NotificationType.ASSIGNMENT_DUE, assignment.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelAssignmentNotification(context: Context, assignmentId: String) {
        cancelAlarm(context, NotificationType.ASSIGNMENT_DUE, assignmentId)
    }

    // ── Exam Reminder ──────────────────────────────────────────────
    // Fires 24 hours before exam AND 1 hour before exam
    fun scheduleExamNotification(context: Context, exam: Exam) {
        scheduleSingleExamAlarm(context, exam, offsetMs = TimeUnit.HOURS.toMillis(24), suffix = "24h")
        scheduleSingleExamAlarm(context, exam, offsetMs = TimeUnit.HOURS.toMillis(1),  suffix = "1h")
    }

    private fun scheduleSingleExamAlarm(context: Context, exam: Exam, offsetMs: Long, suffix: String) {
        val triggerTime = exam.examDate - offsetMs
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_TYPE, NotificationType.EXAM_REMINDER.name)
            putExtra(AlarmReceiver.EXTRA_ITEM_ID, "${exam.id}_$suffix")
            putExtra(AlarmReceiver.EXTRA_EXAM_COURSE, exam.courseName)
            putExtra(AlarmReceiver.EXTRA_EXAM_TYPE, exam.examType)
            putExtra(AlarmReceiver.EXTRA_EXAM_DATE, exam.examDate)
        }

        val requestCode = getRequestCode(NotificationType.EXAM_REMINDER, "${exam.id}_$suffix")
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        setExactAlarm(alarmManager, triggerTime, pendingIntent)
    }

    fun cancelExamNotification(context: Context, examId: String) {
        cancelAlarm(context, NotificationType.EXAM_REMINDER, "${examId}_24h")
        cancelAlarm(context, NotificationType.EXAM_REMINDER, "${examId}_1h")
    }

    // ── Timetable Class Reminder ───────────────────────────────────
    // Fires 15 minutes before each class — repeats weekly
    fun scheduleTimetableNotification(context: Context, entry: TimetableEntry) {
        val nextClassTime = getNextClassTime(entry.dayOfWeek, entry.startTime) ?: return
        val triggerTime = nextClassTime - TimeUnit.MINUTES.toMillis(15)
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_TYPE, NotificationType.TIMETABLE_CLASS.name)
            putExtra(AlarmReceiver.EXTRA_ITEM_ID, entry.id)
            putExtra(AlarmReceiver.EXTRA_CLASS_COURSE, entry.courseName)
            putExtra(AlarmReceiver.EXTRA_CLASS_ROOM, entry.room)
            putExtra(AlarmReceiver.EXTRA_CLASS_TIME, entry.startTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(NotificationType.TIMETABLE_CLASS, entry.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Weekly repeat via setRepeating
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )
    }

    fun cancelTimetableNotification(context: Context, entryId: String) {
        cancelAlarm(context, NotificationType.TIMETABLE_CLASS, entryId)
    }

    fun rescheduleTimetableNotification(context: Context, entry: TimetableEntry) {
        cancelTimetableNotification(context, entry.id)
        scheduleTimetableNotification(context, entry)
    }

    // ── Attendance Warning ─────────────────────────────────────────
    // Triggered manually from AttendanceActivity when threshold breached
    fun showAttendanceWarningNow(context: Context, courseName: String, percentage: Double) {
        NotificationHelper.showNotification(
            context = context,
            type = NotificationType.ATTENDANCE_WARNING,
            itemId = courseName.hashCode().toString(),
            extras = mapOf(
                AlarmReceiver.EXTRA_COURSE_NAME    to courseName,
                AlarmReceiver.EXTRA_ATTENDANCE_PCT to percentage.toString()
            )
        )
    }

    // ── Helpers ───────────────────────────────────────────────────
    private fun cancelAlarm(context: Context, type: NotificationType, itemId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = getRequestCode(type, itemId)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    // Deterministic request code from type + id (avoids collisions across types)
    private fun getRequestCode(type: NotificationType, itemId: String): Int {
        return (type.ordinal * 100_000 + itemId.hashCode()).and(0x7FFFFFFF)
    }

    private fun setExactAlarm(alarmManager: AlarmManager, triggerTime: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    // Compute next occurrence of a weekday + time from now
    private fun getNextClassTime(dayOfWeek: Int, startTime: String): Long? {
        return try {
            val parts = startTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek + 1) // Calendar.MONDAY = 2, our dayOfWeek 1 = Monday
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now) || timeInMillis == now.timeInMillis) {
                    add(Calendar.DAY_OF_YEAR, 7)
                }
            }
            target.timeInMillis
        } catch (e: Exception) {
            null
        }
    }
}
```

---

## 6. AlarmReceiver

```kotlin
// File: com/studbuddy/notifications/AlarmReceiver.kt
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val typeStr = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE) ?: return
        val type = try {
            NotificationType.valueOf(typeStr)
        } catch (e: IllegalArgumentException) {
            return
        }
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: return

        val extras = mutableMapOf<String, String>()

        when (type) {
            NotificationType.ASSIGNMENT_DUE -> {
                extras[EXTRA_ASSIGNMENT_TITLE]  = intent.getStringExtra(EXTRA_ASSIGNMENT_TITLE) ?: ""
                extras[EXTRA_ASSIGNMENT_COURSE] = intent.getStringExtra(EXTRA_ASSIGNMENT_COURSE) ?: ""
                extras[EXTRA_DUE_DATE]          = intent.getLongExtra(EXTRA_DUE_DATE, 0L).toString()
            }
            NotificationType.EXAM_REMINDER -> {
                extras[EXTRA_EXAM_COURSE] = intent.getStringExtra(EXTRA_EXAM_COURSE) ?: ""
                extras[EXTRA_EXAM_TYPE]   = intent.getStringExtra(EXTRA_EXAM_TYPE) ?: ""
                extras[EXTRA_EXAM_DATE]   = intent.getLongExtra(EXTRA_EXAM_DATE, 0L).toString()
            }
            NotificationType.TIMETABLE_CLASS -> {
                extras[EXTRA_CLASS_COURSE] = intent.getStringExtra(EXTRA_CLASS_COURSE) ?: ""
                extras[EXTRA_CLASS_ROOM]   = intent.getStringExtra(EXTRA_CLASS_ROOM) ?: ""
                extras[EXTRA_CLASS_TIME]   = intent.getStringExtra(EXTRA_CLASS_TIME) ?: ""
            }
            NotificationType.ATTENDANCE_WARNING -> {
                extras[EXTRA_COURSE_NAME]    = intent.getStringExtra(EXTRA_COURSE_NAME) ?: ""
                extras[EXTRA_ATTENDANCE_PCT] = intent.getStringExtra(EXTRA_ATTENDANCE_PCT) ?: "0.0"
            }
        }

        NotificationHelper.showNotification(context, type, itemId, extras)
    }

    // Constants
    companion object {
        const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
        const val EXTRA_ITEM_ID           = "extra_item_id"
        const val EXTRA_ASSIGNMENT_TITLE  = "extra_assignment_title"
        const val EXTRA_ASSIGNMENT_COURSE = "extra_assignment_course"
        const val EXTRA_DUE_DATE          = "extra_due_date"
        const val EXTRA_EXAM_COURSE       = "extra_exam_course"
        const val EXTRA_EXAM_TYPE         = "extra_exam_type"
        const val EXTRA_EXAM_DATE         = "extra_exam_date"
        const val EXTRA_CLASS_COURSE      = "extra_class_course"
        const val EXTRA_CLASS_ROOM        = "extra_class_room"
        const val EXTRA_CLASS_TIME        = "extra_class_time"
        const val EXTRA_COURSE_NAME       = "extra_course_name"
        const val EXTRA_ATTENDANCE_PCT    = "extra_attendance_pct"
    }
}
```

---

## 7. NotificationHelper.showNotification()

```kotlin
object NotificationHelper {

    private var notificationIdCounter = 1000

    fun showNotification(
        context: Context,
        type: NotificationType,
        itemId: String,
        extras: Map<String, String>
    ) {
        val (title, body) = buildNotificationContent(type, extras)
        val targetIntent = buildTargetIntent(context, type, itemId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            itemId.hashCode(),
            targetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, type.channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Use deterministic ID so same item doesn't generate duplicate notifications
        val notificationId = (type.ordinal * 100_000 + itemId.hashCode()).and(0x7FFFFFFF)
        notificationManager.notify(notificationId, notification)
    }

    private fun buildNotificationContent(
        type: NotificationType,
        extras: Map<String, String>
    ): Pair<String, String> = when (type) {
        NotificationType.ASSIGNMENT_DUE -> {
            val title  = extras[AlarmReceiver.EXTRA_ASSIGNMENT_TITLE] ?: "Assignment"
            val course = extras[AlarmReceiver.EXTRA_ASSIGNMENT_COURSE] ?: ""
            Pair("Assignment Due Tomorrow", "$title — $course is due in 24 hours")
        }
        NotificationType.EXAM_REMINDER -> {
            val course = extras[AlarmReceiver.EXTRA_EXAM_COURSE] ?: ""
            val type2  = extras[AlarmReceiver.EXTRA_EXAM_TYPE] ?: "Exam"
            val date   = extras[AlarmReceiver.EXTRA_EXAM_DATE]?.toLongOrNull() ?: 0L
            val timeStr = SimpleDateFormat("MMM dd 'at' HH:mm", Locale.getDefault()).format(Date(date))
            Pair("Upcoming Exam", "$course $type2 — $timeStr")
        }
        NotificationType.TIMETABLE_CLASS -> {
            val course = extras[AlarmReceiver.EXTRA_CLASS_COURSE] ?: "Class"
            val room   = extras[AlarmReceiver.EXTRA_CLASS_ROOM] ?: ""
            val time   = extras[AlarmReceiver.EXTRA_CLASS_TIME] ?: ""
            val location = if (room.isBlank()) "" else " in $room"
            Pair("Class in 15 minutes", "$course at $time$location")
        }
        NotificationType.ATTENDANCE_WARNING -> {
            val course = extras[AlarmReceiver.EXTRA_COURSE_NAME] ?: "Course"
            val pct    = extras[AlarmReceiver.EXTRA_ATTENDANCE_PCT]?.toDoubleOrNull() ?: 0.0
            Pair("Low Attendance Warning", "$course attendance is ${String.format("%.0f", pct)}% — below 75% threshold")
        }
    }

    private fun buildTargetIntent(context: Context, type: NotificationType, itemId: String): Intent =
        when (type) {
            NotificationType.ASSIGNMENT_DUE -> Intent(context, AssignmentsActivity::class.java).apply {
                putExtra(AssignmentsActivity.EXTRA_HIGHLIGHT_ID, itemId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            NotificationType.EXAM_REMINDER -> Intent(context, ExamsActivity::class.java).apply {
                putExtra(ExamsActivity.EXTRA_HIGHLIGHT_ID, itemId.removeSuffix("_24h").removeSuffix("_1h"))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            NotificationType.TIMETABLE_CLASS -> Intent(context, TimetableActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            NotificationType.ATTENDANCE_WARNING -> Intent(context, AttendanceActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

    fun createChannels(context: Context) { /* see section 3 above */ }
}
```

---

## 8. AndroidManifest Declarations

```xml
<!-- Permissions -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />

<!-- Inside <application> -->
<receiver
    android:name=".notifications.AlarmReceiver"
    android:exported="false" />

<receiver
    android:name=".notifications.BootReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

---

## 9. Boot Receiver (Re-schedule on Restart)

AlarmManager alarms are cleared when the device reboots. The `BootReceiver` reschedules all active alarms:

```kotlin
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        AppDataStore.initialize(context)

        // Reschedule assignments
        AppDataStore.getAssignmentList()
            .filter { !it.isCompleted }
            .forEach { NotificationScheduler.scheduleAssignmentNotification(context, it) }

        // Reschedule exams
        AppDataStore.getExamList()
            .filter { it.examDate > System.currentTimeMillis() }
            .forEach { NotificationScheduler.scheduleExamNotification(context, it) }

        // Reschedule timetable
        AppDataStore.getTimetableList()
            .forEach { NotificationScheduler.scheduleTimetableNotification(context, it) }
    }
}
```

---

## 10. Scheduling Rules Per Module

| Module | Trigger | Offset | Type |
|---|---|---|---|
| Assignments | Due date | -24 hours | `ASSIGNMENT_DUE` |
| Exams | Exam date | -24 hours | `EXAM_REMINDER` |
| Exams | Exam date | -1 hour | `EXAM_REMINDER` |
| Timetable | Class start time | -15 minutes | `TIMETABLE_CLASS` |
| Attendance | Manual (threshold breach) | Immediate | `ATTENDANCE_WARNING` |
