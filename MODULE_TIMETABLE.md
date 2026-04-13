# MODULE_TIMETABLE.md

# Module: Timetable

**Package:** `com.studbuddy.timetable`  
**Activity:** `TimetableActivity`

---

## 1. Purpose

The Timetable module generates and manages the weekly lecture schedule for courses added in the Courses module. It provides a visual calendar of the week and alerts the user of upcoming lectures.

---

## 2. Data Model

### Timetable Entry
```kotlin
data class TimetableEntry(
    val id: String,
    val courseId: String,        // Reference to Course
    val dayOfWeek: Int,          // 1 (Mon) - 7 (Sun)
    val startTime: String,       // "HH:mm"
    val endTime: String,         // "HH:mm"
    val room: String,            // Room name/number
    val color: String            // Hex color for UI
)
```

---

## 3. Key Features

- **Weekly Schedule**: Automatically generates a recurring weekly timetable using the semester start and end dates.
- **Lecture Highlights**: If a lecture is scheduled within the next 24 hours, it is highlighted in the Timetable UI.
- **Dashboard Integration**: A message "Next lecture: [Course] at [Time] in [Room]" is added to the Home Dashboard when a lecture is less than 1 day away.
- **Attendance Link**: The Timetable data is used by the Attendance module to suggest marking attendance for completed lectures.

---

## 4. UI Components

- **Weekly View**: A RecyclerView or custom grid showing days of the week and their respective classes.
- **Add Entry Dialog**: Select course, day, start/end time, and room.
- **Highlight Logic**: Visual indicator (e.g., glowing border or distinct color) for the next upcoming lecture.

---

## 5. Business Logic

- **Upcoming Check**: 
    ```kotlin
    val timeToNextLecture = nextLectureTime - currentTime
    if (timeToNextLecture in 0..86400000) { // < 24 hours
        updateDashboard(nextLecture)
        highlightInTimetable(nextLecture)
    }
    ```
- **Semester Context**: Uses semester dates from the Home module to define the active period of the timetable.
