package com.associacaocamila.app.ui.help

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme
import com.associacaocamila.app.ui.theme.BackgroundLight
import com.associacaocamila.app.ui.theme.CamilaGray
import com.associacaocamila.app.ui.theme.CamilaGreen
import com.associacaocamila.app.ui.theme.CamilaGreenDark
import com.associacaocamila.app.ui.theme.CamilaOrange
import kotlinx.coroutines.launch

private const val PHONE_NUMBER = "(11) 3592-0330"
private const val PHONE_DIAL_URI = "tel:1135920330"
private const val CONTACT_EMAIL = "contato@associacaocamila.org.br"
private const val HEADQUARTERS_ADDRESS = "Av. Clóvis Assaf, 455 – Conceição, Osasco – SP, 06145-080"
private const val MAPS_QUERY_ADDRESS = "Av. Clóvis Assaf, 455 - Conceição, Osasco - SP, 06145-080"
private const val SERVICE_HOURS = "Segunda a Sexta: 08h às 17h\nSábados, Domingos e Feriados: Fechado"

/**
 * Dados de cada pergunta frequente institucional.
 */
data class FaqItem(
    val id: Int,
    val question: String,
    val answer: String,
)

private val FAQ_ITEMS = listOf(
    FaqItem(
        id = 1,
        question = "Como funciona a presença e tolerância de faltas nas oficinas?",
        answer = "A frequência mínima exigida para certificação e manutenção da vaga na oficina é de 75%. Em caso de falta justificada por motivos médicos ou escolares, o responsável deve anexar o comprovante pelo app."
    ),
    FaqItem(
        id = 2,
        question = "Como enviar atestados e justificar ausências de dependentes?",
        answer = "No menu lateral, acesse 'Frequência'. Selecione o dependente desejado e localize o registro de falta na lista. Clique no botão 'Justificar Falta com Atestado', descreva o motivo e anexe uma foto legível do comprovante. Nossa coordenação analisará o pedido em até 48 horas úteis."
    ),
    FaqItem(
        id = 3,
        question = "Como se inscrever em novas oficinas?",
        answer = "As inscrições para novas oficinas e turmas abrem no início de cada semestre e conforme disponibilidade de vagas, sempre divulgadas no Feed de Avisos na tela inicial. Você também pode manifestar interesse diretamente com a coordenação na sede ou pelo WhatsApp de atendimento."
    ),
    FaqItem(
        id = 4,
        question = "Quem procurar em caso de emergência ou dúvidas pedagógicas?",
        answer = "Para dúvidas pedagógicas e horários de aulas, procure o(a) professor(a) responsável pela oficina. Para atendimentos sociais, apoio à família, orientações de documentação ou situações urgentes, procure o Serviço Social e a Coordenação da Associação Camila em nossos canais oficiais."
    )
)

