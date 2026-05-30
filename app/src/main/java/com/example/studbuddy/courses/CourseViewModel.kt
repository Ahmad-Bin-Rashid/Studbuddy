package com.example.studbuddy.courses

import androidx.lifecycle.*
import com.example.studbuddy.core.models.Course
import com.example.studbuddy.core.models.Semester
import com.example.studbuddy.core.repository.StudBuddyRepository
import kotlinx.coroutines.launch

class CourseViewModel(private val repository: StudBuddyRepository) : ViewModel() {

    val courses = repository.getCoursesFlow().asLiveData()
    val semester = repository.getSemesterFlow().asLiveData()

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
