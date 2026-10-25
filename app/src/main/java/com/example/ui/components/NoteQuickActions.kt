package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.local.entity.NoteEntity
import com.example.ui.NoteKind
import com.example.ui.NoteSortOrder
import java.io.File
import java.util.Calendar

// --- Botão "+" que se expande (inspirado no Google Keep) ---

private data class FabOption(val kind: NoteKind, val label: String, val icon: ImageVector)

private val fabOptions = listOf(
    FabOption(NoteKind.AUDIO, "Áudio", Icons.Default.Mic),
    FabOption(NoteKind.IMAGE, "Imagem", Icons.Default.Image),
    FabOption(NoteKind.LIST, "Lista", Icons.Default.CheckBox),
    FabOption(NoteKind.TEXT, "Texto", Icons.Default.TextFields)
)

/**
 * Véu escuro atrás do menu aberto; toque fora fecha. Fica separado do botão para
 * poder cobrir a tela toda enquanto o botão continua no canto.
 */
@Composable
fun ExpandableFabScrim(expanded: Boolean, onDismiss: () -> Unit) {
    AnimatedVisibility(visible = expanded, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
    }
}

@Composable
fun ExpandableNoteFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (NoteKind) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = expanded) { onExpandedChange(false) }
    val rotation by animateFloatAsState(if (expanded) 45f else 0f, label = "fab_rotation")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        fabOptions.forEachIndexed { index, option ->
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + scaleIn(initialScale = 0.6f) + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + scaleOut(targetScale = 0.6f) + shrinkVertically(shrinkTowards = Alignment.Bottom)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        onClick = {
                            onExpandedChange(false)
                            onSelect(option.kind)
                        }
                    ) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            onExpandedChange(false)
                            onSelect(option.kind)
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("fab_option_${option.kind.name.lowercase()}_$index")
                    ) {
                        Icon(option.icon, contentDescription = option.label)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier.testTag("fab_add_note")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (expanded) "Fechar opções" else "Criar nota",
                modifier = Modifier
                    .size(28.dp)
                    .rotate(rotation)
            )
        }
    }
}

// --- Ordenação ---

@Composable
fun NoteSortMenuButton(
    sortOrder: NoteSortOrder,
    onSortOrderChange: (NoteSortOrder) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.testTag("sort_notes_button")) {
            Icon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Ordenar: ${sortOrder.label}",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            Text(
                text = "Ordenar por",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            NoteSortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = {
                        Text(
                            order.label,
                            fontWeight = if (order == sortOrder) FontWeight.Bold else FontWeight.Normal,
                            color = if (order == sortOrder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        open = false
                        onSortOrderChange(order)
                    }
                )
            }
        }
    }
}

// --- Seleção múltipla ---

