package com.associacaocamila.app.ui.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ripple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.associacaocamila.app.data.model.AttendanceStatus
import com.associacaocamila.app.data.model.EligibleParticipant
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.Workshop
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun AttendanceScreen(
    teacherId: String = "",
    currentUser: User? = null,
    modifier: Modifier = Modifier,
    viewModel: AttendanceViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    val effectiveTeacherId = remember(teacherId, currentUser) {
        teacherId.ifBlank { currentUser?.id.orEmpty() }
    }

    LaunchedEffect(effectiveTeacherId) {
        viewModel.loadWorkshops(effectiveTeacherId)
    }

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

    Box(modifier = modifier.fillMaxSize()) {
        AttendanceContent(
            uiState = uiState,
            onSelectWorkshop = { viewModel.selectWorkshop(it) },
            onPreviousDay = { viewModel.previousDay() },
            onNextDay = { viewModel.nextDay() },
            onSelectDate = { viewModel.selectDate(it) },
            onSetAttendance = { studentId, isPresent -> viewModel.setStudentAttendance(studentId, isPresent) },
            onMarkAll = { viewModel.markAll(it) },
            onSubmitAttendance = { viewModel.submitAttendance(effectiveTeacherId, context) },
            onExportPdf = { startMillis, endMillis ->
                viewModel.exportAttendancePdf(context, startMillis, endMillis)
            },
            onRetry = { viewModel.loadWorkshops(effectiveTeacherId) },
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun AttendanceContent(
    uiState: AttendanceUiState,
    onSelectWorkshop: (Workshop) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSelectDate: (Long) -> Unit = {},
    onSetAttendance: (String, Boolean) -> Unit,
    onMarkAll: (Boolean) -> Unit,
    onSubmitAttendance: () -> Unit,
    onExportPdf: (startDateMillis: Long, endDateMillis: Long) -> Unit,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // 1. Estado de Carregamento (Loading)
    if (uiState.isLoadingWorkshops) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    color = CamilaGreen,
                    strokeWidth = 3.dp
                )
                Text(
                    text = "Carregando turmas da chamada...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CamilaGray
                )
            }
        }
        return
    }

    // 2. Estado de Erro Seguro (Error): Falha na consulta de oficinas no Firestore
    if (uiState.errorMessage != null && uiState.workshops.isEmpty()) {
        AttendanceErrorState(
            message = uiState.errorMessage,
            onRetry = onRetry,
            modifier = modifier
        )
        return
    }

    // 3. Estado Vazio Seguro (Empty): Professor sem turmas vinculadas
    if (uiState.workshops.isEmpty()) {
        EmptyWorkshopsState(modifier = modifier)
        return
    }

    // 4. Estado de Sucesso (Success): Interface completa da chamada digital
    val isTodayDate = remember(uiState.selectedDateMillis) {
        val cal1 = Calendar.getInstance().apply { timeInMillis = uiState.selectedDateMillis }
        val cal2 = Calendar.getInstance()
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    var showDateRangeDialog by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        // Cabeçalho institucional de seleção de turma
        AttendanceHeaderCard(
            selectedWorkshop = uiState.selectedWorkshop,
            workshops = uiState.workshops,
            selectedDateMillis = uiState.selectedDateMillis,
            isToday = isTodayDate,
            onSelectWorkshop = onSelectWorkshop,
            onPreviousDay = onPreviousDay,
            onNextDay = onNextDay,
            onDateClick = { showDatePickerDialog = true },
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Alerta de Edição Bloqueada (Somente Leitura em datas anteriores)
        if (uiState.isReadOnly) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(10.dp),
                color = CamilaOrange.copy(alpha = 0.1f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CamilaOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Histórico (Somente Leitura): Registros de datas anteriores são bloqueados para edição.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Alerta de erro caso ocorra falha
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
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (uiState.isLoadingStudents) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = CamilaGreen,
                    strokeWidth = 3.dp
                )
            }
        } else if (uiState.participants.isEmpty()) {
            EmptyStudentsState(modifier = Modifier.weight(1f))
        } else if (uiState.isReadOnly && !uiState.isDateHasRecord) {
            NoAttendanceForDateState(modifier = Modifier.weight(1f))
        } else {
            // Lista de participantes matriculados (Alunos e Dependentes desduplicados)
            val uniqueStudents = remember(uiState.participants) {
                uiState.participants.distinctBy { it.id }
            }

            // Controles rápidos e resumo
            val presentCount = uiState.statusMap.values.count { it == AttendanceStatus.PRESENTE }
            val justifiedCount = uiState.statusMap.values.count { it == AttendanceStatus.JUSTIFICADO }
            val absenceCount = uiState.statusMap.values.count { it == AttendanceStatus.FALTA }
            val totalCount = uniqueStudents.size

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (justifiedCount > 0) {
                        "Presentes: $presentCount | Justificados: $justifiedCount | Faltas: $absenceCount"
                    } else {
                        "Presentes: $presentCount de $totalCount"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark
                )

                if (!uiState.isReadOnly) {
                    Row {
                        TextButton(onClick = { onMarkAll(true) }) {
                            Text(
                                text = "Todos presentes",
                                style = MaterialTheme.typography.labelMedium,
                                color = CamilaGreen
                            )
                        }
                        TextButton(onClick = { onMarkAll(false) }) {
                            Text(
                                text = "Todos ausentes",
                                style = MaterialTheme.typography.labelMedium,
                                color = CamilaOrange
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(
                    items = uniqueStudents,
                    key = { "${it.id}_${it.name}" }
                ) { participant ->
                    val status = uiState.statusMap[participant.id] ?: AttendanceStatus.PRESENTE
                    val isPresent = uiState.attendanceMap[participant.id] ?: (status == AttendanceStatus.PRESENTE)

                    ParticipantAttendanceItem(
                        participant = participant,
                        status = status,
                        isPresent = isPresent,
                        isReadOnly = uiState.isReadOnly,
                        onSelectPresent = { onSetAttendance(participant.id, true) },
                        onSelectAbsent = { onSetAttendance(participant.id, false) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botões de Ação: Exportar Relatório em PDF e Salvar Lista de Presença
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Botão Exportar Frequência (PDF) com seleção de período
            OutlinedButton(
                onClick = { showDateRangeDialog = true },
                enabled = !uiState.isExportingPdf && uiState.selectedWorkshop != null,
                modifier = Modifier
                    .weight(1.3f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CamilaGreen),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CamilaGreenDark,
                ),
            ) {
                if (uiState.isExportingPdf) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = CamilaGreen,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Exportar Relatório em PDF",
                        tint = CamilaGreenDark,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Exportar PDF",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = CamilaGreenDark,
                    )
                }
            }

            // Botão Salvar Lista de Presença (Habilitado apenas para o dia de hoje)
            if (!uiState.isReadOnly) {
                Button(
                    onClick = onSubmitAttendance,
                    enabled = !uiState.isSubmitting && uiState.participants.isNotEmpty(),
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CamilaGreen,
                        disabledContainerColor = CamilaGreen.copy(alpha = 0.5f),
                    ),
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = if (uiState.isDateHasRecord) "Atualizar Presenças" else "Salvar Lista de Presença",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .weight(2.2f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = CamilaGray.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Histórico Arquivado",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = CamilaGray
                        )
                    }
                }
            }
        }

        // Modal de Seleção de Período para o Relatório de Frequência
        if (showDateRangeDialog) {
            DateRangeReportPickerDialog(
                onDismiss = { showDateRangeDialog = false },
                onConfirm = { startMillis, endMillis ->
                    showDateRangeDialog = false
                    onExportPdf(startMillis, endMillis)
                }
            )
        }

        // Diálogo de Seleção de Data no Calendário
        if (showDatePickerDialog) {
            AttendanceDatePickerDialog(
                initialDateMillis = uiState.selectedDateMillis,
                onDismissRequest = { showDatePickerDialog = false },
                onDateSelected = { pickedUtcMillis ->
                    showDatePickerDialog = false
                    val localMillis = utcMillisToLocalDay(pickedUtcMillis)
                    val now = System.currentTimeMillis()
                    val effectiveMillis = if (localMillis > now) now else localMillis
                    onSelectDate(effectiveMillis)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceHeaderCard(
    selectedWorkshop: Workshop?,
    workshops: List<Workshop>,
    selectedDateMillis: Long,
    isToday: Boolean,
    onSelectWorkshop: (Workshop) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
    val formattedDate = remember(selectedDateMillis) {
        dateFormat.format(Date(selectedDateMillis))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Linha 1: Título e Seletor de Data com Navegação (< dd/MM/yyyy >)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CamilaGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = CamilaGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Chamada Digital",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark
                        )
                        Text(
                            text = if (isToday) "Registro de hoje" else "Histórico de frequência",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isToday) CamilaGreen else CamilaOrange
                        )
                    }
                }

                // Controle de navegação de data
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BackgroundLight,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = onPreviousDay,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Dia anterior",
                                tint = CamilaGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onDateClick)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Selecionar data no calendário",
                                tint = if (isToday) CamilaGreen else CamilaOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = onNextDay,
                            enabled = !isToday,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Próximo dia",
                                tint = if (!isToday) CamilaGreenDark else CamilaGray.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seletor de Oficina
            if (workshops.size > 1) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedWorkshop?.name.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selecione a Oficina") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = CamilaGreen
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CamilaGreen,
                            unfocusedBorderColor = CamilaGray.copy(alpha = 0.4f),
                            focusedLabelColor = CamilaGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        workshops.forEach { workshop ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = workshop.name,
                                        fontWeight = if (workshop.id == selectedWorkshop?.id) FontWeight.Bold else FontWeight.Normal,
                                        color = if (workshop.id == selectedWorkshop?.id) CamilaGreenDark else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onSelectWorkshop(workshop)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CamilaGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = CamilaGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedWorkshop?.name ?: "Oficina",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticipantAttendanceItem(
    participant: EligibleParticipant,
    status: AttendanceStatus,
    isPresent: Boolean,
    isReadOnly: Boolean,
    onSelectPresent: () -> Unit,
    onSelectAbsent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        when (status) {
                            AttendanceStatus.PRESENTE -> CamilaGreen.copy(alpha = 0.12f)
                            AttendanceStatus.FALTA -> CamilaOrange.copy(alpha = 0.12f)
                            AttendanceStatus.JUSTIFICADO -> Color(0xFF2196F3).copy(alpha = 0.12f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        status == AttendanceStatus.JUSTIFICADO -> Icons.Default.HealthAndSafety
                        participant.isDependent -> Icons.Default.FamilyRestroom
                        else -> Icons.Default.Person
                    },
                    contentDescription = null,
                    tint = when (status) {
                        AttendanceStatus.PRESENTE -> CamilaGreen
                        AttendanceStatus.FALTA -> CamilaOrange
                        AttendanceStatus.JUSTIFICADO -> Color(0xFF1976D2)
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = participant.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                if (participant.isDependent && !participant.responsibleName.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = CamilaOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Dep. de ${participant.responsibleName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaOrange,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                } else if (!participant.email.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = CamilaGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = participant.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray,
                            maxLines = 1
                        )
                    }
                } else {
                    Text(
                        text = "Aluno",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (!isReadOnly) {
                // Seletor interativo liberado para edição no dia atual
                AttendanceSegmentedControl(
                    isPresent = isPresent,
                    onSelectPresent = onSelectPresent,
                    onSelectAbsent = onSelectAbsent
                )
            } else {
                // Badge estática para dias anteriores
                AttendanceStatusBadge(status = status)
            }
        }
    }
}

@Composable
private fun AttendanceStatusBadge(
    status: AttendanceStatus,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor, icon, label) = when (status) {
        AttendanceStatus.PRESENTE -> Tuple4(
            CamilaGreen.copy(alpha = 0.15f),
            CamilaGreenDark,
            Icons.Default.Check,
            "PRESENTE"
        )
        AttendanceStatus.FALTA -> Tuple4(
            CamilaOrange.copy(alpha = 0.15f),
            CamilaOrange,
            Icons.Default.Close,
            "FALTA"
        )
        AttendanceStatus.JUSTIFICADO -> Tuple4(
            Color(0xFFE3F2FD),
            Color(0xFF1976D2),
            Icons.Default.HealthAndSafety,
            "JUSTIFICADO"
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@Composable
private fun AttendanceSegmentedControl(
    isPresent: Boolean,
    onSelectPresent: () -> Unit,
    onSelectAbsent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Botão: Presente
        val presentBgColor by animateColorAsState(
            targetValue = if (isPresent) CamilaGreen else Color.White,
            label = "presentBgColor"
        )
        val presentContentColor by animateColorAsState(
            targetValue = if (isPresent) Color.White else CamilaGray,
            label = "presentContentColor"
        )
        val presentBorderColor by animateColorAsState(
            targetValue = if (isPresent) CamilaGreen else CamilaGray.copy(alpha = 0.35f),
            label = "presentBorderColor"
        )

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = presentBgColor,
            border = BorderStroke(1.dp, presentBorderColor),
            shadowElevation = if (isPresent) 1.dp else 0.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = ripple(color = CamilaGreenDark),
                    onClick = onSelectPresent
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isPresent) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = presentContentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = "Presente",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isPresent) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = presentContentColor
                )
            }
        }

        // Botão: Falta
        val absentBgColor by animateColorAsState(
            targetValue = if (!isPresent) CamilaOrange else Color.White,
            label = "absentBgColor"
        )
        val absentContentColor by animateColorAsState(
            targetValue = if (!isPresent) Color.White else CamilaGray,
            label = "absentContentColor"
        )
        val absentBorderColor by animateColorAsState(
            targetValue = if (!isPresent) CamilaOrange else CamilaGray.copy(alpha = 0.35f),
            label = "absentBorderColor"
        )

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = absentBgColor,
            border = BorderStroke(1.dp, absentBorderColor),
            shadowElevation = if (!isPresent) 1.dp else 0.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = ripple(color = CamilaOrange),
                    onClick = onSelectAbsent
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!isPresent) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = absentContentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = "Falta",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (!isPresent) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = absentContentColor
                )
            }
        }
    }
}

