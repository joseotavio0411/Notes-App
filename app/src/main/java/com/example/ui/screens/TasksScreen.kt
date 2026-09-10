package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.DateTimeHelper
import com.example.ui.components.DeliberateEmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    onAddTask: (text: String, isPinned: Boolean, recurrence: String) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onTogglePinTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    var newTaskText by remember { mutableStateOf("") }
    var newTaskIsPinned by remember { mutableStateOf(false) }
    var newTaskRecurrence by remember { mutableStateOf("NONE") }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    // Dialog for adding a new task
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddTaskDialog = false
                newTaskText = ""
                newTaskIsPinned = false
                newTaskRecurrence = "NONE"
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nova Tarefa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { newTaskIsPinned = !newTaskIsPinned },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (newTaskIsPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (newTaskIsPinned) "Desafixar tarefa" else "Fixar no topo",
                            tint = if (newTaskIsPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        placeholder = { Text("O que precisa ser feito?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_text_input"),
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newTaskText.isNotBlank()) {
                                    onAddTask(newTaskText.trim(), newTaskIsPinned, newTaskRecurrence)
                                    newTaskText = ""
                                    newTaskIsPinned = false
                                    newTaskRecurrence = "NONE"
                                    showAddTaskDialog = false
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Repetição / Recorrência",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("NONE" to "Nenhuma", "DAILY" to "Diária").forEach { (code, label) ->
                                val isSelected = newTaskRecurrence == code
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { newTaskRecurrence = code },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("WEEKLY" to "Semanal", "MONTHLY" to "Mensal").forEach { (code, label) ->
                                val isSelected = newTaskRecurrence == code
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { newTaskRecurrence = code },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    if (newTaskIsPinned) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📌 Esta tarefa será fixada no topo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskText.isNotBlank()) {
                            onAddTask(newTaskText.trim(), newTaskIsPinned, newTaskRecurrence)
                            newTaskText = ""
                            newTaskIsPinned = false
                            newTaskRecurrence = "NONE"
                            showAddTaskDialog = false
                        }
                    },
                    enabled = newTaskText.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_task_button")
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddTaskDialog = false
                        newTaskText = ""
                        newTaskIsPinned = false
                        newTaskRecurrence = "NONE"
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog for confirming deletion when clicking the trash icon
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Excluir Tarefa", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente excluir \"${task.text}\"? Você também poderá desfazer na notificação.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTask(task)
                        taskToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val rawProgress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 400),
        label = "task_progress_anim"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Progress summary if there are tasks (clean and at the very top)
            if (tasks.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progresso ($completedCount/$totalCount)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Checklist of tasks
            if (tasks.isEmpty()) {
                DeliberateEmptyState(
                    title = "Tudo em dia!",
                    description = "Toque no botão \"+\" no canto inferior direito para começar sua lista.",
                    icon = Icons.Default.TaskAlt,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val pinnedTasks = tasks.filter { it.isPinned }
                val otherTasks = tasks.filter { !it.isPinned }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pinnedTasks.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FIXADAS",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        items(pinnedTasks, key = { it.id }) { task ->
                            TaskItemRow(
                                task = task,
                                onToggle = { onToggleTask(task) },
                                onTogglePin = { onTogglePinTask(task) },
                                onRequestDelete = { taskToDelete = task },
                                onSwipeDelete = { onDeleteTask(task) },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(150),
                                    fadeOutSpec = tween(150),
                                    placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            )
                        }

                        if (otherTasks.isNotEmpty()) {
                            item {
                                Text(
                                    text = "OUTRAS",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }

                    items(otherTasks, key = { it.id }) { task ->
                        TaskItemRow(
                            task = task,
                            onToggle = { onToggleTask(task) },
                            onTogglePin = { onTogglePinTask(task) },
                            onRequestDelete = { taskToDelete = task },
                            onSwipeDelete = { onDeleteTask(task) },
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(150),
                                fadeOutSpec = tween(150),
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        )
                    }
                }
            }
        }

        // Minimalist circular Floating Action Button with app theme color
        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_task")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar Tarefa",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItemRow(
    task: TaskEntity,
    onToggle: () -> Unit,
    onTogglePin: () -> Unit,
    onRequestDelete: () -> Unit,
    onSwipeDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val containerColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "taskContainerColor"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) 6.dp else if (task.isPinned) 2.dp else 1.dp,
        label = "task_press_elevation"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        label = "task_press_scale"
    )

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                    false // Don't dismiss off screen, bounce back
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSwipeDelete()
                    true // Dismiss card off screen
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.fillMaxWidth(),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val backgroundColor by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                },
                label = "swipeBgColor"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Concluir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (task.isCompleted) "Reabrir" else "Concluir",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Excluir",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                }
                .testTag("task_item_${task.id}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle()
                    },
                    modifier = Modifier.testTag("task_checkbox_${task.id}"),
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = task.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    if (task.recurrence != "NONE") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = DateTimeHelper.getRecurrenceLabel(task.recurrence),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTogglePin()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = if (task.isPinned) "Desafixar tarefa" else "Fixar tarefa no topo" }
                ) {
                    Icon(
                        imageVector = if (task.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = if (task.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onRequestDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_task_button_${task.id}")
                        .semantics { contentDescription = "Excluir tarefa" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
