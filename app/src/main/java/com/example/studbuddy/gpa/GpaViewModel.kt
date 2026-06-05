package com.example.studbuddy.gpa

import androidx.lifecycle.*
import com.example.studbuddy.core.models.Course
import com.example.studbuddy.core.models.Semester
import com.example.studbuddy.core.repository.StudBuddyRepository
import kotlinx.coroutines.flow.*

data class GpaUiState(
    val courses: List<Course> = emptyList(),
    val semester: Semester? = null,
    val calculatedGpa: Double = 0.0
)

class GpaViewModel(private val repository: StudBuddyRepository) : ViewModel() {

    val uiState: StateFlow<GpaUiState> = combine(
        repository.getCoursesFlow(),
        repository.getSemesterFlow()
    ) { courseList, semester ->
        val coursesWithGrades = courseList.filter { it.grade != null }
        val totalPoints = coursesWithGrades.sumOf { it.gradePoints }
        val totalCredits = coursesWithGrades.sumOf { it.creditHours }
        val gpa = if (totalCredits > 0) totalPoints / totalCredits else 0.0
        
        GpaUiState(courseList, semester, gpa)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GpaUiState()
    )
}
