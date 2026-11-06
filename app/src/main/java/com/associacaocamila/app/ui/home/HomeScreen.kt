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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.data.model.UserStatus
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaBlue
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange

private sealed class HomeNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : HomeNavItem("home", "Início", Icons.Default.Home)
    data object Frequency : HomeNavItem("frequency", "Frequência", Icons.Default.DateRange)
    data object Attendance : HomeNavItem("attendance", "Chamada", Icons.Default.CheckCircle)
    data object Approvals : HomeNavItem("approvals", "Aprovações", Icons.Default.Check)
    data object Profile : HomeNavItem("profile", "Perfil", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = remember(currentUser.role) {
        buildList {
            add(HomeNavItem.Home)
            when (currentUser.role) {
                UserRole.ALUNO, UserRole.RESPONSAVEL -> add(HomeNavItem.Frequency)
                UserRole.PROFESSOR -> add(HomeNavItem.Attendance)
                UserRole.ADMIN -> add(HomeNavItem.Approvals)
            }
            add(HomeNavItem.Profile)
        }
    }

    var selectedItemRoute by rememberSaveable(currentUser.role) {
        mutableStateOf(HomeNavItem.Home.route)
    }

    val currentNavItem = navItems.firstOrNull { it.route == selectedItemRoute } ?: HomeNavItem.Home

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
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
                            text = currentNavItem.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = CamilaGray
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = item.route == currentNavItem.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedItemRoute = item.route },
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
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = CamilaGreenDark,
                            indicatorColor = CamilaGreen,
                            unselectedIconColor = CamilaGray,
                            unselectedTextColor = CamilaGray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentNavItem,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "home_tab_animation"
        ) { navItem ->
            when (navItem) {
                is HomeNavItem.Home -> HomeTabContent(currentUser = currentUser)
                is HomeNavItem.Frequency -> FrequencyTabContent(currentUser = currentUser)
                is HomeNavItem.Attendance -> AttendanceTabContent(currentUser = currentUser)
                is HomeNavItem.Approvals -> ApprovalsTabContent()
                is HomeNavItem.Profile -> ProfileTabContent(
                    currentUser = currentUser,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
private fun HomeTabContent(
    currentUser: User,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                            .size(48.dp)
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
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                text = "Avisos Institucionais",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = CamilaGreenDark
            )
        }

        NoticeCard(
            title = "Início do Semestre Letivo",
            description = "As oficinas e projetos sociais já estão a decorrer! Consulte horários e presenças com a coordenação.",
            badgeColor = CamilaOrange
        )

        NoticeCard(
            title = "Plantão da Assistência Social",
            description = "Atendimentos e acolhimento para responsáveis e alunos às terças e quintas-feiras.",
            badgeColor = CamilaBlue
        )

        NoticeCard(
            title = "Projetos Fortalecimento & Mídias Force",
            description = "Inscrições abertas para as novas turmas de capacitação juvenil e tecnológica.",
            badgeColor = CamilaGreenDark
        )
    }
}

@Composable
private fun NoticeCard(
    title: String,
    description: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
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

@Composable
private fun FrequencyTabContent(
    currentUser: User,
    modifier: Modifier = Modifier
) {
    PlaceholderCard(
        modifier = modifier,
        icon = Icons.Default.DateRange,
        iconTint = CamilaOrange,
        title = "Frequência e Oficinas",
        subtitle = if (currentUser.role == UserRole.RESPONSAVEL) {
            "Consulte os registos de presença e participação dos alunos associados às oficinas da ONG."
        } else {
            "Acompanhe as suas presenças nas oficinas e atividades da Associação Camila."
        },
        infoText = "Módulo de acompanhamento de frequência em integração com o sistema acadêmico."
    )
}

@Composable
private fun AttendanceTabContent(
    currentUser: User,
    modifier: Modifier = Modifier
) {
    PlaceholderCard(
        modifier = modifier,
        icon = Icons.Default.CheckCircle,
        iconTint = CamilaGreen,
        title = "Chamada e Registo de Presença",
        subtitle = "Professor(a) ${currentUser.name}, aqui poderá registar a presença diária e o desenvolvimento dos alunos nas turmas.",
        infoText = "Módulo de chamada rápida e relatórios de frequência em preparação para os professores."
    )
}

@Composable
private fun ApprovalsTabContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CamilaOrange.copy(alpha = 0.12f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = CamilaOrange,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Gestão de Novos Cadastros",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = CamilaGreenDark
                    )
                    Text(
                        text = "Área restrita à diretoria institucional",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = CamilaGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Validação Institucional",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aqui serão listados os novos cadastros com status PENDENTE para validação da direção da Associação Camila, permitindo aprovar ou reprovar o acesso ao aplicativo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CamilaGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileTabContent(
    currentUser: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(CamilaGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = CamilaGreen,
                modifier = Modifier.size(52.dp)
            )
        }

        Text(
            text = currentUser.name.ifBlank { "Utilizador" },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = CamilaGreenDark,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ProfileInfoRow(
                    icon = Icons.Default.Email,
                    label = "E-mail",
                    value = currentUser.email.ifBlank { "Não informado" }
                )
                ProfileInfoRow(
                    icon = Icons.Default.Badge,
                    label = "Perfil de Acesso",
                    value = currentUser.role.toDisplayName()
                )
                ProfileInfoRow(
                    icon = Icons.Default.School,
                    label = "Estado da Conta",
                    value = when (currentUser.status) {
                        UserStatus.APPROVED -> "Aprovado"
                        UserStatus.PENDING -> "Pendente de Análise"
                        UserStatus.REJECTED -> "Recusado"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CamilaOrange
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sair da Conta",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CamilaGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CamilaGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = CamilaGray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PlaceholderCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    infoText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = CamilaGreenDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CamilaGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CamilaGreen.copy(alpha = 0.08f))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CamilaGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = infoText,
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGreenDark
                        )
                    }
                }
            }
        }
    }
}

private fun UserRole.toDisplayName(): String = when (this) {
    UserRole.ADMIN -> "Administrador"
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
                status = UserStatus.APPROVED
            ),
            onLogout = {}
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
                status = UserStatus.APPROVED
            ),
            onLogout = {}
        )
    }
}
