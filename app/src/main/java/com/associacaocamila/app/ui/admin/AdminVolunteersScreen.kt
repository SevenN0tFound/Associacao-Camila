package com.associacaocamila.app.ui.admin

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.associacaocamila.app.data.model.Volunteer
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVolunteersScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminVolunteersViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var selectedVolunteer by remember { mutableStateOf<Volunteer?>(null) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Cabeçalho Institucional
            VolunteersHeader(totalCount = uiState.volunteers.size)

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de Busca
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Buscar por nome, telefone, cidade ou área...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = CamilaGreen
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar busca",
                                tint = CamilaGray
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CamilaGreen,
                    unfocusedBorderColor = CamilaGray.copy(alpha = 0.35f),
                    cursorColor = CamilaGreen
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filtros de Status
            StatusFiltersRow(
                selectedFilter = uiState.selectedFilter,
                onSelectFilter = viewModel::setFilter
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de Voluntários ou Estado Vazio
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CamilaGreen)
                    }
                }

                uiState.filteredVolunteers.isEmpty() -> {
                    EmptyVolunteersState(modifier = Modifier.weight(1f))
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.filteredVolunteers, key = { it.id }) { volunteer ->
                            VolunteerCard(
                                volunteer = volunteer,
                                onClick = { selectedVolunteer = volunteer }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal com Detalhes e Ações de Contato
    selectedVolunteer?.let { volunteer ->
        VolunteerDetailsBottomSheet(
            volunteer = volunteer,
            isUpdating = uiState.isUpdatingStatus,
            onDismiss = { selectedVolunteer = null },
            onUpdateStatus = { newStatus ->
                viewModel.updateStatus(volunteer.id, newStatus)
                selectedVolunteer = volunteer.copy(status = newStatus)
            },
            onCall = {
                val digits = volunteer.phone.filter { it.isDigit() }
                if (digits.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits"))
                    launchIntentSafely(context, intent, "Não foi possível abrir o discador telefônico.")
                } else {
                    Toast.makeText(context, "Telefone inválido para chamada.", Toast.LENGTH_SHORT).show()
                }
            },
            onWhatsApp = {
                val digits = volunteer.phone.filter { it.isDigit() }
                val phoneWithCountry = if (digits.startsWith("55")) digits else "55$digits"
                val message = "Olá ${volunteer.fullName}! Somos da Associação Camila e agradecemos sua inscrição como voluntário(a)."
                val url = "https://api.whatsapp.com/send?phone=$phoneWithCountry&text=${Uri.encode(message)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                launchIntentSafely(context, intent, "WhatsApp não instalado no dispositivo.")
            }
        )
    }
}

@Composable
private fun VolunteersHeader(totalCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CamilaGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Gestão de Voluntários",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "$totalCount cadastro(s) recebido(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun StatusFiltersRow(
    selectedFilter: VolunteerStatusFilter,
    onSelectFilter: (VolunteerStatusFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VolunteerStatusFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            FilterChip(
                selected = isSelected,
                onClick = { onSelectFilter(filter) },
                label = {
                    Text(
                        text = filter.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CamilaGreen.copy(alpha = 0.15f),
                    selectedLabelColor = CamilaGreenDark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = CamilaGray.copy(alpha = 0.3f),
                    selectedBorderColor = CamilaGreen
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VolunteerCard(
    volunteer: Volunteer,
    onClick: () -> Unit,
) {
    val statusColor = when (volunteer.status.uppercase()) {
        "APROVADO" -> CamilaGreen
        "CONTATADO" -> Color(0xFF1976D2)
        else -> CamilaOrange
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedDate = remember(volunteer.submittedAt) {
        dateFormatter.format(Date(volunteer.submittedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Linha superior: Nome e Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = volunteer.fullName.ifBlank { "Voluntário sem nome" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = volunteer.status.uppercase(),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Contato rápido
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = CamilaGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = volunteer.phone.ifBlank { "Sem telefone" },
                    style = MaterialTheme.typography.bodySmall,
                    color = CamilaGray
                )

                if (volunteer.city.isNotBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = null,
                        tint = CamilaGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = volunteer.city,
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }
            }

            // Áreas de Apoio como Chips
            if (volunteer.supportAreas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    volunteer.supportAreas.take(3).forEach { area ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BackgroundLight
                        ) {
                            Text(
                                text = area,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (volunteer.supportAreas.size > 3) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BackgroundLight
                        ) {
                            Text(
                                text = "+${volunteer.supportAreas.size - 3}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CamilaGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BackgroundLight)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enviado em $formattedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = CamilaGray
                )
                Text(
                    text = "Ver detalhes →",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreen
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun VolunteerDetailsBottomSheet(
    volunteer: Volunteer,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabeçalho do modal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = volunteer.fullName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                    Text(
                        text = "Status: ${volunteer.status.uppercase()}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = when (volunteer.status.uppercase()) {
                            "APROVADO" -> CamilaGreen
                            "CONTATADO" -> Color(0xFF1976D2)
                            else -> CamilaOrange
                        }
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            HorizontalDivider(color = BackgroundLight)

            // Botões de Ação Imediata (Ligar e WhatsApp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCall,
                    border = BorderStroke(1.2.dp, CamilaGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CamilaGreenDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ligar", fontWeight = FontWeight.Bold)
                }
            }

            // Bloco de Dados de Contato e Documentos
            DetailSectionBox(title = "Dados Pessoais e Contato") {
                DetailItem(label = "Telefone / WhatsApp", value = volunteer.phone)
                DetailItem(label = "E-mail", value = volunteer.email)
                DetailItem(label = "Data de Nascimento", value = volunteer.birthDate)
                DetailItem(label = "Estado Civil", value = volunteer.maritalStatus)
                if (volunteer.rg.isNotBlank()) DetailItem(label = "RG", value = volunteer.rg)
                if (volunteer.cpf.isNotBlank()) DetailItem(label = "CPF", value = volunteer.cpf)
                if (volunteer.profession.isNotBlank()) DetailItem(label = "Profissão", value = volunteer.profession)
                if (volunteer.address.isNotBlank()) DetailItem(label = "Endereço", value = volunteer.address)
                if (volunteer.city.isNotBlank()) DetailItem(label = "Cidade", value = volunteer.city)
            }

            // Bloco de Saúde
            if (volunteer.healthLimitations.isNotBlank()) {
                DetailSectionBox(title = "Saúde e Limitações") {
                    Text(
                        text = volunteer.healthLimitations,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Áreas de Apoio
            if (volunteer.supportAreas.isNotEmpty()) {
                DetailSectionBox(title = "Áreas de Apoio Selecionadas") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        volunteer.supportAreas.forEach { area ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CamilaGreen.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, CamilaGreen.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = area,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = CamilaGreenDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Disponibilidade de Horário
            if (volunteer.availableTimes.isNotEmpty()) {
                DetailSectionBox(title = "Disponibilidade de Horário") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        volunteer.availableTimes.forEach { time ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CamilaOrange.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, CamilaOrange.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = CamilaOrange,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Informações Adicionais
            if (volunteer.additionalInfo.isNotBlank()) {
                DetailSectionBox(title = "Informações Adicionais / Motivação") {
                    Text(
                        text = volunteer.additionalInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(color = BackgroundLight)

            // Alteração de Status
            Text(
                text = "Alterar Status do Voluntário:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onUpdateStatus("PENDENTE") },
                    enabled = !isUpdating && volunteer.status != "PENDENTE",
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CamilaOrange
                    ),
                    border = BorderStroke(1.dp, CamilaOrange)
                ) {
                    Text("Pendente", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = { onUpdateStatus("CONTATADO") },
                    enabled = !isUpdating && volunteer.status != "CONTATADO",
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF1976D2)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF1976D2))
                ) {
                    Text("Contatado", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = { onUpdateStatus("APROVADO") },
                    enabled = !isUpdating && volunteer.status != "APROVADO",
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CamilaGreen
                    )
                ) {
                    Text("Aprovar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            if (isUpdating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = CamilaGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Atualizando status...", style = MaterialTheme.typography.bodySmall, color = CamilaGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailSectionBox(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BackgroundLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark
            )
            content()
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = CamilaGray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyVolunteersState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = null,
                tint = CamilaGray.copy(alpha = 0.5f),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Nenhum voluntário encontrado",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Aguardando novas inscrições através do portal institucional.",
                style = MaterialTheme.typography.bodySmall,
                color = CamilaGray
            )
        }
    }
}

private fun launchIntentSafely(
    context: Context,
    intent: Intent,
    failureMessage: String,
) {
    try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao abrir: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
