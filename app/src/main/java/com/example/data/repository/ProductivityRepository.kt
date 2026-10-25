package com.example.data.repository

import com.example.data.local.dao.ProductivityDao
import com.example.data.local.entity.DeadlineNoteEntity
import com.example.data.local.entity.DeadlineTaskEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class ProductivityRepository(private val dao: ProductivityDao) {

    // Notes
    val allNotes: Flow<List<NoteEntity>> = dao.getAllNotes()
    val deletedNotes: Flow<List<NoteEntity>> = dao.getDeletedNotes()

    suspend fun insertNote(note: NoteEntity): Long = dao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = dao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = dao.deleteNote(note)
    suspend fun deleteNoteById(id: Long) = dao.deleteNoteById(id)
    suspend fun softDeleteNote(id: Long, deletedAt: Long = System.currentTimeMillis()) = dao.softDeleteNote(id, deletedAt)
    suspend fun restoreNote(id: Long) = dao.restoreNote(id)

    val archivedNotes: Flow<List<NoteEntity>> = dao.getArchivedNotes()
    suspend fun getNoteById(id: Long): NoteEntity? = dao.getNoteById(id)
    suspend fun getUnlockedNotes(): List<NoteEntity> = dao.getUnlockedNotes()
    suspend fun getNotesWithFutureReminders(now: Long): List<NoteEntity> = dao.getNotesWithFutureReminders(now)
    suspend fun setNotesArchived(ids: List<Long>, archived: Boolean) = dao.setNotesArchived(ids, archived, System.currentTimeMillis())
    suspend fun setNotesPinned(ids: List<Long>, pinned: Boolean) = dao.setNotesPinned(ids, pinned, System.currentTimeMillis())
    suspend fun setNotesColor(ids: List<Long>, colorHex: String) = dao.setNotesColor(ids, colorHex, System.currentTimeMillis())
    suspend fun softDeleteNotes(ids: List<Long>) = dao.softDeleteNotes(ids, System.currentTimeMillis())
    suspend fun restoreNotes(ids: List<Long>) = dao.restoreNotes(ids)
    suspend fun setNoteReminder(id: Long, reminderAt: Long) = dao.setNoteReminder(id, reminderAt)

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val deletedTasks: Flow<List<TaskEntity>> = dao.getDeletedTasks()

    suspend fun insertTask(task: TaskEntity): Long = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = dao.deleteTaskById(id)
    suspend fun softDeleteTask(id: Long, deletedAt: Long = System.currentTimeMillis()) = dao.softDeleteTask(id, deletedAt)
    suspend fun restoreTask(id: Long) = dao.restoreTask(id)
    suspend fun deletePendingFutureTasks(text: String, recurrence: String, currentTime: Long) =
        dao.deletePendingFutureTasks(text, recurrence, currentTime)

    // Deadline Tasks
    val allDeadlineTasks: Flow<List<DeadlineTaskEntity>> = dao.getAllDeadlineTasks()
    val deletedDeadlineTasks: Flow<List<DeadlineTaskEntity>> = dao.getDeletedDeadlineTasks()

    suspend fun insertDeadlineTask(task: DeadlineTaskEntity): Long = dao.insertDeadlineTask(task)
    suspend fun updateDeadlineTask(task: DeadlineTaskEntity) = dao.updateDeadlineTask(task)
    suspend fun deleteDeadlineTask(task: DeadlineTaskEntity) = dao.deleteDeadlineTask(task)
    suspend fun deleteDeadlineTaskById(id: Long) = dao.deleteDeadlineTaskById(id)
    suspend fun softDeleteDeadlineTask(id: Long, deletedAt: Long = System.currentTimeMillis()) = dao.softDeleteDeadlineTask(id, deletedAt)
    suspend fun restoreDeadlineTask(id: Long) = dao.restoreDeadlineTask(id)
    suspend fun deletePendingFutureDeadlineTasks(text: String, recurrence: String, currentTime: Long) =
        dao.deletePendingFutureDeadlineTasks(text, recurrence, currentTime)
    suspend fun deleteExpiredCompletedTasks(currentTime: Long) = dao.deleteExpiredCompletedDeadlineTasks(currentTime)
    suspend fun getExpiredCompletedRecurringDeadlineTasks(currentTime: Long) = dao.getExpiredCompletedRecurringDeadlineTasks(currentTime)

    // Deadline Notes
    val allDeadlineNotes: Flow<List<DeadlineNoteEntity>> = dao.getAllDeadlineNotes()
    val deletedDeadlineNotes: Flow<List<DeadlineNoteEntity>> = dao.getDeletedDeadlineNotes()

    suspend fun insertDeadlineNote(note: DeadlineNoteEntity): Long = dao.insertDeadlineNote(note)
    suspend fun updateDeadlineNote(note: DeadlineNoteEntity) = dao.updateDeadlineNote(note)
    suspend fun deleteDeadlineNote(note: DeadlineNoteEntity) = dao.deleteDeadlineNote(note)
    suspend fun deleteDeadlineNoteById(id: Long) = dao.deleteDeadlineNoteById(id)
    suspend fun softDeleteDeadlineNote(id: Long, deletedAt: Long = System.currentTimeMillis()) = dao.softDeleteDeadlineNote(id, deletedAt)
    suspend fun restoreDeadlineNote(id: Long) = dao.restoreDeadlineNote(id)
    suspend fun deleteExpiredNotes(currentTime: Long) = dao.deleteExpiredDeadlineNotes(currentTime)
    suspend fun getExpiredRecurringDeadlineNotes(currentTime: Long) = dao.getExpiredRecurringDeadlineNotes(currentTime)

    // Trash cleanup
    suspend fun emptyAllTrash() {
        dao.emptyNotesTrash()
        dao.emptyTasksTrash()
        dao.emptyDeadlineTasksTrash()
        dao.emptyDeadlineNotesTrash()
    }

    suspend fun purgeOldTrash(purgeThreshold: Long) {
        dao.purgeOldDeletedNotes(purgeThreshold)
        dao.purgeOldDeletedTasks(purgeThreshold)
        dao.purgeOldDeletedDeadlineTasks(purgeThreshold)
        dao.purgeOldDeletedDeadlineNotes(purgeThreshold)
    }
}
