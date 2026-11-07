package com.associacaocamila.app.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange

private enum class AuthTab(val title: String) {
    LOGIN("Entrar"),
    REGISTER("Cadastrar")
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (User) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess((uiState as AuthUiState.Success).user)
        }
    }

    LoginContent(
        uiState = uiState,
        modifier = modifier,
        onLogin = { email, password -> viewModel.login(email, password) },
        onRegister = { name, email, password, role -> viewModel.register(name, email, password, role) },
        onResetState = { viewModel.resetState() }
    )
}

@Composable
fun LoginContent(
    uiState: AuthUiState,
    modifier: Modifier = Modifier,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, UserRole) -> Unit,
    onResetState: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(AuthTab.LOGIN.ordinal) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderSection()

            Spacer(modifier = Modifier.height(24.dp))

            TabSection(
                selectedTabIndex = selectedTab,
                onTabSelected = {
                    selectedTab = it
                    onResetState()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState is AuthUiState.Error) {
                ErrorMessageCard(
                    message = uiState.message,
                    onDismiss = onResetState
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            val isLoading = uiState is AuthUiState.Loading

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "auth_form_animation"
            ) { tabIndex ->
                when (tabIndex) {
                    AuthTab.LOGIN.ordinal -> LoginForm(
                        isLoading = isLoading,
                        onLogin = onLogin,
                        onNavigateToRegister = {
                            selectedTab = AuthTab.REGISTER.ordinal
                            onResetState()
                        }
                    )

                    AuthTab.REGISTER.ordinal -> RegisterForm(
                        isLoading = isLoading,
                        onRegister = onRegister,
                        onNavigateToLogin = {
                            selectedTab = AuthTab.LOGIN.ordinal
                            onResetState()
                        }
                    )
                }
            }
        }
    }

    if (uiState is AuthUiState.PendingApproval) {
        PendingApprovalDialog(
            onDismiss = {
                onResetState()
                selectedTab = AuthTab.LOGIN.ordinal
            }
        )
    }
}

@Composable
private fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        com.associacaocamila.app.ui.components.CamilaLogo(
            size = 110.dp,
            contentDescription = "Logotipo Oficial Associação Camila"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Associação Camila",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = CamilaGreenDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "EM DEFESA E VALORIZAÇÃO DA VIDA",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            ),
            color = CamilaGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TabSection(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        contentColor = CamilaGreen,
        divider = {}
    ) {
        AuthTab.entries.forEachIndexed { index, tab ->
            val isSelected = selectedTabIndex == index
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = tab.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CamilaGreen else CamilaGray,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    }
}

@Composable
private fun LoginForm(
    isLoading: Boolean,
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                localError = null
            },
            label = { Text("E-mail") },
            placeholder = { Text("seuemail@exemplo.com") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = CamilaGreen
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = outlinedFieldColors(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                localError = null
            },
            label = { Text("Palavra-passe") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = CamilaGreen
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar palavra-passe" else "Mostrar palavra-passe",
                        tint = CamilaGray
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (validateInput(email, password) { localError = it }) {
                        onLogin(email, password)
                    }
                }
            ),
            colors = outlinedFieldColors(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (localError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = localError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                if (validateInput(email, password) { localError = it }) {
                    onLogin(email, password)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CamilaGreen,
                disabledContainerColor = CamilaGreen.copy(alpha = 0.5f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = "Entrar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ainda não tem conta?",
                style = MaterialTheme.typography.bodyMedium,
                color = CamilaGray
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "Cadastre-se",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = CamilaOrange
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterForm(
    isLoading: Boolean,
    onRegister: (String, String, String, UserRole) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val registrableRoles = remember {
        listOf(
            UserRole.ALUNO to "Aluno",
            UserRole.RESPONSAVEL to "Responsável",
            UserRole.PROFESSOR to "Professor"
        )
    }

    var selectedRole by remember { mutableStateOf(UserRole.ALUNO) }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                localError = null
            },
            label = { Text("Nome Completo") },
            placeholder = { Text("Ex: Maria da Silva") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CamilaGreen
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = outlinedFieldColors(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                localError = null
            },
            label = { Text("E-mail") },
            placeholder = { Text("seuemail@exemplo.com") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = CamilaGreen
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = outlinedFieldColors(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                localError = null
            },
            label = { Text("Palavra-passe") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = CamilaGreen
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar palavra-passe" else "Mostrar palavra-passe",
                        tint = CamilaGray
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            colors = outlinedFieldColors(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = roleMenuExpanded,
            onExpandedChange = { roleMenuExpanded = !roleMenuExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = registrableRoles.first { (role, _) -> role == selectedRole }.second,
                onValueChange = {},
                readOnly = true,
                label = { Text("Perfil de Acesso") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = CamilaGreen
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded)
                },
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = roleMenuExpanded,
                onDismissRequest = { roleMenuExpanded = false }
            ) {
                registrableRoles.forEach { (role, label) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (role == selectedRole) FontWeight.Bold else FontWeight.Normal,
                                color = if (role == selectedRole) CamilaGreenDark else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            selectedRole = role
                            roleMenuExpanded = false
                        }
                    )
                }
            }
        }

        if (localError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = localError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                if (validateRegisterInput(name, email, password) { localError = it }) {
                    onRegister(name, email, password, selectedRole)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CamilaGreen,
                disabledContainerColor = CamilaGreen.copy(alpha = 0.5f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = "Cadastrar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Já possui uma conta?",
                style = MaterialTheme.typography.bodyMedium,
                color = CamilaGray
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "Entrar",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = CamilaOrange
                )
            }
        }
    }
}

@Composable
private fun ErrorMessageCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar mensagem de erro",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun PendingApprovalDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = CamilaOrange.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Registo em Análise",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "O seu registo foi recebido e aguarda aprovação da direção da Associação Camila. Notificaremos assim que o acesso for libertado.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Compreendi",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CamilaGreen,
    unfocusedBorderColor = CamilaGray.copy(alpha = 0.4f),
    focusedLabelColor = CamilaGreen,
    cursorColor = CamilaGreen
)

private fun validateInput(
    email: String,
    password: String,
    onError: (String) -> Unit
): Boolean {
    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
        onError("Por favor, introduza um e-mail válido.")
        return false
    }
    if (password.isBlank()) {
        onError("Por favor, introduza a palavra-passe.")
        return false
    }
    return true
}

private fun validateRegisterInput(
    name: String,
    email: String,
    password: String,
    onError: (String) -> Unit
): Boolean {
    if (name.trim().length < 3) {
        onError("Por favor, introduza o seu nome completo.")
        return false
    }
    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
        onError("Por favor, introduza um e-mail válido.")
        return false
    }
    if (password.length < 6) {
        onError("A palavra-passe deve ter pelo menos 6 caracteres.")
        return false
    }
    return true
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    AssociacaoCamilaTheme {
        LoginContent(
            uiState = AuthUiState.Idle,
            onLogin = { _, _ -> },
            onRegister = { _, _, _, _ -> },
            onResetState = {}
        )
    }
}