@Composable
private fun EmptyStudentsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CamilaOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nenhum participante matriculado",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Esta turma ainda não possui participantes inscritos para registrar presença.",
                style = MaterialTheme.typography.bodyMedium,
                color = CamilaGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NoAttendanceForDateState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CamilaGray.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CamilaGray,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sem chamada nesta data",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Não houve lista de presença registrada para esta oficina no dia selecionado.",
                style = MaterialTheme.typography.bodyMedium,
                color = CamilaGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyWorkshopsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(CamilaGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = CamilaGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Nenhuma oficina vinculada",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Você ainda não possui oficinas vinculadas. Solicite à coordenação para associar suas turmas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CamilaGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun AttendanceErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Não foi possível carregar as turmas",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = message.ifBlank { "Ocorreu uma falha ao consultar suas oficinas no servidor. Verifique sua conexão e tente novamente." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = CamilaGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tentar Novamente",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AttendanceContentPreview() {
    AssociacaoCamilaTheme {
        AttendanceContent(
            uiState = AttendanceUiState(
                isLoadingWorkshops = false,
                workshops = listOf(
                    Workshop(id = "1", name = "Violão e Música", teacherId = "prof1")
                ),
                selectedWorkshop = Workshop(id = "1", name = "Violão e Música", teacherId = "prof1"),
                participants = listOf(
                    EligibleParticipant(id = "1", name = "Lucas Silva", isDependent = false),
                    EligibleParticipant(
                        id = "2",
                        name = "Beatriz Souza",
                        isDependent = true,
                        responsibleName = "Maria Souza",
                        relationship = "Filha"
                    )
                ),
                statusMap = mapOf(
                    "1" to AttendanceStatus.PRESENTE,
                    "2" to AttendanceStatus.FALTA
                ),
                attendanceMap = mapOf("1" to true, "2" to false)
            ),
            onSelectWorkshop = {},
            onPreviousDay = {},
            onNextDay = {},
            onSetAttendance = { _, _ -> },
            onMarkAll = {},
            onSubmitAttendance = {},
            onExportPdf = { _, _ -> },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeReportPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (startMillis: Long, endMillis: Long) -> Unit,
) {
    val dateRangePickerState = rememberDateRangePickerState()

    val formattedRange = remember(
        dateRangePickerState.selectedStartDateMillis,
        dateRangePickerState.selectedEndDateMillis
    ) {
        val startUtc = dateRangePickerState.selectedStartDateMillis
        val endUtc = dateRangePickerState.selectedEndDateMillis
        if (startUtc != null && endUtc != null) {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            "${format.format(Date(startUtc))} até ${format.format(Date(endUtc))}"
        } else if (startUtc != null) {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            "${format.format(Date(startUtc))} até ... (selecione data final)"
        } else {
            "Nenhum período selecionado"
        }
    }

    val isRangeValid = dateRangePickerState.selectedStartDateMillis != null &&
            dateRangePickerState.selectedEndDateMillis != null

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val startUtc = dateRangePickerState.selectedStartDateMillis
                    val endUtc = dateRangePickerState.selectedEndDateMillis
                    if (startUtc != null && endUtc != null) {
                        val utcCalStart = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = startUtc }
                        val localCalStart = Calendar.getInstance().apply {
                            set(Calendar.YEAR, utcCalStart.get(Calendar.YEAR))
                            set(Calendar.MONTH, utcCalStart.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utcCalStart.get(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        val utcCalEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = endUtc }
                        val localCalEnd = Calendar.getInstance().apply {
                            set(Calendar.YEAR, utcCalEnd.get(Calendar.YEAR))
                            set(Calendar.MONTH, utcCalEnd.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utcCalEnd.get(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }

                        onConfirm(localCalStart.timeInMillis, localCalEnd.timeInMillis)
                    }
                },
                enabled = isRangeValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CamilaGreen,
                    disabledContainerColor = CamilaGreen.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Gerar PDF",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = CamilaGray,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = BackgroundLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Text(
                        text = "Selecionar Período do Relatório",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Intervalo selecionado: $formattedRange",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isRangeValid) CamilaOrange else CamilaGray
                    )
                }
            }

            DateRangePicker(
                state = dateRangePickerState,
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = CamilaGreen,
                    selectedDayContentColor = Color.White,
                    dayInSelectionRangeContainerColor = CamilaGreen.copy(alpha = 0.15f),
                    dayInSelectionRangeContentColor = CamilaGreenDark,
                    todayDateBorderColor = CamilaOrange,
                    todayContentColor = CamilaOrange
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Diálogo com Material 3 DatePicker para seleção direta de data na chamada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceDatePickerDialog(
    initialDateMillis: Long,
    onDismissRequest: () -> Unit,
    onDateSelected: (Long) -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen)
            ) {
                Text("Selecionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar", color = CamilaGray)
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = CamilaGreen,
                todayDateBorderColor = CamilaGreen,
                todayContentColor = CamilaGreenDark
            )
        )
    }
}

/**
 * Converte milissegundos UTC retornados pelo DatePicker do Material 3
 * para a data correspondente no fuso horário local.
 */
private fun utcMillisToLocalDay(utcMillis: Long): Long {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = utcMillis
    }
    val localCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
        set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return localCal.timeInMillis
}