/**
 * Tela institucional de Ajuda, Suporte e Canais de Contato com a Associação Camila.
 * Pode ser exibida com TopAppBar própria ou embutida no fluxo principal da HomeScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpContactScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    showTopBar: Boolean = (onNavigateBack != null),
) {
    if (showTopBar) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Central de Ajuda e Contato",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark
                        )
                    },
                    navigationIcon = {
                        if (onNavigateBack != null) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = CamilaGreenDark
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            modifier = modifier.fillMaxSize(),
            containerColor = BackgroundLight
        ) { innerPadding ->
            HelpContactContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    } else {
        HelpContactContent(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight)
        )
    }
}

@Composable
fun HelpContactContent(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner de Acolhimento Institucional
            InstitutionalWelcomeBanner()

            // Card de Ações Rápidas de Atendimento (Telefone Fixo, E-mail, WhatsApp)
            QuickContactActionsCard(
                onCallPhone = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse(PHONE_DIAL_URI))
                    launchIntentSafely(
                        context = context,
                        intent = intent,
                        failureMessage = "Nenhum discador telefônico encontrado no dispositivo."
                    )
                },
                onSendEmail = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$CONTACT_EMAIL")
                        putExtra(Intent.EXTRA_SUBJECT, "Contato / Dúvida - Associação Camila")
                    }
                    launchIntentSafely(
                        context = context,
                        intent = intent,
                        failureMessage = "Nenhum aplicativo de e-mail encontrado no dispositivo."
                    )
                },
                onOpenWhatsApp = {
                    val message = "Canal de WhatsApp em alinhamento com a coordenação"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            )

            // Card de Endereço da Sede e Horários de Atendimento
            LocationAndHoursCard(
                onOpenMaps = {
                    val mapUri = Uri.parse("geo:0,0?q=${Uri.encode(MAPS_QUERY_ADDRESS)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                    launchIntentSafely(
                        context = context,
                        intent = mapIntent,
                        failureMessage = "Não foi possível abrir o aplicativo de mapas ou navegador."
                    )
                }
            )

            // Seção de Perguntas Frequentes (FAQ)
            FaqSection(
                items = FAQ_ITEMS,
                expandedStates = expandedStates,
                onToggleExpand = { id ->
                    val current = expandedStates[id] ?: false
                    expandedStates[id] = !current
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rodapé Institucional
            InstitutionalFooter()
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun InstitutionalWelcomeBanner(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Estamos aqui por você!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A Associação Camila acolhe sua família com dedicação e carinho. Escolha um dos canais abaixo para falar conosco.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun QuickContactActionsCard(
    onCallPhone: () -> Unit,
    onSendEmail: () -> Unit,
    onOpenWhatsApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Canais Rápidos de Atendimento",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fale diretamente com nossa equipe de apoio e coordenação:",
                style = MaterialTheme.typography.bodySmall,
                color = CamilaGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Telefone Fixo
            ContactActionButton(
                icon = Icons.Default.Phone,
                title = "Telefone Fixo",
                subtitle = PHONE_NUMBER,
                buttonText = "Ligar Agora",
                accentColor = CamilaGreenDark,
                onClick = onCallPhone
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. E-mail Institucional
            ContactActionButton(
                icon = Icons.Default.Email,
                title = "E-mail Institucional",
                subtitle = CONTACT_EMAIL,
                buttonText = "Enviar E-mail",
                accentColor = CamilaOrange,
                onClick = onSendEmail
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. WhatsApp (Demonstrativo / Em breve)
            ContactActionButton(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "WhatsApp da Coordenação",
                subtitle = "Canal em configuração",
                statusBadge = "Em breve",
                buttonText = "Em breve",
                accentColor = CamilaGreen,
                onClick = onOpenWhatsApp
            )
        }
    }
}

@Composable
private fun ContactActionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonText: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    statusBadge: String? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = BackgroundLight,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (statusBadge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = accentColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = statusBadge,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = CamilaGray,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LocationAndHoursCard(
    onOpenMaps: () -> Unit,
    modifier: Modifier = Modifier,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CamilaOrange.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = CamilaOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Endereço e Atendimento",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                    Text(
                        text = "Venha nos visitar na sede da Associação",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Endereço
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BackgroundLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Sede Institucional:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = HEADQUARTERS_ADDRESS,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onOpenMaps,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CamilaOrange),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CamilaOrange),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Como Chegar (Abrir no Mapa)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horários de Atendimento
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BackgroundLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = CamilaGreen,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Horários de Atendimento ao Público:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = SERVICE_HOURS,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqSection(
    items: List<FaqItem>,
    expandedStates: Map<Int, Boolean>,
    onToggleExpand: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QuestionAnswer,
                contentDescription = null,
                tint = CamilaGreenDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Perguntas Frequentes (FAQ)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark
            )
        }

        items.forEach { faq ->
            val isExpanded = expandedStates[faq.id] ?: false
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand(faq.id) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(
                    1.dp,
                    if (isExpanded) CamilaGreen.copy(alpha = 0.5f) else CamilaGray.copy(alpha = 0.15f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = faq.question,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isExpanded) CamilaGreenDark else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Recolher" else "Expandir",
                            tint = if (isExpanded) CamilaGreen else CamilaGray
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(
                                color = BackgroundLight,
                                thickness = 1.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = faq.answer,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstitutionalFooter(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Associação Camila",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = CamilaGreenDark
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "EM DEFESA E VALORIZAÇÃO DA VIDA",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = CamilaOrange,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Dispara intents do sistema com tratamento de [ActivityNotFoundException] para evitar falhas no app.
 */
private fun launchIntentSafely(
    context: Context,
    intent: Intent,
    failureMessage: String = "Nenhum aplicativo compatível encontrado.",
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

@Preview(showBackground = true)
@Composable
private fun HelpContactScreenPreview() {
    AssociacaoCamilaTheme {
        HelpContactContent()
    }
}
