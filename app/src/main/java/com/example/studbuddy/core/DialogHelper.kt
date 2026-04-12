package com.example.studbuddy.core

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.studbuddy.R

/**
 * Helper object for displaying reusable dialog patterns.
 * Follows DIALOG_PATTERNS.md documentation.
 */
object DialogHelper {

    /**
     * Shows a sample dialog with two input fields.
     */
    fun showSampleInputDialog(
        context: Context,
        title: String,
        onSave: (title: String, subtitle: String) -> Unit
    ) {
        // 1. Inflate
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_sample, null)

        // 2. Get view references
        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextSampleTitle)
        val editTextSubtitle = dialogView.findViewById<EditText>(R.id.editTextSampleSubtitle)

        // 3. Build dialog
        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save", null) // Set to null to override click listener later
            .setNegativeButton("Cancel", null)
            .create()

        // 4. Override positive button to perform validation without auto-dismiss
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val inputTitle = editTextTitle.text.toString().trim()
                val inputSubtitle = editTextSubtitle.text.toString().trim()

                if (inputTitle.isEmpty()) {
                    editTextTitle.error = "Title cannot be empty"
                    return@setOnClickListener
                }

                onSave(inputTitle, inputSubtitle)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    /**
     * Shows a standard confirmation dialog for deletion.
     */
    fun showDeleteConfirmation(
        context: Context,
        itemName: String,
        onDelete: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete \"$itemName\"? This cannot be undone.")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Delete") { _, _ ->
                onDelete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
