package com.example.ui.screens

import android.Manifest
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
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.material3.ElevatedCard
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
import com.example.ui.components.AttachedImageView
import com.example.ui.components.AudioPlayerView
import com.example.ui.components.ColorPalette
import com.example.ui.components.DateTimeHelper
import com.example.ui.components.DeliberateEmptyState
import com.example.ui.components.LockedNoteOverlay
import com.example.ui.components.NoteCustomizationToolbar
import com.example.ui.components.PinUnlockDialog
import com.example.ui.components.RichContentView
import com.example.ui.components.SetPinDialog
import com.example.ui.components.VoiceRecordingDialog
import com.example.ui.components.copyUriToAppStorage

@Composable
fun NotesScreen(
    notes: List<NoteEntity>,
    isGridMode: Boolean,
    onToggleGridMode: () -> Unit,
    onAddNote: (title: String, content: String, colorHex: String, isPinned: Boolean, isLocked: Boolean, lockPin: String?, imageUri: String?, audioPath: String?) -> Unit,
    onUpdateNote: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onTogglePinNote: (NoteEntity) -> Unit,
    onToggleChecklistItem: (NoteEntity, Int) -> Unit,
    unlockedNoteIds: Set<Long>,
    onUnlockNote: (noteId: Long, enteredPin: String, actualPin: String?) -> Boolean,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteToUnlock by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var startInFullscreen by remember { mutableStateOf(false) }
    var pendingFullscreenOpen by remember { mutableStateOf(false) }

    // Dialog for creating a new note
    if (showAddNoteDialog) {
        CreateNoteDialog(
            isDarkTheme = isDarkTheme,
            onDismiss = { showAddNoteDialog = false },
            onSave = { title, content, colorHex, isPinned, isLocked, lockPin, imageUri, audioPath ->
                onAddNote(title, content, colorHex, isPinned, isLocked, lockPin, imageUri, audioPath)
                showAddNoteDialog = false
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
                val actual = note.lockPin ?: "1234"
                onUnlockNote(note.id, actual, note.lockPin)
                val openFs = pendingFullscreenOpen
                noteToUnlock = null
                startInFullscreen = openFs
                noteToEdit = note
                Toast.makeText(context, "Nota desbloqueada", Toast.LENGTH_SHORT).show()
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

        // Section header with counter & View Mode toggle (Grid / List)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Minhas Notas (${notes.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

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

        // Notes List or Grid
        if (notes.isEmpty()) {
            DeliberateEmptyState(
                title = "Nenhuma nota criada ainda",
                description = "Toque no botão \"+\" no canto inferior direito para criar sua primeira anotação.",
                icon = Icons.Default.Description,
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
                        item(span = StaggeredGridItemSpan.FullLine) {
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

                        items(pinnedNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                isDarkTheme = isDarkTheme,
                                isUnlocked = !note.isLocked || (note.id in unlockedNoteIds),
                                onUnlock = {
                                    pendingFullscreenOpen = false
                                    noteToUnlock = note
                                },
                                onTogglePin = { onTogglePinNote(note) },
                                onToggleCheckbox = { lineIdx -> onToggleChecklistItem(note, lineIdx) },
                                onEdit = {
                                    if (note.isLocked && note.id !in unlockedNoteIds) {
                                        pendingFullscreenOpen = false
                                        noteToUnlock = note
                                    } else {
                                        startInFullscreen = false
                                        noteToEdit = note
                                    }
                                },
                                onOpenFullscreen = {
                                    if (note.isLocked && note.id !in unlockedNoteIds) {
                                        pendingFullscreenOpen = true
                                        noteToUnlock = note
                                    } else {
                                        startInFullscreen = true
                                        noteToEdit = note
                                    }
                                },
                                onDelete = { noteToDelete = note }
                            )
                        }

                        if (otherNotes.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
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

                    items(otherNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            isDarkTheme = isDarkTheme,
                            isUnlocked = !note.isLocked || (note.id in unlockedNoteIds),
                            onUnlock = {
                                pendingFullscreenOpen = false
                                noteToUnlock = note
                            },
                            onTogglePin = { onTogglePinNote(note) },
                            onToggleCheckbox = { lineIdx -> onToggleChecklistItem(note, lineIdx) },
                            onEdit = {
                                if (note.isLocked && note.id !in unlockedNoteIds) {
                                    pendingFullscreenOpen = false
                                    noteToUnlock = note
                                } else {
                                    startInFullscreen = false
                                    noteToEdit = note
                                }
                            },
                            onOpenFullscreen = {
                                if (note.isLocked && note.id !in unlockedNoteIds) {
                                    pendingFullscreenOpen = true
                                    noteToUnlock = note
                                } else {
                                    startInFullscreen = true
                                    noteToEdit = note
                                }
                            },
                            onDelete = { noteToDelete = note }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (pinnedNotes.isNotEmpty()) {
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

                        items(pinnedNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                isDarkTheme = isDarkTheme,
                                isUnlocked = !note.isLocked || (note.id in unlockedNoteIds),
                                onUnlock = {
                                    pendingFullscreenOpen = false
                                    noteToUnlock = note
                                },
                                onTogglePin = { onTogglePinNote(note) },
                                onToggleCheckbox = { lineIdx -> onToggleChecklistItem(note, lineIdx) },
                                onEdit = {
                                    if (note.isLocked && note.id !in unlockedNoteIds) {
                                        pendingFullscreenOpen = false
                                        noteToUnlock = note
                                    } else {
                                        startInFullscreen = false
                                        noteToEdit = note
                                    }
                                },
                                onOpenFullscreen = {
                                    if (note.isLocked && note.id !in unlockedNoteIds) {
                                        pendingFullscreenOpen = true
                                        noteToUnlock = note
                                    } else {
                                        startInFullscreen = true
                                        noteToEdit = note
                                    }
                                },
                                onDelete = { noteToDelete = note }
                            )
                        }

                        if (otherNotes.isNotEmpty()) {
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

                    items(otherNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            isDarkTheme = isDarkTheme,
                            isUnlocked = !note.isLocked || (note.id in unlockedNoteIds),
                            onUnlock = {
                                pendingFullscreenOpen = false
                                noteToUnlock = note
                            },
                            onTogglePin = { onTogglePinNote(note) },
                            onToggleCheckbox = { lineIdx -> onToggleChecklistItem(note, lineIdx) },
                            onEdit = {
                                if (note.isLocked && note.id !in unlockedNoteIds) {
                                    pendingFullscreenOpen = false
                                    noteToUnlock = note
                                } else {
                                    startInFullscreen = false
                                    noteToEdit = note
                                }
                            },
                            onOpenFullscreen = {
                                if (note.isLocked && note.id !in unlockedNoteIds) {
                                    pendingFullscreenOpen = true
                                    noteToUnlock = note
                                } else {
                                    startInFullscreen = true
                                    noteToEdit = note
                                }
                            },
                            onDelete = { noteToDelete = note }
                        )
                    }
                }
            }
        }
    }

    // Minimalist circular Floating Action Button with app theme color
    FloatingActionButton(
        onClick = { showAddNoteDialog = true },
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
            .testTag("fab_add_note")
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Adicionar Nota",
            modifier = Modifier.size(28.dp)
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
            }
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
fun NoteCard(
    note: NoteEntity,
    isDarkTheme: Boolean,
    isUnlocked: Boolean,
    onUnlock: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleCheckbox: (Int) -> Unit,
    onEdit: () -> Unit,
    onOpenFullscreen: () -> Unit,
    onDelete: () -> Unit
) {
    val textColor = ColorPalette.getOptimalTextColor(note.colorHex, isDarkTheme)
    val secondaryTextColor = textColor.copy(alpha = 0.75f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorPalette.getSurfaceColor(note.colorHex, isDarkTheme)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
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
                    .padding(12.dp)
            ) {
                // Attached Image
                note.imageUri?.let { imgPath ->
                    AttachedImageView(
                        imageUriOrPath = imgPath,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Attached Audio
                note.audioPath?.let { audioPath ->
                    AudioPlayerView(
                        audioPath = audioPath,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Title row with Pin & Lock indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    if (note.title.isNotBlank()) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor,
                            maxLines = 2,
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
                                    .size(18.dp)
                                    .padding(end = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = onTogglePin,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = if (note.isPinned) "Desafixar" else "Fixar nota",
                                tint = if (note.isPinned) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (note.title.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Rich content with interactive checklists
                if (note.content.isNotBlank()) {
                    RichContentView(
                        content = note.content,
                        maxLines = 8,
                        onToggleCheckbox = onToggleCheckbox
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Footer with full screen and delete actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onOpenFullscreen,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Abrir nota em tela cheia",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir Nota",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditNoteDialog(
    note: NoteEntity,
    isDarkTheme: Boolean,
    startFullscreen: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit
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
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    if (isFullscreen) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                color = ColorPalette.getSurfaceColor(colorHex, isDarkTheme)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Fullscreen Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Fechar tela cheia"
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Nota em Tela Cheia",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isFullscreen = false },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FullscreenExit,
                                    contentDescription = "Reduzir tela",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = { isPinned = !isPinned },
                                modifier = Modifier.size(40.dp)
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
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Bloquear",
                                    tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = { saveAndClose() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Salvar")
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp)
                    ) {
                        imageUri?.let { path ->
                            AttachedImageView(
                                imageUriOrPath = path,
                                onDeleteImage = { imageUri = null },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        audioPath?.let { audio ->
                            AudioPlayerView(
                                audioPath = audio,
                                onDeleteAudio = { audioPath = null },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Título da nota") },
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Conteúdo da nota (suporta checklists [ ] e markdown)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 280.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

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

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Cor da Nota",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ColorPalette.options.forEach { colorOption ->
                                val isSelected = colorOption.hex.equals(colorHex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isDarkTheme) colorOption.darkColor else colorOption.lightColor)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
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
                                                .size(16.dp)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
                }
            },
            confirmButton = {
                Button(
                    onClick = { saveAndClose() },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CreateNoteDialog(
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        content: String,
        colorHex: String,
        isPinned: Boolean,
        isLocked: Boolean,
        lockPin: String?,
        imageUri: String?,
        audioPath: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var isFullscreen by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var colorHex by remember { mutableStateOf("#FFFFFF") }
    var isPinned by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var lockPin by remember { mutableStateOf<String?>("1234") }
    var imageUri by remember { mutableStateOf<String?>(null) }
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
                audioPath
            )
        }
    }

    if (isFullscreen) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                color = ColorPalette.getSurfaceColor(colorHex, isDarkTheme)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Fullscreen Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Fechar tela cheia"
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Nova Nota em Tela Cheia",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isFullscreen = false },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FullscreenExit,
                                    contentDescription = "Reduzir tela",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = { isPinned = !isPinned },
                                modifier = Modifier.size(40.dp)
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
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Bloquear",
                                    tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = { saveAndClose() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Salvar")
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp)
                    ) {
                        imageUri?.let { path ->
                            AttachedImageView(
                                imageUriOrPath = path,
                                onDeleteImage = { imageUri = null },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        audioPath?.let { audio ->
                            AudioPlayerView(
                                audioPath = audio,
                                onDeleteAudio = { audioPath = null },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Título da nota") },
                            placeholder = { Text("Título da nota...") },
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Conteúdo da nota (suporta checklists [ ] e markdown)") },
                            placeholder = { Text("Escreva sua nota, markdown ou checklist...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 280.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

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

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Cor da Nota",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ColorPalette.options.forEach { colorOption ->
                                val isSelected = colorOption.hex.equals(colorHex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isDarkTheme) colorOption.darkColor else colorOption.lightColor)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
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
                                                .size(16.dp)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
                }
            },
            confirmButton = {
                Button(
                    onClick = { saveAndClose() },
                    modifier = Modifier.testTag("save_note_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}
