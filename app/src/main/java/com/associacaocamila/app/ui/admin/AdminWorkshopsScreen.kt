package com.associacaocamila.app.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.associacaocamila.app.data.model.EligibleParticipant
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.data.model.UserStatus
import com.associacaocamila.app.data.model.Workshop
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange
import kotlinx.coroutines.launch

@Composable
fun AdminWorkshopsScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminWorkshopsViewModel = viewModel(),
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

    AdminWorkshopsContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSeedMockData = { viewModel.seedMockWorkshops() },
        onCreateWorkshop = { name, desc, teacherId, participants, onSuccess ->
            viewModel.createWorkshop(name, desc, teacherId, participants, onSuccess)
        },
        onEditClick = { viewModel.selectWorkshopForEdit(it) },
        onDismissEdit = { viewModel.selectWorkshopForEdit(null) },
        onSaveWorkshopEdit = { viewModel.updateWorkshop(it) },
        onDeleteClick = { viewModel.selectWorkshopForDelete(it) },
        onDismissDelete = { viewModel.selectWorkshopForDelete(null) },
        onConfirmDelete = { viewModel.deleteWorkshop(it) },
        onDismissError = { viewModel.clearMessages() },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWorkshopsContent(
    uiState: AdminWorkshopsUiState,
    snackbarHostState: SnackbarHostState,
    onSeedMockData: () -> Unit,
    onCreateWorkshop: (String, String, String, List<String>, () -> Unit) -> Unit,
    onEditClick: (Workshop) -> Unit,
    onDismissEdit: () -> Unit,
    onSaveWorkshopEdit: (Workshop) -> Unit,
    onDeleteClick: (Workshop) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: (String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCreateBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showSeedConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateBottomSheet = true },
                containerColor = CamilaGreen,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Criar nova turma"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Cabeçalho de Gestão com ação de Seed Mock
            WorkshopsHeader(
                totalWorkshops = uiState.workshops.size,
                isSeeding = uiState.isSeeding,
                onSeedClick = { showSeedConfirmDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Alerta de erro
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismissError) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar erro",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (uiState.isLoadingWorkshops) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = CamilaGreen,
                        strokeWidth = 3.dp
                    )
                }
            } else if (uiState.workshops.isEmpty()) {
                EmptyWorkshopsAdminState(
                    onSeedClick = { showSeedConfirmDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.workshops,
                        key = { it.id }
                    ) { workshop ->
                        val teacherName = uiState.teachers
                            .firstOrNull { it.id == workshop.teacherId }
                            ?.name

                        WorkshopAdminCard(
                            workshop = workshop,
                            teacherName = teacherName,
                            onEditClick = { onEditClick(workshop) },
                            onDeleteClick = { onDeleteClick(workshop) }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmação para Seed Mock Data
    if (showSeedConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSeedConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Gerar Oficinas Institucionais",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Deseja gerar automaticamente as 3 turmas oficiais da Associação Camila (Fortalecimento, Mídias Force e Interconexão) vinculando aos professores e alunos aprovados existentes?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CamilaGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSeedConfirmDialog = false
                        onSeedMockData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen)
                ) {
                    Text("Gerar Dados", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeedConfirmDialog = false }) {
                    Text("Cancelar", color = CamilaGray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Diálogo de confirmação para Exclusão de Oficina
    uiState.workshopToDelete?.let { workshopToDelete ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Excluir Oficina",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Deseja realmente excluir a oficina \"${workshopToDelete.name}\"? Esta ação não poderá ser desfeita e removerá os vínculos da turma.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CamilaGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { onConfirmDelete(workshopToDelete.id) },
                    enabled = !uiState.isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = CamilaOrange)
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Excluir", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissDelete,
                    enabled = !uiState.isDeleting
                ) {
                    Text("Cancelar", color = CamilaGray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ModalBottomSheet para criar nova turma com lista de participantes (Alunos + Dependentes)
    if (showCreateBottomSheet) {
        CreateWorkshopBottomSheet(
            teachers = uiState.teachers,
            participants = uiState.eligibleParticipants,
            isCreating = uiState.isCreating,
            onDismiss = {
                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                    showCreateBottomSheet = false
                }
            },
            onCreate = { name, desc, teacherId, participants ->
                onCreateWorkshop(name, desc, teacherId, participants) {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        showCreateBottomSheet = false
                    }
                }
            }
        )
    }

    // ModalBottomSheet para editar turma selecionada
    uiState.workshopToEdit?.let { workshopToEdit ->
        EditWorkshopBottomSheet(
            workshop = workshopToEdit,
            teachers = uiState.teachers,
            participants = uiState.eligibleParticipants,
            isUpdating = uiState.isUpdating,
            onDismiss = onDismissEdit,
            onSave = onSaveWorkshopEdit
        )
    }
}

@Composable
private fun WorkshopsHeader(
    totalWorkshops: Int,
    isSeeding: Boolean,
    onSeedClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CamilaGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = CamilaGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Gestão de Turmas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                    Text(
                        text = "$totalWorkshops turma(s) em atividade",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }
            }

            // Botão de ação rápida para Seed Mock
            if (isSeeding) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = CamilaOrange,
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = onSeedClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CamilaOrange.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Gerar turmas oficiais de teste",
                        tint = CamilaOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkshopAdminCard(
    workshop: Workshop,
    teacherName: String?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = workshop.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = CamilaGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${workshop.enrolledStudentIds.size} participante(s)",
                            color = CamilaGreenDark,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar oficina ${workshop.name}",
                            tint = CamilaGreenDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir oficina ${workshop.name}",
                            tint = CamilaOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (workshop.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = workshop.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = CamilaGray,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CamilaOrange.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CamilaOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Professor(a) Responsável:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CamilaGray
                    )
                    Text(
                        text = teacherName ?: if (workshop.teacherId.isNotBlank()) "Professor vinculado" else "Não atribuído",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = if ((teacherName != null) || workshop.teacherId.isNotBlank()) MaterialTheme.colorScheme.onSurface else CamilaOrange
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateWorkshopBottomSheet(
    teachers: List<User>,
    participants: List<EligibleParticipant>,
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, List<String>) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedTeacherId by rememberSaveable { mutableStateOf(teachers.firstOrNull()?.id.orEmpty()) }
    var teacherDropdownExpanded by remember { mutableStateOf(false) }

    val uniqueParticipants = remember(participants) {
        participants.distinctBy { it.id }.distinctBy { it.name.trim().lowercase() }
    }
    var selectedStudentIds by remember { mutableStateOf(setOf<String>()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cadastrar Nova Turma",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nome da Oficina
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome da Oficina / Turma") },
                placeholder = { Text("Ex: Oficina de Artes Marciais") },
                singleLine = true,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Descrição
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição dos Objetivos") },
                placeholder = { Text("Breve descrição das atividades desenvolvidas...") },
                minLines = 2,
                maxLines = 4,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown de Professor
            Text(
                text = "Professor(a) Responsável",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (teachers.isEmpty()) {
                Surface(
                    color = CamilaOrange.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aviso: Nenhum professor aprovado cadastrado no momento.",
                        color = CamilaOrange,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                val selectedTeacher = teachers.firstOrNull { it.id == selectedTeacherId } ?: teachers.first()

                ExposedDropdownMenuBox(
                    expanded = teacherDropdownExpanded,
                    onExpandedChange = { teacherDropdownExpanded = !teacherDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedTeacher.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherDropdownExpanded)
                        },
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = teacherDropdownExpanded,
                        onDismissRequest = { teacherDropdownExpanded = false }
                    ) {
                        teachers.forEach { teacher ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = teacher.name,
                                        fontWeight = if (teacher.id == selectedTeacher.id) FontWeight.Bold else FontWeight.Normal,
                                        color = if (teacher.id == selectedTeacher.id) CamilaGreenDark else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedTeacherId = teacher.id
                                    teacherDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seleção de Participantes (Alunos e Dependentes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Matricular Participantes (${selectedStudentIds.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (uniqueParticipants.isNotEmpty()) {
                    val allSelected = uniqueParticipants.all { selectedStudentIds.contains(it.id) }
                    TextButton(
                        onClick = {
                            selectedStudentIds = if (allSelected) {
                                emptySet()
                            } else {
                                selectedStudentIds + uniqueParticipants.map { it.id }
                            }
                        }
                    ) {
                        Text(
                            text = if (allSelected) "Desmarcar todos" else "Marcar todos",
                            color = CamilaGreen
                        )
                    }
                }
            }

            if (uniqueParticipants.isEmpty()) {
                Surface(
                    color = CamilaGray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nenhum participante (aluno ou dependente) disponível para matrícula.",
                        color = CamilaGray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                uniqueParticipants.forEach { participant ->
                    val isSelected = selectedStudentIds.contains(participant.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedStudentIds = if (isSelected) {
                                    selectedStudentIds - participant.id
                                } else {
                                    selectedStudentIds + participant.id
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                selectedStudentIds = if (checked) {
                                    selectedStudentIds + participant.id
                                } else {
                                    selectedStudentIds - participant.id
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = CamilaGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = participant.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (participant.isDependent) CamilaOrange.copy(alpha = 0.12f) else CamilaGreen.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (participant.isDependent) "Dependente" else "Aluno",
                                        color = if (participant.isDependent) CamilaOrange else CamilaGreenDark,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (!participant.responsibleName.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = CamilaOrange,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Responsável: ${participant.responsibleName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CamilaOrange
                                    )
                                }
                            } else if (!participant.email.isNullOrBlank()) {
                                Text(
                                    text = participant.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CamilaGray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Cadastrar Turma
            Button(
                onClick = {
                    onCreate(
                        name,
                        description,
                        selectedTeacherId,
                        selectedStudentIds.toList()
                    )
                },
                enabled = !isCreating && name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CamilaGreen,
                    disabledContainerColor = CamilaGreen.copy(alpha = 0.5f)
                )
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Cadastrar Turma",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditWorkshopBottomSheet(
    workshop: Workshop,
    teachers: List<User>,
    participants: List<EligibleParticipant>,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onSave: (Workshop) -> Unit,
) {
    var name by rememberSaveable(workshop.id) { mutableStateOf(workshop.name) }
    var description by rememberSaveable(workshop.id) { mutableStateOf(workshop.description) }
    var selectedTeacherId by rememberSaveable(workshop.id) {
        mutableStateOf(workshop.teacherId.ifBlank { teachers.firstOrNull()?.id.orEmpty() })
    }
    var teacherDropdownExpanded by remember { mutableStateOf(false) }

    val uniqueParticipants = remember(participants) {
        participants.distinctBy { it.id }.distinctBy { it.name.trim().lowercase() }
    }
    var selectedStudentIds by remember(workshop.id) {
        mutableStateOf(workshop.enrolledStudentIds.toSet())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Editar Turma",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nome da Oficina
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome da Oficina / Turma") },
                placeholder = { Text("Ex: Oficina de Artes Marciais") },
                singleLine = true,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Descrição
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição dos Objetivos") },
                placeholder = { Text("Breve descrição das atividades desenvolvidas...") },
                minLines = 2,
                maxLines = 4,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown de Professor
            Text(
                text = "Professor(a) Responsável",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (teachers.isEmpty()) {
                Surface(
                    color = CamilaOrange.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aviso: Nenhum professor aprovado cadastrado no momento.",
                        color = CamilaOrange,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                val selectedTeacher = teachers.firstOrNull { it.id == selectedTeacherId } ?: teachers.first()

                ExposedDropdownMenuBox(
                    expanded = teacherDropdownExpanded,
                    onExpandedChange = { teacherDropdownExpanded = !teacherDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedTeacher.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherDropdownExpanded)
                        },
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = teacherDropdownExpanded,
                        onDismissRequest = { teacherDropdownExpanded = false }
                    ) {
                        teachers.forEach { teacher ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = teacher.name,
                                        fontWeight = if (teacher.id == selectedTeacher.id) FontWeight.Bold else FontWeight.Normal,
                                        color = if (teacher.id == selectedTeacher.id) CamilaGreenDark else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedTeacherId = teacher.id
                                    teacherDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seleção de Participantes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Participantes Matriculados (${selectedStudentIds.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (uniqueParticipants.isNotEmpty()) {
                    val allSelected = uniqueParticipants.all { selectedStudentIds.contains(it.id) }
                    TextButton(
                        onClick = {
                            selectedStudentIds = if (allSelected) {
                                emptySet()
                            } else {
                                selectedStudentIds + uniqueParticipants.map { it.id }
                            }
                        }
                    ) {
                        Text(
                            text = if (allSelected) "Desmarcar todos" else "Marcar todos",
                            color = CamilaGreen
                        )
                    }
                }
            }

            if (uniqueParticipants.isEmpty()) {
                Surface(
                    color = CamilaGray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nenhum participante disponível para matrícula.",
                        color = CamilaGray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                uniqueParticipants.forEach { participant ->
                    val isSelected = selectedStudentIds.contains(participant.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedStudentIds = if (isSelected) {
                                    selectedStudentIds - participant.id
                                } else {
                                    selectedStudentIds + participant.id
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                selectedStudentIds = if (checked) {
                                    selectedStudentIds + participant.id
                                } else {
                                    selectedStudentIds - participant.id
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = CamilaGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = participant.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (participant.isDependent) CamilaOrange.copy(alpha = 0.12f) else CamilaGreen.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (participant.isDependent) "Dependente" else "Aluno",
                                        color = if (participant.isDependent) CamilaOrange else CamilaGreenDark,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (!participant.responsibleName.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = CamilaOrange,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Responsável: ${participant.responsibleName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CamilaOrange
                                    )
                                }
                            } else if (!participant.email.isNullOrBlank()) {
                                Text(
                                    text = participant.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CamilaGray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Salvar Alterações
            Button(
                onClick = {
                    onSave(
                        workshop.copy(
                            name = name.trim(),
                            description = description.trim(),
                            teacherId = selectedTeacherId.trim(),
                            enrolledStudentIds = selectedStudentIds.toList()
                        )
                    )
                },
                enabled = !isUpdating && name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CamilaGreen,
                    disabledContainerColor = CamilaGreen.copy(alpha = 0.5f)
                )
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Salvar Alterações",
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
private fun EmptyWorkshopsAdminState(
    onSeedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(CamilaGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CamilaGreen,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nenhuma turma cadastrada",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crie uma nova turma clicando no botão (+) abaixo ou clique para gerar as oficinas oficiais da Associação Camila.",
                style = MaterialTheme.typography.bodyMedium,
                color = CamilaGray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSeedClick,
                colors = ButtonDefaults.buttonColors(containerColor = CamilaOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gerar Oficinas Padrão",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CamilaGreen,
    unfocusedBorderColor = CamilaGray.copy(alpha = 0.4f),
    focusedLabelColor = CamilaGreen,
    cursorColor = CamilaGreen
)

@Preview(showBackground = true)
@Composable
private fun AdminWorkshopsScreenPreview() {
    AssociacaoCamilaTheme {
        AdminWorkshopsContent(
            uiState = AdminWorkshopsUiState(
                isLoadingWorkshops = false,
                workshops = listOf(
                    Workshop(
                        id = "1",
                        name = "Oficina de Fortalecimento",
                        description = "Encontros socioeducativos, desenvolvimento cidadão e apoio às famílias.",
                        teacherId = "t1",
                        enrolledStudentIds = listOf("s1", "s2", "s3")
                    ),
                    Workshop(
                        id = "2",
                        name = "Projeto Mídias Force",
                        description = "Capacitação prática em comunicação comunitária e audiovisual.",
                        teacherId = "t1",
                        enrolledStudentIds = listOf("s1", "s2")
                    )
                ),
                teachers = listOf(
                    User(id = "t1", name = "Prof. Marcelo Ramos", role = UserRole.PROFESSOR, status = UserStatus.APPROVED)
                ),
                eligibleParticipants = listOf(
                    EligibleParticipant(id = "s1", name = "Gabriel Silva", isDependent = false, email = "gabriel@email.com"),
                    EligibleParticipant(id = "s2", name = "Lucas Mendes", isDependent = true, responsibleName = "Mariana Mendes")
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onSeedMockData = {},
            onCreateWorkshop = { _, _, _, _, _ -> },
            onEditClick = {},
            onDismissEdit = {},
            onSaveWorkshopEdit = {},
            onDeleteClick = {},
            onDismissDelete = {},
            onConfirmDelete = {},
            onDismissError = {}
        )
    }
}
