package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DeadlineNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(NotificationHelper.EXTRA_ITEM_ID, -1L)
        if (itemId == -1L) return

        when (intent.action) {
            NotificationHelper.ACTION_NOTE_REMINDER -> {
                NotificationHelper.showNoteReminder(
                    context = context,
                    noteId = itemId,
                    title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: ""
                )
                return
            }
            NotificationHelper.ACTION_UNPIN_NOTE_NOTIFICATION -> {
                NotificationHelper.removePinnedNote(context, itemId)
                return
            }
        }

        val itemType = intent.getStringExtra(NotificationHelper.EXTRA_ITEM_TYPE) ?: "TASK"
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: ""
        val reminderType = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_TYPE) ?: "EXPIRED"
        val deadline = intent.getLongExtra(NotificationHelper.EXTRA_DEADLINE, 0L)

        NotificationHelper.showNotification(
            context = context,
            id = itemId,
            itemType = itemType,
            title = title,
            reminderType = reminderType,
            deadlineTimestamp = deadline
        )
    }
}
