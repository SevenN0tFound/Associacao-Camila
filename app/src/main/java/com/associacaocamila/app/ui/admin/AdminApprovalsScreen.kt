package com.associacaocamila.app.ui.admin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.associacaocamila.app.data.model.Dependent
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.data.model.UserStatus
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange

@Composable
fun AdminApprovalsScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminApprovalsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val processingUserIds by viewModel.processingUserIds.collectAsState()
    val isAddingDependent by viewModel.isAddingDependent.collectAsState()
    val availableStudents by viewModel.availableStudents.collectAsState()
    val isLoadingStudents by viewModel.isLoadingStudents.collectAsState()

    AdminApprovalsContent(
        uiState = uiState,
        actionError = actionError,
        feedbackMessage = feedbackMessage,
        processingUserIds = processingUserIds,
        isAddingDependent = isAddingDependent,
        availableStudents = availableStudents,
        isLoadingStudents = isLoadingStudents,
        onApprove = { viewModel.approveUser(it) },
        onReject = { viewModel.rejectUser(it) },
        onLinkStudent = { guardianId, student, onSuccess ->
            viewModel.linkStudentAsDependent(guardianId, student, onSuccess)
        },
        onUnlinkDependent = { guardianId, dependentId, dependentName ->
            viewModel.unlinkDependent(guardianId, dependentId, dependentName)
        },
        onLoadStudents = { viewModel.loadAvailableStudents() },
        onDismissError = { viewModel.clearActionError() },
        onDismissFeedback = { viewModel.clearFeedbackMessage() },
        modifier = modifier,
    )
}

@Composable
fun AdminApprovalsContent(
    uiState: AdminApprovalsUiState,
    actionError: String?,
    feedbackMessage: String? = null,
    processingUserIds: Set<String> = emptySet(),
    isAddingDependent: Boolean = false,
    availableStudents: List<User> = emptyList(),
    isLoadingStudents: Boolean = false,
    onApprove: (String) -> Unit = {},
    onReject: (String) -> Unit = {},
    onLinkStudent: (guardianId: String, student: User, onSuccess: () -> Unit) -> Unit = { _, _, _ -> },
    onUnlinkDependent: (guardianId: String, dependentId: String, dependentName: String) -> Unit = { _, _, _ -> },
    onLoadStudents: () -> Unit = {},
    onDismissError: () -> Unit = {},
    onDismissFeedback: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var targetUserForDependent by remember { mutableStateOf<User?>(null) }
    var dependentToUnlink by remember { mutableStateOf<Pair<User, Dependent>?>(null) }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            onDismissFeedback()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
    ) {
        // Alerta em caso de erro na operação
        AnimatedVisibility(
            visible = actionError != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = actionError.orEmpty(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismissError) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar erro",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        when (uiState) {
            is AdminApprovalsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = CamilaGreen,
                        strokeWidth = 3.dp,
                    )
                }
            }

            is AdminApprovalsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            is AdminApprovalsUiState.Success -> {
                // Abas de navegação: Pendentes, Aprovados e Justificativas de Falta
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = CamilaGreenDark,
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                text = "Pendentes (${uiState.pendingUsers.size})",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                text = "Aprovados (${uiState.approvedUsers.size})",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = {
                            Text(
                                text = "Justificativas",
                                fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }

                if (selectedTabIndex == 2) {
                    JustificationsApprovalScreen(modifier = Modifier.fillMaxSize())
                } else {
                    val currentList = if (selectedTabIndex == 0) {
                        uiState.pendingUsers
                    } else {
                        uiState.approvedUsers
                    }

                if (currentList.isEmpty()) {
                    EmptyApprovalsState(
                        isPendingTab = selectedTabIndex == 0,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            if (selectedTabIndex == 0) {
                                ApprovalsHeader(totalCount = uiState.pendingUsers.size, isPending = true)
                            } else {
                                ApprovalsHeader(totalCount = uiState.approvedUsers.size, isPending = false)
                            }
                        }

                        items(
                            items = currentList,
                            key = { it.id },
                        ) { user ->
                            AdminUserCard(
                                user = user,
                                isPending = selectedTabIndex == 0,
                                isProcessing = processingUserIds.contains(user.id),
                                onApprove = { onApprove(user.id) },
                                onReject = { onReject(user.id) },
                                onOpenAddDependent = {
                                    targetUserForDependent = user
                                    onLoadStudents()
                                },
                                onUnlinkDependent = { dependent ->
                                    dependentToUnlink = user to dependent
                                },
                            )
                        }
                    }
                }
                }
            }
        }
    }

    // Modal Bottom Sheet para Selecionar e Vincular Aluno como Dependente
    targetUserForDependent?.let { responsibleUser ->
        com.associacaocamila.app.ui.components.SelectStudentBottomSheet(
            availableStudents = availableStudents,
            alreadyLinkedIds = (responsibleUser.associatedStudentIds + responsibleUser.dependents.map { it.id }).toSet(),
            isLoading = isLoadingStudents,
            isLinking = isAddingDependent,
            onDismiss = { targetUserForDependent = null },
            onRefreshStudents = onLoadStudents,
            onStudentSelected = { selectedStudent ->
                onLinkStudent(responsibleUser.id, selectedStudent) {
                    targetUserForDependent = null
                }
            },
        )
    }

    // Diálogo de confirmação para desvinculação de dependente
    dependentToUnlink?.let { (guardian, dependent) ->
        androidx.compose.material3.AlertDialog(
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
                    text = "Deseja realmente desvincular ${dependent.name.ifBlank { "este participante" }} do responsável ${guardian.name}?",
                    color = CamilaGray,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUnlinkDependent(guardian.id, dependent.id, dependent.name)
                        dependentToUnlink = null
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

@Composable
private fun ApprovalsHeader(
    totalCount: Int,
    isPending: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPending) CamilaOrange.copy(alpha = 0.12f) else CamilaGreen.copy(alpha = 0.12f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isPending) Icons.Default.VerifiedUser else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isPending) CamilaOrange else CamilaGreenDark,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (isPending) "Validação Institucional" else "Cadastros Aprovados",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark,
                )
                Text(
                    text = if (isPending) {
                        "$totalCount cadastro(s) aguardando liberação de acesso."
                    } else {
                        "$totalCount membro(s) ativos e integrados à Associação Camila."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = CamilaGray,
                )
            }
        }
    }
}

