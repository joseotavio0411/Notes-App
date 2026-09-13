package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenReadingDialog(
    title: String,
    content: String,
    category: String,
    onDismiss: () -> Unit
) {
    var fontSizeSp by remember { mutableFloatStateOf(18f) }

    val wordCount = remember(content) {
        if (content.isBlank()) 0 else content.split("\\s+".toRegex()).count { it.isNotBlank() }
    }
    val readingMinutes = (wordCount / 200).coerceAtLeast(1)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Modo Leitura Zen",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "$wordCount palavras • ~$readingMinutes min de leitura",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { if (fontSizeSp > 14f) fontSizeSp -= 2f },
                            modifier = Modifier.testTag("zen_decrease_font_button")
                        ) {
                            Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Diminuir fonte")
                        }
                        IconButton(
                            onClick = { if (fontSizeSp < 32f) fontSizeSp += 2f },
                            modifier = Modifier.testTag("zen_increase_font_button")
                        ) {
                            Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Aumentar fonte")
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("zen_close_button")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (title.isNotBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (fontSizeSp + 8).sp,
                            lineHeight = (fontSizeSp + 14).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = content.ifBlank { "Nota sem conteúdo." },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp * 1.6f).sp,
                        fontFamily = FontFamily.Default
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}
