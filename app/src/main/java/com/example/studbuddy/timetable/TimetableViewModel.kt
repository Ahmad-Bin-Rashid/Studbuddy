package com.example.studbuddy.timetable

import androidx.lifecycle.*
import com.example.studbuddy.core.models.Course
import com.example.studbuddy.core.models.TimetableEntry
import com.example.studbuddy.core.repository.StudBuddyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TimetableUiState(
    val timetable: List<TimetableEntry> = emptyList(),
    val courses: List<Course> = emptyList()
)

class TimetableViewModel(private val repository: StudBuddyRepository) : ViewModel() {

    val uiState: StateFlow<TimetableUiState> = combine(
        repository.getTimetableFlow(),
        repository.getCoursesFlow()
    ) { timetable, courses ->
        TimetableUiState(timetable, courses)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimetableUiState()
    )

    fun addTimetableEntry(entry: TimetableEntry) {
        viewModelScope.launch {
            repository.addTimetableEntry(entry)
        }
    }

    fun updateTimetableEntry(entry: TimetableEntry) {
        viewModelScope.launch {
            repository.updateTimetableEntry(entry)
        }
    }

    fun deleteTimetableEntry(entryId: String) {
        viewModelScope.launch {
            repository.deleteTimetableEntry(entryId)
        }
    }
}
