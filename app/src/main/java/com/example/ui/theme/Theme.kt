package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkBluePrimary,
    onPrimary = Color.White,
    secondary = OneUIBlueLight,
    onSecondary = OneUIBlueDark,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    error = OneUIRed
)

private val LightColorScheme = lightColorScheme(
    primary = OneUIBluePrimary,
    onPrimary = Color.White,
    secondary = OneUIBlueLight,
    onSecondary = OneUIBlueDark,
    background = OneUIBackground,
    surface = OneUISurface,
    onBackground = OneUITextPrimary,
    onSurface = OneUITextPrimary,
    error = OneUIRed,
    surfaceVariant = OneUIBlueLight,
    onSurfaceVariant = OneUITextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
