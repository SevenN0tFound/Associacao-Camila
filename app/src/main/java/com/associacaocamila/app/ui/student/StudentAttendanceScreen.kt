package com.associacaocamila.app.ui.student

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.associacaocamila.app.data.model.AbsenceJustification
import com.associacaocamila.app.data.model.AttendanceStatus
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

    var selectedDependentId by rememberSaveable(currentUser.id, dependents.size) {
        mutableStateOf(dependents.firstOrNull()?.id.orEmpty())
    }

    val effectiveStudentId = remember(isResponsible, selectedDependentId, currentUser.id) {
        if (isResponsible) selectedDependentId else currentUser.id
    }

    val effectiveStudentName = remember(isResponsible, selectedDependentId, currentUser.id, currentUser.name, dependents) {
        if (isResponsible) {
            dependents.firstOrNull { it.id == selectedDependentId }?.name ?: "Dependente"
        } else {
            currentUser.name.ifBlank { "Aluno" }
        }
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
        effectiveStudentId = effectiveStudentId,
        effectiveStudentName = effectiveStudentName,
        onSelectDependent = { selectedDependentId = it },
        onSubmitJustification = { justification, fileUri, mimeType, onSuccess, onError ->
            viewModel.submitJustification(justification, fileUri, mimeType, onSuccess, onError)
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceContent(
    uiState: StudentAttendanceUiState,
    currentUser: User? = null,
    selectedDependentId: String = "",
    effectiveStudentId: String = "",
    effectiveStudentName: String = "",
    onSelectDependent: (String) -> Unit = {},
    onSubmitJustification: (
        justification: AbsenceJustification,
        fileUri: Uri?,
        mimeType: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isResponsible = currentUser?.role == UserRole.RESPONSAVEL
    val dependents = currentUser?.dependents.orEmpty()

    var justifyingItem by remember { mutableStateOf<StudentAttendanceRecordItem?>(null) }

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
                        justified = uiState.totalJustified,
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
                    AttendanceHistoryCard(
                        item = item,
                        onJustifyClick = { justifyingItem = item }
                    )
                }
            }
        }
    }

    // Modal BottomSheet de Justificativa de Falta
    justifyingItem?.let { item ->
        JustifyAbsenceBottomSheet(
            item = item,
            studentName = effectiveStudentName,
            isSubmitting = uiState.isSubmittingJustification,
            onDismiss = { justifyingItem = null },
            onSubmit = { reason, fileUri, mimeType ->
                val justification = AbsenceJustification(
                    attendanceRecordId = item.id,
                    workshopId = item.workshopId,
                    workshopName = item.workshopName,
                    studentId = effectiveStudentId,
                    studentName = effectiveStudentName,
                    guardianId = currentUser?.id.orEmpty(),
                    dateMillis = item.dateMillis,
                    reason = reason,
                    attachmentUrl = null,
                    status = "PENDENTE",
                    requestedAt = System.currentTimeMillis()
                )

                onSubmitJustification(
                    justification,
                    fileUri,
                    mimeType,
                    {
                        Toast.makeText(context, "Justificativa enviada para análise!", Toast.LENGTH_LONG).show()
                        justifyingItem = null
                    },
                    { errorMsg ->
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
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
            Text(
                text = "Selecione o Dependente",
                style = MaterialTheme.typography.labelSmall,
                color = CamilaGray,
                modifier = Modifier.padding(bottom = 6.dp)
            )

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
    justified: Int,
    absences: Int,
    targetName: String? = null,
    modifier: Modifier = Modifier
) {
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CamilaGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = CamilaGreen,
                            modifier = Modifier.size(22.dp)
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
                            text = "${presents + justified + absences} encontro(s) avaliado(s)",
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

            // Indicadores Presença, Justificadas e Faltas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge(
                    label = "Presenças",
                    count = presents,
                    color = CamilaGreen,
                    modifier = Modifier.weight(1f)
                )

                if (justified > 0) {
                    StatBadge(
                        label = "Justificadas",
                        count = justified,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f)
                    )
                }

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
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
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
                color = CamilaGray,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AttendanceHistoryCard(
    item: StudentAttendanceRecordItem,
    onJustifyClick: () -> Unit,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                when (item.status) {
                                    AttendanceStatus.PRESENTE -> CamilaGreen.copy(alpha = 0.12f)
                                    AttendanceStatus.JUSTIFICADO -> Color(0xFF2196F3).copy(alpha = 0.12f)
                                    AttendanceStatus.FALTA -> CamilaOrange.copy(alpha = 0.12f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (item.status) {
                                AttendanceStatus.PRESENTE -> Icons.Default.Check
                                AttendanceStatus.JUSTIFICADO -> Icons.Default.HealthAndSafety
                                AttendanceStatus.FALTA -> Icons.Default.Close
                            },
                            contentDescription = null,
                            tint = when (item.status) {
                                AttendanceStatus.PRESENTE -> CamilaGreen
                                AttendanceStatus.JUSTIFICADO -> Color(0xFF1976D2)
                                AttendanceStatus.FALTA -> CamilaOrange
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = when (item.status) {
                                AttendanceStatus.PRESENTE -> "Presença Confirmada"
                                AttendanceStatus.JUSTIFICADO -> "Falta Justificada"
                                AttendanceStatus.FALTA -> "Falta Registrada"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.workshopName.ifBlank { "Oficina" },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = CamilaGreenDark
                            )
                            Text(
                                text = " • $dateString",
                                style = MaterialTheme.typography.bodySmall,
                                color = CamilaGray
                            )
                        }
                    }
                }

                // Badge de status
                Surface(
                    color = when (item.status) {
                        AttendanceStatus.PRESENTE -> CamilaGreen.copy(alpha = 0.12f)
                        AttendanceStatus.JUSTIFICADO -> Color(0xFFE3F2FD)
                        AttendanceStatus.FALTA -> CamilaOrange.copy(alpha = 0.12f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = when (item.status) {
                            AttendanceStatus.PRESENTE -> "Presente"
                            AttendanceStatus.JUSTIFICADO -> "Atestado"
                            AttendanceStatus.FALTA -> "Ausente"
                        },
                        color = when (item.status) {
                            AttendanceStatus.PRESENTE -> CamilaGreenDark
                            AttendanceStatus.JUSTIFICADO -> Color(0xFF1976D2)
                            AttendanceStatus.FALTA -> CamilaOrange
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Exibição e Ação de Justificativa para Faltas
            if (item.status == AttendanceStatus.FALTA) {
                Spacer(modifier = Modifier.height(10.dp))

                val justification = item.justification
                when {
                    justification == null -> {
                        // Botão para justificar falta
                        OutlinedButton(
                            onClick = onJustifyClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CamilaOrange),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CamilaOrange),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Justificar Falta com Atestado",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    justification.status == "PENDENTE" -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF8E1),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = Color(0xFFF57F17),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Justificativa enviada - Em análise pela coordenação",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFF57F17),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    justification.status == "RECUSADO" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Justificativa não aprovada",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                            TextButton(onClick = onJustifyClick) {
                                Text(
                                    text = "Reenviar",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CamilaOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JustifyAbsenceBottomSheet(
    item: StudentAttendanceRecordItem,
    studentName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, fileUri: Uri?, mimeType: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var reason by rememberSaveable { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedMimeType by remember { mutableStateOf<String?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = uri.lastPathSegment?.substringAfterLast('/') ?: "atestado_comprovante.jpg"
            selectedMimeType = "image/*"
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dateString = remember(item.dateMillis) {
        if (item.dateMillis > 0) dateFormat.format(Date(item.dateMillis)) else "Data da aula"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CamilaOrange.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = CamilaOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Justificar Falta",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                    Text(
                        text = "$studentName • ${item.workshopName} ($dateString)",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Campo de motivo
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Motivo da Ausência") },
                placeholder = { Text("Ex: Consulta médica, repouso por indisposição, compromisso...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CamilaGreen,
                    unfocusedBorderColor = CamilaGray.copy(alpha = 0.4f),
                    focusedLabelColor = CamilaGreen
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Anexo de Atestado / Comprovante
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BackgroundLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Atestado ou Comprovante (Opcional)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = CamilaGreenDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Anexe uma foto ou documento do atestado médico para agilizar a aprovação.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedFileUri == null) {
                        OutlinedButton(
                            onClick = { filePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CamilaGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CamilaGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Selecionar Foto do Atestado")
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CamilaGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedFileName ?: "Arquivo selecionado",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 1
                                )
                            }
                            IconButton(
                                onClick = {
                                    selectedFileUri = null
                                    selectedFileName = null
                                    selectedMimeType = null
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remover arquivo",
                                    tint = CamilaGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Ações: Cancelar e Enviar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = { onSubmit(reason.trim(), selectedFileUri, selectedMimeType) },
                    enabled = reason.isNotBlank() && !isSubmitting,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CamilaGreen,
                        disabledContainerColor = CamilaGreen.copy(alpha = 0.5f)
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Enviar Justificativa",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
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
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isDependent && !dependentName.isNullOrBlank()) {
                    "Sem frequência registrada para $dependentName"
                } else {
                    "Nenhum registro de frequência"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Assim que as chamadas das oficinas forem realizadas pelos professores, você poderá acompanhar sua assiduidade e justificar faltas aqui.",
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
private fun StudentAttendancePreview() {
    AssociacaoCamilaTheme {
        StudentAttendanceContent(
            uiState = StudentAttendanceUiState(
                isLoading = false,
                history = listOf(
                    StudentAttendanceRecordItem(
                        id = "1",
                        workshopId = "w1",
                        workshopName = "Capoeira",
                        dateMillis = System.currentTimeMillis(),
                        isPresent = true,
                        status = AttendanceStatus.PRESENTE
                    ),
                    StudentAttendanceRecordItem(
                        id = "2",
                        workshopId = "w1",
                        workshopName = "Capoeira",
                        dateMillis = System.currentTimeMillis() - 86400000L,
                        isPresent = false,
                        status = AttendanceStatus.FALTA
                    )
                ),
                totalLessons = 2,
                totalPresences = 1,
                totalAbsences = 1,
                attendancePercentage = 50f
            ),
            currentUser = User(name = "Aluno Modelo", role = UserRole.ALUNO)
        )
    }
}
