package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DeadlineNoteEntity
import com.example.data.local.entity.DeadlineTaskEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.ProductivityRepository
import com.example.ui.components.CryptoHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductivityRepository

    // Current tab index (0: Notes, 1: Tasks, 2: Deadline Tasks, 3: Deadline Notes)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Dark theme toggle
    private val _isDarkTheme = MutableStateFlow<Boolean?>(null) // null = system default
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    // Notes display mode (true = grid, false = list)
    private val _isNotesGridMode = MutableStateFlow(true)
    val isNotesGridMode: StateFlow<Boolean> = _isNotesGridMode.asStateFlow()

    // Decoupled lightweight UI ticker:
    // Emits current timestamp every 10s only while UI is actively subscribed (0% CPU when in background)
    val currentTimeMillis: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(10_000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    // Session-only unlocked note IDs (locks again when app process terminates)
    private val _unlockedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedNoteIds: StateFlow<Set<Long>> = _unlockedNoteIds.asStateFlow()

    private val _unlockedDeadlineNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedDeadlineNoteIds: StateFlow<Set<Long>> = _unlockedDeadlineNoteIds.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProductivityRepository(db.productivityDao())

        // Initial one-time cleanup on startup for items expired while app was closed
        purgeExpiredItemsOnDemand()
    }

    /**
     * Event-driven purge: executes SQLite batch delete only on actual state events (startup or item changes),
     * avoiding infinite loops or continuous disk I/O.
     */
    fun purgeExpiredItemsOnDemand() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            repository.deleteExpiredCompletedTasks(now)
            repository.deleteExpiredNotes(now)
        }
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun toggleDarkTheme(currentSystemDark: Boolean) {
        val current = _isDarkTheme.value ?: currentSystemDark
        _isDarkTheme.value = !current
    }

    fun toggleNotesGridMode() {
        _isNotesGridMode.value = !_isNotesGridMode.value
    }

    // --- Lock & Unlock Management ---
    fun unlockNote(noteId: Long, enteredPin: String, actualPin: String?): Boolean {
        return if (CryptoHelper.verifyPin(enteredPin.trim(), actualPin)) {
            _unlockedNoteIds.value = _unlockedNoteIds.value + noteId
            true
        } else {
            false
        }
    }

    fun unlockNoteDirectly(noteId: Long) {
        _unlockedNoteIds.value = _unlockedNoteIds.value + noteId
    }

    fun unlockDeadlineNote(noteId: Long, enteredPin: String, actualPin: String?): Boolean {
        return if (CryptoHelper.verifyPin(enteredPin.trim(), actualPin)) {
            _unlockedDeadlineNoteIds.value = _unlockedDeadlineNoteIds.value + noteId
            true
        } else {
            false
        }
    }

    fun unlockDeadlineNoteDirectly(noteId: Long) {
        _unlockedDeadlineNoteIds.value = _unlockedDeadlineNoteIds.value + noteId
    }

    // --- Tab 1: Notes (Bloco de Notas) ---
    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(
        title: String,
        content: String,
        colorHex: String = "#FFFFFF",
        isPinned: Boolean = false,
        isLocked: Boolean = false,
        lockPin: String? = null,
        imageUri: String? = null,
        audioPath: String? = null
    ) {
        if (title.isBlank() && content.isBlank() && imageUri.isNullOrBlank() && audioPath.isNullOrBlank()) return
        val securedPin = if (isLocked && !lockPin.isNullOrBlank()) {
            CryptoHelper.hashPin(lockPin.trim())
        } else null

        viewModelScope.launch {
            repository.insertNote(
                NoteEntity(
                    title = title.trim(),
                    content = content.trim(),
                    colorHex = colorHex,
                    isPinned = isPinned,
                    isLocked = isLocked,
                    lockPin = securedPin,
                    imageUri = imageUri,
                    audioPath = audioPath,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun togglePinNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun toggleNoteChecklistItem(note: NoteEntity, lineIndex: Int) {
        val lines = note.content.lines().toMutableList()
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            lines[lineIndex] = when {
                line.startsWith("[ ] ") -> "[x] " + line.removePrefix("[ ] ")
                line.startsWith("[x] ") -> "[ ] " + line.removePrefix("[x] ")
                line.startsWith("- [ ] ") -> "- [x] " + line.removePrefix("- [ ] ")
                line.startsWith("- [x] ") -> "- [ ] " + line.removePrefix("- [ ] ")
                else -> line
            }
            updateNote(note.copy(content = lines.joinToString("\n")))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // --- Tab 2: Tarefas ---
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(text: String, isPinned: Boolean = false) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    text = text.trim(),
                    isCompleted = false,
                    isPinned = isPinned,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun togglePinTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isPinned = !task.isPinned))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Tab 3: Tarefas Diárias (Com Prazo) ---
    // Rule:
    // 1. If not completed and deadline passed -> show in red highlight (stay visible).
    // 2. If completed and deadline passed -> automatically disappear.
    val activeDeadlineTasks: StateFlow<List<DeadlineTaskEntity>> = combine(
        repository.allDeadlineTasks,
        currentTimeMillis
    ) { tasks, now ->
        tasks.filter { task ->
            // If it is completed and expired, it must disappear automatically
            val isExpired = task.deadlineTimestamp < now
            !(task.isCompleted && isExpired)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDeadlineTask(text: String, deadlineMillis: Long, isPinned: Boolean = false) {
        if (text.isBlank() || deadlineMillis <= 0) return
        viewModelScope.launch {
            repository.insertDeadlineTask(
                DeadlineTaskEntity(
                    text = text.trim(),
                    isCompleted = false,
                    deadlineTimestamp = deadlineMillis,
                    isPinned = isPinned,
                    createdAt = System.currentTimeMillis()
                )
            )
            purgeExpiredItemsOnDemand()
        }
    }

    fun toggleDeadlineTaskCompletion(task: DeadlineTaskEntity) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateDeadlineTask(updated)
            // If now completed and already expired, trigger cleanup
            if (updated.isCompleted && updated.deadlineTimestamp < System.currentTimeMillis()) {
                purgeExpiredItemsOnDemand()
            }
        }
    }

    fun togglePinDeadlineTask(task: DeadlineTaskEntity) {
        viewModelScope.launch {
            repository.updateDeadlineTask(task.copy(isPinned = !task.isPinned))
        }
    }

    fun deleteDeadlineTask(task: DeadlineTaskEntity) {
        viewModelScope.launch {
            repository.deleteDeadlineTask(task)
        }
    }

    // --- Tab 4: Notas Diárias (Com Prazo) ---
    // Rule:
    // If deadline passed, the note must be deleted/hidden automatically.
    val activeDeadlineNotes: StateFlow<List<DeadlineNoteEntity>> = combine(
        repository.allDeadlineNotes,
        currentTimeMillis
    ) { notes, now ->
        notes.filter { note ->
            note.deadlineTimestamp >= now
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDeadlineNote(
        title: String,
        content: String,
        deadlineMillis: Long,
        colorHex: String = "#FFFFFF",
        isPinned: Boolean = false,
        isLocked: Boolean = false,
        lockPin: String? = null,
        imageUri: String? = null,
        audioPath: String? = null
    ) {
        if ((title.isBlank() && content.isBlank() && imageUri.isNullOrBlank() && audioPath.isNullOrBlank()) || deadlineMillis <= 0) return
        val securedPin = if (isLocked && !lockPin.isNullOrBlank()) {
            CryptoHelper.hashPin(lockPin.trim())
        } else null

        viewModelScope.launch {
            repository.insertDeadlineNote(
                DeadlineNoteEntity(
                    title = title.trim(),
                    content = content.trim(),
                    deadlineTimestamp = deadlineMillis,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    isLocked = isLocked,
                    lockPin = securedPin,
                    imageUri = imageUri,
                    audioPath = audioPath,
                    createdAt = System.currentTimeMillis()
                )
            )
            purgeExpiredItemsOnDemand()
        }
    }

    fun updateDeadlineNote(note: DeadlineNoteEntity) {
        viewModelScope.launch {
            repository.updateDeadlineNote(note)
        }
    }

    fun togglePinDeadlineNote(note: DeadlineNoteEntity) {
        viewModelScope.launch {
            repository.updateDeadlineNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun toggleDeadlineNoteChecklistItem(note: DeadlineNoteEntity, lineIndex: Int) {
        val lines = note.content.lines().toMutableList()
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            lines[lineIndex] = when {
                line.startsWith("[ ] ") -> "[x] " + line.removePrefix("[ ] ")
                line.startsWith("[x] ") -> "[ ] " + line.removePrefix("[x] ")
                line.startsWith("- [ ] ") -> "- [x] " + line.removePrefix("- [ ] ")
                line.startsWith("- [x] ") -> "- [ ] " + line.removePrefix("- [ ] ")
                else -> line
            }
            updateDeadlineNote(note.copy(content = lines.joinToString("\n")))
        }
    }

    fun deleteDeadlineNote(note: DeadlineNoteEntity) {
        viewModelScope.launch {
            repository.deleteDeadlineNote(note)
        }
    }

    // --- Undo / Restore actions ---
    fun restoreTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun restoreNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.insertNote(note)
        }
    }

    fun restoreDeadlineTask(task: DeadlineTaskEntity) {
        viewModelScope.launch {
            repository.insertDeadlineTask(task)
        }
    }

    fun restoreDeadlineNote(note: DeadlineNoteEntity) {
        viewModelScope.launch {
            repository.insertDeadlineNote(note)
        }
    }
}
