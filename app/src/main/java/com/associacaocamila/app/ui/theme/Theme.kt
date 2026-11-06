package com.associacaocamila.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CamilaColorScheme = lightColorScheme(
    primary = CamilaGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = Color(0xFFE0F5EC),
    onPrimaryContainer = CamilaGreenDark,

    secondary = CamilaOrange,
    onSecondary = SurfaceWhite,
    secondaryContainer = Color(0xFFFFEBD6),
    onSecondaryContainer = CamilaOrangeDark,

    tertiary = CamilaBlue,
    background = BackgroundLight,
    surface = SurfaceWhite,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun AssociacaoCamilaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CamilaColorScheme,
        typography = Typography,
        content = content
    )
}