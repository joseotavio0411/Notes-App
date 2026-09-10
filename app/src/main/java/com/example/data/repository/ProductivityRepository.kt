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

    suspend fun insertNote(note: NoteEntity): Long = dao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = dao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = dao.deleteNote(note)
    suspend fun deleteNoteById(id: Long) = dao.deleteNoteById(id)

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()

    suspend fun insertTask(task: TaskEntity): Long = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = dao.deleteTaskById(id)
    suspend fun deletePendingFutureTasks(text: String, recurrence: String, currentTime: Long) =
        dao.deletePendingFutureTasks(text, recurrence, currentTime)

    // Deadline Tasks
    val allDeadlineTasks: Flow<List<DeadlineTaskEntity>> = dao.getAllDeadlineTasks()

    suspend fun insertDeadlineTask(task: DeadlineTaskEntity): Long = dao.insertDeadlineTask(task)
    suspend fun updateDeadlineTask(task: DeadlineTaskEntity) = dao.updateDeadlineTask(task)
    suspend fun deleteDeadlineTask(task: DeadlineTaskEntity) = dao.deleteDeadlineTask(task)
    suspend fun deleteDeadlineTaskById(id: Long) = dao.deleteDeadlineTaskById(id)
    suspend fun deletePendingFutureDeadlineTasks(text: String, recurrence: String, currentTime: Long) =
        dao.deletePendingFutureDeadlineTasks(text, recurrence, currentTime)
    suspend fun deleteExpiredCompletedTasks(currentTime: Long) = dao.deleteExpiredCompletedDeadlineTasks(currentTime)
    suspend fun getExpiredCompletedRecurringDeadlineTasks(currentTime: Long) = dao.getExpiredCompletedRecurringDeadlineTasks(currentTime)

    // Deadline Notes
    val allDeadlineNotes: Flow<List<DeadlineNoteEntity>> = dao.getAllDeadlineNotes()

    suspend fun insertDeadlineNote(note: DeadlineNoteEntity): Long = dao.insertDeadlineNote(note)
    suspend fun updateDeadlineNote(note: DeadlineNoteEntity) = dao.updateDeadlineNote(note)
    suspend fun deleteDeadlineNote(note: DeadlineNoteEntity) = dao.deleteDeadlineNote(note)
    suspend fun deleteDeadlineNoteById(id: Long) = dao.deleteDeadlineNoteById(id)
    suspend fun deleteExpiredNotes(currentTime: Long) = dao.deleteExpiredDeadlineNotes(currentTime)
    suspend fun getExpiredRecurringDeadlineNotes(currentTime: Long) = dao.getExpiredRecurringDeadlineNotes(currentTime)
}
