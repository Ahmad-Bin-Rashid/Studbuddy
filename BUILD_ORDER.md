# BUILD_ORDER.md

# StudBuddy — Implementation Roadmap

This document outlines the recommended order of development to ensure that dependencies are met and the core functionality is stable before building complex features.

---

## Phase 1: Foundation (Core & Storage)
*Goal: Setup data persistence and shared models.*

1.  **SharedPrefManager**: Implement the low-level `SharedPreferences` wrapper.
2.  **Base Models**: Create Kotlin data classes for `Semester`, `Course`, `TimetableEntry`, `AttendanceRecord`, `Assignment`, and `Exam`.
3.  **AppDataStore (Part 1)**: Implement JSON serialization/deserialization logic for these models.
4.  **AppDataStore (Part 2)**: Implement the Singleton with basic `get/save` methods for `Semester`.

---

## Phase 2: Navigation & Base UI
*Goal: Implement the custom sidebar and app-wide layout.*

1.  **Base Layout**: Design a reusable `ConstraintLayout` structure with the hidden `CardView` sidebar.
2.  **MainActivity Layout**: Set up the initial Dashboard UI with the menu icon.
3.  **Navigation Logic**: Implement the sidebar toggle and `Intent` transitions in `MainActivity`.
4.  **Theme Setup**: Define colors and styles in `res/values` (Dark/Light mode foundations).

---

## 3. Phase 3: Semester & Course Management (The Prerequisites)
*Goal: Unlock the ability to add data.*

1.  **Semester Setup**: Build the "Add Semester" dialog/screen in `MainActivity`.
2.  **CourseActivity**: Create the UI for listing and adding courses.
3.  **Validation**: Ensure courses can only be added if a semester exists.
4.  **Persistence**: Hook up `Course` saving to `AppDataStore`.

---

## Phase 4: Data Entry Modules
*Goal: Implement the main productivity features. These can be done in any order, but this is recommended.*

1.  **Timetable**: Create the weekly schedule UI. Hook up logic to detect lectures within 24 hours.
2.  **Assignments**: Implement task tracking. Add the weightage-based calculation logic that updates the `Course` entity.
3.  **Exams**: Implement Quiz/Mid/Final tracking with weightage integration.
4.  **Attendance**: Build the tracker with the 75% threshold logic and red-card visual alerts.

---

## Phase 5: Aggregation & GPA
*Goal: Calculate the final results.*

1.  **Dashboard Update**: Enhance `MainActivity` to pull data from all modules (Next lecture, Short attendance, Pending tasks).
2.  **GpaActivity**: Implement the calculation logic using finalized course marks and credit hours.
3.  **CGPA**: Add the ability to include previous CGPA in calculations.

---

## Phase 6: Polish & System Features
*Goal: Refinement and notifications.*

1.  **Notification System**: Implement `AlarmManager` and `BroadcastReceiver` for lecture and assignment reminders.
2.  **Settings**: Implement the Dark/Light mode toggle.
3.  **UI Polish**: Ensure consistent padding, fonts, and colors across all screens as per `UI_COMPONENT_GUIDELINES.md`.
