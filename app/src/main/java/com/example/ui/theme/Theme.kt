package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BartaPrimary,
    onPrimary = Color.White,
    primaryContainer = BartaPrimaryDark,
    onPrimaryContainer = BartaPrimaryLight,
    secondary = BartaAccent,
    onSecondary = Color.White,
    background = BartaBackground,
    onBackground = BartaTextPrimary,
    surface = BartaSurface,
    onSurface = BartaTextPrimary,
    surfaceVariant = BartaSurfaceVariant,
    onSurfaceVariant = BartaTextSecondary,
    outline = Color(0xFF374248)
)

private val LightColorScheme = DarkColorScheme // Modern WhatsApp dark by default

@Composable
fun BartaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
