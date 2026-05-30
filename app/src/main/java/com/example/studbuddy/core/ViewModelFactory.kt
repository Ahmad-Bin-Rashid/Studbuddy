package com.example.studbuddy.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.studbuddy.core.repository.StudBuddyRepository
import com.example.studbuddy.home.MainViewModel
import com.example.studbuddy.courses.CourseViewModel
import com.example.studbuddy.timetable.TimetableViewModel
import com.example.studbuddy.attendance.AttendanceViewModel
import com.example.studbuddy.assignments.AssignmentsViewModel
import com.example.studbuddy.exams.ExamsViewModel
import com.example.studbuddy.gpa.GpaViewModel

class ViewModelFactory(private val repository: StudBuddyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(repository) as T
            modelClass.isAssignableFrom(CourseViewModel::class.java) -> CourseViewModel(repository) as T
            modelClass.isAssignableFrom(TimetableViewModel::class.java) -> TimetableViewModel(repository) as T
            modelClass.isAssignableFrom(AttendanceViewModel::class.java) -> AttendanceViewModel(repository) as T
            modelClass.isAssignableFrom(AssignmentsViewModel::class.java) -> AssignmentsViewModel(repository) as T
            modelClass.isAssignableFrom(ExamsViewModel::class.java) -> ExamsViewModel(repository) as T
            modelClass.isAssignableFrom(GpaViewModel::class.java) -> GpaViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
