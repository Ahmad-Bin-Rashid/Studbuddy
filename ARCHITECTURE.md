# StudBuddy — System Architecture

---

## 1. Architecture Overview

StudBuddy follows a **Clean Architecture** approach using **MVVM (Model-View-ViewModel)**. The app is structured as a single-activity application using the **Navigation Component**.

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
│   MainActivity (Single Activity) + DrawerLayout Sidebar     │
│        ↕               ↕                  ↕                 │
│    HomeFragment    CourseFragment    AttendanceFragment ... │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                     Domain Layer                            │
│           ViewModels + UseCases (Repository)                │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                     Data Layer                              │
│              StudBuddyRepository (Single Source of Truth)   │
│                        ↕                                    │
│              Room Database (SQLite Persistence)             │
└─────────────────────────────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                   System Layer                              │
│     WorkManager   AlarmManager   NotificationHelper         │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Component Responsibilities

### 2.1 MainActivity

- **Host**: Manages the `NavHostFragment` and the global `DrawerLayout`.
- **Navigation**: Uses `NavigationView` with `NavController` to handle fragment transitions.
- **Theme**: Applies theme settings on startup.

### 2.2 Fragments (Module Views)

Each feature is implemented as a `Fragment`:
- **HomeFragment**: Dashboard with upcoming tasks and semester overview.
- **CoursesFragment**: Manage enrolled courses.
- **AttendanceFragment**: Track attendance per course.
- **TimetableFragment**: Weekly schedule management.
- **AssignmentsFragment**: Task tracking.
- **ExamsFragment**: Assessment management.
- **GpaFragment**: Semester and CGPA calculation.
- **SettingsFragment**: App configurations.

### 2.3 Data Layer (Room Persistence)

- **StudBuddyDatabase**: Room database instance with tables for all entities.
- **DAOs**: Data Access Objects for CRUD operations.
- **Repository**: `StudBuddyRepository` abstracts data access and provides a clean API to ViewModels.

---

## 3. Data Flow

1. **User Interaction**: User interacts with a Fragment.
2. **ViewModel**: The Fragment calls a method on its corresponding `ViewModel`.
3. **Repository**: The `ViewModel` interacts with the `StudBuddyRepository`.
4. **Database**: The Repository performs operations on the `Room` database.
5. **Reactive UI**: Data changes are observed via `Flow` or `LiveData`, and the UI updates automatically.

---

## 4. Navigation Flow

- **Primary Navigation**: Standard Android `DrawerLayout` with a `NavigationView`.
- **Navigation Graph**: Centralized `nav_graph.xml` defines all fragment destinations and actions.
- **Sidebar Header**: Displays app info (and user info in future updates).

---

## 5. Storage Layer (Room)

| Entity | Description |
|---|---|
| **Semester** | Active semester dates and goals. |
| **Course** | Subject details (Credit hours, Instructor). |
| **AttendanceRecord** | Log of present/absent/late marks. |
| **TimetableEntry** | Weekly schedule slot (Day, Time, Room). |
| **Assignment** | Tasks with due dates and marks. |
| **Exam** | Assessment details (Quiz, Mid, Final). |
