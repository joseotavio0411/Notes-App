package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.LaunchAction
import com.example.ui.NoteKind
import com.example.ui.NoteSortOrder
import com.example.ui.components.ExpandableFabScrim
import com.example.ui.components.ExpandableNoteFab
import com.example.ui.components.NoteSelectionBar
import com.example.ui.components.NoteSharing
import com.example.ui.components.NoteSortMenuButton
import com.example.ui.components.ReminderPickerDialog
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.local.entity.NoteEntity
import com.example.data.model.CategoryHelper
import com.example.ui.components.AttachedImageView
import com.example.ui.components.AudioPlayerView
import com.example.ui.components.CategoryBadge
import com.example.ui.components.ColorPalette
import com.example.ui.components.DateTimeHelper
import com.example.ui.components.DeliberateEmptyState
import com.example.ui.components.KeepStyleFullscreenNoteEditor
import com.example.ui.components.LockedNoteOverlay
import com.example.ui.components.MarkdownHelper
import com.example.ui.components.NoteCustomizationToolbar
import com.example.ui.components.PinUnlockDialog
import com.example.ui.components.RichContentView
import com.example.ui.components.SetPinDialog
import com.example.ui.components.VoiceRecordingDialog
import com.example.ui.components.ZenReadingDialog
import com.example.ui.components.copyUriToAppStorage

