package com.example.studbuddy.core.repository

import com.example.studbuddy.core.db.StudBuddyDatabase
import com.example.studbuddy.core.db.*
import com.example.studbuddy.core.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class SyncRepository(private val db: StudBuddyDatabase) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun syncAll() {
        val userId = auth.currentUser?.uid ?: return
        
        syncTable("semesters", { db.semesterDao().getSemester()?.let { listOf(it) } ?: emptyList() }, db.semesterDao()::insert, userId)
        syncTable("courses", db.courseDao()::getAll, db.courseDao()::insert, userId)
        syncTable("timetable", db.timetableDao()::getAll, db.timetableDao()::insert, userId)
        syncTable("attendance", db.attendanceDao()::getAll, db.attendanceDao()::insert, userId)
        syncTable("assignments", db.assignmentDao()::getAll, db.assignmentDao()::insert, userId)
        syncTable("exams", db.examDao()::getAll, db.examDao()::insert, userId)
    }

    private suspend fun <T : Any> syncTable(
        collectionName: String,
        localFetcher: suspend () -> List<T>,
        localInserter: suspend (T) -> Unit,
        userId: String
    ) {
        val userDoc = firestore.collection("users").document(userId)
        val collection = userDoc.collection(collectionName)

        // 1. Push local changes
        val localData = localFetcher()
        for (item in localData) {
            val id = getEntityId(item)
            val remoteDoc = collection.document(id).get().await()
            
            val localLastModified = getEntityLastModified(item)
            val remoteLastModified = remoteDoc.getLong("lastModified") ?: 0L

            if (localLastModified > remoteLastModified) {
                collection.document(id).set(item, SetOptions.merge()).await()
            }
        }

        // 2. Pull remote changes
        val remoteData = collection.get().await()
        for (doc in remoteData.documents) {
            val remoteItem = doc.toObject(localData.firstOrNull()!!::class.java) ?: continue
            val id = doc.id
            val localItem = localData.find { getEntityId(it) == id }

            val remoteLastModified = doc.getLong("lastModified") ?: 0L
            val localLastModified = localItem?.let { getEntityLastModified(it) } ?: 0L

            if (remoteLastModified > localLastModified) {
                localInserter(remoteItem)
            }
        }
    }

    private fun getEntityId(item: Any): String {
        return when (item) {
            is Semester -> item.id
            is Course -> item.id
            is TimetableEntry -> item.id
            is AttendanceRecord -> item.id
            is Assignment -> item.id
            is Exam -> item.id
            else -> throw IllegalArgumentException("Unknown entity type")
        }
    }

    private fun getEntityLastModified(item: Any): Long {
        return when (item) {
            is Semester -> item.lastModified
            is Course -> item.lastModified
            is TimetableEntry -> item.lastModified
            is AttendanceRecord -> item.lastModified
            is Assignment -> item.lastModified
            is Exam -> item.lastModified
            else -> throw IllegalArgumentException("Unknown entity type")
        }
    }
}
