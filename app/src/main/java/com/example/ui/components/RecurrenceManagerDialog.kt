package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.DeadlineNoteEntity
import com.example.data.local.entity.DeadlineTaskEntity
import com.example.data.local.entity.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class RecurringItem {
    data class DeadlineTask(val task: DeadlineTaskEntity) : RecurringItem()
    data class DeadlineNote(val note: DeadlineNoteEntity) : RecurringItem()
    data class SimpleTask(val task: TaskEntity) : RecurringItem()
}

@Composable
fun RecurrenceManagerDialog(
    deadlineTasks: List<DeadlineTaskEntity>,
    deadlineNotes: List<DeadlineNoteEntity>,
    simpleTasks: List<TaskEntity> = emptyList(),
    currentTime: Long,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onReopenDeadlineTask: (DeadlineTaskEntity) -> Unit,
    onReopenDeadlineNote: (DeadlineNoteEntity) -> Unit,
    onReopenTask: (TaskEntity) -> Unit,
    onDeleteDeadlineTask: (DeadlineTaskEntity) -> Unit,
    onDeleteDeadlineNote: (DeadlineNoteEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val recurringDeadlineTasks = remember(deadlineTasks) {
        deadlineTasks.filter { it.recurrence != "NONE" && it.recurrence.isNotBlank() }
    }
    val recurringDeadlineNotes = remember(deadlineNotes) {
        deadlineNotes.filter { it.recurrence != "NONE" && it.recurrence.isNotBlank() }
    }
    val recurringSimpleTasks = remember(simpleTasks) {
        simpleTasks.filter { it.recurrence != "NONE" && it.recurrence.isNotBlank() }
    }

    val allRecurringItems = remember(recurringDeadlineTasks, recurringDeadlineNotes, recurringSimpleTasks) {
        val list = mutableListOf<RecurringItem>()
        recurringDeadlineTasks.forEach { list.add(RecurringItem.DeadlineTask(it)) }
        recurringDeadlineNotes.forEach { list.add(RecurringItem.DeadlineNote(it)) }
        recurringSimpleTasks.forEach { list.add(RecurringItem.SimpleTask(it)) }
        list
    }

    val filteredItems = remember(allRecurringItems, selectedFilter) {
        when (selectedFilter) {
            "TASKS" -> allRecurringItems.filterIsInstance<RecurringItem.DeadlineTask>()
            "NOTES" -> allRecurringItems.filterIsInstance<RecurringItem.DeadlineNote>()
            "SIMPLE" -> allRecurringItems.filterIsInstance<RecurringItem.SimpleTask>()
            else -> allRecurringItems
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("recurrence_manager_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Gerenciar Recorrências",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Itens com ciclo diário, semanal ou mensal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("Todas (${allRecurringItems.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "TASKS",
                        onClick = { selectedFilter = "TASKS" },
                        label = { Text("Tarefas (${recurringDeadlineTasks.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "NOTES",
                        onClick = { selectedFilter = "NOTES" },
                        label = { Text("Notas (${recurringDeadlineNotes.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Content list
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "Nenhum item com recorrência",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Crie uma tarefa ou nota com prazo e defina recorrência diária, semanal ou mensal para vê-la aqui.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredItems) { item ->
                            when (item) {
                                is RecurringItem.DeadlineTask -> {
                                    val task = item.task
                                    val isWaitingNextPeriod = task.visibleFrom > currentTime
                                    val isDelivered = task.isCompleted

                                    RecurringTaskCard(
                                        title = task.text,
                                        recurrence = task.recurrence,
                                        category = task.category,
                                        deadlineMillis = task.deadlineTimestamp,
                                        visibleFrom = task.visibleFrom,
                                        isCompleted = isDelivered,
                                        isWaitingNextPeriod = isWaitingNextPeriod,
                                        currentTime = currentTime,
                                        dateFormatter = dateFormatter,
                                        onReopen = { onReopenDeadlineTask(task) },
                                        onDelete = { onDeleteDeadlineTask(task) }
                                    )
                                }
                                is RecurringItem.DeadlineNote -> {
                                    val note = item.note
                                    val isWaitingNextPeriod = note.visibleFrom > currentTime
                                    val isPastDeadline = note.deadlineTimestamp < currentTime

                                    RecurringNoteCard(
                                        title = note.title.ifBlank { "Nota com prazo" },
                                        content = note.content,
                                        recurrence = note.recurrence,
                                        category = note.category,
                                        deadlineMillis = note.deadlineTimestamp,
                                        visibleFrom = note.visibleFrom,
                                        isPastDeadline = isPastDeadline,
                                        isWaitingNextPeriod = isWaitingNextPeriod,
                                        dateFormatter = dateFormatter,
                                        onReopen = { onReopenDeadlineNote(note) },
                                        onDelete = { onDeleteDeadlineNote(note) }
                                    )
                                }
                                is RecurringItem.SimpleTask -> {
                                    val task = item.task
                                    val isWaitingNextPeriod = task.visibleFrom > currentTime

                                    RecurringTaskCard(
                                        title = task.text,
                                        recurrence = task.recurrence,
                                        category = task.category,
                                        deadlineMillis = null,
                                        visibleFrom = task.visibleFrom,
                                        isCompleted = task.isCompleted,
                                        isWaitingNextPeriod = isWaitingNextPeriod,
                                        currentTime = currentTime,
                                        dateFormatter = dateFormatter,
                                        onReopen = { onReopenTask(task) },
                                        onDelete = { onDeleteTask(task) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecurringTaskCard(
    title: String,
    recurrence: String,
    category: String,
    deadlineMillis: Long?,
    visibleFrom: Long,
    isCompleted: Boolean,
    isWaitingNextPeriod: Boolean,
    currentTime: Long,
    dateFormatter: SimpleDateFormat,
    onReopen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tarefa",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = DateTimeHelper.getRecurrenceLabel(recurrence),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (category.isNotBlank() && category != "Geral") {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status message & Reopen button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    when {
                        isCompleted -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Entregue / Concluída neste ciclo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        isWaitingNextPeriod -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Update,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reabre em ${dateFormatter.format(Date(visibleFrom))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        deadlineMillis != null -> {
                            Text(
                                text = "Ativa • Prazo: ${dateFormatter.format(Date(deadlineMillis))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> {
                            Text(
                                text = "Ativa na lista",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // If delivered or waiting next cycle, allow instant reopening without waiting!
                if (isCompleted || isWaitingNextPeriod) {
                    FilledTonalButton(
                        onClick = onReopen,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reabrir agora",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecurringNoteCard(
    title: String,
    content: String,
    recurrence: String,
    category: String,
    deadlineMillis: Long,
    visibleFrom: Long,
    isPastDeadline: Boolean,
    isWaitingNextPeriod: Boolean,
    dateFormatter: SimpleDateFormat,
    onReopen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Nota",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = DateTimeHelper.getRecurrenceLabel(recurrence),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (category.isNotBlank() && category != "Geral") {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (content.isNotBlank()) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (isPastDeadline || isWaitingNextPeriod) {
                        Text(
                            text = if (isWaitingNextPeriod) "Reabre em ${dateFormatter.format(Date(visibleFrom))}"
                                else "Ciclo anterior encerrado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "Ativa • Prazo: ${dateFormatter.format(Date(deadlineMillis))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isPastDeadline || isWaitingNextPeriod) {
                    FilledTonalButton(
                        onClick = onReopen,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reabrir agora",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