/** Nova nota pedida pelo botão "+", por um atalho, widget ou compartilhamento. */
private data class CreateNoteRequest(
    val kind: NoteKind,
    val title: String = "",
    val content: String = "",
    val imageUri: String? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    notes: List<NoteEntity>,
    isGridMode: Boolean,
    onToggleGridMode: () -> Unit,
    onAddNote: (title: String, content: String, colorHex: String, isPinned: Boolean, isLocked: Boolean, lockPin: String?, imageUri: String?, audioPath: String?, category: String) -> Unit,
    onUpdateNote: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onTogglePinNote: (NoteEntity) -> Unit,
    onToggleChecklistItem: (NoteEntity, Int) -> Unit,
    unlockedNoteIds: Set<Long>,
    onUnlockNote: (noteId: Long, enteredPin: String, actualPin: String?) -> Boolean,
    onUnlockNoteDirectly: ((Long) -> Unit)? = null,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    sortOrder: NoteSortOrder = NoteSortOrder.UPDATED,
    onSortOrderChange: (NoteSortOrder) -> Unit = {},
    isArchiveView: Boolean = false,
    onExitArchive: () -> Unit = {},
    notificationPinnedIds: Set<Long> = emptySet(),
    onArchiveNotes: (Set<Long>, Boolean) -> Unit = { _, _ -> },
    onPinNotes: (Set<Long>, Boolean) -> Unit = { _, _ -> },
    onColorNotes: (Set<Long>, String) -> Unit = { _, _ -> },
    onDeleteNotes: (Set<Long>) -> Unit = {},
    onSetReminder: (NoteEntity, Long) -> Unit = { _, _ -> },
    onToggleNotificationPin: (NoteEntity) -> Unit = {},
    launchAction: LaunchAction? = null,
    onLaunchActionConsumed: () -> Unit = {}
) {
    val context = LocalContext.current

    var createRequest by remember { mutableStateOf<CreateNoteRequest?>(null) }
    var noteToUnlock by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var noteForZenReading by remember { mutableStateOf<NoteEntity?>(null) }
    var noteForReminder by remember { mutableStateOf<NoteEntity?>(null) }
    var startInFullscreen by remember { mutableStateOf(false) }
    var pendingFullscreenOpen by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val selectionMode = selectedIds.isNotEmpty()

    // Notas que saíram da lista (excluídas, arquivadas, filtradas) deixam a seleção
    LaunchedEffect(notes) {
        val visible = notes.map { it.id }.toSet()
        if (selectedIds.any { it !in visible }) selectedIds = selectedIds.intersect(visible)
    }
    LaunchedEffect(isArchiveView) { selectedIds = emptySet() }
    BackHandler(enabled = selectionMode) { selectedIds = emptySet() }
    BackHandler(enabled = isArchiveView && !selectionMode) { onExitArchive() }

    // Imagem escolhida pelo botão "+" > Imagem: abre a nota já com a imagem
    val imageNoteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val local = copyUriToAppStorage(context, uri)
            if (local != null) createRequest = CreateNoteRequest(NoteKind.IMAGE, imageUri = local)
        }
    }

    fun startNewNote(kind: NoteKind) {
        if (kind == NoteKind.IMAGE) {
            imageNoteLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            createRequest = CreateNoteRequest(kind)
        }
    }

    fun openNote(note: NoteEntity, fullscreen: Boolean) {
        if (note.isLocked && note.id !in unlockedNoteIds) {
            pendingFullscreenOpen = fullscreen
            noteToUnlock = note
        } else {
            startInFullscreen = fullscreen
            noteToEdit = note
        }
    }

    // Aberturas vindas de atalhos, widgets, notificações e compartilhamentos
    LaunchedEffect(launchAction, notes) {
        when (val action = launchAction) {
            is LaunchAction.NewNote -> {
                if (action.kind == NoteKind.IMAGE && action.imageUri == null) {
                    startNewNote(NoteKind.IMAGE)
                } else {
                    createRequest = CreateNoteRequest(action.kind, action.title, action.content, action.imageUri)
                }
                onLaunchActionConsumed()
            }
            is LaunchAction.OpenNote -> {
                val note = notes.find { it.id == action.noteId }
                if (note != null) {
                    openNote(note, fullscreen = true)
                    onLaunchActionConsumed()
                } else if (notes.isNotEmpty()) {
                    Toast.makeText(context, "Nota não encontrada", Toast.LENGTH_SHORT).show()
                    onLaunchActionConsumed()
                }
            }
            else -> Unit
        }
    }

    // Zen reading dialog
    noteForZenReading?.let { zNote ->
        ZenReadingDialog(
            title = zNote.title,
            content = zNote.content,
            category = zNote.category,
            onDismiss = { noteForZenReading = null }
        )
    }

    noteForReminder?.let { note ->
        ReminderPickerDialog(
            currentReminderAt = note.reminderAt,
            onSet = { at ->
                onSetReminder(note, at)
                noteForReminder = null
                Toast.makeText(context, "Lembrete: ${DateTimeHelper.formatDateTime(at)}", Toast.LENGTH_SHORT).show()
            },
            onRemove = {
                onSetReminder(note, 0L)
                noteForReminder = null
            },
            onDismiss = { noteForReminder = null }
        )
    }

    // Nova nota (botão "+", atalhos, widgets e compartilhamentos abrem direto em tela cheia)
    createRequest?.let { request ->
        CreateNoteDialog(
            isDarkTheme = isDarkTheme,
            initialTitle = request.title,
            initialContent = if (request.kind == NoteKind.LIST && request.content.isBlank()) "[ ] " else request.content,
            initialImageUri = request.imageUri,
            startFullscreen = true,
            startVoiceRecording = request.kind == NoteKind.AUDIO,
            onDismiss = { createRequest = null },
            onSave = { title, content, colorHex, isPinned, isLocked, lockPin, imageUri, audioPath, category ->
                onAddNote(title, content, colorHex, isPinned, isLocked, lockPin, imageUri, audioPath, category)
                createRequest = null
            }
        )
    }

    // Unlock PIN dialog
    noteToUnlock?.let { note ->
        PinUnlockDialog(
            noteTitle = note.title,
            actualPin = note.lockPin,
            onDismiss = {
                noteToUnlock = null
                pendingFullscreenOpen = false
            },
            onSuccess = {
                onUnlockNoteDirectly?.invoke(note.id) ?: run {
                    val actual = note.lockPin ?: "1234"
                    onUnlockNote(note.id, actual, note.lockPin)
                }
                val openFs = pendingFullscreenOpen
                noteToUnlock = null
                startInFullscreen = openFs
                noteToEdit = note
                Toast.makeText(context, "Nota desbloqueada", Toast.LENGTH_SHORT).show()
            }
        )
    }

    val renderCard: @Composable (NoteEntity, Boolean) -> Unit = { note, isGrid ->
        val isUnlocked = !note.isLocked || (note.id in unlockedNoteIds)
        NoteCard(
            note = note,
            isDarkTheme = isDarkTheme,
            isUnlocked = isUnlocked,
            isGrid = isGrid,
            isSelected = note.id in selectedIds,
            selectionMode = selectionMode,
            isInNotifications = note.id in notificationPinnedIds,
            onClick = {
                if (selectionMode) {
                    selectedIds = if (note.id in selectedIds) selectedIds - note.id else selectedIds + note.id
                } else {
                    openNote(note, fullscreen = true)
                }
            },
            onLongPress = {
                selectedIds = if (note.id in selectedIds) selectedIds - note.id else selectedIds + note.id
            },
            onUnlock = {
                pendingFullscreenOpen = true
                noteToUnlock = note
            },
            onTogglePin = { onTogglePinNote(note) },
            onToggleCheckbox = { lineIdx -> onToggleChecklistItem(note, lineIdx) },
            onEdit = { openNote(note, fullscreen = false) },
            onOpenZenReading = { noteForZenReading = note },
            onArchiveToggle = { onArchiveNotes(setOf(note.id), !note.isArchived) },
            onSetReminder = { noteForReminder = note },
            onToggleNotificationPin = { onToggleNotificationPin(note) },
            onShare = { NoteSharing.shareAsText(context, note) },
            onExportPdf = { NoteSharing.exportPdf(context, note) },
            onDelete = { noteToDelete = note }
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

        if (selectionMode) {
            val selectedNotes = notes.filter { it.id in selectedIds }
            val allPinned = selectedNotes.isNotEmpty() && selectedNotes.all { it.isPinned }
            NoteSelectionBar(
                selectedCount = selectedIds.size,
                allSelectedPinned = allPinned,
                isArchiveView = isArchiveView,
                isDarkTheme = isDarkTheme,
                onClear = { selectedIds = emptySet() },
                onTogglePin = {
                    onPinNotes(selectedIds, !allPinned)
                    selectedIds = emptySet()
                },
                onColor = { hex ->
                    onColorNotes(selectedIds, hex)
                    selectedIds = emptySet()
                },
                onArchiveToggle = {
                    onArchiveNotes(selectedIds, !isArchiveView)
                    selectedIds = emptySet()
                },
                onDelete = {
                    onDeleteNotes(selectedIds)
                    selectedIds = emptySet()
                }
            )
        } else {
            // Section header with counter, sort and View Mode toggle (Grid / List)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (isArchiveView) {
                        IconButton(onClick = onExitArchive, modifier = Modifier.testTag("exit_archive_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar para as notas")
                        }
                    }
                    Text(
                        text = if (isArchiveView) "Arquivo (${notes.size})" else "Minhas Notas (${notes.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!isArchiveView) {
                    NoteSortMenuButton(sortOrder = sortOrder, onSortOrderChange = onSortOrderChange)
                }

                IconButton(
                    onClick = onToggleGridMode,
                    modifier = Modifier.testTag("toggle_notes_view_button")
                ) {
                    Icon(
                        imageVector = if (isGridMode) Icons.Default.ViewAgenda else Icons.Default.GridView,
                        contentDescription = if (isGridMode) "Modo Lista" else "Modo Grade",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Notes List or Grid
        if (notes.isEmpty()) {
            DeliberateEmptyState(
                title = if (isArchiveView) "Nenhuma nota arquivada" else "Nenhuma nota criada ainda",
                description = if (isArchiveView) {
                    "Notas arquivadas saem da lista principal sem ir para a lixeira."
                } else {
                    "Toque no botão \"+\" no canto inferior direito para criar sua primeira anotação."
                },
                icon = if (isArchiveView) Icons.Default.Archive else Icons.Default.Description,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val pinnedNotes = notes.filter { it.isPinned }
            val otherNotes = notes.filter { !it.isPinned }

            if (isGridMode) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp
                ) {
                    if (pinnedNotes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) { PinnedSectionHeader() }
                        items(pinnedNotes, key = { it.id }) { note -> renderCard(note, true) }
                        if (otherNotes.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) { OthersSectionHeader() }
                        }
                    }
                    items(otherNotes, key = { it.id }) { note -> renderCard(note, true) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (pinnedNotes.isNotEmpty()) {
                        item { PinnedSectionHeader() }
                        items(pinnedNotes, key = { it.id }) { note -> renderCard(note, false) }
                        if (otherNotes.isNotEmpty()) {
                            item { OthersSectionHeader() }
                        }
                    }
                    items(otherNotes, key = { it.id }) { note -> renderCard(note, false) }
                }
            }
        }
    }

    // Botão "+" que se expande com os tipos de nota (inspirado no Google Keep)
    if (!isArchiveView && !selectionMode) {
        ExpandableFabScrim(expanded = fabExpanded, onDismiss = { fabExpanded = false })
        ExpandableNoteFab(
            expanded = fabExpanded,
            onExpandedChange = { fabExpanded = it },
            onSelect = { kind -> startNewNote(kind) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

    // Edit Note Dialog
    noteToEdit?.let { note ->
        EditNoteDialog(
            note = note,
            isDarkTheme = isDarkTheme,
            startFullscreen = startInFullscreen,
            onDismiss = {
                noteToEdit = null
                startInFullscreen = false
            },
            onSave = { updatedNote ->
                onUpdateNote(updatedNote)
                noteToEdit = null
                startInFullscreen = false
            },
            onSetReminder = { noteForReminder = note }
        )
    }

    // Delete Confirmation Dialog
    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Excluir Nota") },
            text = { Text("Tem certeza de que deseja excluir a nota \"${note.title.ifBlank { "sem título" }}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNote(note)
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PinnedSectionHeader() {
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

@Composable
private fun OthersSectionHeader() {
    Text(
        text = "OUTRAS",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    isDarkTheme: Boolean,
    isUnlocked: Boolean,
    isGrid: Boolean = false,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    isInNotifications: Boolean = false,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {},
    onUnlock: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleCheckbox: (Int) -> Unit,
    onEdit: () -> Unit,
    onOpenZenReading: () -> Unit,
    onArchiveToggle: () -> Unit = {},
    onSetReminder: () -> Unit = {},
    onToggleNotificationPin: () -> Unit = {},
    onShare: () -> Unit = {},
    onExportPdf: () -> Unit = {},
    onDelete: () -> Unit
) {
    val textColor = ColorPalette.getOptimalTextColor(note.colorHex, isDarkTheme)
    val secondaryTextColor = textColor.copy(alpha = 0.75f)
    val hasReminder = note.reminderAt > System.currentTimeMillis()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(if (isGrid) 12.dp else 14.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorPalette.getSurfaceColor(note.colorHex, isDarkTheme)
        ),
        border = if (isSelected) {
            BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (note.isPinned) 3.dp else 1.dp)
    ) {
        LockedNoteOverlay(
            isLocked = note.isLocked,
            isUnlockedThisSession = isUnlocked,
            onUnlockClick = onUnlock
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isGrid) 8.dp else 12.dp)
            ) {
                // Attached Image
                note.imageUri?.let { imgPath ->
                    AttachedImageView(
                        imageUriOrPath = imgPath,
                        maxHeight = if (isGrid) 100.dp else 240.dp,
                        modifier = Modifier.padding(bottom = if (isGrid) 4.dp else 8.dp)
                    )
                }

                // Attached Audio
                note.audioPath?.let { audioPath ->
                    AudioPlayerView(
                        audioPath = audioPath,
                        modifier = Modifier.padding(bottom = if (isGrid) 4.dp else 8.dp)
                    )
                }

                // Title row with selection, Pin & Lock indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    if (note.title.isNotBlank()) {
                        Text(
                            text = note.title,
                            style = if (isGrid) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                else MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor,
                            maxLines = if (isGrid) 1 else 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (note.isLocked) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = "Protegida com senha",
                                tint = textColor.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .size(if (isGrid) 14.dp else 18.dp)
                                    .padding(end = 2.dp)
                            )
                        }

                        if (selectionMode) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = if (isSelected) "Selecionada" else "Não selecionada",
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(if (isGrid) 18.dp else 22.dp)
                            )
                        } else if (!note.isArchived) {
                            IconButton(
                                onClick = onTogglePin,
                                modifier = Modifier.size(if (isGrid) 28.dp else 40.dp)
                            ) {
                                Icon(
                                    imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = if (note.isPinned) "Desafixar" else "Fixar nota",
                                    tint = if (note.isPinned) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.6f),
                                    modifier = Modifier.size(if (isGrid) 15.dp else 18.dp)
                                )
                            }
                        }
                    }
                }

                if (note.title.isNotBlank()) {
                    Spacer(modifier = Modifier.height(if (isGrid) 2.dp else 4.dp))
                }

                // Unformatted content preview (symbols like "- item" stay verbatim without bullets)
                if (note.content.isNotBlank()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = if (isGrid) 13.sp else 14.sp,
                            fontFamily = MarkdownHelper.getFontFamily(note.fontFamily)
                        ),
                        color = secondaryTextColor,
                        maxLines = if (isGrid) 4 else 8,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(if (isGrid) 4.dp else 8.dp))
                }

                // Footer: lembrete/notificação e menu com as ações da nota
                var showCardMenu by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasReminder) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.Alarm,
                                    contentDescription = "Lembrete",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = DateTimeHelper.formatShortDateTime(note.reminderAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                    if (isInNotifications) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "Fixada nas notificações",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(if (isGrid) 14.dp else 16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        IconButton(
                            onClick = { showCardMenu = true },
                            enabled = !selectionMode,
                            modifier = Modifier.size(if (isGrid) 28.dp else 36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Mais opções",
                                modifier = Modifier.size(if (isGrid) 16.dp else 20.dp),
                                tint = textColor.copy(alpha = 0.75f)
                            )
                        }

                        DropdownMenu(
                            expanded = showCardMenu,
                            onDismissRequest = { showCardMenu = false }
                        ) {
                            NoteMenuItem("Editar nota", Icons.Default.Edit) { showCardMenu = false; onEdit() }
                            NoteMenuItem("Ler (Modo Zen)", Icons.Default.MenuBook, MaterialTheme.colorScheme.primary) {
                                showCardMenu = false; onOpenZenReading()
                            }
                            if (!note.isLocked || isUnlocked) {
                                NoteMenuItem(if (hasReminder) "Alterar lembrete" else "Lembrete", Icons.Default.Alarm) {
                                    showCardMenu = false; onSetReminder()
                                }
                            }
                            if (!note.isLocked) {
                                NoteMenuItem(
                                    if (isInNotifications) "Tirar das notificações" else "Fixar nas notificações",
                                    if (isInNotifications) Icons.Default.NotificationsOff else Icons.Default.NotificationsActive
                                ) { showCardMenu = false; onToggleNotificationPin() }
                                NoteMenuItem("Compartilhar", Icons.Default.Share) { showCardMenu = false; onShare() }
                                NoteMenuItem("Exportar PDF", Icons.Default.PictureAsPdf) { showCardMenu = false; onExportPdf() }
                            }
                            NoteMenuItem(
                                if (note.isArchived) "Desarquivar" else "Arquivar",
                                if (note.isArchived) Icons.Default.Unarchive else Icons.Default.Archive
                            ) { showCardMenu = false; onArchiveToggle() }
                            HorizontalDivider()
                            NoteMenuItem("Excluir nota", Icons.Default.Delete, MaterialTheme.colorScheme.error, isDestructive = true) {
                                showCardMenu = false; onDelete()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteMenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(label, color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = tint) },
        onClick = onClick
    )
}

@Composable
fun EditNoteDialog(
    note: NoteEntity,
    isDarkTheme: Boolean,
    startFullscreen: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit,
    onSetReminder: (() -> Unit)? = null
) {
    val context = LocalContext.current

    var isFullscreen by remember { mutableStateOf(startFullscreen) }
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var colorHex by remember { mutableStateOf(note.colorHex) }
    var isPinned by remember { mutableStateOf(note.isPinned) }
    var isLocked by remember { mutableStateOf(note.isLocked) }
    var lockPin by remember { mutableStateOf(note.lockPin ?: "1234") }
    var imageUri by remember { mutableStateOf(note.imageUri) }
    var audioPath by remember { mutableStateOf(note.audioPath) }
    var fontSize by remember { mutableStateOf(note.fontSize) }
    var fontFamily by remember { mutableStateOf(note.fontFamily) }

    var isRecordingVoice by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(note.category) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val local = copyUriToAppStorage(context, uri)
            if (local != null) {
                imageUri = local
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isRecordingVoice = true
        } else {
            Toast.makeText(context, "Permissão de gravação necessária", Toast.LENGTH_SHORT).show()
        }
    }

    if (isRecordingVoice) {
        VoiceRecordingDialog(
            onDismiss = { isRecordingVoice = false },
            onFinishRecording = { path ->
                audioPath = path
                isRecordingVoice = false
            }
        )
    }

    if (showSetPinDialog) {
        SetPinDialog(
            initialPin = lockPin,
            onDismiss = { showSetPinDialog = false },
            onSavePin = { pin ->
                lockPin = pin
                isLocked = true
                showSetPinDialog = false
            }
        )
    }

    fun saveAndClose() {
        onSave(
            note.copy(
                title = title.trim(),
                content = content.trim(),
                colorHex = colorHex,
                isPinned = isPinned,
                isLocked = isLocked,
                lockPin = if (isLocked) lockPin else null,
                imageUri = imageUri,
                audioPath = audioPath,
                category = category,
                fontSize = fontSize,
                fontFamily = fontFamily,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    if (isFullscreen) {
        KeepStyleFullscreenNoteEditor(
            title = title,
            onTitleChange = { title = it },
            content = content,
            onContentChange = { content = it },
            colorHex = colorHex,
            onColorChange = { colorHex = it },
            isPinned = isPinned,
            onTogglePin = { isPinned = !isPinned },
            isLocked = isLocked,
            onToggleLock = {
                if (isLocked) {
                    isLocked = false
                } else {
                    showSetPinDialog = true
                }
            },
            imageUri = imageUri,
            onDeleteImage = { imageUri = null },
            onAddImage = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            audioPath = audioPath,
            onDeleteAudio = { audioPath = null },
            onStartVoiceRecording = {
                val hasPerm = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPerm) {
                    isRecordingVoice = true
                } else {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onSave = { saveAndClose() },
            onDismiss = onDismiss,
            isDarkTheme = isDarkTheme,
            fontSize = fontSize,
            onFontSizeChange = { fontSize = it },
            fontFamily = fontFamily,
            onFontFamilyChange = { fontFamily = it },
            reminderAt = note.reminderAt,
            onSetReminder = onSetReminder?.let { open ->
                {
                    // Salva o que foi digitado antes de abrir o seletor de lembrete
                    saveAndClose()
                    open()
                }
            },
            onShare = {
                NoteSharing.shareAsText(context, note.copy(title = title, content = content))
            },
            onExportPdf = {
                NoteSharing.exportPdf(context, note.copy(title = title, content = content))
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Editar Nota", fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isFullscreen = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Abrir nota em tela cheia",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { isPinned = !isPinned },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Fixar",
                                tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isLocked) {
                                    isLocked = false
                                } else {
                                    showSetPinDialog = true
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Bloquear",
                                tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    imageUri?.let { path ->
                        AttachedImageView(
                            imageUriOrPath = path,
                            onDeleteImage = { imageUri = null },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    audioPath?.let { audio ->
                        AudioPlayerView(
                            audioPath = audio,
                            onDeleteAudio = { audioPath = null },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Conteúdo da nota") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    NoteCustomizationToolbar(
                        isPinned = isPinned,
                        onTogglePin = { isPinned = !isPinned },
                        isLocked = isLocked,
                        onToggleLock = {
                            if (isLocked) {
                                isLocked = false
                            } else {
                                showSetPinDialog = true
                            }
                        },
                        onInsertText = { token ->
                            content = if (content.isBlank()) token.trimStart() else "$content$token"
                        },
                        onAddImage = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onStartVoiceRecording = {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                isRecordingVoice = true
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Cor do Cartão",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ColorPalette.options.forEach { colorOption ->
                            val isSelected = colorOption.hex.equals(colorHex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkTheme) colorOption.darkColor else colorOption.lightColor)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { colorHex = colorOption.hex }
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
            },
            confirmButton = {
                Button(
                    onClick = { saveAndClose() },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Salvar", maxLines = 1, softWrap = false)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", maxLines = 1, softWrap = false)
                }
            }
        )
    }
}

@Composable
fun CreateNoteDialog(
    isDarkTheme: Boolean,
    initialTitle: String = "",
    initialContent: String = "",
    initialImageUri: String? = null,
    startFullscreen: Boolean = false,
    startVoiceRecording: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        content: String,
        colorHex: String,
        isPinned: Boolean,
        isLocked: Boolean,
        lockPin: String?,
        imageUri: String?,
        audioPath: String?,
        category: String
    ) -> Unit
) {
    val context = LocalContext.current

    var isFullscreen by remember { mutableStateOf(startFullscreen) }
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    var colorHex by remember { mutableStateOf("#FFFFFF") }
    var category by remember { mutableStateOf("Geral") }
    var isPinned by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var lockPin by remember { mutableStateOf<String?>("1234") }
    var imageUri by remember { mutableStateOf(initialImageUri) }
    var audioPath by remember { mutableStateOf<String?>(null) }

    var isRecordingVoice by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val local = copyUriToAppStorage(context, uri)
            if (local != null) {
                imageUri = local
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isRecordingVoice = true
        } else {
            Toast.makeText(context, "Permissão de gravação necessária", Toast.LENGTH_SHORT).show()
        }
    }

    if (isRecordingVoice) {
        VoiceRecordingDialog(
            onDismiss = { isRecordingVoice = false },
            onFinishRecording = { path ->
                audioPath = path
                isRecordingVoice = false
            }
        )
    }

    if (showSetPinDialog) {
        SetPinDialog(
            initialPin = lockPin,
            onDismiss = { showSetPinDialog = false },
            onSavePin = { pin ->
                lockPin = pin
                isLocked = true
                showSetPinDialog = false
            }
        )
    }

    // Botão "+" > Áudio: já começa gravando
    LaunchedEffect(Unit) {
        if (startVoiceRecording) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) isRecordingVoice = true else audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun saveAndClose() {
        if (title.isNotBlank() || content.isNotBlank() || imageUri != null || audioPath != null) {
            onSave(
                title.trim(),
                content.trim(),
                colorHex,
                isPinned,
                isLocked,
                if (isLocked) lockPin else null,
                imageUri,
                audioPath,
                category
            )
        }
    }

    if (isFullscreen) {
        KeepStyleFullscreenNoteEditor(
            title = title,
            onTitleChange = { title = it },
            content = content,
            onContentChange = { content = it },
            colorHex = colorHex,
            onColorChange = { colorHex = it },
            isPinned = isPinned,
            onTogglePin = { isPinned = !isPinned },
            isLocked = isLocked,
            onToggleLock = {
                if (isLocked) {
                    isLocked = false
                } else {
                    showSetPinDialog = true
                }
            },
            imageUri = imageUri,
            onDeleteImage = { imageUri = null },
            onAddImage = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            audioPath = audioPath,
            onDeleteAudio = { audioPath = null },
            onStartVoiceRecording = {
                val hasPerm = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPerm) {
                    isRecordingVoice = true
                } else {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onSave = { saveAndClose() },
            onDismiss = onDismiss,
            isDarkTheme = isDarkTheme
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nova Nota", fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isFullscreen = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Abrir nota em tela cheia",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { isPinned = !isPinned },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Fixar",
                                tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isLocked) {
                                    isLocked = false
                                } else {
                                    showSetPinDialog = true
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Bloquear",
                                tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    imageUri?.let { path ->
                        AttachedImageView(
                            imageUriOrPath = path,
                            onDeleteImage = { imageUri = null },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    audioPath?.let { audio ->
                        AudioPlayerView(
                            audioPath = audio,
                            onDeleteAudio = { audioPath = null },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título") },
                        placeholder = { Text("Título da nota...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Conteúdo") },
                        placeholder = { Text("Escreva sua nota, markdown ou checklist...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("note_content_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    NoteCustomizationToolbar(
                        isPinned = isPinned,
                        onTogglePin = { isPinned = !isPinned },
                        isLocked = isLocked,
                        onToggleLock = {
                            if (isLocked) {
                                isLocked = false
                            } else {
                                showSetPinDialog = true
                            }
                        },
                        onInsertText = { token ->
                            content = if (content.isBlank()) token.trimStart() else "$content$token"
                        },
                        onAddImage = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onStartVoiceRecording = {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                isRecordingVoice = true
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Cor do Cartão",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ColorPalette.options.forEach { colorOption ->
                            val isSelected = colorOption.hex.equals(colorHex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkTheme) colorOption.darkColor else colorOption.lightColor)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { colorHex = colorOption.hex }
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
            },
            confirmButton = {
                Button(
                    onClick = { saveAndClose() },
                    modifier = Modifier.testTag("save_note_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Salvar", maxLines = 1, softWrap = false)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", maxLines = 1, softWrap = false)
                }
            }
        )
    }
}
