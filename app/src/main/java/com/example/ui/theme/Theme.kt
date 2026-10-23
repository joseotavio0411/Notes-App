package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VioletRoyalDark,
    onPrimary = Color(0xFF1E1D24),
    primaryContainer = SlatePrimaryContainerDark,
    onPrimaryContainer = SlateOnPrimaryContainerDark,
    secondary = VioletNeon,
    onSecondary = Color(0xFF1E1D24),
    background = SlateBackgroundDark,
    surface = SlateSurfaceDark,
    surfaceVariant = SlateContainerDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SlateBorderDark,
    outlineVariant = Color(0xFF3D3A4E),
    error = StatusUrgentCrimson
)

// Superfícies com cor explícita: os padrões do Material deixavam cartões, folhas
// e diálogos quase no mesmo tom do fundo.
private val LightColorScheme = lightColorScheme(
    primary = VioletRoyal,
    onPrimary = Color.White,
    primaryContainer = AmethystPrimaryContainerLight,
    onPrimaryContainer = VioletDeepBase,
    secondary = VioletNeon,
    onSecondary = Color.White,
    background = AmethystSurfaceLight,
    surface = AmethystCardLight,
    surfaceVariant = AmethystContainerLight,
    surfaceContainerLowest = AmethystCardLight,
    surfaceContainerLow = AmethystCardLight,
    surfaceContainer = AmethystCardLight,
    surfaceContainerHigh = AmethystContainerLight,
    surfaceContainerHighest = AmethystContainerLight,
    // Sem véu violeta por elevação: cartões e barras ficam brancos de verdade.
    surfaceTint = Color.Transparent,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = AmethystBorderLight,
    outlineVariant = AmethystBorderLight,
    error = StatusUrgentCrimson
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
