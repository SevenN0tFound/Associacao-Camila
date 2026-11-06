package com.associacaocamila.app.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.ALUNO,
    val status: UserStatus = UserStatus.PENDING, // Todo cadastro entra como pendente até aprovação da ONG
    val associatedStudentIds: List<String> = emptyList(), // Para contas de responsáveis
    val registeredProjects: List<String> = emptyList(),   // Projetos: Fortalecimento, Mídias Force, etc.
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    ADMIN,
    PROFESSOR,
    RESPONSAVEL,
    ALUNO
}

enum class UserStatus {
    PENDING,
    APPROVED,
    REJECTED
}