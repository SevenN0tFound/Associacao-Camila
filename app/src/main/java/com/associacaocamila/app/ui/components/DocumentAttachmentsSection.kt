package com.associacaocamila.app.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.associacaocamila.app.data.model.DocumentAttachment
import com.associacaocamila.app.data.repository.DocumentRepository
import com.associacaocamila.app.data.repository.DocumentRepositoryImpl
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Componente modular para exibição, visualização e upload de documentos do Prontuário Digital.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentAttachmentsSection(
    ownerId: String,
    documents: List<DocumentAttachment>,
    onDocumentsChanged: (List<DocumentAttachment>) -> Unit,
    modifier: Modifier = Modifier,
    documentRepository: DocumentRepository? = null,
    canEdit: Boolean = true,
) {
    val isInspectionMode = androidx.compose.ui.platform.LocalInspectionMode.current
    val actualRepository = remember(documentRepository, isInspectionMode) {
        documentRepository ?: if (isInspectionMode) {
            object : DocumentRepository {
                override suspend fun uploadDocument(ownerId: String, categoryName: String, fileUri: Uri, mimeType: String) =
                    Result.success(DocumentAttachment(name = categoryName, fileType = mimeType))
                override suspend fun deleteDocument(ownerId: String, documentId: String, fileUrl: String) =
                    Result.success(Unit)
            }
        } else {
            DocumentRepositoryImpl()
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileMimeType by remember { mutableStateOf("application/pdf") }
    var showCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var isUploading by rememberSaveable { mutableStateOf(false) }
    var uploadErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    var documentToDelete by remember { mutableStateOf<DocumentAttachment?>(null) }
    var isDeleting by rememberSaveable { mutableStateOf(false) }

    // Launcher do seletor de arquivos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri) ?: "*/*"
            selectedFileUri = uri
            selectedFileMimeType = mimeType
            showCategoryDialog = true
        }
    }

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
            // Cabeçalho da Seção
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
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = CamilaGreenDark,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Documentação & Anexos",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark,
                        )
                        Text(
                            text = "Prontuário digital de arquivos e comprovantes",
                            style = MaterialTheme.typography.labelSmall,
                            color = CamilaGray,
                        )
                    }
                }

                if (canEdit) {
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = CamilaOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 10.dp,
                            vertical = 6.dp,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Anexar Documento",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Anexar",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }

            // Indicador de upload em andamento
            if (isUploading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = CamilaGreen,
                        trackColor = CamilaGreen.copy(alpha = 0.2f),
                    )
                    Text(
                        text = "Enviando arquivo para o armazenamento...",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Lista de documentos existentes
            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundLight)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Nenhum documento anexado ainda.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray,
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    documents.forEach { doc ->
                        DocumentItemCard(
                            document = doc,
                            canDelete = canEdit,
                            onOpen = {
                                if (doc.fileUrl.isNotBlank()) {
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(doc.fileUrl)).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            "Não foi possível abrir o link do arquivo.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                }
                            },
                            onDelete = { documentToDelete = doc },
                        )
                    }
                }
            }
        }
    }

    // Diálogo para escolha da categoria do documento
    if (showCategoryDialog && selectedFileUri != null) {
        var selectedCategory by rememberSaveable { mutableStateOf("RG / CPF") }
        var customCategoryName by rememberSaveable { mutableStateOf("") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        val predefinedCategories = listOf(
            "RG / CPF",
            "Comprovante de Residência",
            "Laudo Médico",
            "Certidão de Nascimento",
            "Declaração Escolar",
            "Outro",
        )

        AlertDialog(
            onDismissRequest = {
                if (!isUploading) {
                    showCategoryDialog = false
                    selectedFileUri = null
                }
            },
            title = {
                Text(
                    text = "Identificar Documento",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Selecione o tipo de documento selecionado para arquivamento no prontuário:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CamilaGray,
                    )

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                        ) {
                            predefinedCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        dropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    if (selectedCategory == "Outro") {
                        OutlinedTextField(
                            value = customCategoryName,
                            onValueChange = { customCategoryName = it },
                            label = { Text("Nome da Categoria / Documento") },
                            placeholder = { Text("Ex: Autorização de Imagem") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    uploadErrorMessage?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalCategory = if (selectedCategory == "Outro") {
                            customCategoryName.trim().ifBlank { "Outro Documento" }
                        } else {
                            selectedCategory
                        }
                        val uri = selectedFileUri ?: return@Button

                        isUploading = true
                        uploadErrorMessage = null

                        coroutineScope.launch {
                            val result = actualRepository.uploadDocument(
                                ownerId = ownerId,
                                categoryName = finalCategory,
                                fileUri = uri,
                                mimeType = selectedFileMimeType,
                            )
                            result.onSuccess { newAttachment ->
                                isUploading = false
                                showCategoryDialog = false
                                selectedFileUri = null
                                onDocumentsChanged(documents + newAttachment)
                                Toast.makeText(
                                    context,
                                    "Documento anexado com sucesso!",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }.onFailure { ex ->
                                isUploading = false
                                uploadErrorMessage = ex.localizedMessage ?: "Erro ao fazer upload do arquivo."
                            }
                        }
                    },
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen),
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Salvar Anexo", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCategoryDialog = false
                        selectedFileUri = null
                    },
                    enabled = !isUploading,
                ) {
                    Text("Cancelar", color = CamilaGray)
                }
            },
        )
    }

    // Diálogo de confirmação para exclusão de documento
    documentToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) documentToDelete = null
            },
            title = {
                Text(
                    text = "Remover Documento",
                    fontWeight = FontWeight.Bold,
                    color = CamilaGreenDark,
                )
            },
            text = {
                Text(
                    text = "Deseja realmente apagar o anexo \"${doc.name}\" do prontuário? O arquivo será excluído permanentemente.",
                    color = CamilaGray,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        coroutineScope.launch {
                            val result = actualRepository.deleteDocument(
                                ownerId = ownerId,
                                documentId = doc.id,
                                fileUrl = doc.fileUrl,
                            )
                            isDeleting = false
                            documentToDelete = null
                            result.onSuccess {
                                onDocumentsChanged(documents.filterNot { it.id == doc.id })
                                Toast.makeText(
                                    context,
                                    "Documento excluído com sucesso.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }.onFailure { ex ->
                                Toast.makeText(
                                    context,
                                    ex.localizedMessage ?: "Erro ao excluir documento.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = CamilaOrange),
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Excluir", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { documentToDelete = null },
                    enabled = !isDeleting,
                ) {
                    Text("Cancelar", color = CamilaGray)
                }
            },
        )
    }
}

@Composable
private fun DocumentItemCard(
    document: DocumentAttachment,
    canDelete: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    val isPdf = document.fileType.contains("pdf", ignoreCase = true)
    val isImage = document.fileType.contains("image", ignoreCase = true)

    val fileIcon = when {
        isPdf -> Icons.Default.PictureAsPdf
        isImage -> Icons.Default.Image
        else -> Icons.Default.AttachFile
    }

    val iconColor = when {
        isPdf -> Color(0xFFD32F2F)
        isImage -> CamilaGreen
        else -> CamilaOrange
    }

    val formattedDate = remember(document.uploadedAt) {
        if (document.uploadedAt > 0) {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))
                .format(java.util.Date(document.uploadedAt))
        } else {
            ""
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onOpen() },
        color = BackgroundLight,
        shape = RoundedCornerShape(10.dp),
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
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = fileIcon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.name.ifBlank { "Documento sem nome" },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (formattedDate.isNotBlank()) {
                        Text(
                            text = "Enviado em $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray,
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpen,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Visualizar Arquivo",
                        tint = CamilaGreenDark,
                        modifier = Modifier.size(18.dp),
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir Arquivo",
                            tint = CamilaOrange,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
