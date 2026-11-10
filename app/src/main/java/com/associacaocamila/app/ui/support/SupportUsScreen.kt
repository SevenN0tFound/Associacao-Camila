package com.associacaocamila.app.ui.support

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.VolunteerActivism
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

private const val PIX_KEY_CNPJ = "06.158.571/0001-35"
private const val NFP_PORTAL_URL = "https://www.nfp.fazenda.sp.gov.br"
private const val HEADQUARTERS_ADDRESS = "Av. Clóvis Assaf, 455 - Conceição, Osasco - SP, 06145-080"
private val BANK_INFO_TEXT = """
Banco: Bradesco (237)
Agência: 2856-8
Conta Corrente: 7086-6
Favorecido: Associação Camila em Defesa e Valorização da Vida
CNPJ: 06.158.571/0001-35
""".trimIndent()

/**
 * Modelo para opções de apoio financeiro mensal recorrente.
 */
data class DonationTier(
    val amount: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
)

private val DONATION_TIERS = listOf(
    DonationTier(
        amount = "R$ 20",
        title = "Doador Amigo",
        description = "Contribui com o lanche e materiais básicos diários das crianças.",
        icon = Icons.Default.Favorite
    ),
    DonationTier(
        amount = "R$ 50",
        title = "Doador Protetor",
        description = "Ajuda na manutenção de oficinas pedagógicas e apoio às famílias.",
        icon = Icons.Default.VolunteerActivism
    ),
    DonationTier(
        amount = "R$ 100",
        title = "Doador Padrinho",
        description = "Garante a sustentabilidade de turmas inteiras e passeios culturais.",
        icon = Icons.Default.Handshake
    )
)

/**
 * Tela institucional "Como Nos Apoiar / Doações".
 * Pode ser exibida com TopAppBar própria (quando chamada externamente) ou integrada ao Drawer da Home.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportUsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToVolunteer: () -> Unit = {},
    showTopBar: Boolean = (onNavigateBack != null),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    if (showTopBar) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Como Nos Apoiar",
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = modifier.fillMaxSize(),
            containerColor = BackgroundLight
        ) { innerPadding ->
            SupportUsContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                snackbarHostState = snackbarHostState,
                onNavigateToVolunteer = onNavigateToVolunteer,
            )
        }
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = modifier.fillMaxSize(),
            containerColor = BackgroundLight
        ) { innerPadding ->
            SupportUsContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                snackbarHostState = snackbarHostState,
                onNavigateToVolunteer = onNavigateToVolunteer,
            )
        }
    }
}

/**
 * Conteúdo principal rolável estruturado em LazyColumn com Cards Material 3.
 */
@Composable
fun SupportUsContent(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateToVolunteer: () -> Unit = {},
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    fun copyToClipboard(text: String, label: String, feedbackMessage: String) {
        clipboardManager.setText(AnnotatedString(text))
        // Compatibilidade nativa direta com Android ClipboardManager
        try {
            val systemClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            systemClipboard?.setPrimaryClip(clip)
        } catch (_: Exception) {}

        Toast.makeText(context, feedbackMessage, Toast.LENGTH_SHORT).show()
        scope.launch {
            snackbarHostState.showSnackbar(feedbackMessage)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // A. Cabeçalho Institucional
        item {
            InstitutionalSupportHeader()
        }

        // B. Card 1: Contribuição Financeira (Pix & Transferência Bancária)
        item {
            FinancialContributionCard(
                onCopyPix = {
                    copyToClipboard(
                        text = PIX_KEY_CNPJ,
                        label = "Chave Pix Associação Camila",
                        feedbackMessage = "Chave Pix copiada com sucesso!"
                    )
                },
                onSelectTier = { tier ->
                    copyToClipboard(
                        text = PIX_KEY_CNPJ,
                        label = "Chave Pix Associação Camila",
                        feedbackMessage = "Chave Pix copiada! Valor sugerido: ${tier.amount} (${tier.title})"
                    )
                },
                onCopyBankDetails = {
                    copyToClipboard(
                        text = BANK_INFO_TEXT,
                        label = "Dados Bancários Associação Camila",
                        feedbackMessage = "Dados bancários copiados com sucesso!"
                    )
                }
            )
        }

        // C. Card 2: Nota Fiscal Paulista (Sem gastar nada)
        item {
            NotaFiscalPaulistaCard(
                onOpenNfpPortal = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(NFP_PORTAL_URL))
                    launchIntentSafely(
                        context = context,
                        intent = intent,
                        failureMessage = "Não foi possível abrir o navegador de internet."
                    )
                },
                onCopyCnpj = {
                    copyToClipboard(
                        text = PIX_KEY_CNPJ,
                        label = "CNPJ Associação Camila",
                        feedbackMessage = "CNPJ copiado para cadastro na NFP!"
                    )
                }
            )
        }

        // D. Card 3: Doações Materiais (Bazar ReUse & Cestas Básicas)
        item {
            MaterialDonationsCard(
                onOpenMapsRoute = {
                    val mapUri = Uri.parse("geo:0,0?q=${Uri.encode(HEADQUARTERS_ADDRESS)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                    launchIntentSafely(
                        context = context,
                        intent = mapIntent,
                        failureMessage = "Não foi possível abrir o aplicativo de mapas ou GPS."
                    )
                }
            )
        }

        // E. Card 4: Voluntariado
        item {
            VolunteerCallToActionCard(
                onNavigateToVolunteer = onNavigateToVolunteer
            )
        }

        // Rodapé Institucional
        item {
            SupportFooter()
        }
    }
}

