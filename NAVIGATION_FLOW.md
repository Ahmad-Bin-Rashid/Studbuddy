# NAVIGATION_FLOW.md

# StudBuddy — Navigation Flow

---

## 1. Navigation Architecture

StudBuddy uses a **Custom Overlay Sidebar** for navigation. Since standard drawer components are not used, the sidebar is implemented as a high-elevation view within each Activity's `ConstraintLayout`.

### Rules
- **Activity-Based**: Each section of the app is a separate `Activity`.
- **Custom Sidebar**: Every activity layout includes a "Sidebar" container (`CardView`) that is toggled via an icon.
- **Intent Navigation**: Navigation between modules is handled exclusively via explicit `Intent`s.
- **Single Top**: Activities use `launchMode="singleTop"` with `FLAG_ACTIVITY_REORDER_TO_FRONT` to maintain a clean back-stack.

---

## 2. Sidebar Structure (Custom Implementation)

The sidebar is a vertical container managed via visibility toggles (`View.VISIBLE` / `View.GONE`).

- **Header**: `ImageView` (Logo) + `TextView` (Student Name).
- **Navigation List**: `LinearLayout` containing clickable `TextView`s:
    - Home (`MainActivity`)
    - Courses (`CourseActivity`)
    - Attendance (`AttendanceActivity`)
    - Timetable (`TimetableActivity`)
    - Assignments (`AssignmentsActivity`)
    - Exams (`ExamsActivity`)
    - GPA (`GpaActivity`)
- **Footer**: `TextView` for **Settings**, positioned at the bottom using `layout_gravity` or `ConstraintLayout` bias.

### Sidebar Toggle Logic
- **Open**: Top-left `ImageView` (Menu Icon) -> `sidebarContainer.visibility = View.VISIBLE`.
- **Close**: 
    - Click on any navigation item.
    - Click on a "Close" icon inside the sidebar.
    - Back button press (handled in `onBackPressed`).

---

## 3. Semester Initialization Flow

1. **First Launch**: App opens `MainActivity`.
2. **Detection**: `AppDataStore` checks for existing semester data.
3. **Empty State**: If no semester is found, Dashboard shows a "Setup Semester" button.
4. **Input**: User enters start date, end date, and target GPA.
5. **Validation**: Total Credit Hours are auto-calculated (initially 0).
6. **Activation**: Once saved, the "Courses" module is unlocked.

---

## 4. UI Layout Pattern (Pseudo-XML)

```xml
<ConstraintLayout>
    <!-- 1. Main Content Area -->
    <LinearLayout android:id="@+id/mainContentContainer">
        <ImageView android:id="@+id/btnOpenSidebar" />
        <!-- Module UI Content Here -->
    </LinearLayout>

    <!-- 2. Overlay Sidebar (Hidden) -->
    <CardView 
        android:id="@+id/sidebarOverlay"
        android:visibility="gone"
        app:cardElevation="10dp"
        app:layout_constraintStart_toStartOf="parent">
        
        <ScrollView>
            <LinearLayout android:orientation="vertical">
                 <TextView android:id="@+id/navHome" />
                 <TextView android:id="@+id/navCourses" />
                 <!-- ... other items -->
            </LinearLayout>
        </ScrollView>
    </CardView>
</ConstraintLayout>
```
