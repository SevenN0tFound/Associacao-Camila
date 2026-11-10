package com.associacaocamila.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.associacaocamila.app.data.model.AdminMetrics
import com.associacaocamila.app.data.model.Notice
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.data.model.UserStatus
import com.associacaocamila.app.ui.admin.AdminApprovalsScreen
import com.associacaocamila.app.ui.admin.AdminVolunteersScreen
import com.associacaocamila.app.ui.admin.AdminWorkshopsScreen
import com.associacaocamila.app.ui.attendance.AttendanceScreen
import com.associacaocamila.app.ui.components.ProfileAvatar
import com.associacaocamila.app.ui.help.HelpContactScreen
import com.associacaocamila.app.ui.profile.ProfileUiState
import com.associacaocamila.app.ui.profile.ProfileViewModel
import com.associacaocamila.app.ui.student.StudentAttendanceScreen
import com.associacaocamila.app.ui.student.StudentWorkshopsScreen
import com.associacaocamila.app.ui.van.VanBookingScreen
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
    data object AgendamentoVan : NavigationItem("agendamento_van", "Agendamento da Van", Icons.Default.DirectionsBus)
    data object Voluntarios : NavigationItem("voluntarios", "Voluntários", Icons.Default.Handshake)
    data object Perfil : NavigationItem("perfil", "Perfil", Icons.Default.Person)
    data object Ajuda : NavigationItem("ajuda", "Ajuda & Contato", Icons.AutoMirrored.Filled.HelpOutline)
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
    val context = LocalContext.current

    // Solicitação elegante de permissão para notificações locais (Android 13+)
    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* Permissão concedida ou negada */ }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (!com.associacaocamila.app.util.NotificationHelper.hasNotificationPermission(context)) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        com.associacaocamila.app.util.NotificationHelper.createNotificationChannels(context)
    }

    val navItems = remember(userState.role) {
        buildList {
            add(NavigationItem.Inicio)
            when (userState.role) {
                UserRole.ALUNO, UserRole.RESPONSAVEL -> {
                    add(NavigationItem.MinhasOficinas)
                    add(NavigationItem.Frequencia)
                }
                UserRole.PROFESSOR -> {
                    add(NavigationItem.Chamada)
                    add(NavigationItem.AgendamentoVan)
                }
                UserRole.ADMIN -> {
                    add(NavigationItem.Aprovacoes)
                    add(NavigationItem.Turmas)
                    add(NavigationItem.AgendamentoVan)
                    add(NavigationItem.Voluntarios)
                }
            }
            add(NavigationItem.Perfil)
            add(NavigationItem.Ajuda)
        }
    }

    var selectedRoute by rememberSaveable(userState.role) {
        mutableStateOf(NavigationItem.Inicio.route)
    }

    val isInspectionMode = LocalInspectionMode.current
    val homeViewModel: HomeViewModel? = if (isInspectionMode) null else viewModel()
    val homeUiState = homeViewModel?.uiState?.collectAsState()?.value ?: HomeUiState(
        isLoadingNotices = false,
        notices = listOf(
            Notice(
                id = "1",
                title = "Oficina de Fortalecimento",
                content = "Aulas e encontros socioeducativos semanais para o desenvolvimento integral de jovens e famílias.",
                category = "EVENTO",
                targetAudience = "TODOS",
                authorName = "Coordenação",
                authorRole = "ADMIN",
            ),
            Notice(
                id = "2",
                title = "Acolhimento e Assistência Social",
                content = "Atendimentos psicossociais disponíveis para agendamento com a nossa equipe técnica.",
                category = "GERAL",
                targetAudience = "RESPONSAVEIS",
                authorName = "Serviço Social",
                authorRole = "PROFESSOR",
            ),
            Notice(
                id = "3",
                title = "Alinhamento Pedagógico Trimestral",
                content = "Reunião de alinhamento com todos os instrutores e professores das oficinas.",
                category = "GERAL",
                targetAudience = "PROFESSORES",
                authorName = "Direção",
                authorRole = "ADMIN",
            ),
        ),
        isLoadingMetrics = false,
        adminMetrics = AdminMetrics(
            totalStudents = 142,
            activeWorkshops = 8,
            pendingVolunteers = 4,
            overallAttendanceRate = 88,
        ),
    )

    // Filtro dinâmico por perfil institucional
    val filteredNotices = remember(homeUiState.notices, userState.role) {
        homeUiState.notices.filter { notice ->
            val audience = notice.targetAudience.trim().uppercase().ifBlank { "TODOS" }
            when (userState.role) {
                UserRole.ADMIN -> true
                UserRole.PROFESSOR -> audience == "TODOS" || audience == "PROFESSORES"
                UserRole.RESPONSAVEL, UserRole.ALUNO -> audience == "TODOS" || audience == "RESPONSAVEIS"
            }
        }
    }

    var showNotificationsSheet by rememberSaveable { mutableStateOf(false) }
    var readNoticeIds by rememberSaveable { mutableStateOf(setOf<String>()) }

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
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CamilaGreen)
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ProfileAvatar(
                                        user = userState,
                                        size = 46.dp,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
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
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Itens de Navegação Dinâmicos por Perfil
                        navItems.forEach { item ->
                            val isSelected = (selectedRoute == item.route)
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
                                    selectedContainerColor = CamilaGreen.copy(alpha = 0.12f),
                                    unselectedContainerColor = Color.Transparent,
                                    selectedIconColor = CamilaGreen,
                                    unselectedIconColor = CamilaGray,
                                    selectedTextColor = CamilaGreen,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
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
                    actions = {
                        val unreadCount = remember(filteredNotices, readNoticeIds) {
                            filteredNotices.count { !readNoticeIds.contains(it.id) }
                        }
                        IconButton(onClick = { showNotificationsSheet = true }) {
                            if (unreadCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = CamilaOrange,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = if (unreadCount > 99) "99+" else "$unreadCount",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Central de Notificações ($unreadCount não lidas)",
                                        tint = CamilaGreenDark
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Central de Notificações",
                                    tint = CamilaGreenDark
                                )
                            }
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
                    is NavigationItem.Inicio -> InicioTabContent(
                        currentUser = userState,
                        snackbarHostState = snackbarHostState,
                        homeViewModel = homeViewModel,
                        filteredNotices = filteredNotices,
                    )
                    is NavigationItem.MinhasOficinas -> StudentWorkshopsScreen(currentUser = userState)
                    is NavigationItem.Frequencia -> StudentAttendanceScreen(currentUser = userState)
                    is NavigationItem.Chamada -> AttendanceScreen(teacherId = userState.id)
                    is NavigationItem.Aprovacoes -> AdminApprovalsScreen()
                    is NavigationItem.Turmas -> AdminWorkshopsScreen()
                    is NavigationItem.AgendamentoVan -> VanBookingScreen(
                        currentUser = userState,
                        onNavigateBack = { selectedRoute = NavigationItem.Inicio.route }
                    )
                    is NavigationItem.Voluntarios -> AdminVolunteersScreen()
                    is NavigationItem.Perfil -> PerfilTabContent(
                        currentUser = userState,
                        onUserUpdated = { updated ->
                            userState = updated
                            onUserUpdated?.invoke(updated)
                        },
                        onLogout = onLogout,
                        snackbarHostState = snackbarHostState,
                    )
                    is NavigationItem.Ajuda -> HelpContactScreen()
                }
            }
        }
    }

    if (showNotificationsSheet) {
        NotificationsBottomSheet(
            notices = filteredNotices,
            readNoticeIds = readNoticeIds,
            onDismiss = { showNotificationsSheet = false },
            onMarkAsRead = { id -> readNoticeIds = readNoticeIds + id },
            onMarkAllAsRead = {
                readNoticeIds = readNoticeIds + filteredNotices.map { it.id }.toSet()
            }
        )
    }
}

