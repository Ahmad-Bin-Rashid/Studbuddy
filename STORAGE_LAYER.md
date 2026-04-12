# FILE: STORAGE_LAYER.md

# StudBuddy — Storage Layer

---

## 1. Overview

The storage layer consists of two classes:

| Class | Role |
|---|---|
| `AppDataStore` | Public API for all data. In-memory cache + serialization. |
| `SharedPrefManager` | Low-level wrapper for `SharedPreferences`. Internal to `AppDataStore`. |

**No Activity, Adapter, or Helper class may import or reference `SharedPrefManager` directly.**

---

## 2. SharedPrefManager

### Location
`com.studbuddy.core.SharedPrefManager`

### Full Implementation

```kotlin
class SharedPrefManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_FILE_NAME = "studbuddy_prefs"
    }

    fun getString(key: String): String? = prefs.getString(key, null)

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        prefs.getBoolean(key, default)

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getInt(key: String, default: Int = 0): Int =
        prefs.getInt(key, default)

    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
```

---

## 3. AppDataStore — Full Public API

### Location
`com.studbuddy.core.AppDataStore`

### Initialization

```kotlin
object AppDataStore {
    private lateinit var prefs: SharedPrefManager

    // Call once in MainActivity.onCreate()
    fun initialize(context: Context) {
        prefs = SharedPrefManager(context.applicationContext)
    }
}
```

---

### 3.1 Timetable API

```kotlin
// ── Keys ──────────────────────────────────────────────────────────
private const val KEY_TIMETABLE = "studbuddy_timetable"

// ── Cache ─────────────────────────────────────────────────────────
private var timetableCache: MutableList<TimetableEntry>? = null

// ── Read ──────────────────────────────────────────────────────────
fun getTimetableList(): List<TimetableEntry> {
    if (timetableCache == null) {
        val json = prefs.getString(KEY_TIMETABLE) ?: return emptyList()
        timetableCache = deserializeTimetable(json)
    }
    return timetableCache!!.toList()
}

// ── Add ───────────────────────────────────────────────────────────
fun addTimetableEntry(entry: TimetableEntry) {
    ensureTimetableCache()
    timetableCache!!.add(entry)
    persistTimetable()
}

// ── Update ────────────────────────────────────────────────────────
fun updateTimetableEntry(entry: TimetableEntry) {
    ensureTimetableCache()
    val idx = timetableCache!!.indexOfFirst { it.id == entry.id }
    if (idx >= 0) {
        timetableCache!![idx] = entry
        persistTimetable()
    }
}

// ── Delete ────────────────────────────────────────────────────────
fun deleteTimetableEntry(id: String) {
    ensureTimetableCache()
    timetableCache!!.removeAll { it.id == id }
    persistTimetable()
}

// ── Helpers ───────────────────────────────────────────────────────
private fun ensureTimetableCache() {
    if (timetableCache == null) getTimetableList()
    if (timetableCache == null) timetableCache = mutableListOf()
}

private fun persistTimetable() {
    val array = JSONArray()
    timetableCache!!.forEach { array.put(it.toJson()) }
    prefs.putString(KEY_TIMETABLE, array.toString())
}

private fun deserializeTimetable(json: String): MutableList<TimetableEntry> {
    return try {
        val array = JSONArray(json)
        (0 until array.length())
            .map { TimetableEntry.fromJson(array.getJSONObject(it)) }
            .toMutableList()
    } catch (e: Exception) {
        mutableListOf()
    }
}
```

---

### 3.2 Assignments API

```kotlin
private const val KEY_ASSIGNMENTS = "studbuddy_assignments"
private var assignmentsCache: MutableList<Assignment>? = null

fun getAssignmentList(): List<Assignment>
fun addAssignment(assignment: Assignment)
fun updateAssignment(assignment: Assignment)
fun deleteAssignment(id: String)
fun markAssignmentComplete(id: String, isComplete: Boolean)

// Convenience queries
fun getOverdueAssignments(): List<Assignment> {
    val now = System.currentTimeMillis()
    return getAssignmentList().filter { !it.isCompleted && it.dueDate < now }
}

fun getUpcomingAssignments(withinMs: Long): List<Assignment> {
    val now = System.currentTimeMillis()
    return getAssignmentList().filter {
        !it.isCompleted && it.dueDate in now..(now + withinMs)
    }
}
```

---

### 3.3 Attendance API

```kotlin
private const val KEY_ATTENDANCE = "studbuddy_attendance"
private var attendanceCache: MutableList<AttendanceRecord>? = null

fun getAttendanceList(): List<AttendanceRecord>
fun addAttendanceRecord(record: AttendanceRecord)
fun updateAttendanceRecord(record: AttendanceRecord)
fun deleteAttendanceRecord(id: String)

// Convenience queries
fun getAttendanceForCourse(courseName: String): List<AttendanceRecord> {
    return getAttendanceList().filter { it.courseName == courseName }
}

fun getAttendancePercentage(courseName: String): Double {
    val records = getAttendanceForCourse(courseName)
    if (records.isEmpty()) return 100.0
    val present = records.count { it.status == AttendanceStatus.PRESENT }
    return (present.toDouble() / records.size) * 100.0
}
```

