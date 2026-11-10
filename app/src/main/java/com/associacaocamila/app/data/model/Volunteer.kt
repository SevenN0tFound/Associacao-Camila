package com.associacaocamila.app.data.model

data class Volunteer(
    val id: String = "",
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val birthDate: String = "",
    val maritalStatus: String = "", // Solteiro(a), Casado(a), Viúvo(a), Divorciado(a), União Estável
    val rg: String = "",
    val cpf: String = "",
    val profession: String = "",
    val address: String = "",
    val city: String = "",
    val healthLimitations: String = "",
    val supportAreas: List<String> = emptyList(), // Limpeza e manutenção, Preparação de refeição, Doar itens, Suporte financeiro, Eventos, Elaborar oficina, Bazar ReUse, Geral, Digitador de NFP
    val availableTimes: List<String> = emptyList(), // Diurno seg-sex, Noturno seg-sex, Fins de semana
    val additionalInfo: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val status: String = "PENDENTE", // PENDENTE, CONTATADO, APROVADO
)
