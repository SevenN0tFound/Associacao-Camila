package com.associacaocamila.app.ui.home

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.data.model.UserStatus
import com.associacaocamila.app.ui.admin.AdminApprovalsScreen
import com.associacaocamila.app.ui.admin.AdminWorkshopsScreen
import com.associacaocamila.app.ui.attendance.AttendanceScreen
import com.associacaocamila.app.ui.profile.ProfileUiState
import com.associacaocamila.app.ui.profile.ProfileViewModel
import com.associacaocamila.app.ui.student.StudentAttendanceScreen
import com.associacaocamila.app.ui.student.StudentWorkshopsScreen
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaBlue
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange
import kotlinx.coroutines.launch

/**
 * Representa os itens de navegação do menu lateral com controle
 * dinâmico de visibilidade por perfil institucional.
 */
sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    data object Inicio : NavigationItem("inicio", "Início", Icons.Default.Home)
    data object MinhasOficinas : NavigationItem("minhas_oficinas", "Minhas Oficinas", Icons.Default.DateRange)
    data object Frequencia : NavigationItem("frequencia", "Frequência", Icons.Default.Assessment)
    data object Chamada : NavigationItem("chamada", "Chamada", Icons.Default.CheckCircle)
    data object Aprovacoes : NavigationItem("aprovacoes", "Aprovações", Icons.Default.Check)
    data object Turmas : NavigationItem("turmas", "Turmas", Icons.Default.School)
    data object Perfil : NavigationItem("perfil", "Perfil", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    initialDrawerValue: DrawerValue = DrawerValue.Closed,
    onUserUpdated: ((User) -> Unit)? = null,
) {
    var userState by remember(currentUser) { mutableStateOf(currentUser) }
    val snackbarHostState = remember { SnackbarHostState() }

    val navItems = remember(userState.role) {
        buildList {
            add(NavigationItem.Inicio)
            when (userState.role) {
                UserRole.ALUNO, UserRole.RESPONSAVEL -> {
                    add(NavigationItem.MinhasOficinas)
                    add(NavigationItem.Frequencia)
                }
                UserRole.PROFESSOR -> add(NavigationItem.Chamada)
                UserRole.ADMIN -> {
                    add(NavigationItem.Aprovacoes)
                    add(NavigationItem.Turmas)
                }
            }
            add(NavigationItem.Perfil)
        }
    }

    var selectedRoute by rememberSaveable(userState.role) {
        mutableStateOf(NavigationItem.Inicio.route)
    }

    val currentItem = navItems.firstOrNull { it.route == selectedRoute } ?: NavigationItem.Inicio
    val drawerState = rememberDrawerState(initialValue = initialDrawerValue)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerTonalElevation = 0.dp,
                modifier = Modifier.width(320.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    // Área rolável: Cabeçalho institucional e lista de rotas
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Cabeçalho Institucional Elegante
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CamilaGreen)
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                           Icon(
                                               imageVector = Icons.Default.Person,
                                               contentDescription = null,
                                               tint = Color.White,
                                               modifier = Modifier.size(28.dp)
                                           )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = userState.name.ifBlank { "Utilizador" },
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color.White
                                            )
                                            Text(
                                                text = userState.email.ifBlank { "Sem e-mail" },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    // Logo Oficial da Associação Camila
                                    com.associacaocamila.app.ui.components.CamilaLogo(
                                        size = 40.dp,
                                        contentDescription = "Logo Associação Camila"
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = userState.role.toDisplayName(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "“Em defesa e valorização da vida”",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.White.copy(alpha = 0.95f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Itens de Navegação Dinâmicos por Perfil
                        navItems.forEach { item ->
                            val isSelected = item.route == currentItem.route
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    selectedRoute = item.route
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = CamilaGreen.copy(alpha = 0.15f),
                                    selectedIconColor = CamilaGreenDark,
                                    selectedTextColor = CamilaGreenDark,
                                    unselectedIconColor = CamilaGray,
                                    unselectedTextColor = CamilaGray
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Rodapé Fixo da Gaveta: Divisória e Ação Sair da Conta
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = CamilaGray.copy(alpha = 0.2f)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Sair da Conta",
                                tint = CamilaOrange
                            )
                        },
                        label = {
                            Text(
                                text = "Sair da Conta",
                                color = CamilaOrange,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = BackgroundLight,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Associação Camila",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = CamilaGreenDark
                            )
                            Text(
                                text = currentItem.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = CamilaGray
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu de navegação",
                                tint = CamilaGreenDark
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = currentItem,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "home_tab_animation"
            ) { navItem ->
                when (navItem) {
                    is NavigationItem.Inicio -> InicioTabContent(currentUser = userState)
                    is NavigationItem.MinhasOficinas -> StudentWorkshopsScreen(currentUser = userState)
                    is NavigationItem.Frequencia -> StudentAttendanceScreen(currentUser = userState)
                    is NavigationItem.Chamada -> AttendanceScreen(teacherId = userState.id)
                    is NavigationItem.Aprovacoes -> AdminApprovalsScreen()
                    is NavigationItem.Turmas -> AdminWorkshopsScreen()
                    is NavigationItem.Perfil -> PerfilTabContent(
                        currentUser = userState,
                        onUserUpdated = { updated ->
                            userState = updated
                            onUserUpdated?.invoke(updated)
                        },
                        onLogout = onLogout,
                        snackbarHostState = snackbarHostState,
                    )
                }
            }
        }
    }
}

@Composable
private fun InicioTabContent(
    currentUser: User,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card de Boas-vindas personalizado
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CamilaGreen
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Olá, ${currentUser.name.ifBlank { "Membro" }}!",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Perfil: ${currentUser.role.toDisplayName()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card com o lema institucional
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "“Em defesa e valorização da vida”",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Seção com avisos e oficinas recentes
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = CamilaOrange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Avisos e Oficinas em Destaque",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = CamilaGreenDark
            )
        }

        NoticeCard(
            title = "Oficina de Fortalecimento",
            description = "Aulas e encontros socioeducativos semanais para o desenvolvimento integral de jovens e famílias.",
            badgeColor = CamilaOrange
        )

        NoticeCard(
            title = "Projeto Mídias Force",
            description = "Capacitação prática em comunicação, audiovisual e produção de conteúdo digital.",
            badgeColor = CamilaBlue
        )

        NoticeCard(
            title = "Acolhimento e Assistência Social",
            description = "Atendimentos psicossociais disponíveis para agendamento com a nossa equipa técnica.",
            badgeColor = CamilaGreenDark
        )
    }
}

