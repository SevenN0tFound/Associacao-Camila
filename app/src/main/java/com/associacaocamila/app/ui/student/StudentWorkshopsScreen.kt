package com.associacaocamila.app.ui.student

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FamilyRestroom
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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

@Composable
fun StudentWorkshopsScreen(
    currentUser: User,
    modifier: Modifier = Modifier,
    viewModel: StudentWorkshopsViewModel = viewModel(),
) {
    val isResponsible = currentUser.role == UserRole.RESPONSAVEL
    val dependents = currentUser.dependents

    // Se for Responsável e tiver dependentes, controla qual dependente está selecionado
    var selectedDependentId by rememberSaveable(currentUser.id, dependents.size) {
        mutableStateOf(dependents.firstOrNull()?.id.orEmpty())
    }

    // Define qual ID buscar: dependente selecionado ou o ID do próprio aluno
    val effectiveStudentId = remember(isResponsible, selectedDependentId, currentUser.id) {
        if (isResponsible) selectedDependentId else currentUser.id
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(effectiveStudentId) {
        if (effectiveStudentId.isNotBlank()) {
            viewModel.loadWorkshops(effectiveStudentId)
        }
    }

    StudentWorkshopsContent(
        uiState = uiState,
        currentUser = currentUser,
        selectedDependentId = selectedDependentId,
        onSelectDependent = { selectedDependentId = it },
        modifier = modifier,
    )
}

@Composable
fun StudentWorkshopsContent(
    uiState: StudentWorkshopsUiState,
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
            .padding(16.dp),
    ) {
        // Se for Responsável, exibe o seletor horizontal de dependentes no topo
        if (isResponsible) {
            if (dependents.isEmpty()) {
                NoDependentsWarningCard()
                return@Column
            } else {
                DependentsSelectorBar(
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
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = CamilaGreen,
                    strokeWidth = 3.dp,
                )
            }
        } else if (uiState.workshops.isEmpty()) {
            EmptyStudentWorkshopsState(
                isDependent = isResponsible,
                dependentName = dependents.firstOrNull { it.id == selectedDependentId }?.name
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    StudentWorkshopsHeader(
                        total = uiState.workshops.size,
                        targetName = if (isResponsible) {
                            dependents.firstOrNull { it.id == selectedDependentId }?.name
                        } else null
                    )
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
private fun DependentsSelectorBar(
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
                    text = "Selecione o Dependente:",
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
private fun NoDependentsWarningCard() {
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
private fun StudentWorkshopsHeader(
    total: Int,
    targetName: String? = null
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
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (targetName != null) "Oficinas de $targetName" else "Minhas Oficinas",
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
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = CamilaGreenDark,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Matriculado(a)",
                            color = CamilaGreenDark,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        )
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

            Spacer(modifier = Modifier.height(14.dp))

            // Informações Compactas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                InfoTag(
                    icon = Icons.Default.Groups,
                    text = "${workshop.enrolledStudentIds.size} participantes",
                    tint = CamilaGreenDark,
                )

                InfoTag(
                    icon = Icons.Default.Person,
                    text = if (workshop.teacherId.isNotBlank()) "Professor atribuído" else "A definir",
                    tint = CamilaOrange,
                )
            }

            // Seção Expansível
            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BackgroundLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Detalhes Pedagógicos",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark,
                )

                Spacer(modifier = Modifier.height(8.dp))

                DetailItem(
                    icon = Icons.Default.DateRange,
                    label = "Frequência:",
                    value = "Encontros semanais presenciais",
                )

                Spacer(modifier = Modifier.height(6.dp))

                DetailItem(
                    icon = Icons.Default.Schedule,
                    label = "Carga Horária:",
                    value = "Certificado de participação ao término do ciclo",
                )

                Spacer(modifier = Modifier.height(6.dp))

                DetailItem(
                    icon = Icons.Default.Place,
                    label = "Local:",
                    value = "Sede Social - Associação Camila",
                )
            }
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
private fun EmptyStudentWorkshopsState(
    isDependent: Boolean = false,
    dependentName: String? = null,
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

            Text(
                text = if (isDependent && !dependentName.isNullOrBlank()) {
                    "$dependentName ainda não está matriculado(a) em nenhuma oficina"
                } else {
                    "Você ainda não está matriculado em nenhuma oficina"
                },
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
