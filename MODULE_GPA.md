# MODULE_GPA.md

# Module: GPA

**Package:** `com.studbuddy.gpa`  
**Activity:** `GpaActivity`

---

## 1. Purpose

The GPA module calculates the semester GPA and tracks the overall CGPA. It uses finalized course grades and credit hours from the Courses module to provide accurate academic standing.

---

## 2. Data Model

### GPA Summary
```kotlin
data class GpaSummary(
    val semesterId: String,
    val semesterGpa: Double,
    val totalCreditHours: Int,
    val previousCgpa: Double?
)
```

---

## 3. Key Features

- **Semester GPA Calculation**: 
    - `GPA = Σ (GradePoints * CreditHours) / Σ (CreditHours)`
    - Automatically calculated when grades are added to courses.
- **CGPA Tracking**: Option to add/edit previous CGPA to see the overall impact.
- **Course Integration**: Pulls data from the Courses module. Credit hours and Grade Points (calculated from Grade and Credit Hours) are used for calculation.

---

## 4. UI Components

- **GPA View**: Displays Semester GPA and CGPA.
- **Course Summary List**: Mini-list showing courses, their credit hours, and final grades.
- **Previous GPA Dialog**: To input/edit the starting CGPA.

---

## 5. Business Logic

- **Calculation Logic**:
    ```kotlin
    val semesterPoints = courses.sumOf { it.gradePoints }
    val semesterCredits = courses.sumOf { it.creditHours }
    val gpa = if (semesterCredits > 0) semesterPoints / semesterCredits else 0.0
    ```
- **Dashboard Integration**: "Current Semester Expected GPA" is shown on the Home Dashboard once at least one course grade is added.
- **Credit Hours**: Total credit hours for the semester are the sum of credit hours of all courses added.
