package com.associacaocamila.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.ui.theme.CamilaGreen

/**
 * Componente reutilizável de Avatar Institucional do Usuário.
 *
 * Componente 100% estático e padronizado: exibe a inicial maiúscula do nome do usuário
 * (ou ícone neutro de pessoa caso o nome esteja vazio) sobre fundo suave CamilaGreen (15% de opacidade).
 */
@Composable
fun ProfileAvatar(
    user: User,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val initial = remember(user.name) {
        user.name.trim().take(1).uppercase()
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(CamilaGreen.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        if (initial.isNotBlank()) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.45f).sp,
                ),
                color = CamilaGreen,
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar do usuário",
                tint = CamilaGreen,
                modifier = Modifier.size(size * 0.55f),
            )
        }
    }
}
