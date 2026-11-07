package com.associacaocamila.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.associacaocamila.app.R

/**
 * Componente visual da Marca Oficial da Associação Camila.
 * Renderiza o drawable vetorial institucional oficial em alta fidelidade.
 */
@Composable
fun CamilaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 104.dp,
    contentDescription: String = "Logotipo Associação Camila",
) {
    Image(
        painter = painterResource(id = R.drawable.logo_associacao_camila),
        contentDescription = contentDescription,
        modifier = modifier.size(size)
    )
}
