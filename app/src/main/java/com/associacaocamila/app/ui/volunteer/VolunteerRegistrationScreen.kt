package com.associacaocamila.app.ui.volunteer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val SUPPORT_AREAS = listOf(
    "Suporte na limpeza e manutenção predial",
    "Preparação de refeição, organização de alimentos",
    "Doar itens necessários",
    "Contribuir com suporte financeiro",
    "Participar como voluntário nos nossos eventos",
    "Elaborar alguma oficina com nossos atendidos",
    "Suporte no nosso Bazar ReUse",
    "Geral",
    "Digitador de NFP"
)

private val AVAILABLE_TIMES = listOf(
    "Diurno de segunda a sexta",
    "Noturno de segunda a sexta",
    "Fins de semana"
)

private val MARITAL_STATUS_OPTIONS = listOf(
    "Solteiro(a)",
    "Casado(a)",
    "Viúvo(a)",
    "Divorciado(a)",
    "União Estável"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerRegistrationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VolunteerRegistrationViewModel = viewModel(),
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is VolunteerRegistrationEvent.Success -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                    delay(1200)
                    onNavigateBack()
                }

                is VolunteerRegistrationEvent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Seja um Voluntário",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar para tela de login",
                            tint = CamilaGreenDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner de Boas-vindas
            VolunteerWelcomeBanner()

            // Erro global se houver
            if (!formState.errorMessage.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CamilaOrange.copy(alpha = 0.12f))
                ) {
                    Text(
                        text = formState.errorMessage.orEmpty(),
                        color = CamilaOrange,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Bloco 1: Dados Pessoais
            PersonalDataSection(
                formState = formState,
                onFullNameChange = viewModel::onFullNameChange,
                onPhoneChange = viewModel::onPhoneChange,
                onEmailChange = viewModel::onEmailChange,
                onBirthDateChange = viewModel::onBirthDateChange,
                onMaritalStatusChange = viewModel::onMaritalStatusChange,
                onRgChange = viewModel::onRgChange,
                onCpfChange = viewModel::onCpfChange,
                onProfessionChange = viewModel::onProfessionChange,
                onAddressChange = viewModel::onAddressChange,
                onCityChange = viewModel::onCityChange,
                enabled = !formState.isSubmitting
            )

            // Bloco 2: Saúde e Cuidados
            HealthSection(
                healthLimitations = formState.healthLimitations,
                onHealthLimitationsChange = viewModel::onHealthLimitationsChange,
                enabled = !formState.isSubmitting
            )

            // Bloco 3: Áreas de Apoio
            SupportAreasSection(
                selectedAreas = formState.supportAreas,
                onToggleArea = viewModel::toggleSupportArea,
                enabled = !formState.isSubmitting
            )

            // Bloco 4: Disponibilidade de Horário
            AvailableTimesSection(
                selectedTimes = formState.availableTimes,
                onToggleTime = viewModel::toggleAvailableTime,
                enabled = !formState.isSubmitting
            )

            // Bloco 5: Informações Adicionais
            AdditionalInfoSection(
                additionalInfo = formState.additionalInfo,
                onAdditionalInfoChange = viewModel::onAdditionalInfoChange,
                enabled = !formState.isSubmitting
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botão de Envio
            Button(
                onClick = { viewModel.submit() },
                enabled = !formState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CamilaGreen,
                    disabledContainerColor = CamilaGreen.copy(alpha = 0.5f)
                )
            ) {
                if (formState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Enviando Inscrição...",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enviar Inscrição de Voluntário",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VolunteerWelcomeBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CamilaGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Apoie Nossa Causa!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cadastre-se como voluntário(a) e colabore diretamente com as famílias e alunos atendidos pela Associação Camila.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalDataSection(
    formState: VolunteerFormState,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onMaritalStatusChange: (String) -> Unit,
    onRgChange: (String) -> Unit,
    onCpfChange: (String) -> Unit,
    onProfessionChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    enabled: Boolean,
) {
    var maritalMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            onBirthDateChange(formatter.format(Date(millis)))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = CamilaGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = CamilaGray)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = CamilaGreen,
                    todayDateBorderColor = CamilaOrange
                )
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle(title = "Dados Pessoais", icon = Icons.Default.Person)

            // Nome Completo
            OutlinedTextField(
                value = formState.fullName,
                onValueChange = onFullNameChange,
                label = { Text("Nome Completo *") },
                placeholder = { Text("Seu nome completo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CamilaGreen) },
                singleLine = true,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )

            // Telefone / WhatsApp
            OutlinedTextField(
                value = formState.phone,
                onValueChange = onPhoneChange,
                label = { Text("Telefone / WhatsApp *") },
                placeholder = { Text("(11) 99999-9999") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = CamilaGreen) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )

            // E-mail
            OutlinedTextField(
                value = formState.email,
                onValueChange = onEmailChange,
                label = { Text("E-mail *") },
                placeholder = { Text("seuemail@exemplo.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CamilaGreen) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )

            // Data de Nascimento
            OutlinedTextField(
                value = formState.birthDate,
                onValueChange = onBirthDateChange,
                label = { Text("Data de Nascimento *") },
                placeholder = { Text("DD/MM/AAAA") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = CamilaGreen) },
                trailingIcon = {
                    IconButton(onClick = { if (enabled) showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Selecionar data", tint = CamilaOrange)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )

            // Dropdown Estado Civil
            ExposedDropdownMenuBox(
                expanded = maritalMenuExpanded,
                onExpandedChange = { if (enabled) maritalMenuExpanded = !maritalMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = formState.maritalStatus,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado Civil") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = maritalMenuExpanded) },
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    enabled = enabled
                )

                ExposedDropdownMenu(
                    expanded = maritalMenuExpanded,
                    onDismissRequest = { maritalMenuExpanded = false }
                ) {
                    MARITAL_STATUS_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onMaritalStatusChange(option)
                                maritalMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // RG e CPF
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = formState.rg,
                    onValueChange = onRgChange,
                    label = { Text("RG") },
                    placeholder = { Text("00.000.000-0") },
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    enabled = enabled
                )

                OutlinedTextField(
                    value = formState.cpf,
                    onValueChange = onCpfChange,
                    label = { Text("CPF") },
                    placeholder = { Text("000.000.000-00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    enabled = enabled
                )
            }

            // Profissão
            OutlinedTextField(
                value = formState.profession,
                onValueChange = onProfessionChange,
                label = { Text("Profissão / Área de Atuação") },
                placeholder = { Text("Ex: Professor(a), Administrador(a)") },
                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = CamilaGreen) },
                singleLine = true,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )

            // Endereço e Cidade
            OutlinedTextField(
                value = formState.address,
                onValueChange = onAddressChange,
                label = { Text("Endereço Residencial") },
                placeholder = { Text("Rua, Número, Bairro") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = CamilaGreen) },
                singleLine = true,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )

            OutlinedTextField(
                value = formState.city,
                onValueChange = onCityChange,
                label = { Text("Cidade / UF") },
                placeholder = { Text("Ex: Osasco - SP") },
                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = CamilaGreen) },
                singleLine = true,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )
        }
    }
}

@Composable
private fun HealthSection(
    healthLimitations: String,
    onHealthLimitationsChange: (String) -> Unit,
    enabled: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTitle(title = "Saúde e Cuidados", icon = Icons.Default.HealthAndSafety)

            Text(
                text = "Possui alguma limitação de saúde, alergia ou recomendação médica?",
                style = MaterialTheme.typography.bodySmall,
                color = CamilaGray
            )

            OutlinedTextField(
                value = healthLimitations,
                onValueChange = onHealthLimitationsChange,
                label = { Text("Limitações de Saúde / Alergias") },
                placeholder = { Text("Se houver alguma restrição física ou alimentar, descreva aqui (opcional)") },
                minLines = 2,
                maxLines = 4,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )
        }
    }
}

@Composable
private fun SupportAreasSection(
    selectedAreas: Set<String>,
    onToggleArea: (String) -> Unit,
    enabled: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle(title = "Áreas de Apoio de Interesse *", icon = Icons.Default.CheckCircle)

            Text(
                text = "Selecione uma ou mais atividades onde gostaria de contribuir:",
                style = MaterialTheme.typography.bodySmall,
                color = CamilaGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            SUPPORT_AREAS.forEach { area ->
                val isChecked = selectedAreas.contains(area)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = enabled) { onToggleArea(area) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggleArea(area) },
                        enabled = enabled,
                        colors = CheckboxDefaults.colors(
                            checkedColor = CamilaGreen,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = area,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isChecked) CamilaGreenDark else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailableTimesSection(
    selectedTimes: Set<String>,
    onToggleTime: (String) -> Unit,
    enabled: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle(title = "Disponibilidade de Horário *", icon = Icons.Default.Schedule)

            Text(
                text = "Em quais períodos você tem disponibilidade para apoiar?",
                style = MaterialTheme.typography.bodySmall,
                color = CamilaGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            AVAILABLE_TIMES.forEach { time ->
                val isChecked = selectedTimes.contains(time)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = enabled) { onToggleTime(time) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggleTime(time) },
                        enabled = enabled,
                        colors = CheckboxDefaults.colors(
                            checkedColor = CamilaGreen,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isChecked) CamilaGreenDark else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun AdditionalInfoSection(
    additionalInfo: String,
    onAdditionalInfoChange: (String) -> Unit,
    enabled: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTitle(title = "Informações Adicionais", icon = Icons.Default.Favorite)

            Text(
                text = "Conte um pouco sobre sua motivação ou deixe recados adicionais para nossa equipe:",
                style = MaterialTheme.typography.bodySmall,
                color = CamilaGray
            )

            OutlinedTextField(
                value = additionalInfo,
                onValueChange = onAdditionalInfoChange,
                label = { Text("Recado / Observações") },
                placeholder = { Text("Escreva livremente aqui...") },
                minLines = 3,
                maxLines = 6,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CamilaGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CamilaGreen,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = CamilaGreenDark
        )
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CamilaGreen,
    unfocusedBorderColor = CamilaGray.copy(alpha = 0.35f),
    focusedLabelColor = CamilaGreen,
    cursorColor = CamilaGreen
)
