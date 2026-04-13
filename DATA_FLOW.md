# DATA_FLOW.md

# StudBuddy — Data Flow & Integration

---

## 1. Core Principles

1.  **Single Source of Truth**: `AppDataStore` manages all persistent data.
2.  **Course-Centric Linking**: Most entities (Attendance, Assignments, Exams, Timetable) reference a `Course` by its unique `id`.
3.  **Automatic Aggregation**: Completing an assignment or exam automatically updates the `marks` field in the parent `Course`.
4.  **Reactive Dashboard**: The Home Dashboard reads from all modules to display the current status.

---

## 2. The Course Dependency Chain

Data flows from specific assessments up to the Semester GPA:

```
[Assignment/Exam/Attendance] 
      │ 
      ▼ (calculates contribution based on weightage)
[Course.marks] 
      │ 
      ▼ (calculated when grade is added)
[Course.gradePoints] 
      │ 
      ▼ (weighted average using credit hours)
[Semester GPA]
```

---

## 3. Data Flow Scenarios

### 3.1 Marking an Assignment as Completed
1.  User enters `obtainedMarks` in **Assignments**.
2.  **Assignments Module** calls `AppDataStore.updateAssignment()`.
3.  **AppDataStore** triggers an internal update to the corresponding `Course`:
    - `course.marks += (obtainedMarks / totalMarks) * weightage`
4.  **Home Dashboard** updates to remove the pending assignment from the list.

### 3.2 Attendance Impact
1.  User marks attendance in **Attendance**.
2.  **Attendance Module** calculates the current percentage.
3.  If `percentage < threshold`, the **Attendance** UI highlights red.
4.  **Home Dashboard** displays "Attendance short in [Course]".
5.  Attendance marks are recalculated: `(percentage / 100) * attendanceWeightage` and added to `Course.marks`.

### 3.3 Timetable & Dashboard
1.  **Timetable Module** checks for lectures within the next 24 hours.
2.  If found, it sends the lecture details to the **Home Dashboard**.
3.  **Home Dashboard** displays "Next lecture: [Course] at [Time]".

---

## 4. Key Registry (AppDataStore)

| Key | Model |
|---|---|
| `KEY_SEMESTER` | `Semester` |
| `KEY_COURSES` | `List<Course>` |
| `KEY_ATTENDANCE` | `List<AttendanceRecord>` |
| `KEY_TIMETABLE` | `List<TimetableEntry>` |
| `KEY_ASSIGNMENTS` | `List<Assignment>` |
| `KEY_EXAMS` | `List<Exam>` |
| `KEY_SETTINGS` | `Settings` |

---

## 5. Initialization Flow

1.  **Empty State**: On first launch, `KEY_SEMESTER` is null. Dashboard shows "Add Semester".
2.  **Semester Setup**: User saves `Semester` (dates, target GPA).
3.  **Course Setup**: User navigates to **Courses** and adds courses referencing the current semester.
4.  **Full Access**: All other modules (Timetable, etc.) are now unlocked as they can reference the added courses.
