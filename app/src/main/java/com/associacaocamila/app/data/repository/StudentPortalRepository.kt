package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.AttendanceRecord
import com.associacaocamila.app.data.model.Workshop
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface StudentPortalRepository {
    fun getStudentWorkshops(studentId: String): Flow<List<Workshop>>
    fun getStudentAttendanceHistory(studentId: String): Flow<List<Pair<AttendanceRecord, Boolean>>>
}

class StudentPortalRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : StudentPortalRepository {

    private val workshopsCollection get() = firestore.collection(WORKSHOPS_COLLECTION)
    private val attendancesCollection get() = firestore.collection(ATTENDANCES_COLLECTION)

    override fun getStudentWorkshops(studentId: String): Flow<List<Workshop>> = callbackFlow {
        if (studentId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val query = workshopsCollection.whereArrayContains("enrolledStudentIds", studentId)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val workshops = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Workshop::class.java)?.copy(id = doc.id)
                } catch (_: Exception) {
                    null
                }
            }.orEmpty()
            trySend(workshops)
        }
        awaitClose { registration.remove() }
    }

    override fun getStudentAttendanceHistory(studentId: String): Flow<List<Pair<AttendanceRecord, Boolean>>> = callbackFlow {
        if (studentId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = attendancesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val records = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(AttendanceRecord::class.java)?.copy(id = doc.id)
                } catch (_: Exception) {
                    null
                }
            }.orEmpty()

            // Filtra os registros que contêm o estudante no mapa de chamada e ordena pelas mais recentes
            val studentRecords = records
                .filter { it.attendanceMap.containsKey(studentId) }
                .sortedByDescending { it.dateMillis }
                .map { record -> record to (record.attendanceMap[studentId] == true) }

            trySend(studentRecords)
        }
        awaitClose { registration.remove() }
    }

    private companion object {
        const val WORKSHOPS_COLLECTION = "workshops"
        const val ATTENDANCES_COLLECTION = "attendances"
    }
}
