# StudyBuddy — Student Companion App

A comprehensive Android app that helps students manage their academic life — courses, timetable, attendance, assignments, exams, and GPA — all in one place.

---

## Features

| Feature | Details |
|---|---|
| **Dashboard** | Semester overview, upcoming lectures, attendance alerts, pending assignments, next exam, expected GPA |
| **Semester Management** | Create semesters with date ranges, set an active semester |
| **Course Management** | Track courses with credit hours, instructor, and grades per semester |
| **Weekly Timetable** | Add class slots with day, time, room, and colour; get exact lecture reminders |
| **Attendance Tracker** | Mark Present / Absent / Late per lecture; view attendance % per course |
| **Assignments** | Create assignments with due dates, marks, and weightage; mark as complete with direct mark entry |
| **Exams** | Schedule Quiz, Midterm, and Final exams with venue and marks |
| **GPA Calculator** | Auto-calculates semester GPA and cumulative CGPA from course grades |
| **Notes** | Attach local files (PDFs, images) to each course |
| **Google Drive Backup** | Manual **Backup to Drive** and **Restore from Drive** using Google Drive REST API (`appDataFolder`) |
| **Profile** | Google Sign-In, display name, and profile photo |
| **Appearance** | Light / Dark / System theme |
| **Notifications** | Configurable lecture reminders, assignment deadlines, and exam alerts |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Architecture | MVVM + Clean Architecture |
| UI | XML Layouts, Material Design 3 |
| Local Persistence | Room Database (SQLite) |
| Cloud Backup | Google Drive REST API (`appDataFolder` JSON backup) |
| Authentication | Firebase Auth (Google Sign-In) |
| Dependency Injection | Hilt |
| Navigation | Navigation Component (Single Activity) |
| Background Tasks | WorkManager (`DailyMaintenanceWorker`) |
| Scheduling | AlarmManager (exact lecture/exam/assignment reminders) |
| Image Loading | Coil |
| Preferences | Jetpack DataStore |

---

## Project Structure

```
app/src/main/java/com/example/studbuddy/
│
├── core/
│   ├── db/               ← Room database, DAOs, TypeConverters
│   ├── models/           ← Data entities (Semester, Course, Exam, …)
│   ├── repository/
│   │   ├── StudBuddyRepository.kt   ← Local data (Room)
│   │   └── DriveBackupRepository.kt ← Google Drive JSON backup/restore
│   ├── di/               ← Hilt modules (DatabaseModule, RepositoryModule)
│   ├── notifications/    ← NotificationHelper, Scheduler, AlarmReceiver, BootReceiver
│   ├── workers/          ← DailyMaintenanceWorker
│   ├── SettingsManager.kt
│   └── UserManager.kt
│
├── home/                 ← Dashboard
├── semesters/            ← Semester management
├── courses/              ← Course list + CourseDetail (tabs for notes, attendance, etc.)
├── attendance/           ← Attendance tracking
├── timetable/            ← Weekly schedule
├── assignments/          ← Assignment tracking
├── exams/                ← Exam management
├── gpa/                  ← GPA calculator
├── notes/                ← Local file notes per course
├── profile/              ← User profile + Google Sign-In
└── settings/
    ├── SettingsFragment.kt
    ├── AppearanceSettingsFragment.kt
    ├── NotificationSettingsFragment.kt
    ├── BackupFragment.kt            ← Account status, Backup to Drive, Restore from Drive
    └── BackupViewModel.kt
```

---

## Google Auth & Google Drive Backup Setup

### Prerequisites
1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com) (or Google Cloud Console).
2. Add an Android app with package name `com.example.studbuddy` and configure SHA-1 fingerprint.
3. Download `google-services.json` and place it in `app/`.
4. Enable **Google Sign-In** under Authentication → Sign-in method.
5. In Google Cloud Console, enable the **Google Drive API** for your project.

### Google Drive Backup Overview

Backup and restore operations use the Google Drive REST API to store a compact, single-file JSON snapshot (`studbuddy_backup.json`) in the user's isolated **Application Data folder** (`appDataFolder`).

- Scope: `https://www.googleapis.com/auth/drive.appdata` (`DriveScopes.DRIVE_APPDATA`)
- File Name: `studbuddy_backup.json`
- Target: Hidden `appData` space in Google Drive (isolated from personal Drive files)

### Backup Data Format

```json
{
  "semesters": [ ... ],
  "courses": [ ... ],
  "timetable": [ ... ],
  "attendance": [ ... ],
  "assignments": [ ... ],
  "exams": [ ... ]
}
```

> **Notes are local-only** — they reference local device file URIs and are not uploaded to Google Drive.

---

## Getting Started

```bash
# Clone the repo
git clone <repo-url>
cd Studbuddy

# Open in Android Studio, let Gradle sync
# Place your google-services.json in app/
# Build & Run on a device or emulator (API 24+)
```

### Requirements
- Android Studio Hedgehog or later
- Android SDK 36 (compileSdk), minSdk 24
- Google Play Services available on test device/emulator for Google Sign-In and Drive API access

---

## How Backup & Restore Works

1. **Sign in** via Profile or Backup screen using your Google Account.
2. Navigate to **Settings → Backup & Restore**.
3. **Backup to Drive**: Serializes all local Room database tables into `studbuddy_backup.json` and uploads it to your Google Account's private `appDataFolder`.
4. **Restore from Drive**: Downloads the latest `studbuddy_backup.json` file from Google Drive, validates its contents, and updates local Room tables.
5. Status indicators show current progress and the timestamp of the last successful backup.

---

## Documentation

- [Architecture Overview](ARCHITECTURE.md)

