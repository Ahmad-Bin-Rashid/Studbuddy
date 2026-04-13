# MODULE_EXAMS.md

# Module: Exams

**Package:** `com.studbuddy.exams`  
**Activity:** `ExamsActivity`

---

## 1. Purpose

The Exams module allows students to schedule and track quizzes, midterms, and final exams. It integrates with the Courses module to update total marks upon completion of an assessment.

---

## 2. Data Model

### Exam Entity
```kotlin
enum class ExamType { QUIZ, MIDTERM, FINAL }

data class Exam(
    val id: String,
    val courseId: String,        // Reference to Course
    val type: ExamType,
    val date: Long,              // Epoch ms
    val venue: String?,
    val totalMarks: Double,
    val obtainedMarks: Double?,  // Null until completed
    val weightage: Double,       // % of total course marks
    val isCompleted: Boolean
)
```

---

## 3. Key Features

- **Exam Types**: Supports Quizzes, Midterms (max 1 per course), and Finals (max 1 per course).
- **Grade Integration**: When marked as completed, the contribution is calculated: `(obtainedMarks / totalMarks) * weightage` and added to `Course.marks`.
- **Dashboard Alerts**: Next exam time and place are displayed on the Home Dashboard.
- **Validation**:
    - Ensures only one Midterm and one Final per course.
    - Ensures weightage is within valid limits.

---

## 4. UI Components

- **Exam List**: Grouped by course or sorted by date.
- **Add/Edit Dialog**: Fields for Course (Spinner), Exam Type (Spinner/Radio), Date/Time Picker, Venue, Marks, and Weightage.
- **Countdown**: Visual indicator for upcoming exams.

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
- **Dashboard Integration**: "Next exam: [Type] - [Course] on [Date] at [Venue]" shown on Home screen.
