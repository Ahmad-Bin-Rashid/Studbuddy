# ARCHITECTURE.md

# StudBuddy — System Architecture

---

## 1. Architecture Overview

StudBuddy uses a **Modular Activity-based architecture** centered around a shared data layer. Navigation is implemented using a **Custom Overlay Sidebar** (using `ConstraintLayout`, `CardView`, and `LinearLayout`) accessible from all main module screens.

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
│   MainActivity (Dashboard) + Custom Overlay Sidebar         │
│        ↕               ↕                  ↕                 │
│    CourseActivity  TimetableActivity  AttendanceActivity ...│
└───────────────────────────┬─────────────────────────────────┘
                            │ read / write
┌───────────────────────────▼─────────────────────────────────┐
│                     Data Layer                              │
│              AppDataStore (JSON Persistence)                │
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

### 2.1 MainActivity (Home/Dashboard)

- **Entry Point**: Displays the Dashboard.
- **Semester Management**: Handles first-time setup for semester details (dates, credit hours).
- **Dashboard Logic**: Aggregates data from all modules:
    - Current semester.
    - Next lecture time and place (from Timetable, if < 1 day remaining).
    - Short attendance alerts (from Attendance, if < threshold).
    - Pending assignments (from Assignments).
    - Next exam time and place (from Exams).
    - Current semester expected GPA (from Courses/GPA).
- **Navigation**: Hosts the custom sidebar logic.

### 2.2 Navigation (Custom Sidebar)

- **Structure**:
    - Top Menu Icon (trigger).
    - Overlay Container: `CardView` + `ScrollView` + `LinearLayout`.
    - Menu Items: Home, Courses, Attendance, Timetable, Assignments, Exams, GPA.
    - Footer: Settings (separated).
- **Implementation**: Manual visibility toggle (`View.VISIBLE`/`View.GONE`) and `Intent` navigation.

### 2.3 Module Activities

Modules are interconnected through the Course ID:

| Module | Dependency | Responsibility |
|---|---|---|
| **Courses** | Semester | Manage courses (credit hours, instructor, marks, grades). |
| **Attendance** | Courses | Track attendance per course, set threshold/weightage, and update course marks. |
| **Timetable** | Courses | Manage weekly schedule (days, times, rooms) and upcoming lecture alerts. |
| **Assignments** | Courses | Manage tasks (total/obtained marks, weightage) and update course marks. |
| **Exams** | Courses | Track Quizzes/Midterms/Finals and update course marks. |
| **GPA** | Courses | Calculate Semester GPA and track CGPA. |

### 2.4 AppDataStore & Storage

- **AppDataStore**: Single source of truth. Handles JSON serialization of objects (Semester, Course, etc.).
- **SharedPrefManager**: Low-level persistence using `SharedPreferences`.
- **Integration**: Changes in sub-modules (Attendance/Assignments/Exams) trigger mark recalculations in the `Course` entity, which updates the GPA.

---

## 3. Data Flow: The "Course-Centric" Model

1. **Initialization**: User adds Semester details on the Home screen.
2. **Setup**: User adds Courses (ID is the primary key).
3. **Activity**:
    - **Assessments**: When an assignment/exam/attendance record is updated, the parent `Course.marks` is updated based on the specified weightage.
4. **Finalization**: `Course.grade` and `Course.gradePoints` are updated when a grade is assigned (usually at semester end).
5. **Output**: GPA module uses finalized Course data to calculate Semester GPA.

---

## 4. UI/UX Standards

- **Theme**: Dark and Light mode toggle in Settings.
- **Alerts**: Color-coded cards (e.g., Red for short attendance) and Dashboard notifications.
- **Navigation**: Overlay sidebar with consistent layout across all Activities.

---

## 5. Package Structure

```
com.studbuddy/
├── core/                ← Shared models, AppDataStore, SharedPrefManager
├── home/                ← MainActivity, Dashboard logic
├── courses/             ← Course management
├── attendance/          ← Attendance tracking & weightage
├── timetable/           ← Weekly schedule & reminders
├── assignments/         ← Task tracking & grade integration
├── exams/               ← Exam scheduling (Quiz/Mid/Final)
├── gpa/                 ← GPA/CGPA calculations
├── settings/            ← Theme (Light/Dark) toggle
└── notifications/       ← AlarmManager & BroadcastReceiver
```
