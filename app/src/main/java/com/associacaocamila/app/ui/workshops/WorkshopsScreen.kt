package com.associacaocamila.app.ui.workshops

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.ui.student.StudentWorkshopsScreen
import com.associacaocamila.app.ui.student.StudentWorkshopsViewModel

/**
 * Ponto de entrada unificado para a tela de Oficinas e Matrículas (Aluno e Responsável).
 */
@Composable
fun WorkshopsScreen(
    currentUser: User,
    modifier: Modifier = Modifier,
    viewModel: StudentWorkshopsViewModel = viewModel(),
) {
    StudentWorkshopsScreen(
        currentUser = currentUser,
        modifier = modifier,
        viewModel = viewModel,
    )
}