@Composable
private fun InicioTabContent(
    currentUser: User,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    homeViewModel: HomeViewModel? = null,
    filteredNotices: List<Notice>? = null,
) {
    val isInspectionMode = LocalInspectionMode.current
    val actualViewModel = if (isInspectionMode) null else (homeViewModel ?: viewModel())
    val uiState = actualViewModel?.uiState?.collectAsState()?.value ?: HomeUiState(
        isLoadingNotices = false,
        notices = listOf(
            Notice(
                id = "1",
                title = "Oficina de Fortalecimento",
                content = "Aulas e encontros socioeducativos semanais para o desenvolvimento integral de jovens e famílias.",
                category = "EVENTO",
                targetAudience = "TODOS",
                authorName = "Coordenação",
                authorRole = "ADMIN",
            ),
            Notice(
                id = "2",
                title = "Acolhimento e Assistência Social",
                content = "Atendimentos psicossociais disponíveis para agendamento com a nossa equipe técnica.",
                category = "GERAL",
                targetAudience = "RESPONSAVEIS",
                authorName = "Serviço Social",
                authorRole = "PROFESSOR",
            ),
            Notice(
                id = "3",
                title = "Alinhamento Pedagógico Trimestral",
                content = "Reunião de alinhamento com todos os instrutores e professores das oficinas.",
                category = "GERAL",
                targetAudience = "PROFESSORES",
                authorName = "Direção",
                authorRole = "ADMIN",
            ),
        ),
        isLoadingMetrics = false,
        adminMetrics = AdminMetrics(
            totalStudents = 142,
            activeWorkshops = 8,
            pendingVolunteers = 4,
            overallAttendanceRate = 88,
        ),
    )
    val context = LocalContext.current

    val noticesToShow = filteredNotices ?: remember(uiState.notices, currentUser.role) {
        uiState.notices.filter { notice ->
            val audience = notice.targetAudience.trim().uppercase().ifBlank { "TODOS" }
            when (currentUser.role) {
                UserRole.ADMIN -> true
                UserRole.PROFESSOR -> audience == "TODOS" || audience == "PROFESSORES"
                UserRole.RESPONSAVEL, UserRole.ALUNO -> audience == "TODOS" || audience == "RESPONSAVEIS"
            }
        }
    }

    LaunchedEffect(currentUser.role) {
        if (currentUser.role == UserRole.ADMIN) {
            actualViewModel?.loadAdminMetrics()
        }
    }

    val canManageNotices = currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.PROFESSOR
    var showAddNoticeDialog by rememberSaveable { mutableStateOf(false) }
    var noticeToDelete by remember { mutableStateOf<Notice?>(null) }

    LaunchedEffect(uiState.userMessage, uiState.errorMessage) {
        uiState.userMessage?.let { msg ->
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val firstName = remember(currentUser.name) {
            currentUser.name.trim().split("\\s+".toRegex()).firstOrNull()?.ifBlank { "Membro" } ?: "Membro"
        }

        // Card de Boas-vindas personalizado com tema claro moderno
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, CamilaGray.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(
                    user = currentUser,
                    size = 56.dp,
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Olá, $firstName!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CamilaGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = currentUser.role.toDisplayName(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = CamilaGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Painel Geral de Indicadores (visível exclusivamente para ADMIN)
        if (currentUser.role == UserRole.ADMIN) {
            AdminMetricsDashboardSection(
                metrics = uiState.adminMetrics,
                isLoading = uiState.isLoadingMetrics,
            )
        }

        // Seção Destacada: Mural de Recados & Avisos
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mural de Recados & Avisos",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = CamilaGreenDark
                )
            }

            if (canManageNotices) {
                Button(
                    onClick = { showAddNoticeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Novo Aviso",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Novo Aviso",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // Lista dinâmica do Mural ou estados de loading/vazio
        if (uiState.isLoadingNotices) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = CamilaGreen,
                    strokeWidth = 3.dp
                )
            }
        } else if (noticesToShow.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = CamilaGray,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nenhum comunicado no mural no momento",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = CamilaGray
                    )
                }
            }
        } else {
            noticesToShow.forEach { notice ->
                NoticeCard(
                    notice = notice,
                    canDelete = canManageNotices,
                    onDeleteClick = { noticeToDelete = notice }
                )
            }
        }
    }

    // Diálogo de criação de novo aviso (ADMIN e PROFESSOR)
    if (showAddNoticeDialog) {
        AddNoticeDialog(
            userRole = currentUser.role,
            isPosting = uiState.isPostingNotice,
            onDismiss = { showAddNoticeDialog = false },
            onConfirm = { title, content, category, targetAudience ->
                actualViewModel?.addNotice(
                    title = title,
                    content = content,
                    category = category,
                    targetAudience = targetAudience,
                    authorName = currentUser.name.ifBlank { "Coordenação" },
                    authorRole = currentUser.role.name,
                    onSuccess = { showAddNoticeDialog = false }
                )
            }
        )
    }

    // Diálogo de confirmação para exclusão de aviso
    noticeToDelete?.let { notice ->
        AlertDialog(
            onDismissRequest = { noticeToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Remover Aviso",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Deseja realmente apagar o aviso \"${notice.title}\"? Ele não será mais visível no mural para os participantes e responsáveis.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CamilaGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        actualViewModel?.deleteNotice(notice.id)
                        noticeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CamilaOrange)
                ) {
                    Text("Excluir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noticeToDelete = null }) {
                    Text("Cancelar", color = CamilaGray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun AdminMetricsDashboardSection(
    metrics: AdminMetrics,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = CamilaGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Painel Geral / Indicadores",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                    Text(
                        text = "Visão executiva em tempo real",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = CamilaGreen
                )
            }
        }

        // Grade 2x2 de Cartões de Indicadores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cartão 1: Alunos Ativos
            MetricCard(
                title = "Alunos Ativos",
                value = "${metrics.totalStudents}",
                icon = Icons.Default.Person,
                accentColor = CamilaGreen,
                modifier = Modifier.weight(1f)
            )

            // Cartão 2: Oficinas
            MetricCard(
                title = "Oficinas",
                value = "${metrics.activeWorkshops}",
                icon = Icons.Default.School,
                accentColor = CamilaGreenDark,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cartão 3: Novos Voluntários
            MetricCard(
                title = "Novos Voluntários",
                value = "${metrics.pendingVolunteers}",
                icon = Icons.Default.Handshake,
                accentColor = CamilaOrange,
                badgeText = if (metrics.pendingVolunteers > 0) "${metrics.pendingVolunteers} pendente(s)" else "Sem pendências",
                modifier = Modifier.weight(1f)
            )

            // Cartão 4: Presença Geral
            MetricCard(
                title = "Presença Geral",
                value = "${metrics.overallAttendanceRate}%",
                icon = Icons.Default.CheckCircle,
                accentColor = CamilaGreen,
                badgeText = "Assiduidade",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = CamilaGray,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NoticeCard(
    notice: Notice,
    canDelete: Boolean,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryUpper = notice.category.uppercase()
    val isUrgentOrRecess = categoryUpper == "URGENTE" || categoryUpper == "RECESSO"
    val categoryBadgeColor = if (isUrgentOrRecess) CamilaOrange else CamilaGreen

    val formattedDate = remember(notice.timestamp) {
        if (notice.timestamp > 0) {
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.forLanguageTag("pt-BR"))
                .format(java.util.Date(notice.timestamp))
        } else {
            ""
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tags/Badges: Categoria e Público-Alvo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = categoryBadgeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, categoryBadgeColor.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = categoryUpper,
                            color = categoryBadgeColor,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    val audienceUpper = notice.targetAudience.trim().uppercase().ifBlank { "TODOS" }
                    val (audienceLabel, audienceColor) = when (audienceUpper) {
                        "PROFESSORES" -> "Professores" to CamilaGreenDark
                        "RESPONSAVEIS" -> "Responsáveis / Alunos" to CamilaOrange
                        else -> "Todos" to CamilaGray
                    }
                    Surface(
                        color = audienceColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, audienceColor.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = audienceLabel,
                            color = audienceColor,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (formattedDate.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = CamilaGray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = CamilaGray
                            )
                        }
                    }

                    if (canDelete) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remover aviso",
                                tint = CamilaOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Título em negrito
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Texto do comunicado com tipografia limpa
            Text(
                text = notice.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            // Rodapé sutil com nome do autor e cargo
            if (notice.authorName.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CamilaGray.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CamilaGray,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = buildString {
                            append("Publicado por: ")
                            append(notice.authorName)
                            if (notice.authorRole.isNotBlank()) {
                                append(" (${notice.authorRole.lowercase().replaceFirstChar { it.uppercase() }})")
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = CamilaGray
                    )
                }
            }
        }
    }
}

@Composable
private fun AddNoticeDialog(
    userRole: UserRole,
    isPosting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, category: String, targetAudience: String) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("GERAL") }
    var selectedAudience by rememberSaveable { mutableStateOf("TODOS") }

    val categories = listOf("GERAL", "EVENTO", "RECESSO", "URGENTE")
    val audienceOptions = remember(userRole) {
        if (userRole == UserRole.ADMIN) {
            listOf(
                "TODOS" to "Todos",
                "PROFESSORES" to "Apenas Professores",
                "RESPONSAVEIS" to "Apenas Responsáveis/Alunos"
            )
        } else {
            listOf(
                "TODOS" to "Todos",
                "RESPONSAVEIS" to "Apenas Responsáveis/Alunos"
            )
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isPosting) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = CamilaGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Novo Aviso no Mural",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = CamilaGreenDark
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Informe o título, o público-alvo, a categoria e a mensagem.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CamilaGray
                )

                // Campo Título
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do Aviso *") },
                    placeholder = { Text("Ex: Recesso escolar no feriado") },
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPosting
                )

                // Seletor de Público-Alvo
                Column {
                    Text(
                        text = "Público-Alvo *",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        audienceOptions.forEach { (code, label) ->
                            val isSelected = selectedAudience == code
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedAudience = code },
                                label = {
                                    Text(
                                        text = label,
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
                                    borderColor = CamilaGreen.copy(alpha = 0.35f),
                                    selectedBorderColor = CamilaGreen
                                ),
                                enabled = !isPosting
                            )
                        }
                    }
                }

                // Seletor de Categoria
                Column {
                    Text(
                        text = "Categoria *",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            val chipColor = if (cat == "URGENTE" || cat == "RECESSO") CamilaOrange else CamilaGreen
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = cat,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = chipColor.copy(alpha = 0.15f),
                                    selectedLabelColor = chipColor
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = chipColor.copy(alpha = 0.35f),
                                    selectedBorderColor = chipColor
                                ),
                                enabled = !isPosting
                            )
                        }
                    }
                }

                // Campo Conteúdo
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Conteúdo do Comunicado *") },
                    placeholder = { Text("Escreva a mensagem detalhada...") },
                    minLines = 3,
                    maxLines = 6,
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPosting
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, content, selectedCategory, selectedAudience) },
                enabled = !isPosting && title.isNotBlank() && content.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CamilaGreen)
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publicando...")
                } else {
                    Text("Publicar Aviso", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isPosting
            ) {
                Text("Cancelar", color = CamilaGray)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsBottomSheet(
    notices: List<Notice>,
    readNoticeIds: Set<String>,
    onDismiss: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val unreadCount = notices.count { !readNoticeIds.contains(it.id) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Cabeçalho da Central de Notificações
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
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = CamilaGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Central de Notificações",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark
                        )
                        Text(
                            text = if (unreadCount > 0) "$unreadCount aviso(s) não lido(s)" else "Tudo em dia",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (unreadCount > 0) CamilaOrange else CamilaGray
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (unreadCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onMarkAllAsRead,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CamilaGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Marcar todas como lidas",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreen
                        )
                    }
                }
            }

            HorizontalDivider(color = BackgroundLight)
            Spacer(modifier = Modifier.height(12.dp))

            if (notices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = CamilaGray.copy(alpha = 0.4f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhum comunicado no momento",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Quando a coordenação publicar avisos para o seu grupo, eles aparecerão aqui.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notices, key = { it.id }) { notice ->
                        val isRead = readNoticeIds.contains(notice.id)
                        NotificationItemCard(
                            notice = notice,
                            isRead = isRead,
                            onClick = {
                                if (!isRead) onMarkAsRead(notice.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    notice: Notice,
    isRead: Boolean,
    onClick: () -> Unit,
) {
    val categoryUpper = notice.category.uppercase()
    val isUrgentOrRecess = categoryUpper == "URGENTE" || categoryUpper == "RECESSO"
    val categoryBadgeColor = if (isUrgentOrRecess) CamilaOrange else CamilaGreen

    val audienceUpper = notice.targetAudience.trim().uppercase().ifBlank { "TODOS" }
    val (audienceLabel, audienceColor) = when (audienceUpper) {
        "PROFESSORES" -> "Exclusivo Professores" to CamilaGreenDark
        "RESPONSAVEIS" -> "Responsáveis & Alunos" to CamilaOrange
        else -> "Geral / Todos" to CamilaGray
    }

    val formattedDate = remember(notice.timestamp) {
        if (notice.timestamp > 0) {
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.forLanguageTag("pt-BR"))
                .format(java.util.Date(notice.timestamp))
        } else {
            ""
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isRead) BackgroundLight else Color.White,
        border = BorderStroke(
            width = if (isRead) 1.dp else 1.5.dp,
            color = if (isRead) CamilaGray.copy(alpha = 0.2f) else CamilaGreen.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CamilaOrange)
                        )
                    }

                    Surface(
                        color = categoryBadgeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = categoryUpper,
                            color = categoryBadgeColor,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = audienceColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = audienceLabel,
                            color = audienceColor,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                if (formattedDate.isNotBlank()) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = CamilaGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isRead) FontWeight.SemiBold else FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notice.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (notice.authorName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CamilaGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Por: ${notice.authorName}" + if (notice.authorRole.isNotBlank()) " (${notice.authorRole})" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = CamilaGray
                    )
                }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CamilaGreen,
    unfocusedBorderColor = CamilaGray.copy(alpha = 0.4f),
    focusedLabelColor = CamilaGreen,
    cursorColor = CamilaGreen
)

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
        // Avatar Institucional Padronizado
        ProfileAvatar(
            user = currentUser,
            size = 90.dp,
        )

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

        // Card 4: Documentação e Anexos (Prontuário Digital)
        com.associacaocamila.app.ui.components.DocumentAttachmentsSection(
            ownerId = currentUser.id,
            documents = currentUser.documents,
            onDocumentsChanged = { updatedDocs ->
                onUserUpdated(currentUser.copy(documents = updatedDocs))
            },
            canEdit = true,
        )

        // Card Especial para RESPONSÁVEL: Gestão de Dependentes / Participantes Vinculados
        if (currentUser.role == UserRole.RESPONSAVEL) {
            var showSelectStudentSheet by rememberSaveable { mutableStateOf(false) }
            var dependentToUnlink by remember { mutableStateOf<com.associacaocamila.app.data.model.Dependent?>(null) }

            com.associacaocamila.app.ui.components.DependentsSection(
                dependents = currentUser.dependents,
                onLinkStudentClick = {
                    showSelectStudentSheet = true
                    actualViewModel?.loadAvailableStudents()
                },
                onUnlinkDependentClick = { dep ->
                    dependentToUnlink = dep
                },
                canEdit = true,
            )

            if (showSelectStudentSheet) {
                com.associacaocamila.app.ui.components.SelectStudentBottomSheet(
                    availableStudents = uiState.availableStudents,
                    alreadyLinkedIds = (currentUser.associatedStudentIds + currentUser.dependents.map { it.id }).toSet(),
                    isLoading = uiState.isLoadingStudents,
                    isLinking = uiState.isLinkingDependent,
                    onDismiss = { showSelectStudentSheet = false },
                    onRefreshStudents = { actualViewModel?.loadAvailableStudents() },
                    onStudentSelected = { selectedStudent ->
                        actualViewModel?.linkStudentAsDependent(
                            guardian = currentUser,
                            student = selectedStudent,
                            onSuccess = { updatedGuardian ->
                                onUserUpdated(updatedGuardian)
                                showSelectStudentSheet = false
                            },
                        )
                    },
                )
            }

            dependentToUnlink?.let { dep ->
                AlertDialog(
                    onDismissRequest = { dependentToUnlink = null },
                    title = {
                        Text(
                            text = "Desvincular Participante",
                            fontWeight = FontWeight.Bold,
                            color = CamilaGreenDark,
                        )
                    },
                    text = {
                        Text(
                            text = "Deseja realmente desvincular ${dep.name.ifBlank { "este participante" }} da sua conta de responsável?",
                            color = CamilaGray,
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                actualViewModel?.unlinkDependent(
                                    guardian = currentUser,
                                    dependentId = dep.id,
                                    dependentName = dep.name,
                                    onSuccess = { updatedGuardian ->
                                        onUserUpdated(updatedGuardian)
                                        dependentToUnlink = null
                                    },
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CamilaOrange),
                        ) {
                            Text("Desvincular", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { dependentToUnlink = null }) {
                            Text("Cancelar", color = CamilaGray)
                        }
                    },
                )
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

                // Avatar Institucional Padronizado
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileAvatar(
                        user = currentUser,
                        size = 80.dp,
                    )
                }

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
