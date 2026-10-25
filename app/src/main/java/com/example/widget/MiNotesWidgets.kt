package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.notification.NotificationHelper
import com.example.ui.NoteKind

/** Cores dos widgets, as mesmas do tema do app (claro e escuro). */
private object WidgetColors {
    val background = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF282733))
    val title = ColorProvider(day = Color(0xFF1A1030), night = Color(0xFFF3F3F7))
    val body = ColorProvider(day = Color(0xFF5B5173), night = Color(0xFFA8A7B8))
    val accent = ColorProvider(day = Color(0xFF6D28D9), night = Color(0xFFA78BFA))
}

private fun newNoteIntent(context: Context, kind: NoteKind) =
    Intent(context, MainActivity::class.java).apply {
        action = MainActivity.ACTION_NEW_NOTE
        putExtra(MainActivity.EXTRA_NOTE_KIND, kind.name)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

private fun newTaskIntent(context: Context) =
    Intent(context, MainActivity::class.java).apply {
        action = MainActivity.ACTION_NEW_TASK
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

private fun openNoteIntent(context: Context, noteId: Long) =
    Intent(context, MainActivity::class.java).apply {
        // Ação própria para cada nota: sem isso o Android reaproveita o mesmo Intent entre widgets
        action = "com.example.action.OPEN_NOTE_$noteId"
        putExtra(NotificationHelper.EXTRA_OPEN_NOTE_ID, noteId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

// --- Widget "Criar rápido" (inspirado no widget de criação rápida do Keep) ---

class QuickAddWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetColors.background)
                    .cornerRadius(20.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuickAddButton(R.drawable.ic_shortcut_note, "Nota", newNoteIntent(context, NoteKind.TEXT))
                QuickAddButton(R.drawable.ic_shortcut_list, "Lista", newNoteIntent(context, NoteKind.LIST))
                QuickAddButton(R.drawable.ic_shortcut_audio, "Áudio", newNoteIntent(context, NoteKind.AUDIO))
                QuickAddButton(R.drawable.ic_shortcut_task, "Tarefa", newTaskIntent(context))
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun RowScope.QuickAddButton(icon: Int, label: String, intent: Intent) {
    Column(
        modifier = GlanceModifier
            .defaultWeight()
            .clickable(actionStartActivity(intent)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(provider = ImageProvider(icon), contentDescription = label, modifier = GlanceModifier.size(36.dp))
        Spacer(GlanceModifier.height(2.dp))
        Text(label, style = TextStyle(color = WidgetColors.title, fontSize = 12.sp, fontWeight = FontWeight.Medium))
    }
}

class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
}

// --- Widget "Nota": uma nota fixa na tela inicial (inspirado no Keep e no ColorNote) ---

class NoteWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val noteId = NoteWidgetPrefs.getNoteId(context, appWidgetId)
        val note = if (noteId > 0) {
            AppDatabase.getDatabase(context).productivityDao().getNoteById(noteId)
        } else {
            null
        }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetColors.background)
                    .cornerRadius(20.dp)
                    .padding(14.dp)
                    .let { if (note != null) it.clickable(actionStartActivity(openNoteIntent(context, note.id))) else it }
            ) {
                when {
                    note == null || note.isDeleted -> WidgetMessage("Nota indisponível", "Remova e adicione o widget de novo.")
                    note.isLocked -> WidgetMessage(note.title.ifBlank { "Nota" }, "Nota protegida com PIN. Toque para abrir.")
                    else -> {
                        if (note.title.isNotBlank()) {
                            Text(
                                note.title,
                                maxLines = 2,
                                style = TextStyle(color = WidgetColors.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            )
                            Spacer(GlanceModifier.height(6.dp))
                        }
                        val lines = note.content.lines().map { line ->
                            when {
                                line.startsWith("[x] ") || line.startsWith("- [x] ") -> "☑ " + line.substringAfter("] ")
                                line.startsWith("[ ] ") || line.startsWith("- [ ] ") -> "☐ " + line.substringAfter("] ")
                                else -> line
                            }
                        }
                        LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                            items(lines.size) { index ->
                                Text(
                                    lines[index],
                                    style = TextStyle(color = WidgetColors.body, fontSize = 13.sp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        suspend fun refreshAll(context: Context) {
            runCatching { NoteWidget().updateAll(context) }
        }
    }
}

@androidx.compose.runtime.Composable
private fun WidgetMessage(title: String, body: String) {
    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = TextStyle(color = WidgetColors.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold))
            Spacer(GlanceModifier.height(4.dp))
            Text(body, style = TextStyle(color = WidgetColors.body, fontSize = 12.sp))
        }
    }
}

class NoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NoteWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { NoteWidgetPrefs.remove(context, it) }
    }
}

/** Qual nota cada widget mostra. */
object NoteWidgetPrefs {
    private const val PREFS = "note_widgets"

    fun getNoteId(context: Context, appWidgetId: Int): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("widget_$appWidgetId", -1L)

    fun setNoteId(context: Context, appWidgetId: Int, noteId: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong("widget_$appWidgetId", noteId).apply()
    }

    fun remove(context: Context, appWidgetId: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove("widget_$appWidgetId").apply()
    }
}
