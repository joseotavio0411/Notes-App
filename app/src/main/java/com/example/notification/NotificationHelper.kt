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
