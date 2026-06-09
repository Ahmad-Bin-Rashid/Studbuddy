package com.example.studbuddy.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.studbuddy.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@AndroidEntryPoint
class BackupFragment : Fragment() {

    private val viewModel: BackupViewModel by viewModels()

    private lateinit var txtSyncStatus: TextView
    private lateinit var txtLastSync: TextView
    private lateinit var btnBackup: MaterialButton
    private lateinit var btnRestore: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_backup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtSyncStatus = view.findViewById(R.id.txtSyncStatus)
        txtLastSync   = view.findViewById(R.id.txtLastSyncTime)
        btnBackup     = view.findViewById(R.id.btnBackupNow)
        btnRestore    = view.findViewById(R.id.btnRestoreNow)

        // Set initial state
        if (!viewModel.isSignedIn) {
            txtSyncStatus.text = "Not signed in — go to Profile to sign in"
            btnBackup.isEnabled = false
            btnRestore.isEnabled = false
        }

        // Observe last sync/backup time
        lifecycleScope.launch {
            viewModel.lastBackupTime.collect { timestamp ->
                txtLastSync.text = if (timestamp == 0L) {
                    "Last backed up: Never"
                } else {
                    val formatted = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(Date(timestamp))
                    "Last backed up: $formatted"
                }
            }
        }

        // Observe backup status and update UI
        lifecycleScope.launch {
            viewModel.backupStatus.collect { status ->
                when (status) {
                    BackupStatus.IDLE -> {
                        txtSyncStatus.text = if (viewModel.isSignedIn) "Ready to backup or restore" else "Not signed in — go to Profile to sign in"
                        txtSyncStatus.setTextColor(getAttrColor(com.google.android.material.R.attr.colorOnSurfaceVariant))
                        btnBackup.isEnabled = viewModel.isSignedIn
                        btnRestore.isEnabled = viewModel.isSignedIn
                    }
                    BackupStatus.BACKING_UP -> {
                        txtSyncStatus.text = "Backing up to Google Drive\u2026"
                        txtSyncStatus.setTextColor(getAttrColor(com.google.android.material.R.attr.itemTextColor))
                        btnBackup.isEnabled = false
                        btnRestore.isEnabled = false
                    }
                    BackupStatus.RESTORING -> {
                        txtSyncStatus.text = "Restoring database from Google Drive\u2026"
                        txtSyncStatus.setTextColor(getAttrColor(com.google.android.material.R.attr.itemTextColor))
                        btnBackup.isEnabled = false
                        btnRestore.isEnabled = false
                    }
                    BackupStatus.SUCCESS_BACKUP -> {
                        txtSyncStatus.text = "Backup completed successfully \u2714"
                        txtSyncStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.sync_success))
                        btnBackup.isEnabled = true
                        btnRestore.isEnabled = true
                    }
                    BackupStatus.SUCCESS_RESTORE -> {
                        txtSyncStatus.text = "Database restored successfully \u2714"
                        txtSyncStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.sync_success))
                        btnBackup.isEnabled = true
                        btnRestore.isEnabled = true
                        Toast.makeText(context, "Data successfully restored!", Toast.LENGTH_LONG).show()
                    }
                    BackupStatus.ERROR -> {
                        txtSyncStatus.text = "Action failed — make sure Drive permission is granted"
                        txtSyncStatus.setTextColor(getAttrColor(com.google.android.material.R.attr.errorTextColor))
                        btnBackup.isEnabled = true
                        btnRestore.isEnabled = true
                    }
                    BackupStatus.NOT_SIGNED_IN -> {
                        txtSyncStatus.text = "Not signed in — go to Profile to sign in"
                        txtSyncStatus.setTextColor(getAttrColor(com.google.android.material.R.attr.errorTextColor))
                        btnBackup.isEnabled = false
                        btnRestore.isEnabled = false
                    }
                }
            }
        }

        btnBackup.setOnClickListener {
            viewModel.backup()
        }

        btnRestore.setOnClickListener {
            showRestoreConfirmDialog()
        }
    }

    private fun showRestoreConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Restore Database?")
            .setMessage("This will download the backup from Google Drive and OVERWRITE your current local data. This action is irreversible.")
            .setPositiveButton("Restore") { _, _ ->
                viewModel.restore()
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }

    private fun getAttrColor(attr: Int): Int {
        val typedArray = requireContext().obtainStyledAttributes(intArrayOf(attr))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }
}
