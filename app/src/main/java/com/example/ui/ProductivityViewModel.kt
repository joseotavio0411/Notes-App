package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DeadlineNoteEntity
import com.example.data.local.entity.DeadlineTaskEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.ProductivityRepository
import com.example.notification.NotificationHelper
import com.example.ui.components.CryptoHelper
import com.example.ui.components.DateTimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductivityRepository
    private val themePrefs = application.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)

    // Current tab index (0: Notes, 1: Tasks, 2: Deadline Tasks, 3: Deadline Notes)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Dark theme toggle - persisted across app restarts
    private val _isDarkTheme = MutableStateFlow<Boolean?>(
        if (themePrefs.contains("is_dark_theme")) themePrefs.getBoolean("is_dark_theme", false) else null
    )
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    // Notes display mode (true = grid, false = list) - persisted across app restarts
    private val _isNotesGridMode = MutableStateFlow(
        themePrefs.getBoolean("is_notes_grid_mode", true)
    )
    val isNotesGridMode: StateFlow<Boolean> = _isNotesGridMode.asStateFlow()

    // Decoupled lightweight UI ticker:
    // Emits current timestamp every 10s only while UI is actively subscribed (0% CPU when in background)
    val currentTimeMillis: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(10_000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    // Session-only unlocked note and task IDs (locks again when app process terminates)
    private val _unlockedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedNoteIds: StateFlow<Set<Long>> = _unlockedNoteIds.asStateFlow()

    private val _unlockedDeadlineNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedDeadlineNoteIds: StateFlow<Set<Long>> = _unlockedDeadlineNoteIds.asStateFlow()

    private val _unlockedTaskIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedTaskIds: StateFlow<Set<Long>> = _unlockedTaskIds.asStateFlow()

    private val _unlockedDeadlineTaskIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedDeadlineTaskIds: StateFlow<Set<Long>> = _unlockedDeadlineTaskIds.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProductivityRepository(db.productivityDao())

        // Initialize deadline notifications channel
        NotificationHelper.initNotificationChannel(application)

        // Initial one-time cleanup on startup for items expired while app was closed
        purgeExpiredItemsOnDemand()

        // Background observer for reliable notifications (1h, 24h, expired)
        viewModelScope.launch(Dispatchers.IO) {
            combine(repository.allDeadlineTasks, repository.allDeadlineNotes, currentTimeMillis) { tasks, notes, _ ->
                NotificationHelper.checkAndTriggerDueReminders(application, tasks, notes)
            }.collect()
        }
    }

    /**
     * Event-driven purge: executes SQLite batch delete only on actual state events (startup or item changes),
     * and handles recurring items by renewing their deadline instead of removing them.
     */
    fun purgeExpiredItemsOnDemand() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            // Renew recurring deadline notes that passed deadline
            val expiredRecurringNotes = repository.getExpiredRecurringDeadlineNotes(now)
            for (note in expiredRecurringNotes) {
                val nextDeadline = DateTimeHelper.getNextRecurrence(note.deadlineTimestamp, note.recurrence)
                repository.updateDeadlineNote(note.copy(deadlineTimestamp = nextDeadline))
            }

            // Remove expired completed tasks (new instances for recurring tasks were already created for the next period)
            // and remove non-recurring expired notes
            repository.deleteExpiredCompletedTasks(now)
            repository.deleteExpiredNotes(now)
        }
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun toggleDarkTheme(currentSystemDark: Boolean) {
        val current = _isDarkTheme.value ?: currentSystemDark
        val next = !current
        _isDarkTheme.value = next
        themePrefs.edit().putBoolean("is_dark_theme", next).apply()
    }

    fun toggleNotesGridMode() {
        val next = !_isNotesGridMode.value
        _isNotesGridMode.value = next
        themePrefs.edit().putBoolean("is_notes_grid_mode", next).apply()
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

    fun unlockTask(taskId: Long, enteredPin: String, actualPin: String?): Boolean {
        return if (CryptoHelper.verifyPin(enteredPin.trim(), actualPin)) {
            _unlockedTaskIds.value = _unlockedTaskIds.value + taskId
            true
        } else {
            false
        }
    }

    fun unlockTaskDirectly(taskId: Long) {
        _unlockedTaskIds.value = _unlockedTaskIds.value + taskId
    }

    fun unlockDeadlineTask(taskId: Long, enteredPin: String, actualPin: String?): Boolean {
        return if (CryptoHelper.verifyPin(enteredPin.trim(), actualPin)) {
            _unlockedDeadlineTaskIds.value = _unlockedDeadlineTaskIds.value + taskId
            true
        } else {
            false
        }
    }

    fun unlockDeadlineTaskDirectly(taskId: Long) {
        _unlockedDeadlineTaskIds.value = _unlockedDeadlineTaskIds.value + taskId
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
        audioPath: String? = null,
        fontSize: Int = 16,
        fontFamily: String = "DEFAULT"
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
                    fontSize = fontSize,
                    fontFamily = fontFamily,
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
    val allTasks: StateFlow<List<TaskEntity>> = combine(
        repository.allTasks,
        currentTimeMillis
    ) { tasks, now ->
        tasks.filter { task ->
            task.visibleFrom <= now
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(text: String, isPinned: Boolean = false, recurrence: String = "NONE") {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    text = text.trim(),
                    isCompleted = false,
                    isPinned = isPinned,
                    recurrence = recurrence,
                    visibleFrom = 0L,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val willBeCompleted = !task.isCompleted
            val now = System.currentTimeMillis()
            repository.updateTask(task.copy(isCompleted = willBeCompleted))

            if (willBeCompleted && task.recurrence != "NONE") {
                // When recurring task is completed normally, the new recurrence instance
                // will only appear at the beginning of the period (e.g. 00:00 of next day/month)
                val nextPeriodStart = DateTimeHelper.getNextPeriodStart(task.recurrence, now)
                repository.insertTask(
                    TaskEntity(
                        text = task.text,
                        isCompleted = false,
                        isPinned = task.isPinned,
                        isLocked = task.isLocked,
                        lockPin = task.lockPin,
                        recurrence = task.recurrence,
                        visibleFrom = nextPeriodStart,
                        createdAt = now
                    )
                )
            } else if (!willBeCompleted && task.recurrence != "NONE") {
                // If user unchecks the completed task, cancel any pending future task for it
                repository.deletePendingFutureTasks(task.text, task.recurrence, now)
            }
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
    // 3. If recurring task is newly scheduled for a future period, only appear when visibleFrom <= now.
    val activeDeadlineTasks: StateFlow<List<DeadlineTaskEntity>> = combine(
        repository.allDeadlineTasks,
        currentTimeMillis
    ) { tasks, now ->
        tasks.filter { task ->
            val isVisible = task.visibleFrom <= now
            val isExpired = task.deadlineTimestamp < now
            isVisible && !(task.isCompleted && isExpired)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDeadlineTask(
        text: String,
        deadlineMillis: Long,
        isPinned: Boolean = false,
        recurrence: String = "NONE"
    ) {
        if (text.isBlank() || deadlineMillis <= 0) return
        viewModelScope.launch {
            val newId = repository.insertDeadlineTask(
                DeadlineTaskEntity(
                    text = text.trim(),
                    isCompleted = false,
                    deadlineTimestamp = deadlineMillis,
                    isPinned = isPinned,
                    recurrence = recurrence,
                    visibleFrom = 0L,
                    createdAt = System.currentTimeMillis()
                )
            )
            NotificationHelper.scheduleAlarms(
                context = getApplication(),
                id = newId,
                itemType = "TASK",
                title = text.trim(),
                deadlineTimestamp = deadlineMillis
            )
            purgeExpiredItemsOnDemand()
        }
    }

    fun toggleDeadlineTaskCompletion(task: DeadlineTaskEntity) {
        viewModelScope.launch {
            val willBeCompleted = !task.isCompleted
            val now = System.currentTimeMillis()
            val updated = task.copy(isCompleted = willBeCompleted)
            repository.updateDeadlineTask(updated)

            if (willBeCompleted) {
                NotificationHelper.cancelAlarms(getApplication(), task.id, "TASK")
            } else {
                NotificationHelper.scheduleAlarms(
                    context = getApplication(),
                    id = task.id,
                    itemType = "TASK",
                    title = task.text,
                    deadlineTimestamp = task.deadlineTimestamp
                )
            }

            if (willBeCompleted && task.recurrence != "NONE") {
                // 1. Current task is marked as completed normally (stays visible until its deadline passes).
                // 2. The new task arising from recurrence only appears at the start of the next period (e.g. 00:00).
                val nextPeriodStart = DateTimeHelper.getNextPeriodStart(task.recurrence, now)
                var nextDeadline = DateTimeHelper.getNextRecurrence(task.deadlineTimestamp, task.recurrence)
                while (nextDeadline < nextPeriodStart) {
                    nextDeadline = DateTimeHelper.getNextRecurrence(nextDeadline, task.recurrence)
                }
                val newRecurringId = repository.insertDeadlineTask(
                    DeadlineTaskEntity(
                        text = task.text,
                        isCompleted = false,
                        deadlineTimestamp = nextDeadline,
                        isPinned = task.isPinned,
                        isLocked = task.isLocked,
                        lockPin = task.lockPin,
                        recurrence = task.recurrence,
                        visibleFrom = nextPeriodStart,
                        createdAt = now
                    )
                )
                NotificationHelper.scheduleAlarms(
                    context = getApplication(),
                    id = newRecurringId,
                    itemType = "TASK",
                    title = task.text,
                    deadlineTimestamp = nextDeadline
                )
            } else if (!willBeCompleted && task.recurrence != "NONE") {
                // If user unchecks the completed task, cancel any pending future task for it
                repository.deletePendingFutureDeadlineTasks(task.text, task.recurrence, now)
            }

            // If completed task is already expired, purge immediately
            if (updated.isCompleted && updated.deadlineTimestamp < now) {
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
        NotificationHelper.cancelAlarms(getApplication(), task.id, "TASK")
        viewModelScope.launch {
            repository.deleteDeadlineTask(task)
        }
    }

    // --- Tab 4: Notas Diárias (Com Prazo) ---
    // Rule:
    // If deadline passed, the note must be deleted/hidden automatically (unless recurring).
    val activeDeadlineNotes: StateFlow<List<DeadlineNoteEntity>> = combine(
        repository.allDeadlineNotes,
        currentTimeMillis
    ) { notes, now ->
        notes.filter { note ->
            val isVisible = note.visibleFrom <= now
            isVisible && (note.deadlineTimestamp >= now || note.recurrence != "NONE")
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
        recurrence: String = "NONE",
        imageUri: String? = null,
        audioPath: String? = null,
        fontSize: Int = 16,
        fontFamily: String = "DEFAULT"
    ) {
        if ((title.isBlank() && content.isBlank() && imageUri.isNullOrBlank() && audioPath.isNullOrBlank()) || deadlineMillis <= 0) return
        val securedPin = if (isLocked && !lockPin.isNullOrBlank()) {
            CryptoHelper.hashPin(lockPin.trim())
        } else null

        viewModelScope.launch {
            val newId = repository.insertDeadlineNote(
                DeadlineNoteEntity(
                    title = title.trim(),
                    content = content.trim(),
                    deadlineTimestamp = deadlineMillis,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    isLocked = isLocked,
                    lockPin = securedPin,
                    recurrence = recurrence,
                    imageUri = imageUri,
                    audioPath = audioPath,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    createdAt = System.currentTimeMillis()
                )
            )
            NotificationHelper.scheduleAlarms(
                context = getApplication(),
                id = newId,
                itemType = "NOTE",
                title = title.trim().ifBlank { content.trim().take(30) },
                deadlineTimestamp = deadlineMillis
            )
            purgeExpiredItemsOnDemand()
        }
    }

    fun updateDeadlineNote(note: DeadlineNoteEntity) {
        viewModelScope.launch {
            repository.updateDeadlineNote(note)
            NotificationHelper.scheduleAlarms(
                context = getApplication(),
                id = note.id,
                itemType = "NOTE",
                title = note.title.ifBlank { note.content.take(30) },
                deadlineTimestamp = note.deadlineTimestamp
            )
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
        NotificationHelper.cancelAlarms(getApplication(), note.id, "NOTE")
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