---

### 3.4 Exams API

```kotlin
private const val KEY_EXAMS = "studbuddy_exams"
private var examsCache: MutableList<Exam>? = null

fun getExamList(): List<Exam>
fun addExam(exam: Exam)
fun updateExam(exam: Exam)
fun deleteExam(id: String)

fun getUpcomingExams(withinMs: Long): List<Exam> {
    val now = System.currentTimeMillis()
    return getExamList().filter { it.examDate in now..(now + withinMs) }
        .sortedBy { it.examDate }
}
```

---

### 3.5 Courses / GPA API

```kotlin
private const val KEY_COURSES = "studbuddy_courses"
private var coursesCache: MutableList<Course>? = null

fun getCourseList(): List<Course>
fun addCourse(course: Course)
fun updateCourse(course: Course)
fun deleteCourse(id: String)

// Used by other modules for Spinner population
fun getCourseNames(): List<String> {
    return getCourseList().map { it.name }.sorted()
}

fun calculateGpa(): Double {
    val courses = getCourseList().filter { it.creditHours > 0 }
    if (courses.isEmpty()) return 0.0
    val totalPoints = courses.sumOf { it.gradePoints * it.creditHours }
    val totalCredits = courses.sumOf { it.creditHours }
    return if (totalCredits == 0) 0.0 else totalPoints / totalCredits
}
```

---

### 3.6 Global API

```kotlin
// Clear all data (settings/reset use case)
fun clearAll() {
    timetableCache = null
    assignmentsCache = null
    attendanceCache = null
    examsCache = null
    coursesCache = null
    prefs.clearAll()
}
```

---

## 4. Data Models

### 4.1 TimetableEntry

```kotlin
data class TimetableEntry(
    val id: String,             // UUID
    val courseName: String,     // e.g. "CS101"
    val dayOfWeek: Int,         // 1=Monday ... 7=Sunday
    val startTime: String,      // "HH:mm" 24-hour format
    val endTime: String,        // "HH:mm" 24-hour format
    val room: String,           // e.g. "Room 204" — optional, can be empty
    val color: String           // hex color string e.g. "#1565C0"
) {
    fun toJson(): JSONObject
    companion object { fun fromJson(obj: JSONObject): TimetableEntry }
}
```

### 4.2 Assignment

```kotlin
enum class Priority { LOW, MEDIUM, HIGH }

data class Assignment(
    val id: String,
    val title: String,
    val courseName: String,
    val dueDate: Long,          // epoch ms
    val priority: Priority,
    val description: String,    // optional, can be empty
    val isCompleted: Boolean
) {
    fun toJson(): JSONObject
    companion object { fun fromJson(obj: JSONObject): Assignment }
}
```

### 4.3 AttendanceRecord

```kotlin
enum class AttendanceStatus { PRESENT, ABSENT, LATE }

data class AttendanceRecord(
    val id: String,
    val courseName: String,
    val date: Long,             // epoch ms — date only (time = 00:00:00)
    val status: AttendanceStatus,
    val notes: String           // optional
) {
    fun toJson(): JSONObject
    companion object { fun fromJson(obj: JSONObject): AttendanceRecord }
}
```

### 4.4 Exam

```kotlin
data class Exam(
    val id: String,
    val courseName: String,
    val examType: String,       // "Midterm", "Final", "Quiz", "Lab"
    val examDate: Long,         // epoch ms
    val venue: String,          // optional
    val durationMinutes: Int,   // e.g. 90
    val notes: String           // optional
) {
    fun toJson(): JSONObject
    companion object { fun fromJson(obj: JSONObject): Exam }
}
```

### 4.5 Course (GPA)

```kotlin
data class Course(
    val id: String,
    val name: String,           // e.g. "CS101 — Data Structures"
    val creditHours: Int,       // 1–6
    val grade: String,          // "A", "A-", "B+", "B", "B-", "C+", "C", "D", "F"
    val gradePoints: Double,    // derived from grade — set by AppDataStore, not UI
    val semester: String        // e.g. "Fall 2024"
) {
    fun toJson(): JSONObject
    companion object {
        fun fromJson(obj: JSONObject): Course

        // Grade to GPA points mapping
        val GRADE_POINTS_MAP = mapOf(
            "A"  to 4.0,
            "A-" to 3.7,
            "B+" to 3.3,
            "B"  to 3.0,
            "B-" to 2.7,
            "C+" to 2.3,
            "C"  to 2.0,
            "C-" to 1.7,
            "D+" to 1.3,
            "D"  to 1.0,
            "F"  to 0.0
        )
    }
}
```

---

## 5. Storage Limits and Warnings

| Data Type | Soft Limit | Hard Limit | Action at Limit |
|---|---|---|---|
| Timetable entries | 50 | 100 | Toast warning at soft, block add at hard |
| Assignments | 100 | 200 | Toast warning at soft, block add at hard |
| Attendance records | 500 | 1000 | No UI limit — SharedPrefs handles it |
| Exams | 50 | 100 | Toast warning at soft |
| Courses | 20 | 40 | Toast warning at soft |

SharedPreferences can store up to approximately 2MB per file on most devices. JSON overhead is minimal for these data volumes.
