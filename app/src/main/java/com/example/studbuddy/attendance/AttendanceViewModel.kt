package com.example.studbuddy.attendance

import androidx.lifecycle.*
import com.example.studbuddy.core.models.AttendanceRecord
import com.example.studbuddy.core.models.Course
import com.example.studbuddy.core.repository.StudBuddyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AttendanceUiState(
    val courses: List<Course> = emptyList(),
    val attendance: List<AttendanceRecord> = emptyList()
)

class AttendanceViewModel(private val repository: StudBuddyRepository) : ViewModel() {

    val uiState: StateFlow<AttendanceUiState> = combine(
        repository.getCoursesFlow(),
        repository.getAttendanceFlow()
    ) { courses, attendance ->
        AttendanceUiState(courses, attendance)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AttendanceUiState()
    )

    fun addAttendanceRecord(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.addAttendanceRecord(record)
        }
    }

    fun updateAttendanceRecord(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.updateAttendanceRecord(record)
        }
    }

    fun deleteAttendanceRecord(recordId: String) {
        viewModelScope.launch {
            repository.deleteAttendanceRecord(recordId)
        }
    }
}
