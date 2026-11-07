package com.associacaocamila.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
fun StudentAttendanceScreen(
    currentUser: User,
    modifier: Modifier = Modifier,
    viewModel: StudentAttendanceViewModel = viewModel(),
) {
    val isResponsible = currentUser.role == UserRole.RESPONSAVEL
    val dependents = currentUser.dependents

    // Se for Responsável e tiver dependentes, controla qual dependente está selecionado
    var selectedDependentId by rememberSaveable(currentUser.id, dependents.size) {
        mutableStateOf(dependents.firstOrNull()?.id.orEmpty())
    }

    val effectiveStudentId = remember(isResponsible, selectedDependentId, currentUser.id) {
        if (isResponsible) selectedDependentId else currentUser.id
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(effectiveStudentId) {
        if (effectiveStudentId.isNotBlank()) {
            viewModel.loadAttendance(effectiveStudentId)
        }
    }

    StudentAttendanceContent(
        uiState = uiState,
        currentUser = currentUser,
        selectedDependentId = selectedDependentId,
        onSelectDependent = { selectedDependentId = it },
        modifier = modifier,
    )
}

@Composable
fun StudentAttendanceContent(
    uiState: StudentAttendanceUiState,
    currentUser: User? = null,
    selectedDependentId: String = "",
    onSelectDependent: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isResponsible = currentUser?.role == UserRole.RESPONSAVEL
    val dependents = currentUser?.dependents.orEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        // Seletor horizontal de dependentes para perfil Responsável
        if (isResponsible) {
            if (dependents.isEmpty()) {
                NoDependentsAttendanceWarningCard()
                return@Column
            } else {
                AttendanceDependentsSelectorBar(
                    dependents = dependents,
                    selectedDependentId = selectedDependentId,
                    onSelectDependent = onSelectDependent
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = CamilaGreen,
                    strokeWidth = 3.dp
                )
            }
        } else if (uiState.history.isEmpty()) {
            EmptyStudentAttendanceState(
                isDependent = isResponsible,
                dependentName = dependents.firstOrNull { it.id == selectedDependentId }?.name
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StudentAttendanceHeader(
                        rate = uiState.attendancePercentage,
                        presents = uiState.totalPresences,
                        absences = uiState.totalAbsences,
                        targetName = if (isResponsible) {
                            dependents.firstOrNull { it.id == selectedDependentId }?.name
                        } else null
                    )
                }

                item {
                    Text(
                        text = "Histórico Detalhado",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(
                    items = uiState.history,
                    key = { "${it.id}_${it.dateMillis}" }
                ) { item ->
                    AttendanceHistoryCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun AttendanceDependentsSelectorBar(
    dependents: List<Dependent>,
    selectedDependentId: String,
    onSelectDependent: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FamilyRestroom,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Acompanhar Frequência de:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dependents.forEach { dependent ->
                    val isSelected = dependent.id == selectedDependentId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectDependent(dependent.id) },
                        label = {
                            Text(
                                text = dependent.name.ifBlank { "Dependente" },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CamilaGreen,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = CamilaOrange.copy(alpha = 0.08f),
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            iconColor = CamilaOrange
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoDependentsAttendanceWarningCard() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
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
                        .background(CamilaOrange.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        tint = CamilaOrange,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Nenhum dependente vinculado ao seu perfil",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Procure a coordenação ou serviço social da Associação Camila para vincular seus filhos ou familiares.",
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
private fun StudentAttendanceHeader(
    rate: Float,
    presents: Int,
    absences: Int,
    targetName: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = CamilaGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (targetName != null) "Assiduidade de $targetName" else "Assiduidade Geral",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark
                        )
                        Text(
                            text = "${presents + absences} encontro(s) avaliado(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray
                        )
                    }
                }

                Surface(
                    color = if (rate >= 75f) CamilaGreen.copy(alpha = 0.12f) else CamilaOrange.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${rate.toInt()}%",
                        color = if (rate >= 75f) CamilaGreenDark else CamilaOrange,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Barra de Progresso
            LinearProgressIndicator(
                progress = { (rate / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (rate >= 75f) CamilaGreen else CamilaOrange,
                trackColor = BackgroundLight,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Indicadores Presença vs Faltas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBadge(
                    label = "Presenças",
                    count = presents,
                    color = CamilaGreen,
                    modifier = Modifier.weight(1f)
                )

                StatBadge(
                    label = "Faltas",
                    count = absences,
                    color = CamilaOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = CamilaGray
            )
        }
    }
}

@Composable
private fun AttendanceHistoryCard(
    item: StudentAttendanceRecordItem,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dateString = remember(item.dateMillis) {
        if (item.dateMillis > 0) dateFormat.format(Date(item.dateMillis)) else "Data não informada"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.isPresent) CamilaGreen.copy(alpha = 0.12f)
                            else CamilaOrange.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isPresent) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (item.isPresent) CamilaGreen else CamilaOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (item.isPresent) "Presença Confirmada" else "Falta Registrada",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = CamilaGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray
                        )
                    }
                }
            }

            Surface(
                color = if (item.isPresent) CamilaGreen.copy(alpha = 0.12f) else CamilaOrange.copy(alpha = 0.12f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (item.isPresent) "Presente" else "Ausente",
                    color = if (item.isPresent) CamilaGreenDark else CamilaOrange,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyStudentAttendanceState(
    isDependent: Boolean = false,
    dependentName: String? = null,
    modifier: Modifier = Modifier
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
                    .background(CamilaOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isDependent && !dependentName.isNullOrBlank()) {
                    "Nenhuma chamada registrada para $dependentName"
                } else {
                    "Nenhum registro de presença encontrado"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Os registros de presença serão exibidos aqui conforme os docentes realizarem as chamadas digitais nas oficinas.",
                style = MaterialTheme.typography.bodyMedium,
                color = CamilaGray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentAttendanceScreenPreview() {
    AssociacaoCamilaTheme {
        StudentAttendanceContent(
            uiState = StudentAttendanceUiState(
                isLoading = false,
                attendancePercentage = 80f,
                totalPresences = 4,
                totalAbsences = 1,
                history = listOf(
                    StudentAttendanceRecordItem(id = "1", workshopId = "w1", dateMillis = 1716500000000L, isPresent = true),
                    StudentAttendanceRecordItem(id = "2", workshopId = "w1", dateMillis = 1715800000000L, isPresent = false)
                )
            )
        )
    }
}
