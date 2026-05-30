package com.example.studbuddy.home

import androidx.lifecycle.*
import com.example.studbuddy.core.models.*
import com.example.studbuddy.core.repository.StudBuddyRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*

data class DashboardUiState(
    val semester: Semester? = null,
    val courses: List<Course> = emptyList(),
    val timetable: List<TimetableEntry> = emptyList(),
    val attendance: List<AttendanceRecord> = emptyList(),
    val assignments: List<Assignment> = emptyList(),
    val exams: List<Exam> = emptyList()
)

class MainViewModel(private val repository: StudBuddyRepository) : ViewModel() {

    private val _uiState = MediatorLiveData<DashboardUiState>()
    val uiState: LiveData<DashboardUiState> = _uiState

    init {
        _uiState.value = DashboardUiState()
        
        val semesterFlow = repository.getSemesterFlow().asLiveData()
        val coursesFlow = repository.getCoursesFlow().asLiveData()
        val timetableFlow = repository.getTimetableFlow().asLiveData()
        val attendanceFlow = repository.getAttendanceFlow().asLiveData()
        val assignmentsFlow = repository.getAssignmentsFlow().asLiveData()
        val examsFlow = repository.getExamsFlow().asLiveData()

        _uiState.addSource(semesterFlow) { _uiState.value = _uiState.value?.copy(semester = it) }
        _uiState.addSource(coursesFlow) { _uiState.value = _uiState.value?.copy(courses = it) }
        _uiState.addSource(timetableFlow) { _uiState.value = _uiState.value?.copy(timetable = it) }
        _uiState.addSource(attendanceFlow) { _uiState.value = _uiState.value?.copy(attendance = it) }
        _uiState.addSource(assignmentsFlow) { _uiState.value = _uiState.value?.copy(assignments = it) }
        _uiState.addSource(examsFlow) { _uiState.value = _uiState.value?.copy(exams = it) }
    }

    fun saveSemester(semester: Semester) {
        viewModelScope.launch {
            repository.saveSemester(semester)
        }
    }
}
