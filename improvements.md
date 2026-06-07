# StudBuddy Improvement Roadmap

This document outlines the prioritized steps to enhance the feature set of StudBuddy, focusing on a modern layout and best-in-class student productivity features.

---

## Phase 1: UI Foundation and Navigation Clarity

### Step 1: Unify the visual system
- Standardize spacing, corner radius, elevation, and typography across all screens.
- Use a single card language for semesters, courses, assignments, exams, notes, and dashboard widgets.
- Replace inconsistent icon styling with a coherent Material 3 icon set.

### Step 2: Make the app easier to navigate
- Simplify the drawer so it clearly separates primary destinations from secondary settings.
- Highlight the active semester and active course more prominently in the app shell.
- Keep page actions in the same place across the app, especially sort, add, edit, and delete actions.

### Step 3: Improve global app feedback
- Replace generic loading states with screen-specific loading and empty states.
- Show clearer success, warning, and error feedback using consistent banners or snackbars.
- Add confirmation dialogs only where the action is destructive or irreversible.

## Phase 2: Home Dashboard and Overview Screens

### Step 1: Redesign the home screen as a true dashboard
- Show the current semester, next class, urgent assignments, upcoming exams, and attendance warnings first.
- Use visually distinct summary cards instead of a mostly empty container layout.
- Reduce scrolling by prioritizing the most useful information above the fold.

### Step 2: Improve the semester screen
- Make semester cards more informative with name, dates, GPA, and active-state indicators.
- Add better sorting controls with clear labels and visible current sort state.
- Add empty-state guidance and a stronger floating add action.

### Step 3: Improve the courses screen
- Make course cards easier to scan with clearer hierarchy for title, credit hours, grade, and GP.
- Add search and sorting controls so users can quickly find a course.
- Ensure the active semester context is obvious when entering the page.

## Phase 3: Detail Pages and Task Management

### Step 1: Turn course details into a focused hub
- Present course summary information in a dashboard-style layout instead of a simple text stack.
- Keep the course actions menu visible and predictable.
- Make the bottom navigation between summary, attendance, notes, assignments, and exams feel cohesive.

### Step 2: Improve attendance, assignments, and exams views
- Group pending and completed items with stronger section headers and count indicators.
- Make status colors consistent across assignments and exams, especially for pending and completed states.
- Add better sorting by due date, time, and priority so lists are more actionable.

### Step 3: Polish the notes experience
- Use a clearer note card design with file type, file size, and date.
- Make add, open, and delete actions obvious without overloading the card.
- Show that notes are stored locally and not synced, but keep that message unobtrusive.

## Phase 4: Data Entry, Empty States, and Accessibility

### Step 1: Improve forms and dialogs
- Make add/edit dialogs feel lighter and more structured with better grouping and spacing.
- Use sensible defaults where possible, especially for dates, semester selection, and status values.
- Reduce form friction by using bottom sheets or full-width dialogs for complex actions.

### Step 2: Upgrade empty states
- Replace the generic empty state with contextual messages per screen.
- Give each empty state a direct next action such as add semester, add course, or add note.
- Use illustrations or clearer iconography to make empty states feel intentional instead of blank.

### Step 3: Improve accessibility and readability
- Increase text contrast and make body text easier to read on both light and dark themes.
- Ensure touch targets are large enough for menus, icons, and floating buttons.
- Support dynamic layouts that stay usable on smaller phones and larger tablets.

## Phase 5: Visual Polish and Quality Pass

### Step 1: Refine animations and transitions
- Add subtle motion for screen entry, card expansion, and sheet transitions.
- Keep animations restrained so they support clarity rather than distract from it.

### Step 2: Strengthen consistency across settings and profile
- Bring profile and settings screens in line with the rest of the app’s card and spacing system.
- Make the most important actions, such as theme changes and sign-in, visually prominent.

### Step 3: Finish with QA and usability testing
- Review every main screen for visual balance, spacing, and readability.
- Test common student flows end to end, including adding a semester, adding a course, tracking attendance, and attaching notes.
- Fix any navigation dead ends, duplicated actions, or unclear labels before release.
