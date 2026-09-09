package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DeadlineTaskEntity
import com.example.ui.components.DateTimeHelper
import com.example.ui.components.DeliberateEmptyState
import com.example.ui.components.breathingGlow
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeadlineTasksScreen(
    tasks: List<DeadlineTaskEntity>,
    currentTime: Long,
    onAddTask: (text: String, deadlineMillis: Long, isPinned: Boolean) -> Unit,
    onToggleTask: (DeadlineTaskEntity) -> Unit,
    onTogglePinTask: (DeadlineTaskEntity) -> Unit,
    onDeleteTask: (DeadlineTaskEntity) -> Unit,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    var taskText by remember { mutableStateOf("") }
    var selectedDeadlineMillis by remember { mutableStateOf<Long?>(null) }
    var isPinnedInput by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<DeadlineTaskEntity?>(null) }

    // Dialog for adding a new deadline task
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                taskText = ""
                selectedDeadlineMillis = null
                isPinnedInput = false
                showValidationError = false
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nova Tarefa com Prazo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { isPinnedInput = !isPinnedInput },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPinnedInput) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Fixar tarefa",
                            tint = if (isPinnedInput) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = taskText,
                        onValueChange = {
                            taskText = it
                            showValidationError = false
                        },
                        placeholder = { Text("O que precisa ser feito?") },
                        label = { Text("Descrição") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deadline_task_input"),
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            DateTimeHelper.showDateTimePicker(
                                context = context,
                                initialMillis = selectedDeadlineMillis ?: (System.currentTimeMillis() + 3600000L)
                            ) { millis ->
                                selectedDeadlineMillis = millis
                                showValidationError = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pick_deadline_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedDeadlineMillis?.let { DateTimeHelper.formatDateTime(it) }
                                ?: "Definir Prazo *",
                            maxLines = 1
                        )
                    }

                    if (isPinnedInput) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📌 Esta tarefa será fixada no topo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (showValidationError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (taskText.isBlank()) "Digite a descrição da tarefa." else "Defina uma data e hora limite.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskText.isBlank() || selectedDeadlineMillis == null) {
                            showValidationError = true
                            return@Button
                        }
                        onAddTask(taskText.trim(), selectedDeadlineMillis!!, isPinnedInput)
                        taskText = ""
                        selectedDeadlineMillis = null
                        isPinnedInput = false
                        showValidationError = false
                        showAddDialog = false
                    },
                    modifier = Modifier.testTag("submit_deadline_task_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        taskText = ""
                        selectedDeadlineMillis = null
                        isPinnedInput = false
                        showValidationError = false
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
            title = { Text("Excluir Tarefa com Prazo", fontWeight = FontWeight.Bold) },
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Counter & status (placed directly at the top)
            val overdueCount = tasks.count { !it.isCompleted && it.deadlineTimestamp < currentTime }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tarefas com Prazo (${tasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (overdueCount > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "$overdueCount atrasada(s)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // List
            if (tasks.isEmpty()) {
                DeliberateEmptyState(
                    title = "Nenhuma tarefa com prazo pendente",
                    description = "Toque no botão \"+\" no canto inferior direito para adicionar uma tarefa com prazo.",
                    icon = Icons.Default.AccessTime,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val pinnedTasks = tasks.filter { it.isPinned }
                val otherTasks = tasks.filter { !it.isPinned }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            DeadlineTaskItemRow(
                                task = task,
                                currentTime = currentTime,
                                isDarkTheme = isDarkTheme,
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
                        DeadlineTaskItemRow(
                            task = task,
                            currentTime = currentTime,
                            isDarkTheme = isDarkTheme,
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
            onClick = { showAddDialog = true },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_deadline_task")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar Tarefa com Prazo",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeadlineTaskItemRow(
    task: DeadlineTaskEntity,
    currentTime: Long,
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    onTogglePin: () -> Unit,
    onRequestDelete: () -> Unit,
    onSwipeDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isOverdue = !task.isCompleted && task.deadlineTimestamp < currentTime
    val isCritical = !task.isCompleted && !isOverdue && (task.deadlineTimestamp - currentTime) in 1..(60 * 60 * 1000L)

    // Expired not completed: background/text highlight in RED!
    val containerColor = when {
        isOverdue -> if (isDarkTheme) Color(0xFF4C1D1D) else Color(0xFFFFECEC)
        task.isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isOverdue) {
        if (isDarkTheme) Color(0xFFEF4444) else Color(0xFFDC2626)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    val textColor = when {
        isOverdue -> if (isDarkTheme) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
        task.isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) 6.dp else if (task.isPinned) 3.dp else if (isOverdue) 2.dp else 1.dp,
        label = "deadline_task_press_elevation"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        label = "deadline_task_press_scale"
    )

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                    false // Don't dismiss, bounce back
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSwipeDelete()
                    true // Dismiss
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
                label = "deadlineSwipeBgColor"
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
                .breathingGlow(isCritical = isCritical)
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
                .testTag("deadline_task_${task.id}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(if (isOverdue) 1.5.dp else 1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggle()
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
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
                                fontWeight = if (isOverdue) FontWeight.SemiBold else FontWeight.Normal,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Deadline info row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isOverdue) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Atrasado",
                                    tint = if (isDarkTheme) Color(0xFFEF4444) else Color(0xFFDC2626),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Prazo esgotado! (${DateTimeHelper.getTimeRemainingDescription(task.deadlineTimestamp, currentTime)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) Color(0xFFEF4444) else Color(0xFFDC2626)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Prazo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${DateTimeHelper.formatDateTime(task.deadlineTimestamp)} • ${DateTimeHelper.getTimeRemainingDescription(task.deadlineTimestamp, currentTime)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
}
