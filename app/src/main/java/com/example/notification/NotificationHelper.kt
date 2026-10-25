package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.entity.DeadlineNoteEntity
import com.example.data.local.entity.DeadlineTaskEntity

object NotificationHelper {

    const val CHANNEL_ID = "deadline_reminders_channel"
    private const val CHANNEL_NAME = "Lembretes de Prazos"
    private const val CHANNEL_DESC = "Notificações para tarefas e notas prestes a vencer ou expiradas"

    const val ACTION_DEADLINE_NOTIFICATION = "com.example.ACTION_DEADLINE_NOTIFICATION"
    const val EXTRA_ITEM_ID = "extra_item_id"
    const val EXTRA_ITEM_TYPE = "extra_item_type" // "TASK" or "NOTE"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_REMINDER_TYPE = "extra_reminder_type" // "24H", "1H", "EXPIRED"
    const val EXTRA_DEADLINE = "extra_deadline"
    const val EXTRA_TARGET_TAB = "extra_target_tab"

    private const val PREFS_NAME = "deadline_notifications_tracker"

    // Lembretes de notas comuns e notas fixadas na barra de notificações
    const val NOTE_REMINDER_CHANNEL_ID = "note_reminders_channel"
    const val PINNED_NOTES_CHANNEL_ID = "pinned_notes_channel"
    const val ACTION_NOTE_REMINDER = "com.example.ACTION_NOTE_REMINDER"
    const val ACTION_UNPIN_NOTE_NOTIFICATION = "com.example.ACTION_UNPIN_NOTE_NOTIFICATION"
    const val EXTRA_OPEN_NOTE_ID = "extra_open_note_id"
    private const val PINNED_PREFS_NAME = "pinned_note_notifications"
    private const val PINNED_IDS_KEY = "ids"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun isAlreadySent(context: Context, itemType: String, id: Long, reminderType: String): Boolean {
        return getPrefs(context).getBoolean("${itemType}_${id}_${reminderType}", false)
    }

    private fun markAsSent(context: Context, itemType: String, id: Long, reminderType: String) {
        getPrefs(context).edit().putBoolean("${itemType}_${id}_${reminderType}", true).apply()
    }

    fun clearTrackerForItem(context: Context, itemType: String, id: Long) {
        getPrefs(context).edit()
            .remove("${itemType}_${id}_24H")
            .remove("${itemType}_${id}_1H")
            .remove("${itemType}_${id}_EXPIRED")
            .apply()
    }

