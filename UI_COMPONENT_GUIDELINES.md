# FILE: UI_COMPONENT_GUIDELINES.md

# StudBuddy — UI Component Guidelines

---

## 1. Design System Overview

All UI components must use the shared design system defined in `res/values/`. No hardcoded values allowed anywhere in any layout file.

---

## 2. Color System

### Color Definitions (`res/values/colors.xml`)

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Brand -->
    <color name="colorPrimary">#1565C0</color>
    <color name="colorPrimaryDark">#003c8f</color>
    <color name="colorPrimaryLight">#5e92f3</color>
    <color name="colorAccent">#42A5F5</color>

    <!-- Status / State Colors -->
    <color name="colorStatusNormal">#4CAF50</color>      <!-- Green — OK, present, on time -->
    <color name="colorStatusWarning">#FF9800</color>     <!-- Orange — warning, late, low attendance -->
    <color name="colorStatusCritical">#F44336</color>    <!-- Red — overdue, absent, <60% attendance -->
    <color name="colorStatusOverdue">#B71C1C</color>     <!-- Dark Red — severely overdue -->
    <color name="colorStatusComplete">#9E9E9E</color>    <!-- Grey — completed item -->

    <!-- Priority Colors -->
    <color name="colorPriorityHigh">#F44336</color>
    <color name="colorPriorityMedium">#FF9800</color>
    <color name="colorPriorityLow">#4CAF50</color>

    <!-- Attendance Threshold Colors -->
    <color name="colorAttendanceGood">#4CAF50</color>   <!-- ≥75% -->
    <color name="colorAttendanceMid">#FF9800</color>    <!-- 60–74% -->
    <color name="colorAttendanceLow">#F44336</color>    <!-- <60% -->

    <!-- Backgrounds -->
    <color name="colorBackground">#F5F5F5</color>
    <color name="colorCardBackground">#FFFFFF</color>
    <color name="colorSurfaceElevated">#FFFFFF</color>

    <!-- Text -->
    <color name="colorTextPrimary">#212121</color>
    <color name="colorTextSecondary">#757575</color>
    <color name="colorTextHint">#BDBDBD</color>
    <color name="colorTextOnPrimary">#FFFFFF</color>

    <!-- Divider & Borders -->
    <color name="colorDivider">#E0E0E0</color>
    <color name="colorBorder">#BDBDBD</color>

    <!-- Empty State -->
    <color name="colorEmptyState">#BDBDBD</color>

    <!-- Course Color Palette (for Timetable entries) -->
    <color name="courseColor1">#1565C0</color>
    <color name="courseColor2">#6A1B9A</color>
    <color name="courseColor3">#00695C</color>
    <color name="courseColor4">#E65100</color>
    <color name="courseColor5">#AD1457</color>
    <color name="courseColor6">#283593</color>
    <color name="courseColor7">#558B2F</color>
    <color name="courseColor8">#4E342E</color>
