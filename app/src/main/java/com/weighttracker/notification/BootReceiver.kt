package com.weighttracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.weighttracker.util.PreferenceManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val preferenceManager = PreferenceManager(context)
            if (preferenceManager.isReminderEnabled) {
                // 重新设置提醒
                NotificationScheduler.scheduleNotification(
                    context,
                    preferenceManager.reminderHour,
                    preferenceManager.reminderMinute
                )
            }
        }
    }
} 