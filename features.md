# StudBuddy Improvement Roadmap

This document outlines the prioritized steps to enhance the feature set of StudBuddy, focusing on a modern layout and best-in-class student productivity features.

---

## Phase 1: Add Multi-Semester Support
### Step 1:
- **Semester Page**: Implement semester page in which all semesters will be displayed in cards. Add Semester option below home in sidebar. Alter semester model if needed and ensure roomDB integration.
### Step 2:
- **Semester Card**: Each semester will be represented by card which will have the following components: 
  - Heading of semester name. Below that starting and ending date. Below that cumulative gpa of its courses (if available otherwise don't show this)
  - Random light shade of color so that they look distinguishable. 
  - Each card will have 3-dots button and by clicking that button we will have 3 options: Set it as Active Semester (which will set it as Active) Edit Semester (by which we can edit semester name, starting date and ending date) and Delete Semester (which will only work if it contains no course in it otherwise it will show message to first delete its courses)
### Step 3:
- **Cards Sorting**: Add 3-dots button on top right header of semester page. By clicking that it should open a menu having option of Sort in it. On clicking sort it should open two radio group options menu
  - 1st group: By Creation time (which should sort semester cards by time semesters created), By Date (which should sort semesters by their starting date)
  - 2nd group: Ascending, Descending
### Step 4:
- **Add plus button**: At the bottom right, add floating plus button by which we can add semester.
- **Cards navigation**: On clicking any semester card it should open courses page which contain courses for that semester only.

## Phase 2: Enhance Courses Page

### Step 1: 
- **Courses Page**: Whenever we click on semester, the courses page will open showing courses of that semester. the courses page button in sidebar will open courses page for active semester.

### Step 2:
- **Course Card**: Each Course card which will have the following components (alter course model if needed): 
  - Heading of Course name. Below that credit hours. Below that its grade point and grade (if available otherwise don't show this)
  - Random light shade of color so that they look distinguishable. 
### Step 3:
- **Cards Sorting**: Add 3-dots button on top right header of the page. By clicking that it should open a menu having option of Sort in it. On clicking sort it should open two radio group options menu
  - 1st group: By Name (to sort alphabetically), By Creation time (which should sort courses cards by time they created), By Credit Hours (same credit hours will be sorted by creation time)
  - 2nd group: Ascending, Descending
### Step 4:
- **Add plus button**: At the bottom right, add floating plus button by which we can add courses.

## Phase 3: Implement pages for each Course and Notes module

- **Course Pages**: Implement new pages having bottom navigation (course summary page, course attendance page, course notes page, course assignments page, course exams page) to open each course details. On clicking any course card in Courses page it should open course summary page. 

- **Bottom Navigation and pages**: these 5 pages for each course will be managed by bottom navigation having icons only.

- **Course Summary Page**: Open by default when we click on a course.
  - Shows course details (Name, Description (if added), Instructor name, Credit hours, Semester name, Grade points, Grade (if added), Marks) in a presentable way that looks like a dashboard. 
  - Have home icon in bottom navigation. 
  - Use 3-dots button on top right header of the page. By clicking that it should open a menu having 2 options: Edit Course (by which we can edit Course Name, Description (optional), Instructor name, Credit hours, Semester name, Grade (optional)) and Delete Course (which opens a confirmation menu and show the warning message that assignments, exams and notes associated with this course will be deleted. On clicking delete, it should delete that course after deleting all associated assignments, exams, notes, attendances and timetable entries)

- **Course Attendance Page**: Use previously implemented attendance page functionality and show status just for that course.
  - Attendance icon in bottom navigation.

- **Course Notes Page**: Implement new page for notes. 
  - Notes icon in bottom navigation.
  - Data Model: Create Note entity (id, courseId, title, localPath, fileType, fileSize, createdAt).
  - Room Setup: Add NoteDao with queries to fetch notes by courseId and register it in StudBuddyDatabase.
  - File Picking: Use ActivityResultContracts.GetContent() to allow users to select PDFs, docs, or images from storage.
  - Persistent Storage: Copy selected files into context.getExternalFilesDir("Notes") to ensure the app keeps access even if the original file is moved/deleted.
  - UI List: Implement NotesFragment with a RecyclerView to display note cards (Title, File Type, and Date).
  - UI Actions:
    - 3-dots menu buttom on page header: To add a new note (opens title dialog + file picker).
    - Open: Clicking a card opens the file using a FileProvider Intent.
    - Delete: 3-dots menu to delete the record from Room (should not delete the physical file from internal storage).
  - Sync Policy: Exclude the notes table and associated files from Cloud Sync to optimize bandwidth and storage. Show this message while adding that these files will not be synced.


- **Course Assignments Page**: Use previously implemented assignment page functionality.
  - Show course associated assignments in cards sorted by due date and time. Each card shows assignment title in heading, due date and time, total marks, obtained marks if added, weightage and status showing completed (written in green) or pending (written in red). 
  - Each assignment card will have 3-dots button to open edit menu by which we can edit or delete that assignment (use previously implemented menu).
  - Use 3-dots button on top right header of the page to add assignments,
  - Assignment icon in bottom navigation.

- **Course Exams Page**: Use previously implemented Exam page functionality.
  - Show course associated Exams in cards sorted by due date and time. Each card shows Exam title in heading, due date and time, total marks, obtained marks if added, weightage, venue (in bold) and status showing completed (written in green) or pending (written in red).
  - Each Exam card will have 3-dots button to open edit menu by which we can edit or delete that Exam (use previously implemented menu).
  - Use 3-dots button on top right header of the page to add Exams,
  - Exam icon in bottom navigation.

