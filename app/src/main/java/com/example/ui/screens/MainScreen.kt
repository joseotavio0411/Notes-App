package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material3.AlertDialog
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
import com.example.ui.components.SearchAttachmentFilter
import com.example.ui.components.SmoothSearchBar
import kotlinx.coroutines.launch

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

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Backup and options menu state
    var showMenu by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    // Search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var attachmentFilter by remember { mutableStateOf(SearchAttachmentFilter.ALL) }

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

    // Instant filtered lists
    val filteredNotes = remember(notes, searchQuery, attachmentFilter) {
        notes.filter { note ->
            val matchesQuery = searchQuery.isBlank() ||
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
            val matchesAttachment = when (attachmentFilter) {
                SearchAttachmentFilter.ALL -> true
                SearchAttachmentFilter.PHOTOS -> !note.imageUri.isNullOrBlank()
                SearchAttachmentFilter.AUDIO -> !note.audioPath.isNullOrBlank()
                SearchAttachmentFilter.PINNED -> note.isPinned
            }
            matchesQuery && matchesAttachment
        }
    }

    val filteredTasks = remember(tasks, searchQuery, attachmentFilter) {
        tasks.filter { task ->
            val matchesQuery = searchQuery.isBlank() || task.text.contains(searchQuery, ignoreCase = true)
            val matchesAttachment = when (attachmentFilter) {
                SearchAttachmentFilter.ALL -> true
                SearchAttachmentFilter.PHOTOS -> false
                SearchAttachmentFilter.AUDIO -> false
                SearchAttachmentFilter.PINNED -> task.isPinned
            }
            matchesQuery && matchesAttachment
        }
    }

    val filteredDeadlineTasks = remember(deadlineTasks, searchQuery, attachmentFilter) {
        deadlineTasks.filter { task ->
            val matchesQuery = searchQuery.isBlank() || task.text.contains(searchQuery, ignoreCase = true)
            val matchesAttachment = when (attachmentFilter) {
                SearchAttachmentFilter.ALL -> true
                SearchAttachmentFilter.PHOTOS -> false
                SearchAttachmentFilter.AUDIO -> false
                SearchAttachmentFilter.PINNED -> task.isPinned
            }
            matchesQuery && matchesAttachment
        }
    }

    val filteredDeadlineNotes = remember(deadlineNotes, searchQuery, attachmentFilter) {
        deadlineNotes.filter { note ->
            val matchesQuery = searchQuery.isBlank() ||
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
            val matchesAttachment = when (attachmentFilter) {
                SearchAttachmentFilter.ALL -> true
                SearchAttachmentFilter.PHOTOS -> !note.imageUri.isNullOrBlank()
                SearchAttachmentFilter.AUDIO -> !note.audioPath.isNullOrBlank()
                SearchAttachmentFilter.PINNED -> note.isPinned
            }
            matchesQuery && matchesAttachment
        }
    }

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
                    IconButton(
                        onClick = onToggleDarkTheme,
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Mudar para Tema Claro" else "Mudar para Tema Escuro",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

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
                        attachmentFilter = attachmentFilter,
                        onAttachmentFilterChange = { attachmentFilter = it },
                        discoveredTags = discoveredTags
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
                                onAddNote = { title, content, colorHex, isPinned, isLocked, lockPin, imageUri, audioPath ->
                                    viewModel.addNote(title, content, colorHex, isPinned, isLocked, lockPin, imageUri, audioPath)
                                },
                                onUpdateNote = { note -> viewModel.updateNote(note) },
                                onDeleteNote = { note ->
                                    viewModel.deleteNote(note)
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val label = if (note.title.isNotBlank()) note.title else "Nota"
                                        val result = snackbarHostState.showSnackbar(
                                            message = "\"${label.take(24)}\" excluída",
                                            actionLabel = "Desfazer",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreNote(note)
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
                                onAddTask = { text, isPinned, recurrence -> viewModel.addTask(text, isPinned, recurrence) },
                                onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                                onTogglePinTask = { task -> viewModel.togglePinTask(task) },
                                onDeleteTask = { task ->
                                    viewModel.deleteTask(task)
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Tarefa \"${task.text.take(24)}\" excluída",
                                            actionLabel = "Desfazer",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreTask(task)
                                        }
                                    }
                                }
                            )
                            2 -> DeadlineTasksScreen(
                                tasks = filteredDeadlineTasks,
                                currentTime = currentTime,
                                onAddTask = { text, deadline, isPinned, recurrence ->
                                    viewModel.addDeadlineTask(text, deadline, isPinned, recurrence)
                                },
                                onToggleTask = { task -> viewModel.toggleDeadlineTaskCompletion(task) },
                                onTogglePinTask = { task -> viewModel.togglePinDeadlineTask(task) },
                                onDeleteTask = { task ->
                                    viewModel.deleteDeadlineTask(task)
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Tarefa com prazo excluída",
                                            actionLabel = "Desfazer",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreDeadlineTask(task)
                                        }
                                    }
                                },
                                isDarkTheme = isDarkTheme
                            )
                            3 -> DeadlineNotesScreen(
                                notes = filteredDeadlineNotes,
                                currentTime = currentTime,
                                onAddNote = { title, content, deadline, colorHex, isPinned, isLocked, lockPin, recurrence, imageUri, audioPath ->
                                    viewModel.addDeadlineNote(title, content, deadline, colorHex, isPinned, isLocked, lockPin, recurrence, imageUri, audioPath)
                                },
                                onUpdateNote = { note -> viewModel.updateDeadlineNote(note) },
                                onDeleteNote = { note ->
                                    viewModel.deleteDeadlineNote(note)
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val label = if (note.title.isNotBlank()) note.title else "Nota com prazo"
                                        val result = snackbarHostState.showSnackbar(
                                            message = "\"${label.take(24)}\" excluída",
                                            actionLabel = "Desfazer",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreDeadlineNote(note)
                                        }
                                    }
                                },
                                onTogglePinNote = { note -> viewModel.togglePinDeadlineNote(note) },
                                onToggleChecklistItem = { note, lineIdx -> viewModel.toggleDeadlineNoteChecklistItem(note, lineIdx) },
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

    if (showExportDialog) {
        val backupJson = remember { viewModel.getBackupJson() }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            icon = { Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Exportar Backup dos Dados") },
            text = {
                Column {
                    Text(
                        "Seus dados atuais:\n" +
                        "• ${notes.size} Notas\n" +
                        "• ${tasks.size} Tarefas\n" +
                        "• ${deadlineTasks.size} Tarefas com Prazo\n" +
                        "• ${deadlineNotes.size} Notas com Prazo\n\n" +
                        "Você pode compartilhar o backup por WhatsApp, Google Drive, Email ou copiar para a área de transferência.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, backupJson)
                            putExtra(Intent.EXTRA_SUBJECT, "Backup Produtividade")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Backup")
                        context.startActivity(shareIntent)
                        showExportDialog = false
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Compartilhar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(backupJson))
                        showExportDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Backup copiado para a área de transferência!")
                        }
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Copiar")
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
                        "Cole o código do backup abaixo para restaurar suas notas e tarefas:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text("Cole o backup aqui...") },
                        maxLines = 8
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    importText = clip
                                }
                            }
                        ) {
                            Text("Colar da Área de Transferência")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importText.isNotBlank()) {
                            viewModel.restoreBackupJson(importText) { success, count, msg ->
                                showImportDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        }
                    },
                    enabled = importText.isNotBlank()
                ) {
                    Text("Restaurar")
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
                Column {
                    Text(
                        "Versão 1.1 (build 2)\n\n" +
                        "✅ Atualização Segura Ativa:\n" +
                        "Ao passar o novo APK para seu celular, o aplicativo atualiza diretamente por cima da versão existente, sem desinstalar e sem perder notas ou tarefas.\n\n" +
                        "✅ Banco de Dados Protegido:\n" +
                        "O sistema de migração de dados preserva todas as suas notas, checklists, áudios e prazos intactos.\n\n" +
                        "✅ Backup Automático do Sistema:\n" +
                        "O backup na nuvem (Google Drive) e transferência entre aparelhos já estão configurados.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showVersionDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

