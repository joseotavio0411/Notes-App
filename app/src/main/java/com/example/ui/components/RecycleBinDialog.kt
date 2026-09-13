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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.DeadlineNoteEntity
import com.example.data.local.entity.DeadlineTaskEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinDialog(
    deletedNotes: List<NoteEntity>,
    deletedTasks: List<TaskEntity>,
    deletedDeadlineTasks: List<DeadlineTaskEntity>,
    deletedDeadlineNotes: List<DeadlineNoteEntity>,
    onRestoreNote: (Long) -> Unit,
    onDeleteNotePermanently: (Long) -> Unit,
    onRestoreTask: (Long) -> Unit,
    onDeleteTaskPermanently: (Long) -> Unit,
    onRestoreDeadlineTask: (Long) -> Unit,
    onDeleteDeadlineTaskPermanently: (Long) -> Unit,
    onRestoreDeadlineNote: (Long) -> Unit,
    onDeleteDeadlineNotePermanently: (Long) -> Unit,
    onEmptyAllTrash: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showEmptyConfirmDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val totalTrashCount = deletedNotes.size + deletedTasks.size + deletedDeadlineTasks.size + deletedDeadlineNotes.size

    if (showEmptyConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirmDialog = false },
            title = { Text("Esvaziar Lixeira?") },
            text = { Text("Todos os itens na lixeira serão excluídos permanentemente e não poderão ser recuperados.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEmptyAllTrash()
                        showEmptyConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_empty_trash_button")
                ) {
                    Text("Esvaziar Tudo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Lixeira",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "$totalTrashCount itens arquivados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (totalTrashCount > 0) {
                        OutlinedButton(
                            onClick = { showEmptyConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("empty_trash_action_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Esvaziar", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 30-day notice card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ℹ️ Os itens na lixeira há mais de 30 dias são apagados automaticamente.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs
                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Notas (${deletedNotes.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Tarefas (${deletedTasks.size})") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Prazos (${deletedDeadlineTasks.size + deletedDeadlineNotes.size})") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> {
                            if (deletedNotes.isEmpty()) {
                                EmptyTrashView("Nenhuma nota na lixeira")
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(deletedNotes, key = { "note_${it.id}" }) { note ->
                                        TrashItemCard(
                                            title = note.title.ifBlank { "Nota sem título" },
                                            subtitle = note.content.take(60),
                                            deletedAtFormatted = if (note.deletedAt > 0) dateFormat.format(Date(note.deletedAt)) else "Recentemente",
                                            onRestore = { onRestoreNote(note.id) },
                                            onPermanentDelete = { onDeleteNotePermanently(note.id) }
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (deletedTasks.isEmpty()) {
                                EmptyTrashView("Nenhuma tarefa na lixeira")
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(deletedTasks, key = { "task_${it.id}" }) { task ->
                                        TrashItemCard(
                                            title = task.text,
                                            subtitle = if (task.isCompleted) "Tarefa concluída" else "Pendente",
                                            deletedAtFormatted = if (task.deletedAt > 0) dateFormat.format(Date(task.deletedAt)) else "Recentemente",
                                            onRestore = { onRestoreTask(task.id) },
                                            onPermanentDelete = { onDeleteTaskPermanently(task.id) }
                                        )
                                    }
                                }
                            }
                        }
                        2 -> {
                            val allDeadlineItemsCount = deletedDeadlineTasks.size + deletedDeadlineNotes.size
                            if (allDeadlineItemsCount == 0) {
                                EmptyTrashView("Nenhum item com prazo na lixeira")
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(deletedDeadlineTasks, key = { "dl_task_${it.id}" }) { task ->
                                        TrashItemCard(
                                            title = task.text,
                                            subtitle = "Tarefa com prazo: ${dateFormat.format(Date(task.deadlineTimestamp))}",
                                            deletedAtFormatted = if (task.deletedAt > 0) dateFormat.format(Date(task.deletedAt)) else "Recentemente",
                                            onRestore = { onRestoreDeadlineTask(task.id) },
                                            onPermanentDelete = { onDeleteDeadlineTaskPermanently(task.id) }
                                        )
                                    }
                                    items(deletedDeadlineNotes, key = { "dl_note_${it.id}" }) { note ->
                                        TrashItemCard(
                                            title = note.title.ifBlank { "Nota com prazo" },
                                            subtitle = "Prazo: ${dateFormat.format(Date(note.deadlineTimestamp))}",
                                            deletedAtFormatted = if (note.deletedAt > 0) dateFormat.format(Date(note.deletedAt)) else "Recentemente",
                                            onRestore = { onRestoreDeadlineNote(note.id) },
                                            onPermanentDelete = { onDeleteDeadlineNotePermanently(note.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Close Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).testTag("close_recycle_bin_button")
                ) {
                    Text("Fechar")
                }
            }
        }
    }
}

@Composable
private fun TrashItemCard(
    title: String,
    subtitle: String,
    deletedAtFormatted: String,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = "Excluído: $deletedAtFormatted",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onPermanentDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excluir", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = onRestore
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restaurar", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyTrashView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
