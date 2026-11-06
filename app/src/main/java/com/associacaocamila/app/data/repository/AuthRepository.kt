package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /** Cria a conta no FirebaseAuth e salva o perfil em "users/{uid}" com status PENDING. */
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<User>

    /** Autentica no FirebaseAuth e busca o perfil do usuário no Firestore. */
    suspend fun signIn(email: String, password: String): Result<User>

    /** Emite o usuário logado (ou null) sempre que o estado de autenticação ou o perfil mudar. */
    fun getCurrentUser(): Flow<User?>

    fun signOut()
}
