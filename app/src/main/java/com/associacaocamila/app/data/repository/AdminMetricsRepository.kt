package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.AdminMetrics
import com.associacaocamila.app.data.model.AttendanceRecord
import com.associacaocamila.app.data.model.AttendanceStatus
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface AdminMetricsRepository {
    suspend fun getAdminMetrics(): Result<AdminMetrics>
}

class AdminMetricsRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AdminMetricsRepository {

    override suspend fun getAdminMetrics(): Result<AdminMetrics> = runCatching {
        // 1. Contagem de alunos cadastrados (users com role "ALUNO" e dependentes de responsáveis)
        val studentsSnapshot = firestore.collection("users")
            .whereEqualTo("role", UserRole.ALUNO.name)
            .get()
            .await()

        val allStudents = mutableSetOf<String>()
        for (doc in studentsSnapshot.documents) {
            allStudents.add(doc.id)
        }

        val guardiansSnapshot = firestore.collection("users")
            .whereEqualTo("role", UserRole.RESPONSAVEL.name)
            .get()
            .await()
        for (doc in guardiansSnapshot.documents) {
            val user = doc.toObject(User::class.java)
            user?.dependents?.forEach { dep ->
                if (dep.id.isNotBlank()) allStudents.add(dep.id)
            }
        }
        val totalStudents = if (allStudents.isNotEmpty()) allStudents.size else studentsSnapshot.size()

        // 2. Contagem de oficinas cadastradas
        val workshopsSnapshot = firestore.collection("workshops")
            .get()
            .await()
        val activeWorkshops = workshopsSnapshot.size()

        // 3. Contagem de voluntários pendentes
        val volunteersSnapshot = firestore.collection("volunteers")
            .whereEqualTo("status", "PENDENTE")
            .get()
            .await()
        val pendingVolunteers = volunteersSnapshot.size()

        // 4. Cálculo consolidado da taxa de presença geral com base nos últimos registros de attendance
        val attendancesSnapshot = firestore.collection("attendances")
            .limit(100)
            .get()
            .await()

        var presencesCount = 0
        var absencesCount = 0

        for (doc in attendancesSnapshot.documents) {
            val record = doc.toObject(AttendanceRecord::class.java) ?: continue
            val participants = (record.statusMap.keys + record.attendanceMap.keys).toSet()
            for (pId in participants) {
                when (record.getParticipantStatus(pId)) {
                    AttendanceStatus.PRESENTE -> presencesCount++
                    AttendanceStatus.FALTA -> absencesCount++
                    AttendanceStatus.JUSTIFICADO -> {
                        // Presença justificada não é contabilizada como falta injustificada
                    }
                }
            }
        }

        val totalRecords = presencesCount + absencesCount
        val overallAttendanceRate = if (totalRecords > 0) {
            ((presencesCount.toDouble() / totalRecords) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        AdminMetrics(
            totalStudents = totalStudents,
            activeWorkshops = activeWorkshops,
            pendingVolunteers = pendingVolunteers,
            overallAttendanceRate = overallAttendanceRate,
        )
    }
}
