# StudBuddy Improvement Roadmap

This document outlines the prioritized steps to refactor the architecture and enhance the feature set of StudBuddy.

## Phase 1: Architectural Foundation & Data Integrity (done)
*Goal: Move away from manual JSON management to a robust, scalable data layer.*

1.  **Migrate to Room Database:** Replace `AppDataStore` and `SharedPrefManager` (JSON strings) with **Room**. This ensures type safety, better performance, and easier querying for attendance and timetable records.
2.  **Implement Repository Pattern:** Create a Repository layer to abstract data sources, making it easier to switch between local DB and potential future cloud sync.
3.  **Refactor to MVVM:** Introduce **ViewModels** and **LiveData/StateFlow**. This will remove business logic from Activities and handle configuration changes (like screen rotation) automatically.

## Phase 2: UI/UX & Navigation Modernization
*Goal: Improve the user experience and align with modern Android standards.*

1. **Jetpack Navigation Component:** Replace the manual sidebar toggle and Intent-based navigation with the **Navigation Component** and a `DrawerLayout`.
2. **Material 3 Theme:** Update UI components (Cards, Buttons, Inputs) to **Material 3** for a more modern, cohesive look.
3. **Reactive UI Updates:** Ensure the Dashboard and lists update automatically using observable data flows from Room, eliminating the need for manual `onResume` refreshes.

## Phase 3: Automation & Proactive Alerts
*Goal: Transform the app from a passive tracker to an active assistant.*

1. **Notification System:** Implement a `NotificationHelper` to handle system-level alerts.
2. **AlarmManager Integration:** Schedule alarms for:
    *   **Class Reminders:** 10-15 minutes before lectures.
    *   **Deadline Alerts:** 24 hours before assignment/exam due dates.
3. **WorkManager for Background Tasks:** Use WorkManager for any periodic data cleanup or heavy background processing.

## Phase 4: Advanced Student Utilities
*Goal: Add high-value features that solve specific student pain points.*

1. **Attendance Predictor:** Add a calculator to tell students exactly how many classes they can miss (or must attend) to stay above their target percentage (e.g., 75%).
2. **Weighted Grade Calculator:** Allow users to set weightages for different assessments (Finals, Mids, Quizzes) to see their "Current Grade" in real-time.
3. **Multi-Semester History:** Add support for tracking previous semesters and calculating a cumulative GPA (CGPA).

## Phase 5: Integration & Ecosystem
*Goal: Connect StudBuddy with external tools and ensure data safety.*

1. **Google Calendar Sync:** Allow users to export their timetable and exam dates directly to their primary Google Calendar.
2. **Cloud Backup & Sync:** Integrate **Firebase** or **Google Drive API** to allow users to backup their data and sync across multiple devices.
