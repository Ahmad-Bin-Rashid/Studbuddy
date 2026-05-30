package com.example.studbuddy.attendance

import androidx.lifecycle.*
import com.example.studbuddy.core.models.AttendanceRecord
import com.example.studbuddy.core.models.Course
import com.example.studbuddy.core.repository.StudBuddyRepository
import kotlinx.coroutines.launch

class AttendanceViewModel(private val repository: StudBuddyRepository) : ViewModel() {

    val courses = repository.getCoursesFlow().asLiveData()
    val attendance = repository.getAttendanceFlow().asLiveData()

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
