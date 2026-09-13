package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupHelper
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
import kotlinx.coroutines.withContext

class ProductivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductivityRepository(AppDatabase.getDatabase(application).productivityDao())
    private val themePrefs = application.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)

    // Current tab index (0: Notes, 1: Tasks, 2: Deadline Tasks, 3: Deadline Notes)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Dark theme toggle - persisted across app restarts
    private val _isDarkTheme = MutableStateFlow<Boolean?>(
        if (themePrefs.contains("is_dark_theme")) themePrefs.getBoolean("is_dark_theme", false) else null
    )
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    // Category filter state ("Todas" or category name)
    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

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

    // Session-only unlocked note and task IDs (locks again when app process terminates or minimizes)
    private val _unlockedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedNoteIds: StateFlow<Set<Long>> = _unlockedNoteIds.asStateFlow()

    private val _unlockedDeadlineNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedDeadlineNoteIds: StateFlow<Set<Long>> = _unlockedDeadlineNoteIds.asStateFlow()

    private val _unlockedTaskIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedTaskIds: StateFlow<Set<Long>> = _unlockedTaskIds.asStateFlow()

    private val _unlockedDeadlineTaskIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedDeadlineTaskIds: StateFlow<Set<Long>> = _unlockedDeadlineTaskIds.asStateFlow()

    // --- Recycle Bin (Lixeira) Flows ---
    val deletedNotes: StateFlow<List<NoteEntity>> = repository.deletedNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedTasks: StateFlow<List<TaskEntity>> = repository.deletedTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedDeadlineTasks: StateFlow<List<DeadlineTaskEntity>> = repository.deletedDeadlineTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedDeadlineNotes: StateFlow<List<DeadlineNoteEntity>> = repository.deletedDeadlineNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashTotalCount: StateFlow<Int> = combine(
        deletedNotes,
        deletedTasks,
        deletedDeadlineTasks,
        deletedDeadlineNotes
    ) { n, t, dt, dn ->
        n.size + t.size + dt.size + dn.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
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
     * handles recurring items, and purges trash older than 30 days.
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

            // Purge trash items older than 30 days
            val thirtyDaysAgo = now - 30L * 24L * 60L * 60L * 1000L
            repository.purgeOldTrash(thirtyDaysAgo)
        }
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun relockAll() {
        _unlockedNoteIds.value = emptySet()
        _unlockedDeadlineNoteIds.value = emptySet()
        _unlockedTaskIds.value = emptySet()
        _unlockedDeadlineTaskIds.value = emptySet()
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
        fontFamily: String = "DEFAULT",
        category: String = "Geral"
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
                    category = category,
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

    fun moveToTrashNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.softDeleteNote(note.id)
        }
    }

    fun deleteNote(note: NoteEntity) {
        moveToTrashNote(note)
    }

    fun restoreNoteById(id: Long) {
        viewModelScope.launch {
            repository.restoreNote(id)
        }
    }

    fun deleteNotePermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
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

    fun addTask(
        text: String,
        isPinned: Boolean = false,
        recurrence: String = "NONE",
        category: String = "Geral",
        subtasksJson: String = "[]"
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    text = text.trim(),
                    isCompleted = false,
                    isPinned = isPinned,
                    recurrence = recurrence,
                    visibleFrom = 0L,
                    category = category,
                    subtasksJson = subtasksJson,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val willBeCompleted = !task.isCompleted
            val now = System.currentTimeMillis()
            repository.updateTask(task.copy(isCompleted = willBeCompleted))

            if (willBeCompleted && task.recurrence != "NONE") {
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
                        category = task.category,
                        subtasksJson = task.subtasksJson,
                        createdAt = now
                    )
                )
            } else if (!willBeCompleted && task.recurrence != "NONE") {
                repository.deletePendingFutureTasks(task.text, task.recurrence, now)
            }
        }
    }

    fun togglePinTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isPinned = !task.isPinned))
        }
    }

    fun moveToTrashTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.softDeleteTask(task.id)
        }
    }

    fun deleteTask(task: TaskEntity) {
        moveToTrashTask(task)
    }

    fun restoreTaskById(id: Long) {
        viewModelScope.launch {
            repository.restoreTask(id)
        }
    }

    fun deleteTaskPermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteTaskById(id)
        }
    }

    // Subtasks for Tasks
    fun toggleSubtask(task: TaskEntity, subtaskId: String) {
        val list = com.example.data.model.SubtaskHelper.fromJson(task.subtasksJson).toMutableList()
        val index = list.indexOfFirst { it.id == subtaskId }
        if (index != -1) {
            val current = list[index]
            list[index] = current.copy(isCompleted = !current.isCompleted)
            val updatedJson = com.example.data.model.SubtaskHelper.toJson(list)
            updateTask(task.copy(subtasksJson = updatedJson))
        }
    }

    fun addSubtask(task: TaskEntity, subtaskTitle: String) {
        if (subtaskTitle.isBlank()) return
        val list = com.example.data.model.SubtaskHelper.fromJson(task.subtasksJson).toMutableList()
        list.add(com.example.data.model.Subtask(title = subtaskTitle.trim(), isCompleted = false))
        val updatedJson = com.example.data.model.SubtaskHelper.toJson(list)
        updateTask(task.copy(subtasksJson = updatedJson))
    }

    fun removeSubtask(task: TaskEntity, subtaskId: String) {
        val list = com.example.data.model.SubtaskHelper.fromJson(task.subtasksJson).filter { it.id != subtaskId }
        val updatedJson = com.example.data.model.SubtaskHelper.toJson(list)
        updateTask(task.copy(subtasksJson = updatedJson))
    }

    // --- Tab 3: Tarefas Diárias (Com Prazo) ---
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
        recurrence: String = "NONE",
        category: String = "Geral",
        subtasksJson: String = "[]"
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
                    category = category,
                    subtasksJson = subtasksJson,
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

    fun updateDeadlineTask(task: DeadlineTaskEntity) {
        viewModelScope.launch {
            repository.updateDeadlineTask(task)
            if (!task.isCompleted && task.deadlineTimestamp > System.currentTimeMillis()) {
                NotificationHelper.scheduleAlarms(
                    context = getApplication(),
                    id = task.id,
                    itemType = "TASK",
                    title = task.text,
                    deadlineTimestamp = task.deadlineTimestamp
                )
            }
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
                        category = task.category,
                        subtasksJson = task.subtasksJson,
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
                repository.deletePendingFutureDeadlineTasks(task.text, task.recurrence, now)
            }

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

    fun moveToTrashDeadlineTask(task: DeadlineTaskEntity) {
        NotificationHelper.cancelAlarms(getApplication(), task.id, "TASK")
        viewModelScope.launch {
            repository.softDeleteDeadlineTask(task.id)
        }
    }

    fun deleteDeadlineTask(task: DeadlineTaskEntity) {
        moveToTrashDeadlineTask(task)
    }

    fun restoreDeadlineTaskById(id: Long) {
        viewModelScope.launch {
            repository.restoreDeadlineTask(id)
        }
    }

    fun deleteDeadlineTaskPermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteDeadlineTaskById(id)
        }
    }

    // Subtasks for Deadline Tasks
    fun toggleDeadlineSubtask(task: DeadlineTaskEntity, subtaskId: String) {
        val list = com.example.data.model.SubtaskHelper.fromJson(task.subtasksJson).toMutableList()
        val index = list.indexOfFirst { it.id == subtaskId }
        if (index != -1) {
            val current = list[index]
            list[index] = current.copy(isCompleted = !current.isCompleted)
            val updatedJson = com.example.data.model.SubtaskHelper.toJson(list)
            updateDeadlineTask(task.copy(subtasksJson = updatedJson))
        }
    }

    fun addDeadlineSubtask(task: DeadlineTaskEntity, subtaskTitle: String) {
        if (subtaskTitle.isBlank()) return
        val list = com.example.data.model.SubtaskHelper.fromJson(task.subtasksJson).toMutableList()
        list.add(com.example.data.model.Subtask(title = subtaskTitle.trim(), isCompleted = false))
        val updatedJson = com.example.data.model.SubtaskHelper.toJson(list)
        updateDeadlineTask(task.copy(subtasksJson = updatedJson))
    }

    fun removeDeadlineSubtask(task: DeadlineTaskEntity, subtaskId: String) {
        val list = com.example.data.model.SubtaskHelper.fromJson(task.subtasksJson).filter { it.id != subtaskId }
        val updatedJson = com.example.data.model.SubtaskHelper.toJson(list)
        updateDeadlineTask(task.copy(subtasksJson = updatedJson))
    }

    // Snooze Deadline Task
    fun snoozeDeadlineTask(task: DeadlineTaskEntity, additionalMinutes: Int) {
        val newDeadline = System.currentTimeMillis() + (additionalMinutes * 60 * 1000L)
        viewModelScope.launch {
            repository.updateDeadlineTask(task.copy(deadlineTimestamp = newDeadline))
            NotificationHelper.clearTrackerForItem(getApplication(), "TASK", task.id)
            NotificationHelper.scheduleAlarms(
                context = getApplication(),
                id = task.id,
                itemType = "TASK",
                title = task.text,
                deadlineTimestamp = newDeadline
            )
        }
    }

    // --- Tab 4: Notas Diárias (Com Prazo) ---
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
        fontFamily: String = "DEFAULT",
        category: String = "Geral"
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
                    category = category,
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

    fun moveToTrashDeadlineNote(note: DeadlineNoteEntity) {
        NotificationHelper.cancelAlarms(getApplication(), note.id, "NOTE")
        viewModelScope.launch {
            repository.softDeleteDeadlineNote(note.id)
        }
    }

    fun deleteDeadlineNote(note: DeadlineNoteEntity) {
        moveToTrashDeadlineNote(note)
    }

    fun restoreDeadlineNoteById(id: Long) {
        viewModelScope.launch {
            repository.restoreDeadlineNote(id)
        }
    }

    fun deleteDeadlineNotePermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteDeadlineNoteById(id)
        }
    }

    // Snooze Deadline Note
    fun snoozeDeadlineNote(note: DeadlineNoteEntity, additionalMinutes: Int) {
        val newDeadline = System.currentTimeMillis() + (additionalMinutes * 60 * 1000L)
        viewModelScope.launch {
            repository.updateDeadlineNote(note.copy(deadlineTimestamp = newDeadline))
            NotificationHelper.clearTrackerForItem(getApplication(), "NOTE", note.id)
            NotificationHelper.scheduleAlarms(
                context = getApplication(),
                id = note.id,
                itemType = "NOTE",
                title = note.title.ifBlank { note.content.take(30) },
                deadlineTimestamp = newDeadline
            )
        }
    }

    // Empty entire trash
    fun emptyAllTrash() {
        viewModelScope.launch {
            repository.emptyAllTrash()
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

    // --- Manual Backup & Restore ---
    fun getBackupJson(): String {
        return BackupHelper.exportToJson(
            notes = allNotes.value,
            tasks = allTasks.value,
            deadlineTasks = activeDeadlineTasks.value,
            deadlineNotes = activeDeadlineNotes.value
        )
    }

    fun restoreBackupJson(jsonString: String, onResult: (Boolean, Int, String) -> Unit) {
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.Default) {
                    BackupHelper.importFromJson(jsonString)
                }
                var count = 0
                data.notes.forEach { note ->
                    repository.insertNote(note.copy(id = 0))
                    count++
                }
                data.tasks.forEach { task ->
                    repository.insertTask(task.copy(id = 0))
                    count++
                }
                data.deadlineTasks.forEach { dt ->
                    val newId = repository.insertDeadlineTask(dt.copy(id = 0))
                    if (!dt.isCompleted && dt.deadlineTimestamp > System.currentTimeMillis()) {
                        NotificationHelper.scheduleAlarms(
                            context = getApplication(),
                            id = newId,
                            itemType = "TASK",
                            title = dt.text.take(40),
                            deadlineTimestamp = dt.deadlineTimestamp
                        )
                    }
                    count++
                }
                data.deadlineNotes.forEach { dn ->
                    val newId = repository.insertDeadlineNote(dn.copy(id = 0))
                    if (dn.deadlineTimestamp > System.currentTimeMillis()) {
                        NotificationHelper.scheduleAlarms(
                            context = getApplication(),
                            id = newId,
                            itemType = "NOTE",
                            title = dn.title.ifBlank { "Nota com Prazo" }.take(40),
                            deadlineTimestamp = dn.deadlineTimestamp
                        )
                    }
                    count++
                }
                onResult(true, count, "Backup restaurado com sucesso! $count itens recuperados.")
            } catch (e: Exception) {
                onResult(false, 0, "Falha ao restaurar backup: ${e.localizedMessage ?: "formato inválido"}")
            }
        }
    }
}
