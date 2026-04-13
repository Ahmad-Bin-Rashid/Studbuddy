# MODULE_ASSIGNMENTS.md

# Module: Assignments

**Package:** `com.studbuddy.assignments`  
**Activity:** `AssignmentsActivity`

---

## 1. Purpose

The Assignments module allows students to track academic tasks, manage deadlines, and record grades. Completed assignments automatically contribute to the total marks of the corresponding course.

---

## 2. Data Model

### Assignment Entity
```kotlin
data class Assignment(
    val id: String,
    val courseId: String,        // Reference to Course
    val title: String,
    val description: String?,
    val dueDate: Long,           // Epoch ms
    val totalMarks: Double,
    val obtainedMarks: Double?,  // Null until completed
    val weightage: Double,       // % of total course marks
    val isCompleted: Boolean
)
```

---

## 3. Key Features

- **Task Tracking**: Add assignments with due dates and weightage.
- **Grade Integration**: When marked as completed and `obtainedMarks` are entered, the contribution is calculated: `(obtainedMarks / totalMarks) * weightage` and added to `Course.marks`.
- **Dashboard Alerts**: If an assignment is pending, its title and due date are displayed on the Home Dashboard.
- **Course Reference**: Assignments can only be added for courses existing in the Courses module.

---

## 4. UI Components

- **Assignment List**: Grouped by course or sorted by due date. Shows completion status and priority.
- **Add/Edit Dialog**: Fields for Course selection (Spinner), Title, Marks, Weightage, and Due Date.
- **Status Indicators**: Visual cues for overdue tasks or high-weightage assignments.

---

## 5. Business Logic

- **Mark Contribution**: 
    ```kotlin
    if (isCompleted) {
        val contribution = (obtainedMarks / totalMarks) * weightage
        course.marks += contribution
        AppDataStore.updateCourse(course)
    }
    ```
- **Validation**: Ensure `weightage` for all assignments in a course does not exceed the total allocated for assignments (defined in syllabus/attendance config).
