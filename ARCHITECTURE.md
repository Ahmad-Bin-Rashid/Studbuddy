# FILE: ARCHITECTURE.md

# StudBuddy — System Architecture

---

## 1. Architecture Overview

StudBuddy uses a flat **Activity-based architecture** with a centralized data layer. There are no Fragments, no ViewModels, and no LiveData. All state is managed explicitly inside Activities with data sourced exclusively from `AppDataStore`.

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
│   MainActivity  TimetableActivity  AssignmentsActivity ...  │
│        ↕               ↕                  ↕                 │
│    XML Layouts     RecyclerView        Dialogs              │
└───────────────────────────┬─────────────────────────────────┘
                            │ read / write
┌───────────────────────────▼─────────────────────────────────┐
│                     Data Layer                              │
│              AppDataStore (in-memory + JSON)                │
│                        ↕                                    │
│              SharedPrefManager (JSON strings)               │
└─────────────────────────────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                   System Layer                              │
│     AlarmManager   BroadcastReceiver   NotificationHelper   │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Component Responsibilities

### 2.1 MainActivity

- Entry point of the application.
- Renders the home dashboard (navigation menu to each module).
- Calls `AppDataStore.initialize(context)` on startup.
- Does NOT own any module-specific data.
- Navigates to module Activities via `Intent`.

```kotlin
// MainActivity.kt — navigation pattern
val intent = Intent(this, TimetableActivity::class.java)
startActivity(intent)
```

### 2.2 Module Activities

Each module Activity is fully self-contained:

| Responsibility | Rule |
|---|---|
| Load data | Call `AppDataStore.get<Module>List()` in `onResume()` |
| Display data | Bind to RecyclerView via module Adapter |
| Mutate data | Call `AppDataStore.save<Module>()`, then `SharedPrefManager.sync()` |
| Navigate | Use `Intent` only — no shared state between Activities |
| Show dialogs | Use `LayoutInflater` pattern (see `DIALOG_PATTERNS.md`) |

### 2.3 AppDataStore

- **Single source of truth** for all application data.
- Maintains in-memory lists for each data type.
- Serializes/deserializes to JSON via Kotlin's `JSONObject`/`JSONArray`.
- Delegates persistence to `SharedPrefManager`.
- Thread: Main thread only — no coroutines, no background threads.

### 2.4 SharedPrefManager

- Wraps Android `SharedPreferences`.
- Stores all data as JSON strings keyed by data type.
- Never called directly from Activities — only from `AppDataStore`.

### 2.5 Adapters

- One Adapter class per module.
- Uses `ViewHolder` pattern exclusively.
- Receives a `MutableList<T>` and a lambda for click callbacks.
- Contains zero business logic — only binding data to views.

### 2.6 Notification System

- `NotificationScheduler` — called by module Activities to schedule alarms.
- `AlarmReceiver` — BroadcastReceiver that fires on alarm trigger.
- `NotificationHelper` — builds and posts `NotificationCompat`.
- See `NOTIFICATION_SYSTEM.md` for full contract.

---

## 3. Data Flow Summary

```
Activity.onResume()
    → AppDataStore.get<Module>List()
        → SharedPrefManager.getString(KEY)
            → JSON deserialization
        → returns List<Model>
    → adapter.updateList(list)
    → RecyclerView.notifyDataSetChanged()

User Action (Add/Edit/Delete)
    → Dialog collects input
    → Activity validates input
    → AppDataStore.save<Module>(item)
        → updates in-memory list
        → serializes to JSON
        → SharedPrefManager.putString(KEY, json)
    → adapter.notifyItemInserted(pos) / notifyItemChanged(pos) / notifyItemRemoved(pos)
```

---

## 4. Package Structure (Strict)

```
com.studbuddy/
├── core/
│   ├── AppDataStore.kt
│   ├── SharedPrefManager.kt
│   ├── MainActivity.kt
│   └── models/
│       ├── TimetableEntry.kt
│       ├── Assignment.kt
│       ├── AttendanceRecord.kt
│       ├── Exam.kt
│       └── Course.kt
├── timetable/
│   ├── TimetableActivity.kt
│   └── TimetableAdapter.kt
├── assignments/
│   ├── AssignmentsActivity.kt
│   └── AssignmentsAdapter.kt
├── attendance/
│   ├── AttendanceActivity.kt
│   └── AttendanceAdapter.kt
├── exams/
│   ├── ExamsActivity.kt
│   └── ExamsAdapter.kt
├── gpa/
│   ├── GpaActivity.kt
│   └── GpaAdapter.kt
└── notifications/
    ├── NotificationHelper.kt
    ├── AlarmReceiver.kt
    └── NotificationScheduler.kt
```

### Package Ownership

| Package | Owner | May NOT touch |
|---|---|---|
| `core/` | All (read), Infra lead (write) | `AppDataStore`, `SharedPrefManager` |
| `timetable/` | Dev-A | All other packages |
| `assignments/` | Dev-B | All other packages |
| `attendance/` | Dev-C | All other packages |
| `exams/` | Dev-D | All other packages |
| `gpa/` | Dev-E | All other packages |
| `notifications/` | Dev-F | All other packages |

---

## 5. Resource File Ownership

| File/Directory | Owner |
|---|---|
| `res/layout/activity_main.xml` | Infra lead |
| `res/layout/activity_timetable.xml` | Dev-A |
| `res/layout/item_timetable.xml` | Dev-A |
| `res/layout/activity_assignments.xml` | Dev-B |
| `res/layout/item_assignment.xml` | Dev-B |
| `res/layout/activity_attendance.xml` | Dev-C |
| `res/layout/item_attendance.xml` | Dev-C |
| `res/layout/activity_exams.xml` | Dev-D |
| `res/layout/item_exam.xml` | Dev-D |
| `res/layout/activity_gpa.xml` | Dev-E |
| `res/layout/item_gpa.xml` | Dev-E |
| `res/values/colors.xml` | Infra lead (all must use existing colors) |
| `res/values/strings.xml` | All (add only, never modify existing keys) |
| `res/values/dimens.xml` | Infra lead |

---

## 6. Shared Contracts (Frozen After Init)

Once defined by the Infra lead and merged, these contracts are **frozen**. Any change requires a team-wide PR:

- `AppDataStore` public function signatures
- `SharedPrefManager` key constants
- Data model class fields
- `NotificationHelper` channel IDs and notification type enum

---

## 7. Error Handling Policy

| Error Type | Handling |
|---|---|
| Empty data list | Show empty state view in Activity |
| JSON parse error | Log error, return empty list, show Toast |
| Null intent extras | Use defaults or show error Toast, never crash |
| Invalid user input | Show validation Toast, do not dismiss dialog |
| Alarm scheduling failure | Log error, show Toast to user |

All errors are handled at the Activity level. No silent failures.
