package com.associacaocamila.app.ui.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

@Composable
fun AttendanceScreen(
    teacherId: String,
    modifier: Modifier = Modifier,
    viewModel: AttendanceViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(teacherId) {
        viewModel.loadWorkshops(teacherId)
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
            onSetAttendance = { studentId, isPresent -> viewModel.setStudentAttendance(studentId, isPresent) },
            onMarkAll = { viewModel.markAll(it) },
            onSubmitAttendance = { viewModel.submitAttendance(teacherId, context) },
            onExportPdf = { viewModel.exportAttendancePdf(context) },
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
    onSetAttendance: (String, Boolean) -> Unit,
    onMarkAll: (Boolean) -> Unit,
    onSubmitAttendance: () -> Unit,
    onExportPdf: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoadingWorkshops) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = CamilaGreen,
                strokeWidth = 3.dp
            )
        }
        return
    }

    if (uiState.workshops.isEmpty()) {
        EmptyWorkshopsState(modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        // Cabeçalho institucional de seleção de turma e data
        AttendanceHeaderCard(
            selectedWorkshop = uiState.selectedWorkshop,
            workshops = uiState.workshops,
            onSelectWorkshop = onSelectWorkshop
        )

        Spacer(modifier = Modifier.height(12.dp))

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
        } else {
            // Controles rápidos: presença total / faltas
            val presentCount = uiState.attendanceMap.values.count { it }
            val totalCount = uiState.participants.size

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Presentes: $presentCount de $totalCount",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark
                )

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

            // Lista de participantes matriculados (Alunos e Dependentes)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(
                    items = uiState.participants,
                    key = { it.id }
                ) { participant ->
                    val isPresent = uiState.attendanceMap[participant.id] ?: true
                    ParticipantAttendanceItem(
                        participant = participant,
                        isPresent = isPresent,
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
            // Botão Exportar Frequência (PDF)
            androidx.compose.material3.OutlinedButton(
                onClick = onExportPdf,
                enabled = !uiState.isExportingPdf && uiState.selectedWorkshop != null,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CamilaGreen),
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
                        contentDescription = "Exportar Frequência em PDF",
                        tint = CamilaGreenDark,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PDF",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = CamilaGreenDark,
                    )
                }
            }

            // Botão Salvar Lista de Presença
            Button(
                onClick = onSubmitAttendance,
                enabled = !uiState.isSubmitting && uiState.participants.isNotEmpty(),
                modifier = Modifier
                    .weight(2.2f)
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
                        text = "Salvar Lista de Presença",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceHeaderCard(
    selectedWorkshop: Workshop?,
    workshops: List<Workshop>,
    onSelectWorkshop: (Workshop) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatted = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            text = "Registro diário de participação",
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray
                        )
                    }
                }

                // Badge de data
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = CamilaOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            text = "Oficina Ativa",
                            style = MaterialTheme.typography.labelSmall,
                            color = CamilaGray
                        )
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
}

@Composable
private fun ParticipantAttendanceItem(
    participant: EligibleParticipant,
    isPresent: Boolean,
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
                        if (isPresent) CamilaGreen.copy(alpha = 0.12f)
                        else CamilaOrange.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (participant.isDependent) Icons.Default.FamilyRestroom else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isPresent) CamilaGreen else CamilaOrange,
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

            // Seletor Segmentado de Presença / Falta Moderno
            AttendanceSegmentedControl(
                isPresent = isPresent,
                onSelectPresent = onSelectPresent,
                onSelectAbsent = onSelectAbsent
            )
        }
    }
}

@Composable
private fun AttendanceSegmentedControl(
    isPresent: Boolean,
    onSelectPresent: () -> Unit,
    onSelectAbsent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F3F4))
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Segmento: Presente
        val presentBgColor by animateColorAsState(
            targetValue = if (isPresent) CamilaGreen else Color.Transparent,
            label = "presentBgColor"
        )
        val presentContentColor by animateColorAsState(
            targetValue = if (isPresent) Color.White else CamilaGray,
            label = "presentContentColor"
        )

        Box(
            modifier = Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(presentBgColor)
                .clickable(onClick = onSelectPresent)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = presentContentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Presente",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isPresent) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = presentContentColor
                )
            }
        }

        // Segmento: Falta
        val absentBgColor by animateColorAsState(
            targetValue = if (!isPresent) CamilaOrange else Color.Transparent,
            label = "absentBgColor"
        )
        val absentContentColor by animateColorAsState(
            targetValue = if (!isPresent) Color.White else CamilaGray,
            label = "absentContentColor"
        )

        Box(
            modifier = Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(absentBgColor)
                .clickable(onClick = onSelectAbsent)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = absentContentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
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
private fun EmptyWorkshopsState(modifier: Modifier = Modifier) {
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
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = CamilaGreen,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nenhuma oficina atribuída",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Você ainda não possui turmas sob sua responsabilidade pedagógica. Entre em contato com a coordenação.",
                style = MaterialTheme.typography.bodyMedium,
                color = CamilaGray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
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
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CamilaOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nenhum participante matriculado",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Esta turma ainda não possui estudantes ou dependentes vinculados para realização da chamada.",
                style = MaterialTheme.typography.bodySmall,
                color = CamilaGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AttendanceScreenPreview() {
    AssociacaoCamilaTheme {
        AttendanceContent(
            uiState = AttendanceUiState(
                isLoadingWorkshops = false,
                workshops = listOf(
                    Workshop(
                        id = "1",
                        name = "Oficina de Fortalecimento",
                        description = "Encontros socioeducativos",
                        teacherId = "t1"
                    )
                ),
                selectedWorkshop = Workshop(
                    id = "1",
                    name = "Oficina de Fortalecimento",
                    description = "Encontros socioeducativos",
                    teacherId = "t1"
                ),
                isLoadingStudents = false,
                participants = listOf(
                    EligibleParticipant(
                        id = "1",
                        name = "Ana Beatriz Souza",
                        isDependent = false,
                        email = "ana.souza@email.com"
                    ),
                    EligibleParticipant(
                        id = "2",
                        name = "Lucas Mendes",
                        isDependent = true,
                        responsibleName = "Mariana Mendes",
                        relationship = "Filho(a)"
                    )
                ),
                attendanceMap = mapOf("1" to true, "2" to false)
            ),
            onSelectWorkshop = {},
            onSetAttendance = { _, _ -> },
            onMarkAll = {},
            onSubmitAttendance = {}
        )
    }
}
