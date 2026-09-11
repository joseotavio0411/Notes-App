package com.example.data.backup

import com.example.data.local.entity.DeadlineNoteEntity
import com.example.data.local.entity.DeadlineTaskEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import org.json.JSONArray
import org.json.JSONObject

object BackupHelper {

    data class BackupData(
        val notes: List<NoteEntity>,
        val tasks: List<TaskEntity>,
        val deadlineTasks: List<DeadlineTaskEntity>,
        val deadlineNotes: List<DeadlineNoteEntity>
    )

    fun exportToJson(
        notes: List<NoteEntity>,
        tasks: List<TaskEntity>,
        deadlineTasks: List<DeadlineTaskEntity>,
        deadlineNotes: List<DeadlineNoteEntity>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val notesArray = JSONArray()
        notes.forEach { n ->
            val obj = JSONObject().apply {
                put("title", n.title)
                put("content", n.content)
                put("colorHex", n.colorHex)
                put("isPinned", n.isPinned)
                put("isLocked", n.isLocked)
                put("lockPin", n.lockPin ?: JSONObject.NULL)
                put("imageUri", n.imageUri ?: JSONObject.NULL)
                put("audioPath", n.audioPath ?: JSONObject.NULL)
                put("fontSize", n.fontSize)
                put("fontFamily", n.fontFamily)
                put("createdAt", n.createdAt)
                put("updatedAt", n.updatedAt)
            }
            notesArray.put(obj)
        }
        root.put("notes", notesArray)

        val tasksArray = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject().apply {
                put("text", t.text)
                put("isCompleted", t.isCompleted)
                put("isPinned", t.isPinned)
                put("isLocked", t.isLocked)
                put("lockPin", t.lockPin ?: JSONObject.NULL)
                put("recurrence", t.recurrence)
                put("visibleFrom", t.visibleFrom)
                put("createdAt", t.createdAt)
            }
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        val dlTasksArray = JSONArray()
        deadlineTasks.forEach { dt ->
            val obj = JSONObject().apply {
                put("text", dt.text)
                put("isCompleted", dt.isCompleted)
                put("deadlineTimestamp", dt.deadlineTimestamp)
                put("isPinned", dt.isPinned)
                put("isLocked", dt.isLocked)
                put("lockPin", dt.lockPin ?: JSONObject.NULL)
                put("recurrence", dt.recurrence)
                put("visibleFrom", dt.visibleFrom)
                put("createdAt", dt.createdAt)
            }
            dlTasksArray.put(obj)
        }
        root.put("deadlineTasks", dlTasksArray)

        val dlNotesArray = JSONArray()
        deadlineNotes.forEach { dn ->
            val obj = JSONObject().apply {
                put("title", dn.title)
                put("content", dn.content)
                put("deadlineTimestamp", dn.deadlineTimestamp)
                put("colorHex", dn.colorHex)
                put("isPinned", dn.isPinned)
                put("isLocked", dn.isLocked)
                put("lockPin", dn.lockPin ?: JSONObject.NULL)
                put("recurrence", dn.recurrence)
                put("visibleFrom", dn.visibleFrom)
                put("imageUri", dn.imageUri ?: JSONObject.NULL)
                put("audioPath", dn.audioPath ?: JSONObject.NULL)
                put("fontSize", dn.fontSize)
                put("fontFamily", dn.fontFamily)
                put("createdAt", dn.createdAt)
            }
            dlNotesArray.put(obj)
        }
        root.put("deadlineNotes", dlNotesArray)

        return root.toString(2)
    }

    fun importFromJson(jsonString: String): BackupData {
        val root = JSONObject(jsonString)
        val notes = mutableListOf<NoteEntity>()
        val tasks = mutableListOf<TaskEntity>()
        val deadlineTasks = mutableListOf<DeadlineTaskEntity>()
        val deadlineNotes = mutableListOf<DeadlineNoteEntity>()

        if (root.has("notes")) {
            val arr = root.getJSONArray("notes")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                notes.add(
                    NoteEntity(
                        title = obj.optString("title", ""),
                        content = obj.optString("content", ""),
                        colorHex = obj.optString("colorHex", "#FFFFFF"),
                        isPinned = obj.optBoolean("isPinned", false),
                        isLocked = obj.optBoolean("isLocked", false),
                        lockPin = if (obj.isNull("lockPin")) null else obj.optString("lockPin"),
                        imageUri = if (obj.isNull("imageUri")) null else obj.optString("imageUri"),
                        audioPath = if (obj.isNull("audioPath")) null else obj.optString("audioPath"),
                        fontSize = obj.optInt("fontSize", 16),
                        fontFamily = obj.optString("fontFamily", "DEFAULT"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        }

        if (root.has("tasks")) {
            val arr = root.getJSONArray("tasks")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                tasks.add(
                    TaskEntity(
                        text = obj.optString("text", ""),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        isPinned = obj.optBoolean("isPinned", false),
                        isLocked = obj.optBoolean("isLocked", false),
                        lockPin = if (obj.isNull("lockPin")) null else obj.optString("lockPin"),
                        recurrence = obj.optString("recurrence", "NONE"),
                        visibleFrom = obj.optLong("visibleFrom", 0L),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        if (root.has("deadlineTasks")) {
            val arr = root.getJSONArray("deadlineTasks")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                deadlineTasks.add(
                    DeadlineTaskEntity(
                        text = obj.optString("text", ""),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        deadlineTimestamp = obj.optLong("deadlineTimestamp", System.currentTimeMillis() + 86400000L),
                        isPinned = obj.optBoolean("isPinned", false),
                        isLocked = obj.optBoolean("isLocked", false),
                        lockPin = if (obj.isNull("lockPin")) null else obj.optString("lockPin"),
                        recurrence = obj.optString("recurrence", "NONE"),
                        visibleFrom = obj.optLong("visibleFrom", 0L),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        if (root.has("deadlineNotes")) {
            val arr = root.getJSONArray("deadlineNotes")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                deadlineNotes.add(
                    DeadlineNoteEntity(
                        title = obj.optString("title", ""),
                        content = obj.optString("content", ""),
                        deadlineTimestamp = obj.optLong("deadlineTimestamp", System.currentTimeMillis() + 86400000L),
                        colorHex = obj.optString("colorHex", "#FFFFFF"),
                        isPinned = obj.optBoolean("isPinned", false),
                        isLocked = obj.optBoolean("isLocked", false),
                        lockPin = if (obj.isNull("lockPin")) null else obj.optString("lockPin"),
                        recurrence = obj.optString("recurrence", "NONE"),
                        visibleFrom = obj.optLong("visibleFrom", 0L),
                        imageUri = if (obj.isNull("imageUri")) null else obj.optString("imageUri"),
                        audioPath = if (obj.isNull("audioPath")) null else obj.optString("audioPath"),
                        fontSize = obj.optInt("fontSize", 16),
                        fontFamily = obj.optString("fontFamily", "DEFAULT"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        return BackupData(notes, tasks, deadlineTasks, deadlineNotes)
    }
}
