package com.associacaocamila.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.associacaocamila.app.data.model.Dependent
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange

/**
 * Seção e diálogo para gestão de dependentes por busca e seleção de participantes/alunos.
 */
@Composable
fun DependentsSection(
    dependents: List<Dependent>,
    onLinkStudentClick: () -> Unit = {},
    onUnlinkDependentClick: (Dependent) -> Unit = {},
    modifier: Modifier = Modifier,
    currentUser: User? = null,
    canEdit: Boolean = (currentUser?.role?.uppercase() == "ADMIN"),
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
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
                            text = "Dependentes / Participantes",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark,
                        )
                        Text(
                            text = "${dependents.size} participante(s) vinculado(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = CamilaGray,
                        )
                    }
                }

                // 1. Verificação de Papel (Role-Based Access):
                // Apenas ADMIN pode visualizar e acionar a ação de vincular dependente
                if (currentUser?.role?.uppercase() == "ADMIN" && canEdit) {
                    Button(
                        onClick = onLinkStudentClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 10.dp,
                            vertical = 6.dp,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Vincular Dependente",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Vincular Dependente",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }

            // 2. Visão do Responsável (RESPONSAVEL):
            // Visualização em modo somente leitura e mensagem informativa amigável se vazio
            if (dependents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundLight)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (currentUser?.role?.uppercase() == "ADMIN") {
                            "Nenhum dependente vinculado a este responsável."
                        } else {
                            "Nenhum dependente vinculado. Para cadastrar um aluno ou filho, solicite à coordenação da Associação Camila."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dependents.forEach { dependent ->
                        LinkedDependentCard(
                            dependent = dependent,
                            canUnlink = currentUser?.role?.uppercase() == "ADMIN",
                            onUnlink = { onUnlinkDependentClick(dependent) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LinkedDependentCard(
    dependent: Dependent,
    canUnlink: Boolean,
    onUnlink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = BackgroundLight,
        border = BorderStroke(1.dp, CamilaGray.copy(alpha = 0.15f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dependent.name.ifBlank { "Sem nome" },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = dependent.relationship.ifBlank { "Dependente" },
                            style = MaterialTheme.typography.labelSmall,
                            color = CamilaGreenDark,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (dependent.birthDate.isNotBlank()) {
                            Text(
                                text = "•  Nascimento: ${dependent.birthDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CamilaGray,
                            )
                        }
                    }
                }
            }

            if (canUnlink) {
                IconButton(
                    onClick = onUnlink,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Desvincular Participante",
                        tint = CamilaOrange,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * ModalBottomSheet para buscar e vincular alunos disponíveis ao responsável.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectStudentBottomSheet(
    availableStudents: List<User>,
    alreadyLinkedIds: Set<String>,
    isLoading: Boolean,
    isLinking: Boolean,
    onDismiss: () -> Unit,
    onStudentSelected: (User) -> Unit,
    onRefreshStudents: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        onRefreshStudents()
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredStudents = remember(availableStudents, alreadyLinkedIds, searchQuery) {
        val nonLinked = availableStudents.filterNot { it.id in alreadyLinkedIds }
        if (searchQuery.isBlank()) {
            nonLinked
        } else {
            val query = searchQuery.trim().lowercase()
            nonLinked.filter { student ->
                student.name.lowercase().contains(query) ||
                    student.email.lowercase().contains(query) ||
                    student.phone.contains(query)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isLinking) onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Selecionar Dependente",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark,
                    )
                    Text(
                        text = "Vincule participantes cadastrados ao responsável",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray,
                    )
                }
                IconButton(onClick = onDismiss, enabled = !isLinking) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Campo de busca com ícone
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar Aluno / Participante") },
                placeholder = { Text("Pesquisar por nome ou CPF...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = CamilaGreen,
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CamilaGreen,
                    unfocusedBorderColor = CamilaGray.copy(alpha = 0.4f),
                    focusedLabelColor = CamilaGreen,
                    cursorColor = CamilaGreen,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = CamilaGreen, strokeWidth = 2.dp)
                }
            } else if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackgroundLight)
                        .padding(28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CamilaGray,
                            modifier = Modifier.size(32.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "Nenhum participante encontrado para \"$searchQuery\"."
                            } else {
                                "Nenhum aluno disponível para vínculo no momento."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = CamilaGray,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundLight,
                            border = BorderStroke(1.dp, CamilaGray.copy(alpha = 0.15f)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.name.ifBlank { "Aluno sem nome" },
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = if (student.birthDate.isNotBlank()) {
                                                "Nasc: ${student.birthDate}"
                                            } else {
                                                student.email.ifBlank { "Sem e-mail" }
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CamilaGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onStudentSelected(student) },
                                    enabled = !isLinking,
                                    colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 12.dp,
                                        vertical = 6.dp,
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Vincular",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
