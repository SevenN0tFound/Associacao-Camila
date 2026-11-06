package com.associacaocamila.app.ui.student

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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    studentId: String,
    modifier: Modifier = Modifier,
    viewModel: StudentAttendanceViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.loadAttendance(studentId)
    }

    StudentAttendanceContent(
        uiState = uiState,
        modifier = modifier,
    )
}

@Composable
fun StudentAttendanceContent(
    uiState: StudentAttendanceUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
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
            EmptyStudentAttendanceState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card de Resumo de Frequência Geral
                item {
                    AttendanceSummaryCard(uiState = uiState)
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = CamilaGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Histórico de Chamadas",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark
                        )
                    }
                }

                items(
                    items = uiState.history,
                    key = { it.id }
                ) { recordItem ->
                    AttendanceHistoryCard(item = recordItem)
                }
            }
        }
    }
}

@Composable
private fun AttendanceSummaryCard(uiState: StudentAttendanceUiState) {
    val statusColor = if (uiState.isRegular) CamilaGreen else CamilaOrange
    val progressValue = (uiState.attendancePercentage / 100f).coerceIn(0f, 1f)

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
                Column {
                    Text(
                        text = "Frequência Geral",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Acompanhamento acadêmico do aluno",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isRegular) Icons.Default.Check else Icons.Default.Warning,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isRegular) "Frequência Regular" else "Atenção: Baixa",
                            color = statusColor,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Porcentagem e Barra de Progresso
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${"%.1f".format(Locale.getDefault(), uiState.attendancePercentage)}%",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
                Text(
                    text = "Mínimo recomendado: 75%",
                    style = MaterialTheme.typography.bodySmall,
                    color = CamilaGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Métricas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricItem(label = "Total Aulas", value = uiState.totalLessons.toString(), color = CamilaGray)
                MetricItem(label = "Presenças", value = uiState.totalPresences.toString(), color = CamilaGreen)
                MetricItem(label = "Faltas", value = uiState.totalAbsences.toString(), color = CamilaOrange)
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = CamilaGray
        )
    }
}

@Composable
private fun AttendanceHistoryCard(
    item: StudentAttendanceRecordItem,
    modifier: Modifier = Modifier,
) {
    val dateFormatted = remember(item.dateMillis) {
        val sdf = SimpleDateFormat("EEEE, dd 'de' MMMM", Locale.forLanguageTag("pt-BR"))
        sdf.format(Date(item.dateMillis)).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("pt-BR")) else it.toString() }
    }

    val statusColor = if (item.isPresent) CamilaGreen else CamilaOrange
    val statusText = if (item.isPresent) "Presente" else "Falta"
    val statusIcon = if (item.isPresent) Icons.Default.Check else Icons.Default.Close

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Chamada Registrada",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }
            }

            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyStudentAttendanceState(modifier: Modifier = Modifier) {
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
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = CamilaGreen,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nenhum registro de chamada",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ainda não foram registradas chamadas ou presenças para este aluno nas oficinas da Associação Camila.",
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
                history = listOf(
                    StudentAttendanceRecordItem(
                        id = "1",
                        workshopId = "w1",
                        dateMillis = System.currentTimeMillis() - (86400000L * 2),
                        isPresent = true
                    ),
                    StudentAttendanceRecordItem(
                        id = "2",
                        workshopId = "w1",
                        dateMillis = System.currentTimeMillis() - 86400000L * 4,
                        isPresent = false
                    ),
                    StudentAttendanceRecordItem(
                        id = "3",
                        workshopId = "w1",
                        dateMillis = System.currentTimeMillis() - 86400000L * 6,
                        isPresent = true
                    ),
                    StudentAttendanceRecordItem(
                        id = "4",
                        workshopId = "w1",
                        dateMillis = System.currentTimeMillis() - 86400000L * 8,
                        isPresent = true
                    )
                ),
                totalLessons = 4,
                totalPresences = 3,
                totalAbsences = 1,
                attendancePercentage = 75f,
                isRegular = true
            )
        )
    }
}