@Composable
fun NoteSelectionBar(
    selectedCount: Int,
    allSelectedPinned: Boolean,
    isArchiveView: Boolean,
    isDarkTheme: Boolean,
    onClear: () -> Unit,
    onTogglePin: () -> Unit,
    onColor: (String) -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showColors by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("note_selection_bar")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Close, contentDescription = "Cancelar seleção", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Text(
                text = if (selectedCount == 1) "1 selecionada" else "$selectedCount selecionadas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            if (!isArchiveView) {
                IconButton(onClick = onTogglePin) {
                    Icon(
                        if (allSelectedPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (allSelectedPinned) "Desafixar" else "Fixar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Box {
                IconButton(onClick = { showColors = true }) {
                    Icon(Icons.Default.Palette, contentDescription = "Mudar cor", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                DropdownMenu(expanded = showColors, onDismissRequest = { showColors = false }) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        ColorPalette.options.forEach { option ->
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkTheme) option.darkColor else option.lightColor)
                                    .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                                    .clickable {
                                        showColors = false
                                        onColor(option.hex)
                                    }
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onArchiveToggle) {
                Icon(
                    if (isArchiveView) Icons.Default.Unarchive else Icons.Default.Archive,
                    contentDescription = if (isArchiveView) "Desarquivar" else "Arquivar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Mover para a lixeira", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// --- Lembrete rápido (atalhos estilo Keep) ---

private data class ReminderPreset(val label: String, val detail: String, val icon: ImageVector, val at: Long)

private fun reminderPresets(now: Long = System.currentTimeMillis()): List<ReminderPreset> {
    fun at(daysAhead: Int, hour: Int): Long = Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.DAY_OF_YEAR, daysAhead)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val presets = mutableListOf(
        ReminderPreset("Em 15 minutos", DateTimeHelper.formatTimeOnly(now + 15 * 60_000L), Icons.Default.Schedule, now + 15 * 60_000L),
        ReminderPreset("Em 1 hora", DateTimeHelper.formatTimeOnly(now + 60 * 60_000L), Icons.Default.Schedule, now + 60 * 60_000L)
    )
    val laterToday = at(0, 18)
    if (laterToday - now > 60 * 60_000L) {
        presets += ReminderPreset("Mais tarde hoje", "18:00", Icons.Default.WbTwilight, laterToday)
    }
    presets += ReminderPreset("Amanhã de manhã", "08:00", Icons.Default.WbSunny, at(1, 8))
    presets += ReminderPreset("Amanhã à noite", "18:00", Icons.Default.WbTwilight, at(1, 18))
    return presets
}

@Composable
fun ReminderPickerDialog(
    currentReminderAt: Long,
    onSet: (Long) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val presets = remember { reminderPresets() }
    val hasReminder = currentReminderAt > System.currentTimeMillis()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lembrar-me", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (hasReminder) {
                    Text(
                        text = "Atual: ${DateTimeHelper.formatDateTime(currentReminderAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                presets.forEach { preset ->
                    ReminderOptionRow(preset.icon, preset.label, preset.detail) { onSet(preset.at) }
                }
                ReminderOptionRow(Icons.Default.Event, "Escolher data e hora…", "") {
                    DateTimeHelper.showDateTimePicker(context) { millis ->
                        if (millis > System.currentTimeMillis()) onSet(millis)
                        else Toast.makeText(context, "Escolha um horário no futuro", Toast.LENGTH_SHORT).show()
                    }
                }
                if (hasReminder) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    ReminderOptionRow(Icons.Default.NotificationsOff, "Remover lembrete", "", tint = MaterialTheme.colorScheme.error) {
                        onRemove()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ReminderOptionRow(
    icon: ImageVector,
    label: String,
    detail: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (tint == MaterialTheme.colorScheme.error) tint else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (detail.isNotBlank()) {
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// --- Compartilhar e exportar (inspirado no ColorNote e no Notein) ---

object NoteSharing {

    /** Texto legível: caixas de seleção viram ☐/☑ e o título vai na primeira linha. */
    fun toPlainText(title: String, content: String): String {
        val body = content.lines().joinToString("\n") { line ->
            when {
                line.startsWith("[x] ") || line.startsWith("- [x] ") -> "☑ " + line.substringAfter("] ")
                line.startsWith("[ ] ") || line.startsWith("- [ ] ") -> "☐ " + line.substringAfter("] ")
                else -> line
            }
        }
        return if (title.isBlank()) body else "$title\n\n$body"
    }

    fun shareAsText(context: Context, note: NoteEntity) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, note.title)
            putExtra(Intent.EXTRA_TEXT, toPlainText(note.title, note.content))
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar nota"))
    }

    fun exportPdf(context: Context, note: NoteEntity) {
        val file = runCatching { writePdf(context, note) }.getOrNull()
        if (file == null) {
            Toast.makeText(context, "Não foi possível gerar o PDF", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, note.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exportar PDF"))
    }

    private fun writePdf(context: Context, note: NoteEntity): File {
        val pageWidth = 595 // A4 em pontos
        val pageHeight = 842
        val margin = 48
        val contentWidth = pageWidth - margin * 2

        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = AndroidColor.rgb(0x1A, 0x10, 0x30)
        }
        val bodyPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 12f
            color = AndroidColor.rgb(0x1A, 0x10, 0x30)
        }
        val footerPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 9f
            color = AndroidColor.rgb(0x5B, 0x51, 0x73)
        }

        fun layout(text: String, paint: TextPaint) =
            StaticLayout.Builder.obtain(text, 0, text.length, paint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.25f)
                .build()

        val document = PdfDocument()
        val title = note.title.ifBlank { "Nota" }
        val body = toPlainText("", note.content).trim()
        val titleLayout = layout(title, titlePaint)
        val bodyLayout = layout(body, bodyPaint)

        var pageNumber = 0
        var line = 0
        while (pageNumber == 0 || line < bodyLayout.lineCount) {
            pageNumber++
            val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            val canvas = page.canvas
            var y = margin.toFloat()
            if (pageNumber == 1) {
                canvas.save()
                canvas.translate(margin.toFloat(), y)
                titleLayout.draw(canvas)
                canvas.restore()
                y += titleLayout.height + 16f
            }
            // Desenha as linhas do corpo que cabem nesta página
            val available = pageHeight - margin - 24 - y
            val firstLine = line
            while (line < bodyLayout.lineCount &&
                bodyLayout.getLineBottom(line) - bodyLayout.getLineTop(firstLine) <= available
            ) {
                line++
            }
            if (line == firstLine && line < bodyLayout.lineCount) line++ // linha maior que a página
            if (line > firstLine) {
                val top = bodyLayout.getLineTop(firstLine)
                val bottom = bodyLayout.getLineBottom(line - 1)
                canvas.save()
                canvas.translate(margin.toFloat(), y - top)
                canvas.clipRect(0f, top.toFloat(), contentWidth.toFloat(), bottom.toFloat())
                bodyLayout.draw(canvas)
                canvas.restore()
            }
            canvas.drawText("MiNotes · página $pageNumber", margin.toFloat(), (pageHeight - margin / 2).toFloat(), footerPaint)
            document.finishPage(page)
        }

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = title.replace(Regex("[^\\p{L}\\p{N} _-]"), "").trim().take(40).ifBlank { "nota" }
        val file = File(dir, "$safeName.pdf")
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }
}
