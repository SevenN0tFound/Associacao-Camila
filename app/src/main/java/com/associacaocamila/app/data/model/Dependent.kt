package com.associacaocamila.app.data.model

import java.util.UUID

data class Dependent(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val birthDate: String = "",
    val relationship: String = "Filho(a)", // Filho(a), Pai/Mãe, Irmão(a), Neto(a), Outro
    val medicalNotes: String = "",
    val enrolledWorkshopIds: List<String> = emptyList(),
)
