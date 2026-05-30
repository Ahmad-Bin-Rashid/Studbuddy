package com.example.studbuddy.gpa

import androidx.lifecycle.*
import com.example.studbuddy.core.repository.StudBuddyRepository

class GpaViewModel(private val repository: StudBuddyRepository) : ViewModel() {

    val courses = repository.getCoursesFlow().asLiveData()
    val semester = repository.getSemesterFlow().asLiveData()

    val calculatedGpa: LiveData<Double> = courses.map { courseList ->
        val coursesWithGrades = courseList.filter { it.grade != null }
        val totalPoints = coursesWithGrades.sumOf { it.gradePoints }
        val totalCredits = coursesWithGrades.sumOf { it.creditHours }
        if (totalCredits > 0) totalPoints / totalCredits else 0.0
    }
}
