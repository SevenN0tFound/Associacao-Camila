package com.associacaocamila.app.ui.student

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.associacaocamila.app.data.model.Workshop
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange

@Composable
fun StudentWorkshopsScreen(
    studentId: String,
    modifier: Modifier = Modifier,
    viewModel: StudentWorkshopsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.loadWorkshops(studentId)
    }

    StudentWorkshopsContent(
        uiState = uiState,
        modifier = modifier,
    )
}

@Composable
fun StudentWorkshopsContent(
    uiState: StudentWorkshopsUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp),
    ) {
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
        } else if (uiState.workshops.isEmpty()) {
            EmptyStudentWorkshopsState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    StudentWorkshopsHeader(total = uiState.workshops.size)
                }

                items(
                    items = uiState.workshops,
                    key = { it.id },
                ) { workshop ->
                    StudentWorkshopCard(workshop = workshop)
                }
            }
        }
    }
}

@Composable
private fun StudentWorkshopsHeader(total: Int) {
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
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Minhas Oficinas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Text(
                        text = "$total projeto(s) em andamento",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Participe com frequência para fortalecer seu aprendizado e convivência comunitária.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.95f),
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun StudentWorkshopCard(
    workshop: Workshop,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false,
) {
    var expanded by rememberSaveable { mutableStateOf(initialExpanded) }

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
            // Topo do Card: Badge "Matriculado" e Chevron interativo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    color = CamilaGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = CamilaGreenDark,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Matriculado",
                            color = CamilaGreenDark,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Recolher detalhes" else "Expandir detalhes",
                        tint = CamilaGray,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nome da Oficina em destaque
            Text(
                text = workshop.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Nome do professor/orientador com ícone
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = getWorkshopTeacherName(workshop),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = CamilaGray,
                )
            }

            // Conteúdo Expandido
            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(
                    color = CamilaGray.copy(alpha = 0.2f),
                    thickness = 1.dp,
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Descrição detalhada da oficina
                if (workshop.description.isNotBlank()) {
                    Text(
                        text = "Sobre a Oficina",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = workshop.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Card com Dias/Horários e Local na ONG Camila
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Linha Horário
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(CamilaOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = CamilaOrange,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Dias e Horários",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CamilaGray,
                                )
                                Text(
                                    text = getWorkshopSchedule(workshop.name),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        // Linha Local
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(CamilaGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = CamilaGreenDark,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Local das Aulas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CamilaGray,
                                )
                                Text(
                                    text = getWorkshopLocation(workshop.name),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Seção "Professor Responsável"
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CamilaGreen.copy(alpha = 0.08f),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CamilaGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = CamilaGreenDark,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Professor Responsável",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CamilaGreenDark,
                            )
                            Text(
                                text = getWorkshopTeacherDetail(workshop),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "contato@associacaocamila.org",
                                style = MaterialTheme.typography.labelSmall,
                                color = CamilaGray,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Informação sobre os colegas matriculados
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = CamilaOrange,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${workshop.enrolledStudentIds.size} estudantes matriculados nesta turma",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

private fun getWorkshopSchedule(workshopName: String): String {
    val name = workshopName.lowercase()
    return when {
        "fortalecimento" in name -> "Terças e Quintas • 14h às 16h"
        ("mídia" in name) || ("midia" in name) -> "Segundas e Quartas • 09h às 11h"
        ("interconexão" in name) || ("interconexao" in name) -> "Sextas-feiras • 13h30 às 16h30"
        else -> "Segundas e Quartas • 14h às 16h"
    }
}

private fun getWorkshopLocation(workshopName: String): String {
    val name = workshopName.lowercase()
    return when {
        ("mídia" in name) || ("midia" in name) -> "Laboratório de Comunicação • Sede Associação Camila"
        "fortalecimento" in name -> "Sala Multiuso 1 • Sede Associação Camila"
        ("interconexão" in name) || ("interconexao" in name) -> "Espaço Maker & Inovação • Sede Associação Camila"
        else -> "Sala de Oficinas Pedagógicas • Sede Associação Camila"
    }
}

private fun getWorkshopTeacherName(workshop: Workshop): String {
    val name = workshop.name.lowercase()
    return when {
        "fortalecimento" in name -> "Profª. Ana Paula (Orientadora Social)"
        ("mídia" in name) || ("midia" in name) -> "Prof. Carlos Eduardo (Comunicação)"
        ("interconexão" in name) || ("interconexao" in name) -> "Prof. Rafael Santos (Tecnologia)"
        workshop.teacherId.isNotBlank() -> "Professor(a) Vinculado(a)"
        else -> "Educador Social da Associação Camila"
    }
}

private fun getWorkshopTeacherDetail(workshop: Workshop): String {
    val name = workshop.name.lowercase()
    return when {
        "fortalecimento" in name -> "Profª. Ana Paula — Pedagogia e Assistência Social"
        ("mídia" in name) || ("midia" in name) -> "Prof. Carlos Eduardo — Comunicação e Audiovisual"
        ("interconexão" in name) || ("interconexao" in name) -> "Prof. Rafael Santos — Robótica e Inclusão Digital"
        else -> "Equipe Pedagógica — Associação Camila"
    }
}

@Composable
private fun EmptyStudentWorkshopsState(modifier: Modifier = Modifier) {
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
                    .background(CamilaOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Você ainda não está matriculado em nenhuma oficina",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Consulte a secretaria ou assistência social da Associação Camila para inscrição nos projetos disponíveis.",
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
                    ),
                    Workshop(
                        id = "2",
                        name = "Projeto Mídias Force",
                        description = "Capacitação prática em comunicação comunitária, edição audiovisual e mídias sociais.",
                        teacherId = "t2",
                        enrolledStudentIds = listOf("s1", "s2"),
                    ),
                ),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentWorkshopCardExpandedPreview() {
    AssociacaoCamilaTheme {
        Box(
            modifier = Modifier
                .background(BackgroundLight)
                .padding(16.dp),
        ) {
            StudentWorkshopCard(
                workshop = Workshop(
                    id = "1",
                    name = "Oficina de Fortalecimento",
                    description = "Encontros socioeducativos, desenvolvimento cidadão e apoio às famílias.",
                    teacherId = "t1",
                    enrolledStudentIds = listOf("s1", "s2", "s3", "s4", "s5"),
                ),
                initialExpanded = true,
            )
        }
    }
}
