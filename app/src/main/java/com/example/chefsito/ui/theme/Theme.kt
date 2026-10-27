package com.example.chefsito.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ChefsitoColorScheme = lightColorScheme(
    primary = NaranjaCoral,
    secondary = AmarilloMostaza,
    tertiary = VerdeFresco,
    background = FondoCrema,
    surface = BlancoPuro,
    onPrimary = BlancoPuro,
    onBackground = TintaComic,
    onSurface = TintaComic
)

@Composable
fun ChefsitoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ChefsitoColorScheme,
        typography = Typography,
        content = content
    )
}