# StudBuddy — Android Student Companion App

## Project Overview

StudBuddy is a comprehensive Android application designed to help students manage their academic life efficiently. It integrates course management, timetable scheduling, attendance tracking, assignment management, exam preparation, and GPA calculation into a single platform.

---

## Key Features

- **Dashboard**: Centralized view of current semester status, upcoming lectures, short attendance alerts, pending assignments, next exams, and expected GPA.
- **Course Management**: Track courses with credit hours, instructor details, and grade calculation.
- **Weekly Timetable**: Automated weekly schedule generation with reminders for upcoming classes.
- **Attendance Tracker**: Monitor attendance percentages against thresholds and calculate its impact on course marks.
- **Assignment & Exam Tracking**: Manage deadlines and grades for all assessments with automatic course mark updates.
- **GPA Calculator**: Track CGPA and calculate semester GPA based on course grades and credit hours.
- **Settings**: Personalize the app with dark/light mode and user-defined thresholds.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | XML Layouts (ConstraintLayout + Custom Overlay Sidebar) |
| Architecture | Activity-based Modular Architecture |
| Storage | AppDataStore (JSON-based persistence in SharedPrefs) |
| Navigation | Intent-based Sidebar Navigation (Visibility Toggle) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |

---

## Project Modules

| Module | Purpose |
|---|---|
| **Home** | Dashboard and Semester initialization |
| **Courses** | Manage courses for the current semester |
| **Attendance** | Track lecture attendance and course weightage |
| **Timetable** | Weekly schedule management and alerts |
| **Assignments** | Task tracking with grade integration |
| **Exams** | Quiz, Midterm, and Final exam management |
| **GPA** | Semester GPA and CGPA calculation |
| **Settings** | UI themes and app configurations |

---

## Repository Structure

```
StudBuddy/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/studbuddy/
│   │   │   │   ├── core/                ← Shared logic and data models
│   │   │   │   ├── home/                ← Dashboard logic
│   │   │   │   ├── courses/             ← Course management
│   │   │   │   ├── attendance/          ← Attendance tracking
│   │   │   │   ├── timetable/           ← Schedule management
│   │   │   │   ├── assignments/         ← Task management
│   │   │   │   ├── exams/               ← Assessment management
│   │   │   │   └── gpa/                 ← Grade calculation
│   │   │   └── res/
│   │   │       ├── layout/              ← XML Layouts
│   │   │       └── values/              ← Resources (colors, strings)
└── README.md                            ← Project documentation
```

---

## Documentation Index

| File | Purpose |
|---|---|
| `ARCHITECTURE.md` | System architecture and component wiring |
| `STORAGE_LAYER.md` | Data persistence and AppDataStore API |
| `MODULE_TIMETABLE.md` | Timetable module full spec |
| `MODULE_ASSIGNMENTS.md` | Assignments module full spec |
| `MODULE_ATTENDANCE.md` | Attendance module full spec |
| `MODULE_EXAMS.md` | Exams module full spec |
| `MODULE_GPA.md` | GPA module full spec |
| `NAVIGATION_FLOW.md` | Sidebar and screen transitions |
