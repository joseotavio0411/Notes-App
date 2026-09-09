package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DeadlineNoteEntity
import com.example.data.local.entity.DeadlineTaskEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductivityDao {
    // --- Notas normais ---
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    // --- Tarefas normais ---
    @Query("SELECT * FROM tasks ORDER BY isPinned DESC, isCompleted ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    // --- Tarefas com Prazo ---
    @Query("SELECT * FROM deadline_tasks ORDER BY isPinned DESC, deadlineTimestamp ASC")
    fun getAllDeadlineTasks(): Flow<List<DeadlineTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeadlineTask(task: DeadlineTaskEntity): Long

    @Update
    suspend fun updateDeadlineTask(task: DeadlineTaskEntity)

    @Delete
    suspend fun deleteDeadlineTask(task: DeadlineTaskEntity)

    @Query("DELETE FROM deadline_tasks WHERE id = :id")
    suspend fun deleteDeadlineTaskById(id: Long)

    @Query("DELETE FROM deadline_tasks WHERE isCompleted = 1 AND deadlineTimestamp < :currentTime")
    suspend fun deleteExpiredCompletedDeadlineTasks(currentTime: Long)

    // --- Notas com Prazo ---
    @Query("SELECT * FROM deadline_notes ORDER BY isPinned DESC, deadlineTimestamp ASC")
    fun getAllDeadlineNotes(): Flow<List<DeadlineNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeadlineNote(note: DeadlineNoteEntity): Long

    @Update
    suspend fun updateDeadlineNote(note: DeadlineNoteEntity)

    @Delete
    suspend fun deleteDeadlineNote(note: DeadlineNoteEntity)

    @Query("DELETE FROM deadline_notes WHERE id = :id")
    suspend fun deleteDeadlineNoteById(id: Long)

    @Query("DELETE FROM deadline_notes WHERE deadlineTimestamp < :currentTime")
    suspend fun deleteExpiredDeadlineNotes(currentTime: Long)
}