/**
 * A. Cabeçalho Institucional com apelo comunitário e identidade visual.
 */
@Composable
private fun InstitutionalSupportHeader() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CamilaGreen.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.2.dp, CamilaGreen.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = CamilaGreen,
                shape = CircleShape,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = CamilaOrange.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "SOLIDARIEDADE QUE TRANSFORMA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = CamilaOrange,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Como Apoiar Nossa Missão",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = CamilaGreenDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Acreditamos que transformar vidas é um trabalho coletivo. Conheça as formas de contribuir com nossos projetos sociais.",
                style = MaterialTheme.typography.bodyMedium,
                color = CamilaGray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

/**
 * B. Card 1: Contribuição Financeira com destaque Pix, Apoio Recorrente e Dados Bancários.
 */
@Composable
private fun FinancialContributionCard(
    onCopyPix: () -> Unit,
    onSelectTier: (DonationTier) -> Unit,
    onCopyBankDetails: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Título do Card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = CamilaGreen.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = CamilaGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Contribuição Financeira",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                    Text(
                        text = "Pix instantâneo e transferência bancária",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Destaque para o Pix
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CamilaGreen.copy(alpha = 0.07f)),
                border = BorderStroke(1.dp, CamilaGreen.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "CHAVE PIX OFICIAL (CNPJ)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark
                        )
                        Surface(
                            color = CamilaGreen,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "INSTANTÂNEO",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = PIX_KEY_CNPJ,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        ),
                        color = CamilaGreenDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Favorecido: Associação Camila em Defesa e Valorização da Vida",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onCopyPix,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CamilaGreen,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar Pix",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Copiar Chave Pix",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sugestões de Apoio Recorrente
            Text(
                text = "Sugestões de Apoio Recorrente",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark
            )
            Text(
                text = "Ajude a manter os projetos ativos com uma doação mensal:",
                style = MaterialTheme.typography.bodySmall,
                color = CamilaGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DONATION_TIERS.forEach { tier ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                        border = BorderStroke(1.dp, CamilaGray.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTier(tier) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Surface(
                                color = CamilaGreen.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = tier.icon,
                                        contentDescription = null,
                                        tint = CamilaGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tier.amount,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = CamilaOrange
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${tier.title}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = CamilaGreenDark
                                    )
                                }
                                Text(
                                    text = tier.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CamilaGray
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar Pix para doar ${tier.amount}",
                                tint = CamilaGray.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = CamilaGray.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(16.dp))

            // Dados Bancários Institucionais
            Text(
                text = "Dados Bancários Institucionais (TED / DOC)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = CamilaGreenDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = BackgroundLight,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CamilaGray.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BankDetailRow(label = "Banco:", value = "Bradesco (237)")
                    BankDetailRow(label = "Agência:", value = "2856-8")
                    BankDetailRow(label = "Conta Corrente:", value = "7086-6")
                    BankDetailRow(label = "Favorecido:", value = "Associação Camila em Defesa e Valorização da Vida")
                    BankDetailRow(label = "CNPJ:", value = "06.158.571/0001-35")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onCopyBankDetails,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CamilaGreen),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CamilaGreenDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Copiar Dados Bancários Completos",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun BankDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = CamilaGray,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black
        )
    }
}

/**
 * C. Card 2: Nota Fiscal Paulista (Doação sem gastar nada e cupons físicos).
 */
