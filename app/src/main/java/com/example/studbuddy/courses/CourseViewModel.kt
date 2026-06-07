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
class CourseViewModel @Inject constructor(
    private val repository: StudBuddyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val semesterId: String? = savedStateHandle["semesterId"]
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<CourseUiState> = combine(
        if (semesterId != null) repository.getCoursesBySemesterFlow(semesterId) else repository.getCoursesFlow(),
        if (semesterId != null) repository.getAllSemestersFlow().map { list -> list.find { it.id == semesterId } } else repository.getActiveSemesterFlow(),
        _isLoading
    ) { courses, semester, loading ->
        CourseUiState(courses, semester, loading)
    }.onEach {
        _isLoading.value = false
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CourseUiState(isLoading = true)
    )

    fun addCourse(course: Course) {
        viewModelScope.launch {
            repository.addCourse(course)
            uiState.value.semester?.let { updateSemesterGpa(it.id) }
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            repository.updateCourse(course)
            uiState.value.semester?.let { updateSemesterGpa(it.id) }
        }
    }

    private suspend fun updateSemesterGpa(id: String) {
        val currentSemester = repository.getAllSemestersFlow().first().find { it.id == id } ?: return
        val currentCourses = repository.getCoursesBySemester(id)
        
        val coursesWithGrades = currentCourses.filter { it.grade != null }
        val totalPoints = coursesWithGrades.sumOf { it.gradePoints }
        val totalCredits = coursesWithGrades.sumOf { it.creditHours }
        
        val calculatedGpa = if (totalCredits > 0) totalPoints / totalCredits else 0.0
        
        if (calculatedGpa != currentSemester.gpa) {
            repository.updateSemester(currentSemester.copy(gpa = calculatedGpa))
        }
    }
}
