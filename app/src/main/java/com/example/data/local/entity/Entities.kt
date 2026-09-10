package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val colorHex: String = "#FFFFFF",
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val lockPin: String? = null,
    val imageUri: String? = null,
    val audioPath: String? = null,
    val fontSize: Int = 16,
    val fontFamily: String = "DEFAULT",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val lockPin: String? = null,
    val recurrence: String = "NONE",
    val visibleFrom: Long = 0L, // Epoch millis from which task becomes visible (0 = immediately)
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "deadline_tasks")
data class DeadlineTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isCompleted: Boolean = false,
    val deadlineTimestamp: Long, // Epoch millis
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val lockPin: String? = null,
    val recurrence: String = "NONE",
    val visibleFrom: Long = 0L, // Epoch millis from which task becomes visible (0 = immediately)
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "deadline_notes")
data class DeadlineNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val deadlineTimestamp: Long, // Epoch millis
    val colorHex: String = "#FFFFFF",
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val lockPin: String? = null,
    val recurrence: String = "NONE",
    val visibleFrom: Long = 0L,
    val imageUri: String? = null,
    val audioPath: String? = null,
    val fontSize: Int = 16,
    val fontFamily: String = "DEFAULT",
    val createdAt: Long = System.currentTimeMillis()
)
