package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DeadlineNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(NotificationHelper.EXTRA_ITEM_ID, -1L)
        if (itemId == -1L) return

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