    /**
     * Schedules system alarms for:
     * - 24 hours before deadline
     * - 1 hour before deadline
     * - When deadline expires
     */
    fun scheduleAlarms(
        context: Context,
        id: Long,
        itemType: String,
        title: String,
        deadlineTimestamp: Long
    ) {
        val now = System.currentTimeMillis()
        if (deadlineTimestamp <= 0L) return

        initNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val reminderTypes = listOf(
            Triple("24H", deadlineTimestamp - 24 * 60 * 60 * 1000L, 24 * 60 * 60 * 1000L),
            Triple("1H", deadlineTimestamp - 1 * 60 * 60 * 1000L, 1 * 60 * 60 * 1000L),
            Triple("EXPIRED", deadlineTimestamp, 0L)
        )

        for ((reminderType, triggerTime, _) in reminderTypes) {
            // Only schedule if in the future and not previously sent
            if (triggerTime > now && !isAlreadySent(context, itemType, id, reminderType)) {
                val intent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
                    action = ACTION_DEADLINE_NOTIFICATION
                    putExtra(EXTRA_ITEM_ID, id)
                    putExtra(EXTRA_ITEM_TYPE, itemType)
                    putExtra(EXTRA_TITLE, title)
                    putExtra(EXTRA_REMINDER_TYPE, reminderType)
                    putExtra(EXTRA_DEADLINE, deadlineTimestamp)
                }

                val requestCode = generateRequestCode(id, itemType, reminderType)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } catch (e: SecurityException) {
                    // Fallback if exact alarm permission is restricted
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            }
        }
    }

    /**
     * Cancels any scheduled alarms when item is deleted or completed
     */
    fun cancelAlarms(context: Context, id: Long, itemType: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val reminderTypes = listOf("24H", "1H", "EXPIRED")
        for (reminderType in reminderTypes) {
            val intent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
                action = ACTION_DEADLINE_NOTIFICATION
            }
            val requestCode = generateRequestCode(id, itemType, reminderType)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        clearTrackerForItem(context, itemType, id)
    }

    /**
     * Displays the notification immediately
     */
    fun showNotification(
        context: Context,
        id: Long,
        itemType: String,
        title: String,
        reminderType: String,
        deadlineTimestamp: Long
    ) {
        // Guard against duplicate notification
        if (isAlreadySent(context, itemType, id, reminderType)) return

        initNotificationChannel(context)

        val cleanTitle = if (title.isBlank()) {
            if (itemType == "TASK") "Tarefa com prazo" else "Nota com prazo"
        } else {
            title
        }

        val (notificationTitle, notificationBody) = when (reminderType) {
            "24H" -> Pair(
                "Faltam 24 horas!",
                "O prazo para \"$cleanTitle\" acaba em 24 horas."
            )
            "1H" -> Pair(
                "Falta apenas 1 hora!",
                "Atenção: o prazo para \"$cleanTitle\" acaba em 1 hora!"
            )
            else -> Pair(
                "Prazo expirado!",
                "O prazo para \"$cleanTitle\" acabou de expirar."
            )
        }

        val targetTab = if (itemType == "TASK") 2 else 3

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_TAB, targetTab)
        }

        val notificationId = generateNotificationId(id, itemType, reminderType)

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
            markAsSent(context, itemType, id, reminderType)
        } catch (e: SecurityException) {
            // In case notification permission was revoked
        }
    }

    /**
     * Periodic/Startup verification check to catch due items even across reboots or sleep.
     */
    fun checkAndTriggerDueReminders(
        context: Context,
        tasks: List<DeadlineTaskEntity>,
        notes: List<DeadlineNoteEntity>
    ) {
        val now = System.currentTimeMillis()

        // Check active deadline tasks
        for (task in tasks) {
            if (task.isCompleted) continue
            val deadline = task.deadlineTimestamp
            val timeRemaining = deadline - now

            // 1. Expired
            if (timeRemaining <= 0) {
                showNotification(context, task.id, "TASK", task.text, "EXPIRED", deadline)
            }
            // 2. Less than 1 hour remaining
            else if (timeRemaining <= 60 * 60 * 1000L) {
                showNotification(context, task.id, "TASK", task.text, "1H", deadline)
            }
            // 3. Less than 24 hours remaining
            else if (timeRemaining <= 24 * 60 * 60 * 1000L) {
                showNotification(context, task.id, "TASK", task.text, "24H", deadline)
            }
        }

        // Check active deadline notes
        for (note in notes) {
            val deadline = note.deadlineTimestamp
            val timeRemaining = deadline - now

            // 1. Expired
            if (timeRemaining <= 0) {
                showNotification(context, note.id, "NOTE", note.title.ifBlank { note.content.take(30) }, "EXPIRED", deadline)
            }
            // 2. Less than 1 hour remaining
            else if (timeRemaining <= 60 * 60 * 1000L) {
                showNotification(context, note.id, "NOTE", note.title.ifBlank { note.content.take(30) }, "1H", deadline)
            }
            // 3. Less than 24 hours remaining
            else if (timeRemaining <= 24 * 60 * 60 * 1000L) {
                showNotification(context, note.id, "NOTE", note.title.ifBlank { note.content.take(30) }, "24H", deadline)
            }
        }
    }

    // --- Lembretes de notas comuns ---

    private fun initNoteChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            manager.createNotificationChannel(
                NotificationChannel(NOTE_REMINDER_CHANNEL_ID, "Lembretes de notas", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Lembretes definidos nas notas"
                    enableVibration(true)
                }
            )
            manager.createNotificationChannel(
                NotificationChannel(PINNED_NOTES_CHANNEL_ID, "Notas fixadas", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Notas mantidas na barra de notificações para consulta rápida"
                    setShowBadge(false)
                }
            )
        }
    }

    private fun noteReminderRequestCode(noteId: Long): Int = 3_000_000 + (noteId % 1_000_000).toInt()
    private fun pinnedNoteNotificationId(noteId: Long): Int = 4_000_000 + (noteId % 1_000_000).toInt()

    private fun openNoteIntent(context: Context, noteId: Long, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_NOTE_ID, noteId)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleNoteReminder(context: Context, noteId: Long, title: String, reminderAt: Long) {
        cancelNoteReminder(context, noteId)
        if (reminderAt <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
            action = ACTION_NOTE_REMINDER
            putExtra(EXTRA_ITEM_ID, noteId)
            putExtra(EXTRA_TITLE, title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteReminderRequestCode(noteId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderAt, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderAt, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderAt, pendingIntent)
        }
    }

    fun cancelNoteReminder(context: Context, noteId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
            action = ACTION_NOTE_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteReminderRequestCode(noteId),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun showNoteReminder(context: Context, noteId: Long, title: String) {
        initNoteChannels(context)
        val requestCode = noteReminderRequestCode(noteId)
        val builder = NotificationCompat.Builder(context, NOTE_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Lembrete")
            .setContentText(title.ifBlank { "Nota sem título" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openNoteIntent(context, noteId, requestCode))
        try {
            NotificationManagerCompat.from(context).notify(requestCode, builder.build())
        } catch (e: SecurityException) {
            // Permissão de notificação revogada
        }
    }

    // --- Notas fixadas na barra de notificações ---

    fun getPinnedNoteIds(context: Context): Set<Long> =
        context.getSharedPreferences(PINNED_PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(PINNED_IDS_KEY, emptySet())
            .orEmpty()
            .mapNotNull { it.toLongOrNull() }
            .toSet()

    private fun savePinnedNoteIds(context: Context, ids: Set<Long>) {
        context.getSharedPreferences(PINNED_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(PINNED_IDS_KEY, ids.map { it.toString() }.toSet())
            .apply()
    }

    /** Mostra (ou atualiza) a nota fixa na barra. Retorna false se faltar permissão. */
    fun showPinnedNote(context: Context, noteId: Long, title: String, content: String): Boolean {
        initNoteChannels(context)
        val notificationId = pinnedNoteNotificationId(noteId)
        val unpinIntent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
            action = ACTION_UNPIN_NOTE_NOTIFICATION
            putExtra(EXTRA_ITEM_ID, noteId)
        }
        val unpinPending = PendingIntent.getBroadcast(
            context,
            notificationId,
            unpinIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val body = content.lines().joinToString("\n") { line ->
            when {
                line.startsWith("[x] ") || line.startsWith("- [x] ") -> "☑ " + line.substringAfter("] ")
                line.startsWith("[ ] ") || line.startsWith("- [ ] ") -> "☐ " + line.substringAfter("] ")
                else -> line
            }
        }
        val builder = NotificationCompat.Builder(context, PINNED_NOTES_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle(title.ifBlank { "Nota" })
            .setContentText(body.lineSequence().firstOrNull { it.isNotBlank() } ?: "")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openNoteIntent(context, noteId, notificationId))
            .addAction(0, "Desafixar", unpinPending)
        return try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            savePinnedNoteIds(context, getPinnedNoteIds(context) + noteId)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun removePinnedNote(context: Context, noteId: Long) {
        NotificationManagerCompat.from(context).cancel(pinnedNoteNotificationId(noteId))
        savePinnedNoteIds(context, getPinnedNoteIds(context) - noteId)
    }

    private fun generateRequestCode(id: Long, itemType: String, reminderType: String): Int {
        val typeCode = if (itemType == "TASK") 1000 else 2000
        val reminderCode = when (reminderType) {
            "24H" -> 1
            "1H" -> 2
            else -> 3
        }
        return ((id % 100000).toInt() * 10) + typeCode + reminderCode
    }

    private fun generateNotificationId(id: Long, itemType: String, reminderType: String): Int {
        return generateRequestCode(id, itemType, reminderType)
    }
}
