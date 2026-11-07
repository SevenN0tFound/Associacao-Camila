package com.associacaocamila.app.data.model

/**
 * Representa um participante elegível para matrícula ou lista de chamada em oficinas.
 * Pode ser um Usuário (ALUNO) ou um Dependente vinculado a um RESPONSAVEL.
 */
data class EligibleParticipant(
    val id: String,
    val name: String,
    val isDependent: Boolean = false,
    val responsibleName: String? = null,
    val relationship: String? = null,
    val email: String? = null,
) {
    /**
     * Rótulo de exibição padronizado para seletores e chamadas:
     * - Se for Aluno: "Nome (Aluno)"
     * - Se for Dependente: "Nome (Dependente de [Nome do Responsável])"
     */
    val displayName: String
        get() = if (isDependent && !responsibleName.isNullOrBlank()) {
            "$name (Dependente de $responsibleName)"
        } else if (isDependent) {
            "$name (Dependente)"
        } else {
            "$name (Aluno)"
        }
}
