# FILE: README.md

# StudBuddy — Android Student Productivity App

## Project Overview

StudBuddy is a collaborative Android application designed to help university students manage their academic life. It provides modules for timetable management, assignment tracking, attendance monitoring, exam scheduling, and GPA calculation.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | XML Layouts only (NO Jetpack Compose) |
| Architecture | Activity-based (no Fragments required) |
| Storage | SharedPrefManager + AppDataStore (in-memory + JSON) |
| Navigation | Intent-based |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |

---

## Project Modules

| Module | Activity | Developer Ownership |
|---|---|---|
| Timetable | `TimetableActivity` | Dev-A |
| Assignments | `AssignmentsActivity` | Dev-B |
| Attendance | `AttendanceActivity` | Dev-C |
| Exams | `ExamsActivity` | Dev-D |
| GPA Calculator | `GpaActivity` | Dev-E |
| Notifications | `NotificationService` | Dev-F |

---

## Repository Structure

```
StudBuddy/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/studbuddy/
│   │   │   │   ├── core/
│   │   │   │   │   ├── AppDataStore.kt          ← DO NOT EDIT without team approval
│   │   │   │   │   ├── SharedPrefManager.kt     ← DO NOT EDIT without team approval
│   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   └── models/
│   │   │   │   │       ├── TimetableEntry.kt
│   │   │   │   │       ├── Assignment.kt
│   │   │   │   │       ├── AttendanceRecord.kt
│   │   │   │   │       ├── Exam.kt
│   │   │   │   │       └── Course.kt
│   │   │   │   ├── timetable/
│   │   │   │   │   ├── TimetableActivity.kt
│   │   │   │   │   └── TimetableAdapter.kt
│   │   │   │   ├── assignments/
│   │   │   │   │   ├── AssignmentsActivity.kt
│   │   │   │   │   └── AssignmentsAdapter.kt
│   │   │   │   ├── attendance/
│   │   │   │   │   ├── AttendanceActivity.kt
│   │   │   │   │   └── AttendanceAdapter.kt
│   │   │   │   ├── exams/
│   │   │   │   │   ├── ExamsActivity.kt
│   │   │   │   │   └── ExamsAdapter.kt
│   │   │   │   ├── gpa/
│   │   │   │   │   ├── GpaActivity.kt
│   │   │   │   │   └── GpaAdapter.kt
│   │   │   │   └── notifications/
│   │   │   │       ├── NotificationHelper.kt
│   │   │   │       ├── AlarmReceiver.kt
│   │   │   │       └── NotificationScheduler.kt
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       ├── values/
│   │   │       └── drawable/
│   └── build.gradle
├── docs/                                        ← All .md documentation files
└── README.md
```

---

## Critical Rules — All Developers Must Read

1. **Never edit** `AppDataStore.kt` or `SharedPrefManager.kt` without a team-wide PR review.
2. **Each developer owns exactly one package.** Do not touch another developer's package.
3. **All data access goes through `AppDataStore`** — never read/write SharedPreferences directly.
4. **All UI patterns** must follow `UI_COMPONENT_GUIDELINES.md`.
5. **All git operations** must follow `GIT_STRATEGY.md` and `BRANCHING_RULES.md`.
6. **No external libraries** may be added without team approval in a PR discussion.

---

## Quick Start

```bash
# Clone
git clone https://github.com/org/studbuddy.git
cd studbuddy

# Create your feature branch (see BRANCHING_RULES.md)
git checkout -b feature/<your-module>/<short-description>

# Build
./gradlew assembleDebug

# Run tests
./gradlew test
```

---

## Documentation Index

| File | Purpose |
|---|---|
| `ARCHITECTURE.md` | System architecture and component wiring |
| `CODING_STANDARDS.md` | Naming, formatting, style rules |
| `NAVIGATION_FLOW.md` | Screen flow and Intent contracts |
| `DATA_FLOW.md` | Data read/write lifecycle |
| `STORAGE_LAYER.md` | AppDataStore + SharedPrefManager API |
| `MODULE_TIMETABLE.md` | Timetable module full spec |
| `MODULE_ASSIGNMENTS.md` | Assignments module full spec |
| `MODULE_ATTENDANCE.md` | Attendance module full spec |
| `MODULE_EXAMS.md` | Exams module full spec |
| `MODULE_GPA.md` | GPA module full spec |
| `UI_COMPONENT_GUIDELINES.md` | UI rules and design system |
| `RECYCLER_VIEW_PATTERNS.md` | RecyclerView standards |
| `DIALOG_PATTERNS.md` | AlertDialog standards |
| `NOTIFICATION_SYSTEM.md` | AlarmManager + notification flow |
| `PERMISSIONS.md` | Runtime permission handling |
| `BROADCAST_RECEIVER.md` | BroadcastReceiver patterns |
| `BUILD_ORDER.md` | Build dependencies and order |
| `GIT_STRATEGY.md` | Git workflow |
| `BRANCHING_RULES.md` | Branch naming and rules |
| `MERGE_CONFLICT_PREVENTION.md` | Anti-conflict strategies |

---

## Contact & Ownership

All questions about shared infrastructure (`AppDataStore`, `SharedPrefManager`, `MainActivity`) must be raised as GitHub Issues tagged `[infra]` before any edits are made.
