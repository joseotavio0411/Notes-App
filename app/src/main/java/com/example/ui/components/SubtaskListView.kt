package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subtask
import com.example.data.model.SubtaskHelper

@Composable
fun TaskSubtasksSection(
    subtasksJson: String,
    onToggleSubtask: (String) -> Unit,
    onAddSubtask: (String) -> Unit,
    onRemoveSubtask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val subtasks = remember(subtasksJson) { SubtaskHelper.fromJson(subtasksJson) }
    if (subtasks.isEmpty()) return

    val completedCount = subtasks.count { it.isCompleted }
    val totalCount = subtasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    var isExpanded by remember { mutableStateOf(false) }
    var newSubtaskText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // Progress header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .width(90.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "$completedCount/$totalCount subtarefas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.size(24.dp).testTag("expand_subtasks_button")
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Ocultar subtarefas" else "Mostrar subtarefas",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Subtasks checklist items
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                subtasks.forEach { subtask ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = subtask.isCompleted,
                            onCheckedChange = { onToggleSubtask(subtask.id) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("subtask_checkbox_${subtask.id}"),
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = subtask.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else null,
                                color = if (subtask.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onRemoveSubtask(subtask.id) },
                            modifier = Modifier.size(24.dp).testTag("remove_subtask_${subtask.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remover subtarefa",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Inline quick add
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSubtaskText,
                        onValueChange = { newSubtaskText = it },
                        placeholder = { Text("+ Adicionar subtarefa...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("inline_add_subtask_input")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (newSubtaskText.isNotBlank()) {
                                onAddSubtask(newSubtaskText)
                                newSubtaskText = ""
                            }
                        },
                        modifier = Modifier.size(36.dp).testTag("inline_confirm_add_subtask_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Editor component for creating or modifying subtasks inside task dialogs
 */
@Composable
fun SubtaskEditorSection(
    subtasks: List<Subtask>,
    onSubtasksChange: (List<Subtask>) -> Unit,
    modifier: Modifier = Modifier
) {
    var newSubtaskInput by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Subtarefas (${subtasks.size})",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        subtasks.forEachIndexed { index, subtask ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = subtask.isCompleted,
                    onCheckedChange = { isChecked ->
                        val updated = subtasks.toMutableList()
                        updated[index] = subtask.copy(isCompleted = isChecked)
                        onSubtasksChange(updated)
                    },
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = subtask.title,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        val updated = subtasks.filterIndexed { i, _ -> i != index }
                        onSubtasksChange(updated)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remover",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newSubtaskInput,
                onValueChange = { newSubtaskInput = it },
                placeholder = { Text("Nova subtarefa...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("dialog_new_subtask_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (newSubtaskInput.isNotBlank()) {
                        val updated = subtasks + Subtask(title = newSubtaskInput.trim(), isCompleted = false)
                        onSubtasksChange(updated)
                        newSubtaskInput = ""
                    }
                },
                modifier = Modifier.testTag("dialog_add_subtask_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar subtarefa",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
