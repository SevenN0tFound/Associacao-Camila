package com.associacaocamila.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.repository.UserRepository
import com.associacaocamila.app.data.repository.UserRepositoryImpl
import com.associacaocamila.app.ui.theme.CamilaGreen
import kotlinx.coroutines.launch

/**
 * Componente reutilizável de Avatar do Usuário.
 *
 * Exibe a foto carregada do Firebase Storage via Coil ou a inicial do primeiro nome,
 * com suporte a upload direto da galeria e feedback visual de carregamento.
 */
@Composable
fun ProfileAvatar(
    user: User,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    isEditable: Boolean = false,
    userRepository: UserRepository = remember { UserRepositoryImpl() },
    onPhotoSelected: ((Uri) -> Unit)? = null,
    onUserUpdated: ((User) -> Unit)? = null,
    onPhotoUpdated: ((String) -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onPhotoSelected?.invoke(uri)
            if (user.id.isNotBlank()) {
                scope.launch {
                    isUploading = true
                    val result = userRepository.uploadProfilePhoto(user.id, uri, context)
                    isUploading = false
                    if (result.isSuccess) {
                        val downloadUrl = result.getOrThrow()
                        onPhotoUpdated?.invoke(downloadUrl)
                        onUserUpdated?.invoke(user.copy(photoUrl = downloadUrl))
                    } else {
                        onError?.invoke(
                            result.exceptionOrNull()?.localizedMessage
                                ?: "Erro ao realizar upload da foto de perfil."
                        )
                    }
                }
            }
        }
    }

    val firstName = remember(user.name) {
        user.name.trim().split("\\s+".toRegex()).firstOrNull().orEmpty()
    }
    val initial = remember(firstName) {
        firstName.firstOrNull()?.uppercase() ?: "U"
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (isEditable && !isUploading) {
                    Modifier.clickable { galleryLauncher.launch("image/*") }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Imagem do Perfil ou Inicial do Primeiro Nome
        if (!user.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "Foto de perfil de ${user.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(CamilaGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.42f).sp
                    ),
                    color = CamilaGreen
                )
            }
        }

        // Indicador de Carregamento durante upload
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size * 0.45f),
                    color = CamilaGreen,
                    strokeWidth = 2.dp
                )
            }
        }

        // Ícone discreto indicando que a foto é editável
        if (isEditable && !isUploading) {
            val badgeSize = (size.value * 0.32f).coerceAtLeast(18f).dp
            val iconSize = (badgeSize.value * 0.65f).dp

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(badgeSize)
                    .clip(CircleShape)
                    .background(CamilaGreen)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Alterar foto de perfil",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
