package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// --- PALETA HERO / PRIMÁRIA ---
val VioletRoyal = Color(0xFF7C3AED)          // Ação primária vibrante (Light mode) - WCAG AA > 4.8:1
val VioletRoyalDark = Color(0xFFA78BFA)      // Luminescência primária para Dark Mode
val VioletNeon = Color(0xFF956AFA)           // Destaque vibrante / acentos elétricos (Rules UI)
val VioletDeepBase = Color(0xFF5B21B6)       // Fundo de botões pressionados / active state

// --- SUPERFÍCIES LIGHT (Névoa Lilás Perolada) ---
val AmethystSurfaceLight = Color(0xFFFAF8FF)     // Fundo da janela (substitui o branco estéril)
val AmethystCardLight = Color(0xFFFFFFFF)        // Cartões elevados em branco puro
val AmethystContainerLight = Color(0xFFF3EDFD)   // Pílulas, chips e inputs inativos
val AmethystBorderLight = Color(0xFFE5DCF9)      // Hairline stroke sutil de 1dp

// --- SUPERFÍCIES DARK (Twilight Obsidian & Ametista Profunda) ---
val ObsidianVoidDark = Color(0xFF0A0713)         // Fundo da janela (profundo para telas OLED)
val ObsidianSurfaceDark = Color(0xFF140D24)      // Cards e superfícies de trabalho elevadas
val ObsidianContainerDark = Color(0xFF1F1438)    // Containers internos e campos de input
val ObsidianBorderDark = Color(0xFF2E1F52)       // Hairline border translúcida de 1dp

// --- TIPOGRAFIA DE ALTO CONTRASTE ---
val TextPrimaryLight = Color(0xFF150A2A)         // Preto enriquecido com matiz violeta profundo
val TextSecondaryLight = Color(0xFF63567D)       // Cinza ametista para legendas e datas
val TextPrimaryDark = Color(0xFFFAF8FF)          // Branco perolado suave
val TextSecondaryDark = Color(0xFFA99BC4)        // Lavanda acinzentado de fácil leitura

// --- SEMÂNTICA DE STATUS & PRAZOS (Harmonizados com o Roxo) ---
val StatusUrgentCrimson = Color(0xFFF43F5E)      // Prazo crítico (< 1h) - Contraste alto com violeta
val StatusUrgentContainer = Color(0xFFFFE4E6)
val StatusWarningAmber = Color(0xFFF59E0B)       // Prazo intermediário (< 24h)
val StatusWarningContainer = Color(0xFFFEF3C7)
val StatusSuccessEmerald = Color(0xFF10B981)     // Tarefa concluída / Checklist completo
val StatusSuccessContainer = Color(0xFFD1FAE5)

// Alias mantidos para retrocompatibilidade onde chamados diretamente
val PurplePrimaryLight = VioletRoyal
val PurpleOnPrimaryLight = Color.White
val PurplePrimaryContainerLight = AmethystContainerLight
val PurpleOnPrimaryContainerLight = VioletDeepBase

val PurplePrimaryDark = VioletRoyalDark
val PurpleOnPrimaryDark = ObsidianVoidDark
val PurplePrimaryContainerDark = ObsidianContainerDark
val PurpleOnPrimaryContainerDark = VioletRoyalDark

val SurfaceLight = AmethystSurfaceLight
val SurfaceDark = ObsidianVoidDark
val CardSurfaceLight = AmethystCardLight
val CardSurfaceDark = ObsidianSurfaceDark



