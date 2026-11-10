package com.associacaocamila.app.ui.student

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
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
import com.associacaocamila.app.data.model.Workshop
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class WorkshopListFilter {
    TODAS,
    MATRICULADAS,
    ABERTAS
}

@Composable
fun StudentWorkshopsScreen(
    currentUser: User,
    modifier: Modifier = Modifier,
    viewModel: StudentWorkshopsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    StudentWorkshopsContent(
        uiState = uiState,
        currentUser = currentUser,
        snackbarHostState = snackbarHostState,
        onEnrollSelf = { workshopId ->
            viewModel.enrollStudent(workshopId, currentUser.id, currentUser.name)
        },
        onUnrollSelf = { workshopId ->
            viewModel.unrollStudent(workshopId, currentUser.id, currentUser.name)
        },
        onUpdateDependentEnrollments = { workshopId, selectedDepIds, currentEnrolledDepIds ->
            viewModel.updateDependentEnrollments(workshopId, selectedDepIds, currentEnrolledDepIds)
        },
        modifier = modifier,
    )
}

/**
 * Alias de retrocompatibilidade para [StudentWorkshopsScreen]
 */
@Composable
fun WorkshopsScreen(
    currentUser: User,
    modifier: Modifier = Modifier,
    viewModel: StudentWorkshopsViewModel = viewModel(),
) {
    StudentWorkshopsScreen(currentUser = currentUser, modifier = modifier, viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentWorkshopsContent(
    uiState: StudentWorkshopsUiState,
    currentUser: User? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEnrollSelf: (String) -> Unit = {},
    onUnrollSelf: (String) -> Unit = {},
    onUpdateDependentEnrollments: (String, Set<String>, Set<String>) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    val role = currentUser?.role ?: UserRole.ALUNO
    val isResponsible = role == UserRole.RESPONSAVEL
    val dependents = currentUser?.dependents.orEmpty()

    var activeFilter by rememberSaveable { mutableStateOf(WorkshopListFilter.TODAS) }
    var workshopToCancelEnrollment by remember { mutableStateOf<Workshop?>(null) }
    var workshopForDependentEnrollment by remember { mutableStateOf<Workshop?>(null) }

    // Filtragem reativa das oficinas
    val filteredWorkshops = remember(uiState.workshops, activeFilter, currentUser) {
        val all = uiState.workshops
        when (activeFilter) {
            WorkshopListFilter.TODAS -> all
            WorkshopListFilter.MATRICULADAS -> {
                if (isResponsible) {
                    val depIds = dependents.map { it.id }.toSet()
                    all.filter { w -> w.enrolledStudentIds.any { depIds.contains(it) } }
                } else {
                    val studentId = currentUser?.id.orEmpty()
                    all.filter { it.enrolledStudentIds.contains(studentId) }
                }
            }
            WorkshopListFilter.ABERTAS -> all.filter { it.isCurrentlyOpen() }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // Header Institucional
            StudentWorkshopsHeader(
                totalWorkshops = uiState.workshops.size,
                role = role,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de Filtros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = activeFilter == WorkshopListFilter.TODAS,
                    onClick = { activeFilter = WorkshopListFilter.TODAS },
                    label = { Text("Todas (${uiState.workshops.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CamilaGreen,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                val enrolledCount = remember(uiState.workshops, currentUser) {
                    if (isResponsible) {
                        val depIds = dependents.map { it.id }.toSet()
                        uiState.workshops.count { w -> w.enrolledStudentIds.any { depIds.contains(it) } }
                    } else {
                        val studentId = currentUser?.id.orEmpty()
                        uiState.workshops.count { it.enrolledStudentIds.contains(studentId) }
                    }
                }

                FilterChip(
                    selected = activeFilter == WorkshopListFilter.MATRICULADAS,
                    onClick = { activeFilter = WorkshopListFilter.MATRICULADAS },
                    label = {
                        Text(if (isResponsible) "Com Dependentes ($enrolledCount)" else "Minhas Inscrições ($enrolledCount)")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CamilaGreen,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                val openCount = remember(uiState.workshops) {
                    uiState.workshops.count { it.isCurrentlyOpen() }
                }

                FilterChip(
                    selected = activeFilter == WorkshopListFilter.ABERTAS,
                    onClick = { activeFilter = WorkshopListFilter.ABERTAS },
                    label = { Text("Inscrições Abertas ($openCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CamilaGreen,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = CamilaGreen,
                        strokeWidth = 3.dp,
                    )
                }
            } else if (filteredWorkshops.isEmpty()) {
                EmptyWorkshopsState(filter = activeFilter, isResponsible = isResponsible)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(
                        items = filteredWorkshops,
                        key = { it.id },
                    ) { workshop ->
                        WorkshopEnrollmentCard(
                            workshop = workshop,
                            currentUser = currentUser,
                            isPerformingAction = uiState.isPerformingAction,
                            onEnrollClick = { onEnrollSelf(workshop.id) },
                            onUnrollClick = { workshopToCancelEnrollment = workshop },
                            onManageDependentsClick = { workshopForDependentEnrollment = workshop }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmação para cancelamento de inscrição (Aluno)
    workshopToCancelEnrollment?.let { workshop ->
        AlertDialog(
            onDismissRequest = { workshopToCancelEnrollment = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Cancelar Inscrição?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark
                )
            },
            text = {
                Text(
                    text = "Tem certeza de que deseja cancelar sua matrícula na oficina \"${workshop.name}\"? Sua vaga será liberada para outros participantes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUnrollSelf(workshop.id)
                        workshopToCancelEnrollment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CamilaOrange)
                ) {
                    Text("Confirmar Cancelamento", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { workshopToCancelEnrollment = null }) {
                    Text("Voltar", color = CamilaGray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // BottomSheet para seleção e gerenciamento de dependentes (Responsável)
    workshopForDependentEnrollment?.let { workshop ->
        EnrollDependentsBottomSheet(
            workshop = workshop,
            dependents = dependents,
            isPerformingAction = uiState.isPerformingAction,
            onDismiss = { workshopForDependentEnrollment = null },
            onConfirm = { selectedIds ->
                val currentEnrolled = dependents
                    .filter { workshop.enrolledStudentIds.contains(it.id) }
                    .map { it.id }
                    .toSet()
                onUpdateDependentEnrollments(workshop.id, selectedIds, currentEnrolled)
                workshopForDependentEnrollment = null
            }
        )
    }
}

@Composable
private fun StudentWorkshopsHeader(
    totalWorkshops: Int,
    role: UserRole,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CamilaGreen),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (role == UserRole.RESPONSAVEL) Icons.Default.FamilyRestroom else Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (role == UserRole.RESPONSAVEL) "Inscrições & Oficinas" else "Oficinas da Associação",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Text(
                        text = "$totalWorkshops turma(s) disponíveis",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (role == UserRole.RESPONSAVEL) {
                    "Gerencie e acompanhe a matrícula dos seus filhos e dependentes nas oficinas socioeducativas."
                } else {
                    "Inscreva-se nas oficinas abertas e participe ativamente das atividades e projetos comunitários."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.95f),
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun WorkshopEnrollmentCard(
    workshop: Workshop,
    currentUser: User?,
    isPerformingAction: Boolean,
    onEnrollClick: () -> Unit,
    onUnrollClick: () -> Unit,
    onManageDependentsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val role = currentUser?.role ?: UserRole.ALUNO
    val isResponsible = role == UserRole.RESPONSAVEL
    val dependents = currentUser?.dependents.orEmpty()
    val isStudentEnrolled = currentUser?.let { workshop.enrolledStudentIds.contains(it.id) } == true

    // Identifica quais dependentes do responsável estão matriculados nesta oficina
    val enrolledDependents = remember(workshop.enrolledStudentIds, dependents) {
        dependents.filter { workshop.enrolledStudentIds.contains(it.id) }
    }

    val isOpen = workshop.isCurrentlyOpen()
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            // Linha Superior: Badges de Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge de Inscrições Abertas / Encerradas
                    if (isOpen) {
                        Surface(
                            color = CamilaGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = "Inscrições Abertas",
                                color = CamilaGreenDark,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    } else {
                        Surface(
                            color = CamilaOrange.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = if (workshop.isFull()) "Vagas Esgotadas" else "Inscrições Encerradas",
                                color = CamilaOrange,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }

                    // Badge de Matrícula Pessoal / Dependente
                    if (!isResponsible && isStudentEnrolled) {
                        Surface(
                            color = CamilaGreen,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Matriculado(a)",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                    } else if (isResponsible && enrolledDependents.isNotEmpty()) {
                        Surface(
                            color = CamilaGreen,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${enrolledDependents.size} matriculado(s)",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Recolher detalhes" else "Expandir detalhes",
                        tint = CamilaGray,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nome da Oficina
            Text(
                text = workshop.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (workshop.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = workshop.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CamilaGray,
                    lineHeight = 20.sp,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tags informativas: participantes, prazos e vagas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                InfoTag(
                    icon = Icons.Default.Groups,
                    text = "${workshop.enrolledStudentIds.size} participante(s)",
                    tint = CamilaGreenDark,
                )

                if (workshop.registrationDeadline != null) {
                    InfoTag(
                        icon = Icons.Default.DateRange,
                        text = "Até ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(workshop.registrationDeadline))}",
                        tint = if (isOpen) CamilaGreenDark else CamilaOrange,
                    )
                }

                if (workshop.maxCapacity > 0) {
                    val remaining = workshop.availableSpots ?: 0
                    InfoTag(
                        icon = Icons.Default.CheckCircle,
                        text = "$remaining vaga(s) restante(s)",
                        tint = if (remaining > 0) CamilaGreenDark else CamilaOrange,
                    )
                }
            }

            // Exibição de dependentes já matriculados para responsáveis
            if (isResponsible && enrolledDependents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = CamilaGreen.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = CamilaGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Matriculado(s): ${enrolledDependents.joinToString { it.name.ifBlank { "Dependente" } }}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = CamilaGreenDark
                        )
                    }
                }
            }

            // Ações de Inscrição conforme Perfil
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BackgroundLight, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            if (!isResponsible) {
                // Perfil Aluno
                if (isStudentEnrolled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = CamilaGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Você está matriculado",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CamilaGreenDark
                            )
                        }

                        OutlinedButton(
                            onClick = onUnrollClick,
                            enabled = !isPerformingAction,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CamilaOrange
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancelar Inscrição")
                        }
                    }
                } else {
                    // Não matriculado
                    if (isOpen) {
                        Button(
                            onClick = onEnrollClick,
                            enabled = !isPerformingAction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quero me Inscrever",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = CamilaGray.copy(alpha = 0.2f),
                                disabledContentColor = CamilaGray
                            )
                        ) {
                            Text(
                                text = if (workshop.isFull()) "Vagas Esgotadas" else "Inscrições Encerradas",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            } else {
                // Perfil Responsável
                if (isOpen) {
                    Button(
                        onClick = onManageDependentsClick,
                        enabled = !isPerformingAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (enrolledDependents.isNotEmpty()) CamilaGreenDark else CamilaGreen
                        )
                    ) {
                        Icon(imageVector = Icons.Default.FamilyRestroom, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (enrolledDependents.isNotEmpty()) "Gerenciar Inscrições de Dependentes" else "Inscrever Dependente",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                } else {
                    if (enrolledDependents.isNotEmpty()) {
                        Button(
                            onClick = onManageDependentsClick,
                            enabled = !isPerformingAction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CamilaOrange)
                        ) {
                            Icon(imageVector = Icons.Default.FamilyRestroom, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gerenciar Matrículas Ativas",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = CamilaGray.copy(alpha = 0.2f),
                                disabledContentColor = CamilaGray
                            )
                        ) {
                            Text(
                                text = if (workshop.isFull()) "Vagas Esgotadas" else "Inscrições Encerradas",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Seção Expansível com Detalhes Pedagógicos
            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BackgroundLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Informações da Oficina",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark,
                )

                Spacer(modifier = Modifier.height(8.dp))

                DetailItem(
                    icon = Icons.Default.DateRange,
                    label = "Encontros:",
                    value = "Atividades semanais presenciais na sede",
                )

                Spacer(modifier = Modifier.height(6.dp))

                DetailItem(
                    icon = Icons.Default.Schedule,
                    label = "Certificação:",
                    value = "Certificado institucional após conclusão do ciclo",
                )

                Spacer(modifier = Modifier.height(6.dp))

                DetailItem(
                    icon = Icons.Default.Place,
                    label = "Local:",
                    value = "Sede Social - Associação Camila",
                )

                if (workshop.maxCapacity > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    DetailItem(
                        icon = Icons.Default.Groups,
                        label = "Capacidade:",
                        value = "Limite de ${workshop.maxCapacity} vagas por turma",
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollDependentsBottomSheet(
    workshop: Workshop,
    dependents: List<Dependent>,
    isPerformingAction: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
) {
    // Inicializa seleção com dependentes já cadastrados no array enrolledStudentIds
    val initiallyEnrolled = remember(workshop.enrolledStudentIds, dependents) {
        dependents.filter { workshop.enrolledStudentIds.contains(it.id) }.map { it.id }.toSet()
    }
    var selectedDependentIds by remember { mutableStateOf(initiallyEnrolled) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Inscrição de Dependentes",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                    Text(
                        text = workshop.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = CamilaOrange
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Marque ou desmarque abaixo os dependentes que participarão desta oficina. Cada filho(a) terá frequência e acompanhamento individuais.",
                style = MaterialTheme.typography.bodySmall,
                color = CamilaGray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (dependents.isEmpty()) {
                Surface(
                    color = CamilaOrange.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Nenhum dependente vinculado à sua conta.",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CamilaOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Entre em contato com a equipe social da Associação Camila para cadastrar os dados dos seus dependentes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dependentes Selecionados (${selectedDependentIds.size}/${dependents.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        onClick = {
                            selectedDependentIds = if (selectedDependentIds.size == dependents.size) {
                                emptySet()
                            } else {
                                dependents.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Text(
                            text = if (selectedDependentIds.size == dependents.size) "Desmarcar todos" else "Marcar todos",
                            color = CamilaGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                dependents.forEach { dependent ->
                    val isChecked = selectedDependentIds.contains(dependent.id)
                    val wasInitiallyEnrolled = initiallyEnrolled.contains(dependent.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedDependentIds = if (isChecked) {
                                    selectedDependentIds - dependent.id
                                } else {
                                    selectedDependentIds + dependent.id
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) CamilaGreen.copy(alpha = 0.08f) else BackgroundLight
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    selectedDependentIds = if (checked) {
                                        selectedDependentIds + dependent.id
                                    } else {
                                        selectedDependentIds - dependent.id
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = CamilaGreen)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isChecked) CamilaGreen.copy(alpha = 0.18f) else CamilaOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    tint = if (isChecked) CamilaGreenDark else CamilaOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dependent.name.ifBlank { "Dependente" },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = dependent.relationship.ifBlank { "Filho(a)" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CamilaGray
                                    )

                                    if (wasInitiallyEnrolled) {
                                        Surface(
                                            color = CamilaGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Já matriculado",
                                                color = CamilaGreenDark,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Confirmar Inscrições
            Button(
                onClick = { onConfirm(selectedDependentIds) },
                enabled = !isPerformingAction && dependents.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CamilaGreen,
                    disabledContainerColor = CamilaGreen.copy(alpha = 0.5f)
                )
            ) {
                if (isPerformingAction) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Confirmar Inscrições",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoTag(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CamilaGray,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label ",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = CamilaGray,
        )
    }
}

@Composable
private fun EmptyWorkshopsState(
    filter: WorkshopListFilter,
    isResponsible: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
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
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = CamilaGreen,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val title = when (filter) {
                WorkshopListFilter.TODAS -> "Nenhuma oficina cadastrada"
                WorkshopListFilter.MATRICULADAS -> {
                    if (isResponsible) "Nenhum dependente matriculado no momento"
                    else "Você ainda não está matriculado em nenhuma oficina"
                }
                WorkshopListFilter.ABERTAS -> "Nenhuma oficina com inscrições abertas no momento"
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val desc = when (filter) {
                WorkshopListFilter.TODAS -> "Aguarde a divulgação de novas turmas e projetos pela Associação Camila."
                WorkshopListFilter.MATRICULADAS -> "Explore a aba 'Inscrições Abertas' para encontrar projetos socioeducativos disponíveis."
                WorkshopListFilter.ABERTAS -> "Fique atento aos comunicados oficiais no Mural de Avisos para novos períodos de matrícula."
            }

            Text(
                text = desc,
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
private fun StudentWorkshopsScreenPreview() {
    AssociacaoCamilaTheme {
        StudentWorkshopsContent(
            uiState = StudentWorkshopsUiState(
                isLoading = false,
                workshops = listOf(
                    Workshop(
                        id = "1",
                        name = "Oficina de Fortalecimento",
                        description = "Encontros socioeducativos, desenvolvimento cidadão e apoio às famílias.",
                        teacherId = "t1",
                        enrolledStudentIds = listOf("s1", "s2", "s3"),
                        isRegistrationOpen = true,
                        maxCapacity = 20,
                    ),
                    Workshop(
                        id = "2",
                        name = "Projeto Mídias Force",
                        description = "Capacitação prática em comunicação comunitária, edição audiovisual e mídias sociais.",
                        teacherId = "t2",
                        enrolledStudentIds = listOf("s1", "s2"),
                        isRegistrationOpen = false,
                    ),
                ),
            ),
            currentUser = User(
                id = "s1",
                name = "Carlos Eduardo",
                role = UserRole.ALUNO,
            )
        )
    }
}
