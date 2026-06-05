package com.example.studbuddy.assignments

import androidx.lifecycle.*
import com.example.studbuddy.core.models.Assignment
import com.example.studbuddy.core.models.Course
import com.example.studbuddy.core.repository.StudBuddyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AssignmentsUiState(
    val assignments: List<Assignment> = emptyList(),
    val courses: List<Course> = emptyList()
)

class AssignmentsViewModel(private val repository: StudBuddyRepository) : ViewModel() {

    val uiState: StateFlow<AssignmentsUiState> = combine(
        repository.getAssignmentsFlow(),
        repository.getCoursesFlow()
    ) { assignments, courses ->
        AssignmentsUiState(assignments, courses)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AssignmentsUiState()
    )

    fun updateAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.updateAssignment(assignment)
            updateCourseMarks(assignment.courseId)
        }
    }

    fun deleteAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment.id)
            updateCourseMarks(assignment.courseId)
        }
    }

    private suspend fun updateCourseMarks(courseId: String) {
        val courses = repository.getCourses()
        val course = courses.find { it.id == courseId } ?: return
        
        val allAssignments = repository.getAssignments()
        val courseAssignments = allAssignments.filter { it.courseId == courseId && it.isCompleted }
        
        val totalMarksFromAssignments = courseAssignments.sumOf { 
            if (it.totalMarks > 0) (it.obtainedMarks ?: 0.0) / it.totalMarks * it.weightage else 0.0 
        }
        
        // This only updates assignment marks. Exams and Attendance also contribute.
        // For a full implementation, we'd need to sum all components.
        // For now, we'll keep the logic as it was (partially updating).
        
        repository.updateCourse(course.copy(marks = totalMarksFromAssignments))
    }
}
