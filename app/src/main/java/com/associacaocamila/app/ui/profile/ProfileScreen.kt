package com.associacaocamila.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.associacaocamila.app.data.model.Dependent
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.data.model.UserStatus
import com.associacaocamila.app.ui.components.DependentsSection
import com.associacaocamila.app.ui.components.SelectStudentBottomSheet
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange

/**
 * Tela de Gestão de Dependentes / Responsáveis com restrição de acesso por papel (Role).
 *
 * Regras de Negócio e Segurança:
 * 1. Apenas ADMIN pode cadastrar, vincular ou desvincular dependentes.
 * 2. Para RESPONSAVEL, a visualização é estritamente somente leitura.
 * 3. Se não houver dependentes vinculados, exibe a mensagem institucional para contato com a coordenação.
 */
@Composable
fun DependentsScreen(
    currentUser: User?,
    guardian: User = currentUser ?: User(),
    onUserUpdated: (User) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSelectStudentSheet by rememberSaveable { mutableStateOf(false) }
    var dependentToUnlink by remember { mutableStateOf<Dependent?>(null) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundLight,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Institucional
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(CamilaGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.FamilyRestroom,
                            contentDescription = null,
                            tint = CamilaGreenDark,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gestão de Dependentes",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark,
                        )
                        Text(
                            text = "Responsável: ${guardian.name.ifBlank { "Usuário" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray,
                        )
                    }
                }
            }

            // 1. Verificação de Papel (Role-Based Access) & Visão Somente Leitura para RESPONSAVEL
            DependentsSection(
                dependents = guardian.dependents,
                onLinkStudentClick = {
                    if (currentUser?.role?.uppercase() == "ADMIN") {
                        showSelectStudentSheet = true
                        viewModel.loadAvailableStudents()
                    }
                },
                onUnlinkDependentClick = { dep ->
                    if (currentUser?.role?.uppercase() == "ADMIN") {
                        dependentToUnlink = dep
                    }
                },
                currentUser = currentUser,
                canEdit = (currentUser?.role?.uppercase() == "ADMIN"),
            )

            // Ações de alteração de vínculo restritas estritamente para perfil ADMIN
            if (currentUser?.role?.uppercase() == "ADMIN") {
                if (showSelectStudentSheet) {
                    SelectStudentBottomSheet(
                        availableStudents = uiState.availableStudents,
                        alreadyLinkedIds = (guardian.associatedStudentIds + guardian.dependents.map { it.id }).toSet(),
                        isLoading = uiState.isLoadingStudents,
                        isLinking = uiState.isLinkingDependent,
                        onDismiss = { showSelectStudentSheet = false },
                        onRefreshStudents = { viewModel.loadAvailableStudents() },
                        onStudentSelected = { selectedStudent ->
                            viewModel.linkStudentAsDependent(
                                guardian = guardian,
                                student = selectedStudent,
                                currentUser = currentUser,
                                onSuccess = { updatedGuardian ->
                                    onUserUpdated(updatedGuardian)
                                    showSelectStudentSheet = false
                                },
                            )
                        },
                    )
                }

                dependentToUnlink?.let { dep ->
                    AlertDialog(
                        onDismissRequest = { dependentToUnlink = null },
                        title = {
                            Text(
                                text = "Desvincular Participante",
                                fontWeight = FontWeight.Bold,
                                color = CamilaGreenDark,
                            )
                        },
                        text = {
                            Text(
                                text = "Deseja realmente desvincular ${dep.name.ifBlank { "este participante" }} da conta de responsável?",
                                color = CamilaGray,
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.unlinkDependent(
                                        guardian = guardian,
                                        dependentId = dep.id,
                                        dependentName = dep.name,
                                        currentUser = currentUser,
                                        onSuccess = { updatedGuardian ->
                                            onUserUpdated(updatedGuardian)
                                            dependentToUnlink = null
                                        },
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CamilaOrange),
                            ) {
                                Text("Desvincular", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { dependentToUnlink = null }) {
                                Text("Cancelar", color = CamilaGray)
                            }
                        },
                    )
                }
            }
        }
    }
}

/**
 * Alias com nome de conveniência para tela de responsáveis
 */
@Composable
fun GuardianScreen(
    currentUser: User?,
    onUserUpdated: (User) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
) {
    DependentsScreen(
        currentUser = currentUser,
        guardian = currentUser ?: User(),
        onUserUpdated = onUserUpdated,
        modifier = modifier,
        viewModel = viewModel,
    )
}

/**
 * Alias com nome de conveniência ProfileScreen
 */
@Composable
fun ProfileScreen(
    currentUser: User?,
    onUserUpdated: (User) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
) {
    DependentsScreen(
        currentUser = currentUser,
        guardian = currentUser ?: User(),
        onUserUpdated = onUserUpdated,
        modifier = modifier,
        viewModel = viewModel,
    )
}

@Preview(showBackground = true)
@Composable
private fun DependentsScreenResponsiblePreview() {
    AssociacaoCamilaTheme {
        DependentsScreen(
            currentUser = User(
                id = "resp1",
                name = "Maria Souza (Mãe)",
                email = "maria@exemplo.com",
                role = UserRole.RESPONSAVEL,
                status = UserStatus.APPROVED,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DependentsScreenAdminPreview() {
    AssociacaoCamilaTheme {
        DependentsScreen(
            currentUser = User(
                id = "admin1",
                name = "Coordenação Associação Camila",
                email = "coord@associacaocamila.org.br",
                role = UserRole.ADMIN,
                status = UserStatus.APPROVED,
            ),
        )
    }
}