@Composable
private fun NoticeCard(
    title: String,
    description: String,
    badgeColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = CamilaGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PerfilTabContent(
    currentUser: User,
    onUserUpdated: (User) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    profileViewModel: ProfileViewModel? = null,
) {
    val isInspectionMode = LocalInspectionMode.current
    val actualViewModel = if (isInspectionMode) null else (profileViewModel ?: viewModel())
    val uiState = actualViewModel?.uiState?.collectAsState()?.value ?: ProfileUiState()
    val context = LocalContext.current
    var showEditSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let { msg ->
            if (snackbarHostState != null) {
                snackbarHostState.showSnackbar(msg)
            } else {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            actualViewModel?.clearMessages()
        }
        uiState.errorMessage?.let { err ->
            if (snackbarHostState != null) {
                snackbarHostState.showSnackbar(err)
            } else {
                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            }
            actualViewModel?.clearMessages()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Avatar Institucional
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(CamilaGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = CamilaGreen,
                modifier = Modifier.size(52.dp),
            )
        }

        // Nome e perfil
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentUser.name.ifBlank { "Utilizador" },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = CamilaGreenDark,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = CamilaGreen.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = currentUser.role.toDisplayName(),
                    color = CamilaGreenDark,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        // Botão "Editar Perfil"
        Button(
            onClick = { showEditSheet = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen),
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Editar Perfil",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
            )
        }

        // Card 1: Dados da Conta
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Dados da Conta",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark,
                )
                ProfileInfoRow(
                    icon = Icons.Default.Email,
                    label = "E-mail",
                    value = currentUser.email.ifBlank { "Não informado" },
                )
                ProfileInfoRow(
                    icon = Icons.Default.Badge,
                    label = "Tipo de Conta",
                    value = currentUser.role.toDisplayName(),
                )
                ProfileInfoRow(
                    icon = Icons.Default.School,
                    label = "Status da Conta",
                    value = when (currentUser.status) {
                        UserStatus.APPROVED -> "Aprovado"
                        UserStatus.PENDING -> "Pendente de Análise"
                        UserStatus.REJECTED -> "Recusado"
                    },
                )
            }
        }

        // Card 2: Contato e Endereço
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Contato e Localização",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark,
                )
                ProfileInfoRow(
                    icon = Icons.Default.Phone,
                    label = "Telefone / WhatsApp",
                    value = currentUser.phone.ifBlank { "Não informado" },
                    iconTint = CamilaOrange,
                )
                ProfileInfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Data de Nascimento",
                    value = currentUser.birthDate.ifBlank { "Não informada" },
                    iconTint = CamilaOrange,
                )
                ProfileInfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Endereço Residencial",
                    value = currentUser.address.ifBlank { "Não informado" },
                    iconTint = CamilaOrange,
                )
            }
        }

        // Card 3: Prontuário & Observações Médicas
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CamilaOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = CamilaOrange,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Prontuário & Observações de Saúde",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark,
                        )
                        Text(
                            text = "Alergias, remédios contínuos ou observações da ONG",
                            style = MaterialTheme.typography.labelSmall,
                            color = CamilaGray,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (currentUser.medicalNotes.isNotBlank()) {
                                CamilaOrange.copy(alpha = 0.08f)
                            } else {
                                BackgroundLight
                            }
                        )
                        .padding(12.dp),
                ) {
                    if (currentUser.medicalNotes.isNotBlank()) {
                        Text(
                            text = currentUser.medicalNotes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp,
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CamilaGray,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Nenhuma restrição ou observação médica cadastrada.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CamilaGray,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão Sair da Conta
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CamilaOrange,
            ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = Color.White,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sair da Conta",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
            )
        }
    }

    // Modal Bottom Sheet de Edição de Perfil
    if (showEditSheet) {
        var editPhone by rememberSaveable { mutableStateOf(currentUser.phone) }
        var editBirthDate by rememberSaveable { mutableStateOf(currentUser.birthDate) }
        var editAddress by rememberSaveable { mutableStateOf(currentUser.address) }
        var editMedicalNotes by rememberSaveable { mutableStateOf(currentUser.medicalNotes) }

        ModalBottomSheet(
            onDismissRequest = {
                if (!uiState.isSaving) {
                    showEditSheet = false
                    actualViewModel?.clearMessages()
                }
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Editar Perfil",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark,
                        )
                        Text(
                            text = "Atualize suas informações e prontuário",
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray,
                        )
                    }

                    IconButton(
                        onClick = {
                            if (!uiState.isSaving) {
                                showEditSheet = false
                                actualViewModel?.clearMessages()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = CamilaGray,
                        )
                    }
                }

                HorizontalDivider(color = CamilaGray.copy(alpha = 0.2f))

                OutlinedTextField(
                    value = editPhone,
                    onValueChange = { editPhone = it },
                    label = { Text("Telefone / WhatsApp") },
                    placeholder = { Text("(11) 98765-4321") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = CamilaOrange,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CamilaGreen,
                        focusedLabelColor = CamilaGreenDark,
                    ),
                    enabled = !uiState.isSaving,
                )

                OutlinedTextField(
                    value = editBirthDate,
                    onValueChange = { editBirthDate = it },
                    label = { Text("Data de Nascimento") },
                    placeholder = { Text("DD/MM/AAAA") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = CamilaOrange,
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CamilaGreen,
                        focusedLabelColor = CamilaGreenDark,
                    ),
                    enabled = !uiState.isSaving,
                )

                OutlinedTextField(
                    value = editAddress,
                    onValueChange = { editAddress = it },
                    label = { Text("Endereço Residencial") },
                    placeholder = { Text("Rua, número, bairro e cidade") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = CamilaOrange,
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CamilaGreen,
                        focusedLabelColor = CamilaGreenDark,
                    ),
                    enabled = !uiState.isSaving,
                )

                OutlinedTextField(
                    value = editMedicalNotes,
                    onValueChange = { editMedicalNotes = it },
                    label = { Text("Prontuário & Observações de Saúde") },
                    placeholder = { Text("Alergias, remédios contínuos, cuidados específicos ou recomendações") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = CamilaOrange,
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CamilaGreen,
                        focusedLabelColor = CamilaGreenDark,
                    ),
                    enabled = !uiState.isSaving,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            showEditSheet = false
                            actualViewModel?.clearMessages()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CamilaGray,
                        ),
                        enabled = !uiState.isSaving,
                    ) {
                        Text(
                            text = "Cancelar",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }

                    Button(
                        onClick = {
                            if (actualViewModel != null) {
                                actualViewModel.updateProfile(
                                    currentUser = currentUser,
                                    phone = editPhone,
                                    birthDate = editBirthDate,
                                    address = editAddress,
                                    medicalNotes = editMedicalNotes,
                                    onSuccess = { updatedUser ->
                                        onUserUpdated(updatedUser)
                                        showEditSheet = false
                                    },
                                )
                            } else {
                                onUserUpdated(
                                    currentUser.copy(
                                        phone = editPhone.trim(),
                                        birthDate = editBirthDate.trim(),
                                        address = editAddress.trim(),
                                        medicalNotes = editMedicalNotes.trim(),
                                    ),
                                )
                                showEditSheet = false
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen),
                        enabled = !uiState.isSaving,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = "Salvar Alterações",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    iconTint: Color = CamilaGreen,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = CamilaGray,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun UserRole.toDisplayName(): String = when (this) {
    UserRole.ADMIN -> "ADMIN"
    UserRole.PROFESSOR -> "Professor"
    UserRole.RESPONSAVEL -> "Responsável"
    UserRole.ALUNO -> "Aluno"
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenAdminPreview() {
    AssociacaoCamilaTheme {
        HomeScreen(
            currentUser = User(
                id = "1",
                name = "Maria Coordenadora",
                email = "maria@associacaocamila.org",
                role = UserRole.ADMIN,
                status = UserStatus.APPROVED,
                phone = "(11) 99887-1122",
                birthDate = "12/03/1985",
                address = "Av. Social da Paz, 100",
                medicalNotes = "Sem restrições de saúde.",
            ),
            onLogout = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenStudentPreview() {
    AssociacaoCamilaTheme {
        HomeScreen(
            currentUser = User(
                id = "2",
                name = "Lucas Silva",
                email = "lucas@email.com",
                role = UserRole.ALUNO,
                status = UserStatus.APPROVED,
                phone = "(11) 98765-4321",
                birthDate = "15/05/2008",
                address = "Rua das Flores, 120 - Jd. Esperança",
                medicalNotes = "Alergia leve a amendoim. Sem restrições para atividades esportivas.",
            ),
            onLogout = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenDrawerPreview() {
    AssociacaoCamilaTheme {
        HomeScreen(
            currentUser = User(
                id = "1",
                name = "Maria Coordenadora",
                email = "maria@associacaocamila.org",
                role = UserRole.ADMIN,
                status = UserStatus.APPROVED,
                phone = "(11) 99887-1122",
                birthDate = "12/03/1985",
                address = "Av. Social da Paz, 100",
            ),
            onLogout = {},
            initialDrawerValue = DrawerValue.Open,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenStudentDrawerPreview() {
    AssociacaoCamilaTheme {
        HomeScreen(
            currentUser = User(
                id = "2",
                name = "Lucas Aluno",
                email = "lucas@associacaocamila.org",
                role = UserRole.ALUNO,
                status = UserStatus.APPROVED,
                phone = "(11) 98765-4321",
                birthDate = "15/05/2008",
                address = "Rua das Flores, 120",
            ),
            onLogout = {},
            initialDrawerValue = DrawerValue.Open,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PerfilTabContentPreview() {
    AssociacaoCamilaTheme {
        Box(
            modifier = Modifier
                .background(BackgroundLight)
                .fillMaxSize(),
        ) {
            PerfilTabContent(
                currentUser = User(
                    id = "2",
                    name = "Lucas Silva",
                    email = "lucas@email.com",
                    role = UserRole.ALUNO,
                    status = UserStatus.APPROVED,
                    phone = "(11) 98765-4321",
                    birthDate = "15/05/2008",
                    address = "Rua das Flores, 120 - Jd. Esperança",
                    medicalNotes = "Alergia leve a amendoim. Sem restrições para esportes.",
                ),
                onUserUpdated = {},
                onLogout = {},
            )
        }
    }
}
