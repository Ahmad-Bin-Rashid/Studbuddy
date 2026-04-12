# FILE: NAVIGATION_FLOW.md

# StudBuddy — Navigation Flow

---

## 1. Navigation Architecture

StudBuddy uses **explicit Intent-based navigation** exclusively. There is no Navigation Component, no FragmentManager, and no back-stack manipulation beyond the system default.

### Rules
- All navigation is one-way (forward). Return is handled by system Back button.
- No shared state is passed between Activities via static fields or singletons.
- All data passed via Intent extras must use the constants defined in each Activity's `companion object`.
- Activities reload their data from `AppDataStore` in `onResume()` — not from Intent extras (except for edit/view mode where an item ID is needed).

---

## 2. Screen Map

```
MainActivity (Dashboard)
│
├── → TimetableActivity
│       └── (no child activities)
│
├── → AssignmentsActivity
│       └── (no child activities)
│
├── → AttendanceActivity
│       └── (no child activities)
│
├── → ExamsActivity
│       └── (no child activities)
│
└── → GpaActivity
        └── (no child activities)
```

All module Activities are leaves — they do not navigate to any child Activity.

---

## 3. Intent Contracts

### 3.1 MainActivity → TimetableActivity

```kotlin
// Caller (MainActivity)
val intent = Intent(this, TimetableActivity::class.java)
// No extras required
startActivity(intent)

// Receiver (TimetableActivity)
// No extras to read — loads all data from AppDataStore.getTimetableList()
```

### 3.2 MainActivity → AssignmentsActivity

```kotlin
// Caller (MainActivity)
val intent = Intent(this, AssignmentsActivity::class.java)
// No extras required
startActivity(intent)
```

### 3.3 MainActivity → AttendanceActivity

```kotlin
// Caller (MainActivity)
val intent = Intent(this, AttendanceActivity::class.java)
// No extras required
startActivity(intent)
```

### 3.4 MainActivity → ExamsActivity

```kotlin
// Caller (MainActivity)
val intent = Intent(this, ExamsActivity::class.java)
// No extras required
startActivity(intent)
```

### 3.5 MainActivity → GpaActivity

```kotlin
// Caller (MainActivity)
val intent = Intent(this, GpaActivity::class.java)
// No extras required
startActivity(intent)
```

### 3.6 Notification → Module Activity (Deep Link)

When a notification is tapped, the `AlarmReceiver` creates an Intent to the relevant module Activity.

```kotlin
// In AlarmReceiver / NotificationHelper
val intent = when (notificationType) {
    NotificationType.ASSIGNMENT_DUE -> Intent(context, AssignmentsActivity::class.java).apply {
        putExtra(AssignmentsActivity.EXTRA_HIGHLIGHT_ID, itemId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    NotificationType.EXAM_REMINDER -> Intent(context, ExamsActivity::class.java).apply {
        putExtra(ExamsActivity.EXTRA_HIGHLIGHT_ID, itemId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    NotificationType.ATTENDANCE_WARNING -> Intent(context, AttendanceActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    NotificationType.TIMETABLE_CLASS -> Intent(context, TimetableActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
}
```

---

## 4. Intent Extra Constants (Per Activity)

### TimetableActivity
```kotlin
companion object {
    const val EXTRA_HIGHLIGHT_ID = "extra_timetable_highlight_id"  // String (UUID)
}
```

### AssignmentsActivity
```kotlin
companion object {
    const val EXTRA_HIGHLIGHT_ID = "extra_assignment_highlight_id"  // String (UUID)
}
```

### AttendanceActivity
```kotlin
companion object {
    const val EXTRA_HIGHLIGHT_ID = "extra_attendance_highlight_id"  // String (UUID)
}
```

### ExamsActivity
```kotlin
companion object {
    const val EXTRA_HIGHLIGHT_ID = "extra_exam_highlight_id"  // String (UUID)
}
```

### GpaActivity
```kotlin
companion object {
    // No extras — always loads all courses
}
```

---

## 5. Back Navigation

- System Back button is the only way to return from module Activities to `MainActivity`.
- Do NOT call `finish()` on Back unless you are explicitly clearing a task stack.
- Do NOT call `overridePendingTransition()` unless the entire team agrees on a transition style.

---

## 6. Activity Launch Modes

Declare in `AndroidManifest.xml`:

```xml
<activity
    android:name=".core.MainActivity"
    android:launchMode="singleTop"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER"/>
    </intent-filter>
</activity>

<activity
    android:name=".timetable.TimetableActivity"
    android:launchMode="singleTop"
    android:parentActivityName=".core.MainActivity" />

<activity
    android:name=".assignments.AssignmentsActivity"
    android:launchMode="singleTop"
    android:parentActivityName=".core.MainActivity" />

<activity
    android:name=".attendance.AttendanceActivity"
    android:launchMode="singleTop"
    android:parentActivityName=".core.MainActivity" />

<activity
    android:name=".exams.ExamsActivity"
    android:launchMode="singleTop"
    android:parentActivityName=".core.MainActivity" />

<activity
    android:name=".gpa.GpaActivity"
    android:launchMode="singleTop"
    android:parentActivityName=".core.MainActivity" />
```

---

## 7. Highlight Behavior (Deep Link from Notification)

When an Activity receives `EXTRA_HIGHLIGHT_ID`:

```kotlin
override fun onResume() {
    super.onResume()
    loadData() // Always reload first
    val highlightId = intent.getStringExtra(EXTRA_HIGHLIGHT_ID)
    if (highlightId != null) {
        val position = itemList.indexOfFirst { it.id == highlightId }
        if (position >= 0) {
            recyclerView.scrollToPosition(position)
            // Optional: adapter.setHighlightedId(highlightId) then notifyItemChanged(position)
        }
        intent.removeExtra(EXTRA_HIGHLIGHT_ID) // Consume so it doesn't re-highlight on next onResume
    }
}
```

---

## 8. Forbidden Navigation Patterns

| Pattern | Status | Reason |
|---|---|---|
| `startActivityForResult` | FORBIDDEN | Deprecated API |
| Fragment transactions | FORBIDDEN | No Fragments in project |
| Navigation Component | FORBIDDEN | Project constraint |
| Static fields for cross-Activity state | FORBIDDEN | Causes bugs on process death |
| `Application` subclass for state | FORBIDDEN | Use AppDataStore |
| Implicit Intents for internal navigation | FORBIDDEN | Use explicit only |
