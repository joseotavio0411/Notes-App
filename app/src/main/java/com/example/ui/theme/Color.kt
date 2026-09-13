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

// --- SUPERFÍCIES DARK (Cinza Grafite/Slate com Roxo) ---
val SlateBackgroundDark = Color(0xFF1E1D24)      // Fundo principal em cinza grafite elegante (substitui o preto absoluto)
val SlateSurfaceDark = Color(0xFF282733)         // Cartões e superfícies elevadas em cinza médio refinado
val SlateContainerDark = Color(0xFF353444)       // Containers internos, inputs e chips inativos
val SlateBorderDark = Color(0xFF48455A)          // Borda sutil de 1dp em cinza com matiz roxo
val SlatePrimaryContainerDark = Color(0xFF3F3556)// Container de destaque roxo-acinzentado elegante
val SlateOnPrimaryContainerDark = Color(0xFFE2D4FD) // Texto e ícones em containers com destaque roxo

// Alias mantidos para compatibilidade
val ObsidianVoidDark = SlateBackgroundDark
val ObsidianSurfaceDark = SlateSurfaceDark
val ObsidianContainerDark = SlateContainerDark
val ObsidianBorderDark = SlateBorderDark

// --- TIPOGRAFIA DE ALTO CONTRASTE ---
val TextPrimaryLight = Color(0xFF150A2A)         // Preto enriquecido com matiz violeta profundo
val TextSecondaryLight = Color(0xFF63567D)       // Cinza ametista para legendas e datas
val TextPrimaryDark = Color(0xFFF3F3F7)          // Branco-cinza suave de alta legibilidade sobre cinza
val TextSecondaryDark = Color(0xFFA8A7B8)        // Cinza neutro suave para legendas e informações secundárias

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
val PurpleOnPrimaryDark = SlateBackgroundDark
val PurplePrimaryContainerDark = SlatePrimaryContainerDark
val PurpleOnPrimaryContainerDark = SlateOnPrimaryContainerDark

val SurfaceLight = AmethystSurfaceLight
val SurfaceDark = SlateBackgroundDark
val CardSurfaceLight = AmethystCardLight
val CardSurfaceDark = SlateSurfaceDark