</resources>
```

### Color Usage Rules

| Context | Color to Use |
|---|---|
| On-time / normal / present | `colorStatusNormal` |
| Warning / near deadline / late | `colorStatusWarning` |
| Overdue / absent / critical | `colorStatusCritical` |
| Severely overdue (>3 days past) | `colorStatusOverdue` |
| Completed/inactive item | `colorStatusComplete` |
| High priority badge | `colorPriorityHigh` |
| Medium priority badge | `colorPriorityMedium` |
| Low priority badge | `colorPriorityLow` |
| Attendance ≥75% | `colorAttendanceGood` |
| Attendance 60–74% | `colorAttendanceMid` |
| Attendance <60% | `colorAttendanceLow` |

---

## 3. Typography

All text sizes defined in `res/values/dimens.xml` and must be used via `@dimen/`:

```xml
<dimen name="text_size_caption">11sp</dimen>
<dimen name="text_size_small">12sp</dimen>
<dimen name="text_size_body">14sp</dimen>
<dimen name="text_size_subtitle">15sp</dimen>
<dimen name="text_size_title">16sp</dimen>
<dimen name="text_size_headline">18sp</dimen>
<dimen name="text_size_display">20sp</dimen>
<dimen name="text_size_gpa_large">48sp</dimen>
```

| Text Role | Size | Style | Color |
|---|---|---|---|
| Screen title (Toolbar) | System default | Bold | White (on primary) |
| Item title / Course name | `text_size_title` (16sp) | Bold | `colorTextPrimary` |
| Item subtitle | `text_size_body` (14sp) | Normal | `colorTextSecondary` |
| Caption / metadata | `text_size_small` (12sp) | Normal | `colorTextSecondary` |
| Badge text | `text_size_caption` (11sp) | Bold | White |
| GPA value (summary card) | `text_size_gpa_large` (48sp) | Bold | `colorPrimary` |
| Section header | `text_size_headline` (18sp) | Bold | `colorTextPrimary` |

---

## 4. CardView Rules

### Required Attributes

```xml
<androidx.cardview.widget.CardView
    android:id="@+id/cardView<Name>"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="@dimen/card_margin"
    app:cardCornerRadius="@dimen/card_corner_radius"
    app:cardElevation="@dimen/card_elevation"
    app:cardBackgroundColor="@color/colorCardBackground">

    <!-- ALWAYS a single direct child, always LinearLayout or RelativeLayout -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="@dimen/card_padding"
        android:orientation="vertical">

        <!-- Content here -->

    </LinearLayout>
</androidx.cardview.widget.CardView>
```

### CardView Rules

| Rule | Value |
|---|---|
| Corner radius | `@dimen/card_corner_radius` (8dp) |
| Elevation | `@dimen/card_elevation` (4dp) — summary cards: 6dp |
| Margin | `@dimen/card_margin` (8dp) |
| Internal padding | `@dimen/card_padding` (16dp) |
| Background | `@color/colorCardBackground` (#FFFFFF) |
| Direct children | Exactly 1 — always a layout container |

### Status-Indicating Cards

When a card represents a status (overdue, warning, complete), change the **left border stripe** (a `View` element), NOT the CardView background:

```xml
<!-- Left color stripe — inside the CardView's LinearLayout -->
<View
    android:id="@+id/viewColorStripe"
    android:layout_width="4dp"
    android:layout_height="match_parent"
    android:background="@color/colorStatusCritical" />
```

Do NOT change `app:cardBackgroundColor` for status indication.

---

## 5. RecyclerView Rules

```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView<Feature>"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:clipToPadding="false"
    android:paddingBottom="72dp"
    tools:listitem="@layout/item_<feature>" />
```

- `paddingBottom="72dp"` — always include so FAB does not cover last item
- `clipToPadding="false"` — allows content to scroll under FAB
- No `dividerItemDecoration` unless explicitly approved — CardView provides visual separation

---

## 6. FloatingActionButton Rules

```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fab<Action>"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="bottom|end"
    android:layout_margin="@dimen/spacing_large"
    android:src="@drawable/ic_add"
    app:tint="@color/colorTextOnPrimary" />
```

- Always bottom-right (`gravity: bottom|end`)
- Icon: `@drawable/ic_add` for all add actions
- Margin: `@dimen/spacing_large` (24dp)

---

## 7. Toolbar Rules

```xml
<com.google.android.material.appbar.AppBarLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar<Feature>"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="@color/colorPrimary"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar"
        app:title="@string/<feature>_screen_title"
        app:titleTextColor="@color/colorTextOnPrimary"
        app:navigationIcon="@drawable/ic_arrow_back" />
