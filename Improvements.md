# StudBuddy Improvement Roadmap

This document outlines the prioritized steps to refactor the architecture and enhance the feature set of StudBuddy.

## Phase 1. User Management & Google Integration (Priority)

### Step 1: Local Profile & Guest Mode
*   **User Model**: Create a `User` data class to store `id`, `displayName`, `email`, `profileImageUri`, and `authStatus`.
*   **UserManager**: Implement a manager to handle the current user's state (stored in SharedPreferences/DataStore).
*   **Sidebar Integration**: Add a "Profile" entry in `nav_menu.xml` and update `nav_header.xml` to show user info.
*   **Profile UI**: Create `ProfileFragment` where users can:
    *   Set a guest username.
    *   Upload/choose a local profile image.
    *   View their current sync status.

### Step 2: Google Sign-In Integration
*   **Dependency**: Add Google Play Services Auth dependency.
*   **Auth Flow**: Implement Google Sign-In client and handle the result in `ProfileFragment`.
*   **Account Linking**: Update `UserManager` to transition from "Guest" to "Signed In" state, merging local profile data with Google account info.

### Step 3: Data Synchronization
*   **Cloud Backend**: Use Firebase Auth and Firestore for secure data storage.
*   **Sync Logic**: 
    *   Implement `SyncRepository` to handle bidirectional sync between Room (local) and Firestore (cloud).
    *   Use **WorkManager** to trigger sync in the background (on data change or periodic).
*   **Conflict Resolution**: Define rules for merging data from multiple devices (e.g., "Last Write Wins").

## Phase 2. Architectural Improvements

*   **Dependency Injection**: 
    *   Introduce **Hilt** or **Koin** to manage dependencies more cleanly, especially with the addition of `UserManager` and `SyncRepository`.
*   **State Management**: 
    *   Ensure all Fragments use `StateFlow` or `LiveData` from ViewModels for robust UI updates during orientation changes.

## Phase 3. UI/UX Enhancements

*   **Loading & Empty States**: 
    *   Add `ProgressBar`s for long-running operations (like initial sync).
    *   Add friendly "Empty State" illustrations when no courses, assignments, or exams are added.
*   **Animations**: 
    *   Use Material 3 motion for smooth transitions between fragments.
    *   Add swipe-to-delete gestures for list items.
*   **Accessibility**: 
    *   Verify Content Descriptions for all icons.
    *   Ensure proper contrast and text scaling support.

## Phase 4. Advanced Features

*   **Dashboard Widgets**: Add a Home Screen widget showing the next lecture and pending assignments.
*   **Export/Backup**: 
    *   Provide an option to export study data to PDF or CSV.
    *   Manual local backup/restore functionality.
*   **Gamification**: Add a "Study Streak" feature to encourage daily app usage.
*   **Collaboration**: Allow users to share their timetable or course notes via a link.
