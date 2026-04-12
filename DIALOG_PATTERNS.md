# FILE: DIALOG_PATTERNS.md

# StudBuddy — Dialog Patterns

---

## 1. Core Rule

**All dialogs must use `LayoutInflater` to inflate a dedicated XML layout.** Inline view creation (`AlertDialog.Builder.setMessage()` alone, or programmatic `LinearLayout` building) is forbidden for any dialog that contains input fields.

---

## 2. Standard Dialog Structure

### Step 1: Create the layout file

File: `dialog_<purpose>.xml`  
Root element: `ScrollView` (required to handle small screens)

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="@dimen/spacing_medium">

        <!-- Label -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="@string/label_field_name"
            android:textSize="@dimen/text_size_body"
            android:textStyle="bold"
            android:textColor="@color/colorTextPrimary"
            android:layout_marginBottom="@dimen/spacing_xsmall" />

        <!-- Input -->
        <EditText
            android:id="@+id/editText<FieldName>"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="@dimen/spacing_medium"
            android:inputType="text"
            android:maxLength="100"
            android:hint="@string/hint_<fieldname>" />

    </LinearLayout>
</ScrollView>
```

### Step 2: Inflate and build in Activity

```kotlin
private fun showAddDialog() {
    // 1. Inflate
    val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_<feature>, null)

    // 2. Get view references
    val editTextName = dialogView.findViewById<EditText>(R.id.editText<FieldName>)
    val spinnerOption = dialogView.findViewById<Spinner>(R.id.spinnerOption)

    // 3. Populate dynamic data (Spinners)
    spinnerOption.adapter = ArrayAdapter(
        this,
        android.R.layout.simple_spinner_item,
        optionsList
    ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

    // 4. Build dialog — use setPositiveButton(null) if you need to prevent auto-dismiss
    val dialog = AlertDialog.Builder(this)
        .setTitle(getString(R.string.dialog_title_add_<feature>))
        .setView(dialogView)
        .setPositiveButton("Save", null)  // null = override below to prevent auto-dismiss on error
        .setNegativeButton("Cancel", null)
        .create()

    // 5. Override positive button to prevent dismiss on validation failure
    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val inputValue = editTextName.text.toString().trim()
            if (!validateInput(inputValue)) return@setOnClickListener  // keeps dialog open
            processInput(inputValue)
            dialog.dismiss()
        }
    }

    dialog.show()
}
```

---

## 3. Pre-filled Edit Dialog

```kotlin
private fun showEditDialog(item: <Model>) {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_<feature>, null)

    val editTextName = dialogView.findViewById<EditText>(R.id.editText<FieldName>)
    val spinnerOption = dialogView.findViewById<Spinner>(R.id.spinnerOption)

    // Pre-fill ALL fields with existing values
    editTextName.setText(item.name)

    // Pre-select Spinner value
    val options = listOf("Option A", "Option B", "Option C")
    spinnerOption.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    val currentIndex = options.indexOf(item.optionValue).coerceAtLeast(0)
    spinnerOption.setSelection(currentIndex)

    val dialog = AlertDialog.Builder(this)
        .setTitle("Edit <Feature>")
        .setView(dialogView)
        .setPositiveButton("Save", null)
        .setNegativeButton("Cancel", null)
        .create()

    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val updatedName = editTextName.text.toString().trim()
            if (!validateInput(updatedName)) return@setOnClickListener
            val updatedItem = item.copy(
                name = updatedName,
                optionValue = options[spinnerOption.selectedItemPosition]
            )
            updateItem(updatedItem)
            dialog.dismiss()
        }
    }

    dialog.show()
}
```

---

## 4. Confirmation Dialog (Delete)

Confirmation dialogs do NOT require a custom layout. Use `setMessage()` only for simple confirm/cancel dialogs:

```kotlin
private fun showDeleteConfirmation(item: <Model>) {
    AlertDialog.Builder(this)
        .setTitle("Delete <Feature>")
        .setMessage("Are you sure you want to delete \"${item.name}\"? This cannot be undone.")
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setPositiveButton("Delete") { _, _ ->
            deleteItem(item.id)
        }
        .setNegativeButton("Cancel", null)
        .show()
}
```

Rules for delete confirmation dialogs:
- Title: "Delete <FeatureName>"
- Message: Always include the item's name in quotes
- Positive button text: "Delete" (not "OK", not "Yes", not "Confirm")
- Negative button text: "Cancel"
- No custom layout needed

---

## 5. Date Picker Dialog

```kotlin
private fun showDatePicker(
    initialDate: Long = System.currentTimeMillis(),
    minDate: Long? = null,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialDate }

    val picker = DatePickerDialog(
        this,
        { _, year, month, dayOfMonth ->
            val selected = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 23, 59, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(selected.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    minDate?.let { picker.datePicker.minDate = it }

    picker.show()
}

// Usage:
showDatePicker(minDate = System.currentTimeMillis()) { selectedDate ->
    this.selectedDueDateMs = selectedDate
    textViewSelectedDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        .format(Date(selectedDate))
}
```

---

## 6. Time Picker Dialog

```kotlin
private fun showTimePicker(
    initialHour: Int = 9,
    initialMinute: Int = 0,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    TimePickerDialog(
        this,
        { _, hourOfDay, minute -> onTimeSelected(hourOfDay, minute) },
        initialHour,
        initialMinute,
        true  // 24-hour format
    ).show()
}

// Usage:
showTimePicker(initialHour = 9) { h, m ->
    selectedHour = h
    selectedMinute = m
    textViewExamTimeDisplay.text = String.format("%02d:%02d", h, m)
}
```

---

## 7. Info Dialog (No Input)

For read-only detail views or informational messages:

```kotlin
private fun showInfoDialog(title: String, message: String) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK", null)
        .show()
}
```

---

## 8. Dialog Checklist

Before merging any dialog code, verify:

- [ ] Layout file is named `dialog_<purpose>.xml`
- [ ] Root element is `ScrollView`
- [ ] LayoutInflater is used: `LayoutInflater.from(this).inflate(...)`
- [ ] All view references are stored in local variables before dialog is built
- [ ] Spinners use `setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)`
- [ ] Positive button uses `setPositiveButton("Save", null)` with `setOnShowListener` override (for input dialogs)
- [ ] Validation is called before `dialog.dismiss()`
- [ ] All string literals are in `res/values/strings.xml`
- [ ] `dialog.show()` is the last line of the function

---

## 9. Forbidden Dialog Patterns

| Pattern | Status | Reason |
|---|---|---|
| `setView(LinearLayout(context))` inline | FORBIDDEN | Must use inflated XML |
| `setMessage()` for input forms | FORBIDDEN | Use custom view |
| Accessing dialog views after `show()` without `setOnShowListener` | FORBIDDEN | Views not attached yet |
| `AlertDialog.Builder.create().show()` (both calls) | FORBIDDEN | Call `.show()` only, or `.create()` then `dialog.show()` — not both |
| Nested dialogs (opening dialog from dialog callback) | FORBIDDEN | Causes UX issues |
| Static dialog references | FORBIDDEN | Memory leaks |
