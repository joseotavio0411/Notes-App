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
    onPrimary = ObsidianVoidDark,
    primaryContainer = ObsidianContainerDark,
    onPrimaryContainer = VioletRoyalDark,
    secondary = VioletNeon,
    onSecondary = ObsidianVoidDark,
    background = ObsidianVoidDark,
    surface = ObsidianSurfaceDark,
    surfaceVariant = ObsidianContainerDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = ObsidianBorderDark,
    error = StatusUrgentCrimson
)

private val LightColorScheme = lightColorScheme(
    primary = VioletRoyal,
    onPrimary = Color.White,
    primaryContainer = AmethystContainerLight,
    onPrimaryContainer = VioletDeepBase,
    secondary = VioletNeon,
    onSecondary = Color.White,
    background = AmethystSurfaceLight,
    surface = AmethystCardLight,
    surfaceVariant = AmethystContainerLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = AmethystBorderLight,
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
