package com.example.data.model

import androidx.compose.ui.graphics.Color

object CategoryHelper {
    val defaultCategories = listOf("Geral", "Trabalho", "Pessoal", "Estudos", "Finanças", "Ideias")

    fun getCategoryColor(category: String): Color {
        return when (category.trim().lowercase()) {
            "trabalho" -> Color(0xFF2563EB) // Blue
            "pessoal" -> Color(0xFF059669) // Emerald
            "estudos" -> Color(0xFF7C3AED) // Violet
            "finanças", "financas" -> Color(0xFFD97706) // Amber
            "ideias" -> Color(0xFFDB2777) // Pink
            "saúde", "saude" -> Color(0xFFE11D48) // Rose
            "urgente" -> Color(0xFFDC2626) // Red
            else -> Color(0xFF4B5563) // Slate/Gray
        }
    }

    fun getCategoryContainerColor(category: String): Color {
        return when (category.trim().lowercase()) {
            "trabalho" -> Color(0xFFDBEAFE)
            "pessoal" -> Color(0xFFD1FAE5)
            "estudos" -> Color(0xFFEDE9FE)
            "finanças", "financas" -> Color(0xFFFEF3C7)
            "ideias" -> Color(0xFFFCE7F3)
            "saúde", "saude" -> Color(0xFFFFE4E6)
            "urgente" -> Color(0xFFFEE2E2)
            else -> Color(0xFFF3F4F6)
        }
    }
}
