# MODULE_ATTENDANCE.md

# Module: Attendance

**Package:** `com.studbuddy.attendance`  
**Activity:** `AttendanceActivity`

---

## 1. Purpose

The Attendance module tracks student attendance for each course added in the Courses module. It calculates the attendance percentage, monitors it against a user-defined threshold, and calculates the contribution of attendance to the total course marks based on weightage.

---

## 2. Data Model

### Attendance Configuration (Per Course)
```kotlin
data class AttendanceConfig(
    val courseId: String,        // Reference to Course
    val totalLectures: Int,      // Total planned lectures
    val threshold: Double,       // Default 75.0
    val weightage: Double        // Default 10.0% of total course marks
)
```

### Attendance Record
```kotlin
data class AttendanceRecord(
    val id: String,
    val courseId: String,
    val dateTime: Long,          // Epoch ms
    val status: AttendanceStatus // PRESENT, ABSENT, LATE
)
```

---

## 3. Key Features

- **Mark Attendance**: Users can mark attendance for lectures. The app suggests completed lectures by comparing the current time with Timetable entries.
- **Threshold Monitoring**: If attendance falls below the threshold (default 75%), the course card in the attendance section turns **Red**, and a warning is displayed on the Home Dashboard.
- **Mark Calculation**: Attendance contributes to the `Course.marks` attribute based on the weightage set by the user.
- **Course Integration**: Attendance is tracked only for courses already added in the Courses module.

---

## 4. UI Components

- **Attendance List**: Displays cards for each course.
    - Card turns **Red** if attendance < threshold.
    - Shows current percentage and status (Short/OK).
- **Settings Dialog**: For each course, set total lectures, threshold, and weightage.
- **Marking UI**: Interface to select a date/time (integrated with Timetable) and mark status.

---

## 5. Business Logic

- **Percentage Calculation**: `(Present + (Late * 0.5)) / AttendedSessions * 100` (or based on Total Lectures).
- **Mark Contribution**: `(Percentage / 100) * weightage` -> Added to `Course.marks`.
- **Dashboard Alert**: If `currentPercentage < threshold`, add "Attendance short in [Course Name]" message to the Dashboard.
