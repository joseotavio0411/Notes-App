package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ProductivityViewModel
import com.example.ui.components.RecycleBinDialog
import com.example.ui.components.SearchAttachmentFilter
import com.example.ui.components.Skiper26CircularRevealOverlay
import com.example.ui.components.Skiper26ThemeToggle
import com.example.ui.components.SmoothSearchBar
import kotlinx.coroutines.launch
import java.util.Calendar

data class TabItem(
    val title: String,
    val shortLabel: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

val navigationTabs = listOf(
    TabItem(
        title = "Bloco de Notas",
        shortLabel = "Notas",
        selectedIcon = Icons.Filled.EditNote,
        unselectedIcon = Icons.Outlined.EditNote,
        testTag = "nav_tab_notes"
    ),
    TabItem(
        title = "Lista de Tarefas",
        shortLabel = "Tarefas",
        selectedIcon = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle,
        testTag = "nav_tab_tasks"
    ),
    TabItem(
        title = "Tarefas Diárias (Com Prazo)",
        shortLabel = "Tarefas c/ Prazo",
        selectedIcon = Icons.Filled.Alarm,
        unselectedIcon = Icons.Outlined.Alarm,
        testTag = "nav_tab_deadline_tasks"
    ),
    TabItem(
        title = "Notas Diárias (Com Prazo)",
        shortLabel = "Notas c/ Prazo",
        selectedIcon = Icons.Filled.HourglassBottom,
        unselectedIcon = Icons.Outlined.HourglassBottom,
        testTag = "nav_tab_deadline_notes"
    )
)

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ProductivityViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTimeMillis.collectAsStateWithLifecycle()
    val isNotesGridMode by viewModel.isNotesGridMode.collectAsStateWithLifecycle()

    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val deadlineTasks by viewModel.activeDeadlineTasks.collectAsStateWithLifecycle()
    val deadlineNotes by viewModel.activeDeadlineNotes.collectAsStateWithLifecycle()
    val unlockedNoteIds by viewModel.unlockedNoteIds.collectAsStateWithLifecycle()
    val unlockedDeadlineNoteIds by viewModel.unlockedDeadlineNoteIds.collectAsStateWithLifecycle()

    // Trash state
    val deletedNotes by viewModel.deletedNotes.collectAsStateWithLifecycle()
    val deletedTasks by viewModel.deletedTasks.collectAsStateWithLifecycle()
    val deletedDeadlineTasks by viewModel.deletedDeadlineTasks.collectAsStateWithLifecycle()
    val deletedDeadlineNotes by viewModel.deletedDeadlineNotes.collectAsStateWithLifecycle()
    val trashTotalCount by viewModel.trashTotalCount.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Dialog state
    var showMenu by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }
    var showRecycleBinDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    // Skiper26 circular reveal view transition animation state
    var isThemeRevealing by remember { mutableStateOf(false) }
    var themeRevealOrigin by remember { mutableStateOf(Offset.Zero) }
    val themeRevealProgress = remember { Animatable(0f) }

    // Search and filter state
    var searchQuery by remember { mutableStateOf("") }

    // Discovered tags extraction across notes and tasks
    val hashtagRegex = remember { Regex("""#\w+""") }
    val discoveredTags = remember(notes, deadlineNotes, tasks, deadlineTasks) {
        val tags = linkedSetOf<String>()
        tags.add("#urgente")
        tags.add("#cardapio")
        tags.add("#trabalho")
        tags.add("#pessoal")

        notes.forEach { n ->
            hashtagRegex.findAll("${n.title} ${n.content}").forEach { tags.add(it.value) }
        }
        deadlineNotes.forEach { n ->
            hashtagRegex.findAll("${n.title} ${n.content}").forEach { tags.add(it.value) }
        }
        tasks.forEach { t ->
            hashtagRegex.findAll(t.text).forEach { tags.add(it.value) }
        }
        deadlineTasks.forEach { dt ->
            hashtagRegex.findAll(dt.text).forEach { tags.add(it.value) }
        }
        tags.toList()
    }

    // Instant filtered lists matching search query
    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) notes
        else notes.filter { note ->
            note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredTasks = remember(tasks, searchQuery) {
        if (searchQuery.isBlank()) tasks
        else tasks.filter { task ->
            task.text.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredDeadlineTasks = remember(deadlineTasks, searchQuery) {
        if (searchQuery.isBlank()) deadlineTasks
        else deadlineTasks.filter { task ->
            task.text.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredDeadlineNotes = remember(deadlineNotes, searchQuery) {
        if (searchQuery.isBlank()) deadlineNotes
        else deadlineNotes.filter { note ->
            note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
        }
    }

    // Daily summary calculations
    val pendingTasksCount = remember(tasks) { tasks.count { !it.isCompleted } }
    val dueTodayDeadlinesCount = remember(deadlineTasks, deadlineNotes, currentTime) {
        deadlineTasks.count { !it.isCompleted && isSameDay(it.deadlineTimestamp, currentTime) } +
            deadlineNotes.count { isSameDay(it.deadlineTimestamp, currentTime) }
    }
    val overdueDeadlinesCount = remember(deadlineTasks, deadlineNotes, currentTime) {
        deadlineTasks.count { !it.isCompleted && it.deadlineTimestamp < currentTime } +
            deadlineNotes.count { it.deadlineTimestamp < currentTime }
    }

    // Recycle bin dialog
    if (showRecycleBinDialog) {
        RecycleBinDialog(
            deletedNotes = deletedNotes,
            deletedTasks = deletedTasks,
            deletedDeadlineTasks = deletedDeadlineTasks,
            deletedDeadlineNotes = deletedDeadlineNotes,
            onRestoreNote = { viewModel.restoreNoteById(it) },
            onDeleteNotePermanently = { viewModel.deleteNotePermanently(it) },
            onRestoreTask = { viewModel.restoreTaskById(it) },
            onDeleteTaskPermanently = { viewModel.deleteTaskPermanently(it) },
            onRestoreDeadlineTask = { viewModel.restoreDeadlineTaskById(it) },
            onDeleteDeadlineTaskPermanently = { viewModel.deleteDeadlineTaskPermanently(it) },
            onRestoreDeadlineNote = { viewModel.restoreDeadlineNoteById(it) },
            onDeleteDeadlineNotePermanently = { viewModel.deleteDeadlineNotePermanently(it) },
            onEmptyAllTrash = { viewModel.emptyAllTrash() },
            onDismiss = { showRecycleBinDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val currentTabItem = navigationTabs.getOrElse(selectedTab) { navigationTabs[0] }
                    AnimatedContent(
                        targetState = currentTabItem.title,
                        label = "top_bar_title_anim",
                        transitionSpec = {
                            (fadeIn() + slideInHorizontally { 30 }).togetherWith(fadeOut() + slideOutHorizontally { -30 })
                        }
                    ) { titleText ->
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    // Recycle Bin button with item count badge
                    IconButton(
                        onClick = { showRecycleBinDialog = true },
                        modifier = Modifier.testTag("recycle_bin_top_button")
                    ) {
                        if (trashTotalCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text(if (trashTotalCount > 99) "99+" else trashTotalCount.toString())
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Lixeira",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Lixeira",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Quick Auto-Lock button
                    IconButton(
                        onClick = {
                            viewModel.relockAll()
                            Toast.makeText(context, "Itens protegidos bloqueados", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("auto_lock_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloquear itens protegidos",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Skiper26 animated theme toggle button with morphing sun/moon & spring physics
                    Skiper26ThemeToggle(
                        isDarkTheme = isDarkTheme,
                        onToggle = { origin ->
                            themeRevealOrigin = origin
                            onToggleDarkTheme()
                            coroutineScope.launch {
                                isThemeRevealing = true
                                themeRevealProgress.snapTo(0f)
                                themeRevealProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                                )
                                isThemeRevealing = false
                            }
                        }
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("more_options_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Mais opções",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Lixeira ($trashTotalCount)") },
                                onClick = {
                                    showMenu = false
                                    showRecycleBinDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Bloquear Itens Protegidos") },
                                onClick = {
                                    showMenu = false
                                    viewModel.relockAll()
                                    Toast.makeText(context, "Itens bloqueados", Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Exportar Backup") },
                                onClick = {
                                    showMenu = false
                                    showExportDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Backup, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Restaurar Backup") },
                                onClick = {
                                    showMenu = false
                                    importText = ""
                                    showImportDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Restore, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Atualizações e Versão") },
                                onClick = {
                                    showMenu = false
                                    showVersionDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Info, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navigationTabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTab(index) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.shortLabel
                            )
                        },
                        label = {
                            Text(
                                text = tab.shortLabel,
                                fontSize = 11.sp,
                                maxLines = 1,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Integrated smooth search bar with cursor & focus animations
                    SmoothSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Pesquisar notas e tarefas..."
                    )

                    AnimatedContent(
                        targetState = selectedTab,
                        label = "tab_content_anim",
                        modifier = Modifier.weight(1f),
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width / 4 } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width / 4 } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> -width / 4 } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width / 4 } + fadeOut()
                                )
                            }
                        }
                    ) { currentTab ->
                        when (currentTab) {
                            0 -> NotesScreen(
                                notes = filteredNotes,
                                isGridMode = isNotesGridMode,
                                onToggleGridMode = { viewModel.toggleNotesGridMode() },
                                onAddNote = { title, content, colorHex, isPinned, isLocked, lockPin, imageUri, audioPath, category ->
                                    viewModel.addNote(title, content, colorHex, isPinned, isLocked, lockPin, imageUri, audioPath, category = category)
                                },
                                onUpdateNote = { note -> viewModel.updateNote(note) },
                                onDeleteNote = { note ->
                                    viewModel.deleteNote(note)
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val label = if (note.title.isNotBlank()) note.title else "Nota"
                                        val result = snackbarHostState.showSnackbar(
                                            message = "\"${label.take(24)}\" movida para a lixeira",
                                            actionLabel = "Desfazer",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreNoteById(note.id)
                                        }
                                    }
                                },
                                onTogglePinNote = { note -> viewModel.togglePinNote(note) },
                                onToggleChecklistItem = { note, lineIdx -> viewModel.toggleNoteChecklistItem(note, lineIdx) },
                                unlockedNoteIds = unlockedNoteIds,
                                onUnlockNote = { noteId, enteredPin, actualPin -> viewModel.unlockNote(noteId, enteredPin, actualPin) },
                                onUnlockNoteDirectly = { noteId -> viewModel.unlockNoteDirectly(noteId) },
                                isDarkTheme = isDarkTheme
                            )
                            1 -> TasksScreen(
                                tasks = filteredTasks,
                                onAddTask = { text, isPinned, recurrence, category, subtasksJson ->
                                    viewModel.addTask(text, isPinned, recurrence, category = category, subtasksJson = subtasksJson)
                                },
                                onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                                onTogglePinTask = { task -> viewModel.togglePinTask(task) },
                                onDeleteTask = { task ->
                                    viewModel.deleteTask(task)
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Tarefa \"${task.text.take(24)}\" movida para a lixeira",
                                            actionLabel = "Desfazer",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreTaskById(task.id)
                                        }
                                    }
                                },
                                onToggleSubtask = { task, subId -> viewModel.toggleSubtask(task, subId) },
                                onAddSubtask = { task, title -> viewModel.addSubtask(task, title) },
                                onRemoveSubtask = { task, subId -> viewModel.removeSubtask(task, subId) }
                            )
                            2 -> DeadlineTasksScreen(
                                tasks = filteredDeadlineTasks,
                                currentTime = currentTime,
                                onAddTask = { text, deadline, isPinned, recurrence, category, subtasksJson ->
                                    viewModel.addDeadlineTask(text, deadline, isPinned, recurrence, category = category, subtasksJson = subtasksJson)
                                },
                                onToggleTask = { task -> viewModel.toggleDeadlineTaskCompletion(task) },
                                onTogglePinTask = { task -> viewModel.togglePinDeadlineTask(task) },
                                onDeleteTask = { task ->
                                    viewModel.deleteDeadlineTask(task)
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Tarefa com prazo movida para a lixeira",
                                            actionLabel = "Desfazer",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreDeadlineTaskById(task.id)
                                        }
                                    }
                                },
                                onSnoozeTask = { task, minutes -> viewModel.snoozeDeadlineTask(task, minutes) },
                                onToggleSubtask = { task, subId -> viewModel.toggleDeadlineSubtask(task, subId) },
                                onAddSubtask = { task, title -> viewModel.addDeadlineSubtask(task, title) },
                                onRemoveSubtask = { task, subId -> viewModel.removeDeadlineSubtask(task, subId) },
                                isDarkTheme = isDarkTheme
                            )
                            3 -> DeadlineNotesScreen(
                                notes = filteredDeadlineNotes,
                                currentTime = currentTime,
                                onAddNote = { title, content, deadline, colorHex, isPinned, isLocked, lockPin, recurrence, imageUri, audioPath, category ->
                                    viewModel.addDeadlineNote(title, content, deadline, colorHex, isPinned, isLocked, lockPin, recurrence, imageUri, audioPath, category = category)
                                },
                                onUpdateNote = { note -> viewModel.updateDeadlineNote(note) },
                                onDeleteNote = { note ->
                                    viewModel.deleteDeadlineNote(note)
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val label = if (note.title.isNotBlank()) note.title else "Nota com prazo"
                                        val result = snackbarHostState.showSnackbar(
                                            message = "\"${label.take(24)}\" movida para a lixeira",
                                            actionLabel = "Desfazer",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreDeadlineNoteById(note.id)
                                        }
                                    }
                                },
                                onTogglePinNote = { note -> viewModel.togglePinDeadlineNote(note) },
                                onToggleChecklistItem = { note, lineIdx -> viewModel.toggleDeadlineNoteChecklistItem(note, lineIdx) },
                                onSnoozeNote = { note, minutes -> viewModel.snoozeDeadlineNote(note, minutes) },
                                unlockedNoteIds = unlockedDeadlineNoteIds,
                                onUnlockNote = { noteId, enteredPin, actualPin -> viewModel.unlockDeadlineNote(noteId, enteredPin, actualPin) },
                                onUnlockNoteDirectly = { noteId -> viewModel.unlockDeadlineNoteDirectly(noteId) },
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
            }
        }
    }

    // Skiper26 Circular Reveal View Transition Overlay
    Skiper26CircularRevealOverlay(
        isRevealing = isThemeRevealing,
        origin = themeRevealOrigin,
        progressProvider = { themeRevealProgress.value },
        targetIsDark = isDarkTheme
    )
}

    if (showExportDialog) {
        val backupJson = remember { viewModel.getBackupJson() }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            icon = { Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Exportar Backup dos Dados") },
            text = {
                Column {
                    Text(
                        text = "Copie o texto abaixo ou toque em \"Compartilhar\" para salvar em seu celular, WhatsApp, Drive ou e-mail:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupJson,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("backup_export_textfield"),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(backupJson))
                            Toast.makeText(context, "Backup copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_backup_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copiar")
                    }
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, backupJson)
                                putExtra(Intent.EXTRA_SUBJECT, "Backup Minhas Notas e Tarefas")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Salvar backup em:"))
                        },
                        modifier = Modifier.testTag("share_backup_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartilhar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            icon = { Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Restaurar Backup") },
            text = {
                Column {
                    Text(
                        text = "Cole o código do backup (JSON) que você salvou anteriormente:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        placeholder = { Text("Cole o JSON de backup aqui...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("backup_import_textfield"),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importText.isBlank()) {
                            Toast.makeText(context, "Por favor, cole o código do backup.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.restoreBackupJson(importText.trim()) { success, count, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) {
                                showImportDialog = false
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_restore_backup_button")
                ) {
                    Text("Restaurar Dados")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showVersionDialog) {
        AlertDialog(
            onDismissRequest = { showVersionDialog = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Versão e Atualizações") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Versão Atual: v6.0 (Com Lixeira, Subtarefas e Categorias)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "✨ Novidades desta versão:\n" +
                               "• Lixeira com retenção de 30 dias e restauração completa\n" +
                               "• Subtarefas com progresso e checklist dentro das tarefas\n" +
                               "• Categorias coloridas e filtro por tags\n" +
                               "• Adiar prazos (Snooze) e resumo diário\n" +
                               "• Modo Leitura Zen para notas longas\n" +
                               "• Migração de banco de dados automática sem perda de dados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showVersionDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}