</com.google.android.material.appbar.AppBarLayout>
```

Every module Activity must call:
```kotlin
setSupportActionBar(toolbar<Feature>)
supportActionBar?.setDisplayHomeAsUpEnabled(true)
```

---

## 8. Empty State View Rules

```xml
<TextView
    android:id="@+id/textViewEmpty<Feature>"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_centerInParent="true"
    android:gravity="center"
    android:padding="@dimen/spacing_large"
    android:textColor="@color/colorEmptyState"
    android:textSize="@dimen/text_size_subtitle"
    android:text="@string/<feature>_empty_state"
    android:visibility="gone"
    tools:visibility="visible" />
```

- Always center in parent
- Text color: `colorEmptyState`
- Initial visibility: `gone` — shown programmatically only
- `tools:visibility="visible"` for layout preview only

---

## 9. Spinner Rules

```xml
<Spinner
    android:id="@+id/spinner<Name>"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="@dimen/spacing_small"
    android:background="@drawable/spinner_background"
    android:padding="@dimen/spacing_small" />
```

Adapter binding (Activity code):
```kotlin
val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, optionsList)
adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
spinner.adapter = adapter
```

**Always call `setDropDownViewResource`** — mandatory.

---

## 10. RadioGroup / RadioButton Rules

```xml
<RadioGroup
    android:id="@+id/radioGroup<Name>"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="@dimen/spacing_small">

    <RadioButton
        android:id="@+id/radio<Option>"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="@string/<option>" />
</RadioGroup>
```

- Horizontal layout for 2–3 options
- Use `layout_weight="1"` for equal spacing
- First option is checked by default in XML: `android:checked="true"`

---

## 11. EditText Rules

```xml
<EditText
    android:id="@+id/editText<Name>"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="@dimen/spacing_xsmall"
    android:layout_marginBottom="@dimen/spacing_medium"
    android:inputType="text"
    android:maxLength="100"
    android:hint="@string/<feature>_hint_<field>"
    android:background="@drawable/edittext_background" />
```

- Always set `maxLength`
- Always set `hint` from string resource
- Never use `android:ems` for width — use `match_parent`

---

## 12. CheckBox Rules (Assignments Module)

```xml
<CheckBox
    android:id="@+id/checkBox<Name>"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:buttonTint="@color/colorPrimary" />
```

In Adapter `onBindViewHolder`, always suppress listener before setting state:
```kotlin
holder.checkBox.setOnCheckedChangeListener(null)
holder.checkBox.isChecked = item.isCompleted
holder.checkBox.setOnCheckedChangeListener { _, checked -> callback(item, checked) }
```

---

## 13. ProgressBar Rules (Attendance Module)

```xml
<ProgressBar
    android:id="@+id/progressBar<Name>"
    style="@style/Widget.AppCompat.ProgressBar.Horizontal"
    android:layout_width="match_parent"
    android:layout_height="8dp"
    android:layout_marginTop="@dimen/spacing_small"
    android:max="100"
    android:progress="0" />
```

Progress tint is set programmatically based on attendance percentage:
```kotlin
progressBar.progressTintList = ColorStateList.valueOf(color)
```

---

## 14. Badge / Status Label Rules

Small status labels (e.g., "HIGH", "P", "A"):

```xml
<TextView
    android:id="@+id/textViewBadge<Name>"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:padding="4dp"
    android:textColor="@color/colorTextOnPrimary"
    android:textSize="@dimen/text_size_caption"
    android:textStyle="bold" />
```

Background set programmatically:
```kotlin
textViewBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPriorityHigh))
```

---

## 15. Spacing Rules

| Use Case | Value |
|---|---|
| Between sibling views | `@dimen/spacing_small` (8dp) |
| Section gaps | `@dimen/spacing_medium` (16dp) |
| Screen edges | `@dimen/spacing_medium` (16dp) |
| Card internal padding | `@dimen/card_padding` (16dp) |
| Card margin | `@dimen/card_margin` (8dp) |
| FAB margin | `@dimen/spacing_large` (24dp) |
| Dialog padding | `@dimen/spacing_medium` (16dp) |
| Between label and input | `@dimen/spacing_xsmall` (4dp) |
