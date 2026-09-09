package com.example.ui.components

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmethystCardLight
import com.example.ui.theme.ObsidianSurfaceDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextPrimaryLight

data class NoteColor(
    val hex: String,
    val name: String,
    val lightColor: Color,
    val darkColor: Color
)

object ColorPalette {
    val options = listOf(
        NoteColor("#FFFFFF", "Padrão", AmethystCardLight, ObsidianSurfaceDark),
        NoteColor("#F3E8FF", "Ametista", Color(0xFFFAF5FF), Color(0xFF26163F)),
        NoteColor("#EDE9FE", "Lavanda", Color(0xFFF5F3FF), Color(0xFF1E1338)),
        NoteColor("#DCFCE7", "Esmeralda", Color(0xFFF0FDF4), Color(0xFF132B1E)),
        NoteColor("#DBEAFE", "Safira", Color(0xFFEFF6FF), Color(0xFF152243)),
        NoteColor("#FEF3C7", "Âmbar", Color(0xFFFFFBEB), Color(0xFF332612)),
        NoteColor("#FCE7F3", "Quartzo", Color(0xFFFDF2F8), Color(0xFF37162C))
    )

    fun getSurfaceColor(hex: String, isDark: Boolean): Color {
        val found = options.find { it.hex.equals(hex, ignoreCase = true) }
        return if (found != null) {
            if (isDark) found.darkColor else found.lightColor
        } else {
            if (isDark) ObsidianSurfaceDark else AmethystCardLight
        }
    }

    /**
     * WCAG 2.1 relative luminance calculation to select optimal high-contrast text color.
     */
    fun getOptimalTextColor(backgroundColor: Color): Color {
        val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
        return if (luminance > 0.5) TextPrimaryLight else TextPrimaryDark
    }

    fun getOptimalTextColor(hex: String, isDark: Boolean): Color {
        val bg = getSurfaceColor(hex, isDark)
        return getOptimalTextColor(bg)
    }
}

