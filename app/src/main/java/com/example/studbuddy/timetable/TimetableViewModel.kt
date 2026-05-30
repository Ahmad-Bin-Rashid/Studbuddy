package com.example.studbuddy.timetable

import androidx.lifecycle.*
import com.example.studbuddy.core.models.TimetableEntry
import com.example.studbuddy.core.repository.StudBuddyRepository
import kotlinx.coroutines.launch

class TimetableViewModel(private val repository: StudBuddyRepository) : ViewModel() {

    val timetable = repository.getTimetableFlow().asLiveData()
    val courses = repository.getCoursesFlow().asLiveData()

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