@Composable
private fun NotaFiscalPaulistaCard(
    onOpenNfpPortal: () -> Unit,
    onCopyCnpj: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = CamilaOrange.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = CamilaOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Nota Fiscal Paulista",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                    Text(
                        text = "Doe seus créditos sem gastar absolutamente nada",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Passo a passo Doador Automático
            Surface(
                color = CamilaGreen.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CamilaGreen.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CamilaGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seja um Doador Automático da NFP",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Cadastre a Associação Camila no portal da Fazenda SP para doar seus créditos automaticamente a cada compra:",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    NfpStepItem(number = "1", text = "Acesse o portal da Nota Fiscal Paulista.")
                    NfpStepItem(number = "2", text = "Faça login com seu CPF e senha da NFP.")
                    NfpStepItem(number = "3", text = "Vá em Entidades > Doação de Cupons com CPF (automática).")
                    NfpStepItem(number = "4", text = "Pesquise pelo CNPJ: $PIX_KEY_CNPJ e selecione a Associação Camila.")

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onCopyCnpj,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CamilaGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CamilaGreenDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Copiar CNPJ",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Button(
                            onClick = onOpenNfpPortal,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CamilaGreen,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Acessar Portal da NFP",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Doação de Cupons Físicos sem CPF
            Surface(
                color = CamilaOrange.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CamilaOrange.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CamilaOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Doação de Cupons Físicos (sem CPF)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CamilaOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Guarda suas notas fiscais de compras físicas? Entregue na sede da Associação Camila até o dia 20 de cada mês para nossa equipe digitar no sistema!",
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NfpStepItem(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = CamilaGreen,
            shape = CircleShape,
            modifier = Modifier.size(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black
        )
    }
}

/**
 * D. Card 3: Doações Materiais (Bazar ReUse & Cestas Básicas) com botão de rota.
 */
@Composable
private fun MaterialDonationsCard(
    onOpenMapsRoute: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = CamilaGreen.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Checkroom,
                            contentDescription = null,
                            tint = CamilaGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Doações Materiais",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                    Text(
                        text = "Roupas, calçados e alimentos de primeira necessidade",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bazar ReUse
            Surface(
                color = BackgroundLight,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CamilaGray.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = CamilaOrange.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Checkroom,
                                contentDescription = null,
                                tint = CamilaOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Bazar ReUse",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Arrecadamos roupas, calçados, bolsas e acessórios em bom estado. As peças geram renda solidária para apoiar o custeio das oficinas sociais e manutenção das atividades.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Alimentos e Cestas
            Surface(
                color = BackgroundLight,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CamilaGray.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = CamilaGreen.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = CamilaGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Alimentos e Cestas Básicas",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CamilaGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Arrecadamos alimentos não perecíveis (arroz, feijão, leite, óleo, macarrão), produtos de higiene pessoal e itens de primeira necessidade para as famílias atendidas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CamilaGray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Local de Entrega e Botão de Rota
            Surface(
                color = CamilaGreen.copy(alpha = 0.05f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Ponto de Entrega Física:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaGreenDark
                    )
                    Text(
                        text = HEADQUARTERS_ADDRESS,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                    Text(
                        text = "Horário: Segunda a sexta, das 08h às 17h",
                        style = MaterialTheme.typography.labelSmall,
                        color = CamilaGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenMapsRoute,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CamilaGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Traçar Rota até a Sede no Mapa",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * E. Card 4: Voluntariado com botão de navegação para a tela de inscrição de voluntários.
 */
@Composable
private fun VolunteerCallToActionCard(
    onNavigateToVolunteer: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CamilaOrange.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = CamilaOrange.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = CamilaOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Doe Seu Tempo e Talento",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamilaOrange
                    )
                    Text(
                        text = "Seja parte ativa da nossa equipe voluntária",
                        style = MaterialTheme.typography.bodySmall,
                        color = CamilaGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Você pode nos apoiar ministrando oficinas, auxiliando na organização de eventos, na triagem do Bazar ReUse, na separação de alimentos ou na digitação de notas fiscais.",
                style = MaterialTheme.typography.bodyMedium,
                color = CamilaGray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToVolunteer,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CamilaOrange,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quero me voluntariar",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Rodapé Institucional com mensagem acolhedora de gratidão.
 */
@Composable
private fun SupportFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Toda doação gera oportunidades e constrói um futuro digno.",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = CamilaGreenDark,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Gratidão por caminhar junto com a Associação Camila! 💚",
            style = MaterialTheme.typography.bodySmall,
            color = CamilaGray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "CNPJ: $PIX_KEY_CNPJ • Osasco - SP",
            style = MaterialTheme.typography.labelSmall,
            color = CamilaGray.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Executa intents externas com segurança para evitar travamentos caso o app correspondente não exista.
 */
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

@Preview(showBackground = true)
@Composable
private fun SupportUsScreenPreview() {
    AssociacaoCamilaTheme {
        SupportUsScreen()
    }
}
