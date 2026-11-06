package com.associacaocamila.app.ui.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
            onToggleAttendance = { viewModel.toggleStudentAttendance(it) },
            onMarkAll = { viewModel.markAll(it) },
            onSubmitAttendance = { viewModel.submitAttendance(teacherId) },
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
fun AttendanceContent(
    uiState: AttendanceUiState,
    onSelectWorkshop: (Workshop) -> Unit,
    onToggleAttendance: (String) -> Unit,
    onMarkAll: (Boolean) -> Unit,
    onSubmitAttendance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentDateFormatted = remember {
        val locale = Locale.forLanguageTag("pt-BR")
        val sdf = SimpleDateFormat("EEEE, dd 'de' MMMM", locale)
        sdf.format(Date()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    if (uiState.isLoadingWorkshops) {
        Box(
            modifier = modifier.fillMaxSize(),
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
        // Card de Cabeçalho da Chamada e Seleção de Oficina
        WorkshopHeaderCard(
            workshops = uiState.workshops,
            selectedWorkshop = uiState.selectedWorkshop,
            dateFormatted = currentDateFormatted,
            onSelectWorkshop = onSelectWorkshop
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Alerta de erro caso ocorra
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
        } else if (uiState.students.isEmpty()) {
            EmptyStudentsState(modifier = Modifier.weight(1f))
        } else {
            // Controles rápidos: presença total / faltas
            val presentCount = uiState.attendanceMap.values.count { it }
            val totalCount = uiState.students.size

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

            // Lista de alunos matriculados
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(
                    items = uiState.students,
                    key = { it.id }
                ) { student ->
                    val isPresent = uiState.attendanceMap[student.id] ?: true
                    StudentAttendanceItem(
                        student = student,
                        isPresent = isPresent,
                        onToggle = { onToggleAttendance(student.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão Salvar Lista de Presença
        Button(
            onClick = onSubmitAttendance,
            enabled = !uiState.isSubmitting && uiState.students.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CamilaGreen,
                disabledContainerColor = CamilaGreen.copy(alpha = 0.5f)
            )
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Salvar Lista de Presença",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkshopHeaderCard(
    workshops: List<Workshop>,
    selectedWorkshop: Workshop?,
    dateFormatted: String,
    onSelectWorkshop: (Workshop) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Data do dia
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CamilaOrange.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = CamilaOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Data da Chamada",
                        style = MaterialTheme.typography.labelSmall,
                        color = CamilaGray
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
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
private fun StudentAttendanceItem(
    student: User,
    isPresent: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPresent) CamilaGreen.copy(alpha = 0.12f)
                        else CamilaOrange.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isPresent) CamilaGreen else CamilaOrange,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name.ifBlank { "Sem nome" },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (student.email.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = CamilaGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = student.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botão / FilterChip estilizado de alternância
            FilterChip(
                selected = isPresent,
                onClick = onToggle,
                label = {
                    Text(
                        text = if (isPresent) "Presente" else "Falta",
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isPresent) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CamilaGreen,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White,
                    containerColor = CamilaOrange.copy(alpha = 0.15f),
                    labelColor = CamilaOrange,
                    iconColor = CamilaOrange
                ),
                shape = RoundedCornerShape(8.dp)
            )
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
                    .background(CamilaOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = CamilaOrange,
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
                text = "Ainda não existem turmas ou oficinas vinculadas ao seu perfil de professor. Por favor, consulte a coordenação da Associação Camila.",
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
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = CamilaGray,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Nenhum aluno matriculado",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Esta turma ainda não possui estudantes cadastrados na sua lista.",
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
                        id = "w1",
                        name = "Oficina de Fortalecimento Juvenil",
                        description = "Encontros de desenvolvimento socioeducativo"
                    ),
                    Workshop(
                        id = "w2",
                        name = "Mídias Force - Audiovisual",
                        description = "Produção de vídeo e comunicação digital"
                    )
                ),
                selectedWorkshop = Workshop(
                    id = "w1",
                    name = "Oficina de Fortalecimento Juvenil"
                ),
                students = listOf(
                    User(id = "s1", name = "Gabriel Oliveira", email = "gabriel@email.com", role = UserRole.ALUNO, status = UserStatus.APPROVED),
                    User(id = "s2", name = "Juliana Costa", email = "juliana@email.com", role = UserRole.ALUNO, status = UserStatus.APPROVED),
                    User(id = "s3", name = "Matheus Santos", email = "matheus@email.com", role = UserRole.ALUNO, status = UserStatus.APPROVED)
                ),
                attendanceMap = mapOf(
                    "s1" to true,
                    "s2" to false,
                    "s3" to true
                )
            ),
            onSelectWorkshop = {},
            onToggleAttendance = {},
            onMarkAll = {},
            onSubmitAttendance = {}
        )
    }
}
