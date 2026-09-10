package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
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
}

