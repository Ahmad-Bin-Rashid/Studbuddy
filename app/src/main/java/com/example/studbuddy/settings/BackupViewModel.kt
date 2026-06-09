package com.example.studbuddy.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studbuddy.core.SettingsManager
import com.example.studbuddy.core.repository.DriveBackupRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BackupStatus { IDLE, BACKING_UP, RESTORING, SUCCESS_BACKUP, SUCCESS_RESTORE, ERROR, NOT_SIGNED_IN }

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val backupRepository: DriveBackupRepository
) : ViewModel() {

    val lastBackupTime: StateFlow<Long> = settingsManager.lastSyncTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _backupStatus = MutableStateFlow(BackupStatus.IDLE)
    val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()

    val isSignedIn: Boolean
        get() = FirebaseAuth.getInstance().currentUser != null

    fun backup() {
        if (!isSignedIn) {
            _backupStatus.value = BackupStatus.NOT_SIGNED_IN
            return
        }

        viewModelScope.launch {
            _backupStatus.value = BackupStatus.BACKING_UP
            val success = backupRepository.backup()
            if (success) {
                _backupStatus.value = BackupStatus.SUCCESS_BACKUP
                Log.d("BackupViewModel", "Backup succeeded")
            } else {
                _backupStatus.value = BackupStatus.ERROR
            }
        }
    }

    fun restore() {
        if (!isSignedIn) {
            _backupStatus.value = BackupStatus.NOT_SIGNED_IN
            return
        }

        viewModelScope.launch {
            _backupStatus.value = BackupStatus.RESTORING
            val success = backupRepository.restore()
            if (success) {
                _backupStatus.value = BackupStatus.SUCCESS_RESTORE
                Log.d("BackupViewModel", "Restore succeeded")
            } else {
                _backupStatus.value = BackupStatus.ERROR
            }
        }
    }
}
