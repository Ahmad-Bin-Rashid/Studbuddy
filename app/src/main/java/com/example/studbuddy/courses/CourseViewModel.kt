package com.example.studbuddy.courses

import androidx.lifecycle.*
import com.example.studbuddy.core.models.Course
import com.example.studbuddy.core.models.Semester
import com.example.studbuddy.core.repository.StudBuddyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourseUiState(
    val courses: List<Course> = emptyList(),
    val semester: Semester? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class CourseViewModel @Inject constructor(private val repository: StudBuddyRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<CourseUiState> = combine(
        repository.getCoursesFlow(),
        repository.getSemesterFlow(),
        _isLoading
    ) { courses, semester, loading ->
        CourseUiState(courses, semester, loading)
    }.onEach {
        if (it.courses.isNotEmpty() || it.semester != null) {
            _isLoading.value = false
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CourseUiState()
    )

    fun addCourse(course: Course) {
        viewModelScope.launch {
            repository.addCourse(course)
            updateSemesterGpa()
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            repository.updateCourse(course)
            updateSemesterGpa()
        }
    }

    private suspend fun updateSemesterGpa() {
        val currentSemester = repository.getSemester() ?: return
        val currentCourses = repository.getCourses()
        
        val coursesWithGrades = currentCourses.filter { it.grade != null }
        val totalPoints = coursesWithGrades.sumOf { it.gradePoints }
        val totalCredits = coursesWithGrades.sumOf { it.creditHours }
        
        val calculatedGpa = if (totalCredits > 0) totalPoints / totalCredits else 0.0
        
        if (calculatedGpa != currentSemester.gpa) {
            repository.saveSemester(currentSemester.copy(gpa = calculatedGpa))
        }
    }
}