@Composable
private fun AdminUserCard(
    user: User,
    isPending: Boolean,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onOpenAddDependent: () -> Unit,
    onUnlinkDependent: (Dependent) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CamilaGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CamilaGreen,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name.ifBlank { "Sem nome" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = CamilaGray,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.email.ifBlank { "Sem e-mail" },
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray,
                        )
                    }
                }

                if (!isPending) {
                    Surface(
                        color = CamilaGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "Aprovado",
                            color = CamilaGreenDark,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badge com o perfil solicitado ou aprovado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RoleBadge(role = user.role, isPending = isPending)

                // Contador de dependentes se for RESPONSAVEL
                if (user.role == UserRole.RESPONSAVEL) {
                    Surface(
                        color = CamilaOrange.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.FamilyRestroom,
                                contentDescription = null,
                                tint = CamilaOrange,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (user.dependents.isEmpty()) {
                                    "0 dependentes"
                                } else {
                                    "${user.dependents.size} dependente(s)"
                                },
                                color = CamilaOrange,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                }
            }

            // Seção Especial de Gestão de Dependentes para perfis RESPONSAVEL
            if (user.role == UserRole.RESPONSAVEL) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CamilaGray.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                // Botão secundário "Vincular Dependente"
                OutlinedButton(
                    onClick = onOpenAddDependent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CamilaGreen),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CamilaGreenDark,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = CamilaGreenDark,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vincular Dependente",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark,
                    )
                }

                // Lista de Dependentes já vinculados ao responsável
                if (user.dependents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Dependentes Vinculados:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark,
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        user.dependents.forEach { dependent ->
                            DependentItemCard(
                                dependent = dependent,
                                onUnlink = { onUnlinkDependent(dependent) },
                            )
                        }
                    }
                }
            }

            // Botões de Ação para usuários pendentes
            if (isPending) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Recusar",
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Button(
                        onClick = onApprove,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CamilaGreen,
                            disabledContainerColor = CamilaGreen.copy(alpha = 0.5f),
                        ),
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Aprovar",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DependentItemCard(
    dependent: Dependent,
    onUnlink: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = BackgroundLight,
        border = BorderStroke(1.dp, CamilaGray.copy(alpha = 0.15f)),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(CamilaGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CamilaGreenDark,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dependent.name.ifBlank { "Sem nome" },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CamilaGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = dependent.relationship.ifBlank { "Dependente" },
                            color = CamilaGreenDark,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }

                    if (onUnlink != null) {
                        IconButton(
                            onClick = onUnlink,
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Desvincular",
                                tint = CamilaOrange,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            if (dependent.birthDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = CamilaGray,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Nascimento: ${dependent.birthDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CamilaGray,
                    )
                }
            }

            if (dependent.medicalNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CamilaOrange.copy(alpha = 0.1f))
                        .padding(6.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = CamilaOrange,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dependent.medicalNotes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(role: UserRole, isPending: Boolean) {
    val roleLabel = when (role) {
        UserRole.ADMIN -> "Administrador"
        UserRole.PROFESSOR -> "Professor"
        UserRole.RESPONSAVEL -> "Responsável"
        UserRole.ALUNO -> "Aluno"
    }

    Surface(
        color = if (isPending) CamilaOrange.copy(alpha = 0.12f) else CamilaGreen.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = if (isPending) "Solicitado: $roleLabel" else "Perfil: $roleLabel",
            color = if (isPending) CamilaOrange else CamilaGreenDark,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDependentBottomSheet(
    responsible: User,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, birthDate: String, relationship: String, medicalNotes: String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var relationship by rememberSaveable { mutableStateOf("Filho(a)") }
    var relationshipDropdownExpanded by remember { mutableStateOf(false) }
    var medicalNotes by rememberSaveable { mutableStateOf("") }

    val relationshipOptions = listOf("Filho(a)", "Pai/Mãe", "Irmão(a)", "Neto(a)", "Outro")

    ModalBottomSheet(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Vincular Dependente",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark,
                    )
                    Text(
                        text = "Responsável: ${responsible.name.ifBlank { "Sem nome" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray,
                    )
                }

                IconButton(
                    onClick = {
                        if (!isLoading) onDismiss()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar formulário",
                        tint = CamilaGray,
                    )
                }
            }

            HorizontalDivider(color = CamilaGray.copy(alpha = 0.15f))

            // Campo Nome Completo
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome Completo da Criança ou Idoso") },
                placeholder = { Text("Ex: Lucas Silva") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CamilaOrange,
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CamilaGreen,
                    focusedLabelColor = CamilaGreenDark,
                ),
                enabled = !isLoading,
            )

            // Campo Data de Nascimento
            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("Data de Nascimento") },
                placeholder = { Text("DD/MM/AAAA") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = CamilaOrange,
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CamilaGreen,
                    focusedLabelColor = CamilaGreenDark,
                ),
                enabled = !isLoading,
            )

            // Dropdown Grau de Parentesco
            ExposedDropdownMenuBox(
                expanded = relationshipDropdownExpanded,
                onExpandedChange = {
                    if (!isLoading) {
                        relationshipDropdownExpanded = !relationshipDropdownExpanded
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = relationship,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grau de Parentesco") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationshipDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CamilaGreen,
                        focusedLabelColor = CamilaGreenDark,
                    ),
                    enabled = !isLoading,
                )

                ExposedDropdownMenu(
                    expanded = relationshipDropdownExpanded,
                    onDismissRequest = { relationshipDropdownExpanded = false },
                ) {
                    relationshipOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontWeight = if (option == relationship) FontWeight.Bold else FontWeight.Normal,
                                    color = if (option == relationship) CamilaGreenDark else MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            onClick = {
                                relationship = option
                                relationshipDropdownExpanded = false
                            },
                        )
                    }
                }
            }

            // Campo Observações de Saúde / Cuidados Especiais
            OutlinedTextField(
                value = medicalNotes,
                onValueChange = { medicalNotes = it },
                label = { Text("Observações de Saúde / Cuidados Especiais") },
                placeholder = { Text("Alergias, remédios contínuos, cuidados específicos ou recomendações da ONG") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = CamilaOrange,
                    )
                },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CamilaGreen,
                    focusedLabelColor = CamilaGreenDark,
                ),
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CamilaGray,
                    ),
                    enabled = !isLoading,
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )
                }

                Button(
                    onClick = {
                        onSave(name, birthDate, relationship, medicalNotes)
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen),
                    enabled = !isLoading && name.isNotBlank(),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "Salvar e Vincular",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyApprovalsState(isPendingTab: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(CamilaGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isPendingTab) Icons.Default.HourglassEmpty else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CamilaGreen,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isPendingTab) "Tudo em dia!" else "Nenhum usuário aprovado",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isPendingTab) {
                    "Nenhum cadastro pendente de aprovação no momento."
                } else {
                    "Nenhum cadastro ativo encontrado nesta listagem."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = CamilaGray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminApprovalsScreenPreview() {
    AssociacaoCamilaTheme {
        AdminApprovalsContent(
            uiState = AdminApprovalsUiState.Success(
                pendingUsers = listOf(
                    User(
                        id = "1",
                        name = "Mariana Alves",
                        email = "mariana.alves@email.com",
                        role = UserRole.RESPONSAVEL,
                        status = UserStatus.PENDING,
                        dependents = listOf(
                            Dependent(
                                id = "d1",
                                name = "Enzo Gabriel Alves",
                                birthDate = "10/04/2015",
                                relationship = "Filho(a)",
                                medicalNotes = "Alergia a lactose",
                            ),
                        ),
                    ),
                    User(
                        id = "2",
                        name = "Carlos Souza",
                        email = "carlos.souza@email.com",
                        role = UserRole.PROFESSOR,
                        status = UserStatus.PENDING,
                    ),
                ),
                approvedUsers = listOf(
                    User(
                        id = "3",
                        name = "Juliana Lima",
                        email = "juliana@email.com",
                        role = UserRole.RESPONSAVEL,
                        status = UserStatus.APPROVED,
                    ),
                ),
            ),
            actionError = null,
            processingUserIds = emptySet(),
            onApprove = {},
            onReject = {},
            onDismissError = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminApprovalsScreenEmptyPreview() {
    AssociacaoCamilaTheme {
        AdminApprovalsContent(
            uiState = AdminApprovalsUiState.Success(emptyList()),
            actionError = null,
            processingUserIds = emptySet(),
            onApprove = {},
            onReject = {},
            onDismissError = {},
        )
    }
}
