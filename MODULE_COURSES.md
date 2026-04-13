# MODULE_COURSES.md

# Module: Courses

**Package:** `com.studbuddy.courses`  
**Activity:** `CourseActivity`

---

## 1. Purpose

The Courses module is the central repository for all academic subjects in a semester. It stores course details and serves as the primary reference for all other modules (Attendance, Timetable, Assignments, Exams).

---

## 2. Data Model

### Course Entity
```kotlin
data class Course(
    val id: String,              // Primary Key
    val name: String,
    val description: String?,    // Optional
    val instructor: String?,     // Optional
    val creditHours: Int,
    val semesterId: String,      // Reference to Semester
    val marks: Double,           // Aggregated from Attendance, Assignments, Exams
    val grade: String?,          // A, B, C, etc. (Filled upon completion)
    val gradePoints: Double      // Calculated: f(grade, creditHours)
)
```

---

## 3. Key Features

- **Add/Edit Courses**: Users can manage courses for the current semester.
- **Semester Dependency**: If no semester is initialized on the Home screen, the user is prompted to add one first.
- **Integration**: The `courseId` is used as a foreign key in all other modules.
- **Auto-Calculations**:
    - `marks`: Automatically updated as assessments (Assignments/Exams/Attendance) are completed.
    - `gradePoints`: Calculated once a grade is assigned.

---

## 4. UI Components

- **Course List**: Displays all courses for the current semester.
- **Add Course Dialog**: Fields for Name, Description, Instructor, Credit Hours.
- **Validation**: Ensures Semester is added first; ensures mandatory fields are present.

---

## 5. Business Logic

- **Grade Points**: Typically `PointsPerGrade * CreditHours`.
- **Pre-requisite**: Check `AppDataStore.getSemester()` before allowing course addition.
- **Impact**: Changes here (e.g., deleting a course) will cascade to related records in other modules.
