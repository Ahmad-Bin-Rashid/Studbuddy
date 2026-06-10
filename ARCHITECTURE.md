# StudyBuddy — System Architecture

---

## 1. Architecture Overview

StudBuddy follows a **Clean Architecture** approach using **MVVM (Model-View-ViewModel)**. The app is structured as a single-activity application using the **Navigation Component** and **Hilt** for dependency injection.

```
┌──────────────────────────────────────────────────────────────────┐
│                          UI Layer                                │
│     MainActivity (Single Activity) + DrawerLayout Sidebar        │
│          ↕                 ↕                    ↕                │
│   HomeFragment    CoursesFragment    AttendanceFragment  ...     │
│   SettingsFragment → ProfileFragment                             │
│                   → NotificationSettingsFragment                 │
│                   → AppearanceSettingsFragment                   │
│                   → BackupFragment (Google Drive Backup/Restore) │
└──────────────────────────┬───────────────────────────────────────┘
                           │  observes via StateFlow / LiveData
┌──────────────────────────▼───────────────────────────────────────┐
│                       Domain Layer                               │
│              ViewModels (HiltViewModel, @Inject)                 │
└──────────────────────────┬───────────────────────────────────────┘
                           │
┌──────────────────────────▼───────────────────────────────────────┐
│                       Data Layer                                 │
│   StudBuddyRepository  ←——→  DriveBackupRepository                │
│         ↓                          ↓                             │
│   Room Database              Google Drive API                    │
│   (Local Persistence)        (appDataFolder JSON Backup)         │
└──────────────────────────┬───────────────────────────────────────┘
                           │
┌──────────────────────────▼───────────────────────────────────────┐
│                      System Layer                                │
│      WorkManager (DailyMaintenanceWorker)                        │
│      AlarmManager + AlarmReceiver (Exact Reminders)              │
│      NotificationHelper + NotificationScheduler                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 2. Component Responsibilities

### 2.1 StudBuddyApp (Application Class)

- Initializes **Hilt** via `@HiltAndroidApp`.
- Implements `Configuration.Provider` to supply the **HiltWorkerFactory** to WorkManager.
- On startup, schedules:
  - `DailyMaintenanceWorker` — daily maintenance worker scheduled at the user-configured daily summary time to maintain and reschedule alarms.
- Initializes the local guest user via `UserManager` if no user is present.

### 2.2 MainActivity

- **Host**: Manages the `NavHostFragment` and the global `DrawerLayout`.
- **Navigation**: Uses `NavigationView` with `NavController` to handle fragment transitions with a custom `setNavigationItemSelectedListener` (ensuring sidebar stays in sync on back-press and argument-based navigation).
- **Theme**: Observes `SettingsManager.themeMode` via `lifecycleScope` and applies it at runtime.
- **User Header**: Observes `UserManager.userFlow` to display the user's name, email, and profile photo in the nav drawer header (loaded with Coil).
- **Notifications**: Creates notification channels and requests `POST_NOTIFICATIONS` permission on Android 13+.

### 2.3 Fragments (Feature Views)

Each feature is a `Fragment` paired with a `@HiltViewModel`:

| Fragment | Responsibility |
|---|---|
| **HomeFragment** | Dashboard — upcoming lectures, short attendance, pending assignments, next exam, semester GPA |
| **SemesterFragment** | CRUD for semesters, set active semester |
| **CoursesFragment** | Course list (filtered by active or specified semester) |
| **CourseDetailFragment** | Timetable, attendance, assignments, exams, and notes per course |
| **AttendanceFragment** | All attendance records with percentage per course |
| **TimetableFragment** | Weekly timetable view |
| **AssignmentsFragment** | Pending and completed assignment list |
| **ExamsFragment** | Upcoming and completed exam list |
| **GpaFragment** | Per-semester GPA and cumulative CGPA |
| **ProfileFragment** | User display name, profile photo, Google Sign-In / Sign-Out |
| **SettingsFragment** | Entry point to sub-settings pages |
| **NotificationSettingsFragment** | Class reminders, assignment and exam lead times |
| **AppearanceSettingsFragment** | Light / Dark / System theme |
| **BackupFragment** | Google Drive account status, manual "Backup to Drive", and "Restore from Drive" |

### 2.4 Data Layer

#### StudBuddyRepository
Single source of truth for all **local** data operations. Wraps Room DAOs and exposes `Flow`-based reactive streams to ViewModels. Also handles GPA recalculation when course grades change.

#### DriveBackupRepository
Handles **Google Drive JSON backup and restore**:
- **Authentication**: Fetches OAuth2 access token for the authenticated Google account using scope `oauth2:https://www.googleapis.com/auth/drive.appdata`.
- **Backup**: Serializes all Room tables (Semesters, Courses, Timetable, Attendance, Assignments, Exams) into `studbuddy_backup.json` and uploads it via Google Drive REST API to `appDataFolder`.
- **Restore**: Queries Google Drive REST API for `studbuddy_backup.json` in `appDataFolder`, downloads the payload, parses entity arrays, and performs an atomic overwrite of Room tables.
- **Scope & Security**: Stored strictly in Google Drive's hidden `appDataFolder`, keeping backup data isolated and inaccessible from regular user-facing Drive folders.
- **Notes Handling**: Local file path references for notes are preserved in JSON, but note file binaries stay on device.
- Updates last successful backup time via `SettingsManager.setLastSyncTime()`.

### 2.5 Room Database (`StudBuddyDatabase`)

Version **6**, with `fallbackToDestructiveMigration`. Contains:

| Entity | Table | Description |
|---|---|---|
| `Semester` | `semesters` | Semester date range, active flag, GPA |
| `Course` | `courses` | Course details with `semesterId` FK |
| `TimetableEntry` | `timetable_entries` | Day/time/room slot with `courseId` FK |
| `AttendanceRecord` | `attendance_records` | Present/Absent/Late log with `courseId` FK |
| `Assignment` | `assignments` | Due date, marks, completion with `courseId` FK |
| `Exam` | `exams` | Quiz/Midterm/Final with `courseId` FK |
| `Note` | `notes` | Local file reference with `courseId` FK (local file paths, omitted from cloud backup) |

All entities carry a `lastModified: Long` timestamp.

### 2.6 Cloud & Backup Integration

| Service | Usage |
|---|---|
| **Firebase Auth** | Google Sign-In via `GoogleSignInOptions`. Manages user identity and profile details. |
| **Google Drive REST API** | JSON backup/restore (`studbuddy_backup.json`) in the isolated `appDataFolder` (`DriveScopes.DRIVE_APPDATA`). |

Google Drive `appDataFolder` file query endpoint:
```
GET https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name='studbuddy_backup.json' and trashed=false
```

### 2.7 Background Workers (WorkManager)

| Worker | Trigger | Responsibility |
|---|---|---|
| `DailyMaintenanceWorker` | Periodic — every 24 hours at user-configured summary time | Reschedules exact alarms for upcoming lectures, assignments, and exams. |

### 2.8 Notification System

| Component | Role |
|---|---|
| `NotificationHelper` | Creates notification channels; posts notifications |
| `NotificationScheduler` | Schedules exact alarms via `AlarmManager` for lectures, assignments, exams |
| `AlarmReceiver` | BroadcastReceiver — fires when alarm triggers, posts the notification |
| `BootReceiver` | BroadcastReceiver — reschedules alarms after device reboot |
| `NotificationEntryPoint` | Hilt entry point for injection inside `AlarmReceiver` |

### 2.9 Persistence & Settings

| Manager | Storage | Responsibility |
|---|---|---|
| `SettingsManager` | DataStore Preferences (`studbuddy_settings`) | Theme mode, notification preferences, last backup timestamp |
| `UserManager` | DataStore Preferences (`user_prefs`) | User ID, display name, email, profile image URI, auth status |

---

## 3. Data Flow

### Normal Write (e.g., Adding a Semester)

```
User taps "Add"
  → SemesterFragment calls SemesterViewModel.saveSemester()
  → SemesterViewModel calls StudBuddyRepository.saveSemester()
  → Repository calls SemesterDao.insert()
  → Room emits update on getAllFlow()
  → SemesterFragment RecyclerView auto-updates
```

### Google Drive Backup Flow (Manual)

```
User taps "Backup to Drive" in BackupFragment
  → BackupViewModel.backup()
  → viewModelScope.launch { driveBackupRepository.backup() }
    → Acquire OAuth token (https://www.googleapis.com/auth/drive.appdata)
    → Serialize local Room DB (semesters, courses, timetable, attendance, assignments, exams) to JSON
    → Query/Upload studbuddy_backup.json to Google Drive appDataFolder
  → status = SUCCESS / ERROR
  → SettingsManager.setLastSyncTime() updated
  → UI reflects backup timestamp & status
```

### Google Drive Restore Flow (Manual)

```
User taps "Restore from Drive" in BackupFragment
  → BackupViewModel.restore()
  → viewModelScope.launch { driveBackupRepository.restore() }
    → Acquire OAuth token
    → Query & download studbuddy_backup.json from Google Drive appDataFolder
    → Parse JSON and perform atomic overwrite of Room DB tables
  → status = SUCCESS / ERROR
  → UI refreshes with restored data
```

---

## 4. Dependency Injection (Hilt)

| Module | Provides |
|---|---|
| `DatabaseModule` | `StudBuddyDatabase`, all DAOs |
| `RepositoryModule` | `StudBuddyRepository`, `DriveBackupRepository`, `UserManager`, `SettingsManager` |

All ViewModels are `@HiltViewModel` with `@Inject constructor`. Workers are `@HiltWorker` with `@AssistedInject`.

---

## 5. Navigation Structure

```
nav_graph.xml
├── homeFragment (start)
├── semesterFragment
├── coursesFragment  →  courseDetailFragment
│                           └── [tab: notes, attendance, assignments, exams]
├── attendanceFragment
├── timetableFragment
├── assignmentsFragment
├── examsFragment
├── gpaFragment
├── profileFragment
└── settingsFragment
        ├── → notificationSettingsFragment
        ├── → appearanceSettingsFragment
        └── → backupFragment
```

Navigation is handled through the DrawerLayout sidebar. Back-stack and single-top behaviour are managed per destination.

---

## 6. Key Design Decisions

| Decision | Rationale |
|---|---|
| JSON backup via Google Drive `appDataFolder` | Replaced Firestore sync; provides user-owned, private, single-file backup hidden from personal Drive files |
| Manual backup/restore model | Avoids unexpected network usage and gives students direct control over backup snapshots and restore overwrites |
| Notes excluded from Drive payload | Note file binaries stay on device; file paths are preserved to avoid large binary uploads |
| `DriveBackupRepository` separate from `StudBuddyRepository` | Keeps local persistence logic cleanly isolated from remote cloud backup APIs |
| `fallbackToDestructiveMigration` | Acceptable during active development; should be replaced with proper migration scripts before production release |

