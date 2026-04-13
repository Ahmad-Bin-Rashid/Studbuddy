# STORAGE_LAYER.md

# StudBuddy — Storage Layer

---

## 1. Overview

The storage layer provides persistent data management using a JSON-based approach. It is built on two main components:

| Class | Role |
|---|---|
| `AppDataStore` | The public API and in-memory cache. Handles all business logic related to data. |
| `SharedPrefManager` | A low-level wrapper for Android `SharedPreferences`. Internal use only. |

---

## 2. Core Mechanism

- **Persistence**: Data is serialized to JSON strings and stored in `SharedPreferences`.
- **In-Memory Cache**: `AppDataStore` maintains local lists of objects to ensure fast read access.
- **Sync**: Every write operation (`add`, `update`, `delete`) automatically triggers a sync to disk.
- **Single Source of Truth**: Activities always interact with `AppDataStore`, never the storage files directly.

---

## 3. Data Models (Updated Architecture)

### 3.1 Semester
Stores the active semester configuration.
```kotlin
data class Semester(
    val startDate: Long,
    val endDate: Long,
    val targetGpa: Double,
    val previousCgpa: Double?
)
```

### 3.2 Course
The primary entity. All other modules link to this via `id`.
```kotlin
data class Course(
    val id: String,
    val name: String,
    val description: String?,
    val instructor: String?,
    val creditHours: Int,
    val semesterId: String,
    val marks: Double,           // Aggregated score (0-100)
    val grade: String?,          // A, B, C, etc.
    val gradePoints: Double      // (GPA points * Credit Hours)
)
```

### 3.3 Attendance Record & Config
```kotlin
data class AttendanceConfig(
    val courseId: String,
    val totalLectures: Int,
    val threshold: Double,       // e.g. 75.0
    val weightage: Double        // e.g. 10.0
)

data class AttendanceRecord(
    val id: String,
    val courseId: String,
    val dateTime: Long,
    val status: String           // PRESENT, ABSENT, LATE
)
```

### 3.4 Assignment
```kotlin
data class Assignment(
    val id: String,
    val courseId: String,
    val title: String,
    val dueDate: Long,
    val totalMarks: Double,
    val obtainedMarks: Double?,
    val weightage: Double,
    val isCompleted: Boolean
)
```

### 3.5 Exam
```kotlin
data class Exam(
    val id: String,
    val courseId: String,
    val type: String,            // QUIZ, MIDTERM, FINAL
    val date: Long,
    val totalMarks: Double,
    val obtainedMarks: Double?,
    val weightage: Double,
    val isCompleted: Boolean
)
```

---

## 4. AppDataStore API

The `AppDataStore` provides the following specialized methods:

- `getSemester()` / `saveSemester(semester)`
- `getCourses()` / `addCourse(course)` / `updateCourse(course)`
- `getAttendanceRecords(courseId)` / `addAttendanceRecord(record)`
- `getAssignments(courseId)` / `updateAssignment(assignment)`
- `getExams(courseId)` / `addExam(exam)`

---

## 5. Integration Logic

`AppDataStore` is responsible for cross-model updates:
- When an **Assignment** or **Exam** is marked completed, it automatically recalculates the `marks` field in the corresponding **Course**.
- When **Course** marks are updated, it updates the `grade` and `gradePoints`.
- **GPA** calculations are performed on-the-fly using the cached **Course** list.
