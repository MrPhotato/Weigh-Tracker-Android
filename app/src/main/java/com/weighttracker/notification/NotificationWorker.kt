package com.weighttracker.notification

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.weighttracker.util.PreferenceManager

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val TAG = "NotificationWorker"
        const val WORK_NAME = "weight_reminder_work"
    }

    override fun doWork(): Result {
        Log.d(TAG, "NotificationWorker executing...")
        
        val preferenceManager = PreferenceManager(applicationContext)
        
        // 检查提醒是否仍然启用
        if (preferenceManager.isReminderEnabled) {
            // 创建并发送通知
            val notificationReceiver = NotificationReceiver()
            notificationReceiver.onReceive(applicationContext, android.content.Intent())
            
            Log.d(TAG, "Notification sent successfully")
            return Result.success()
        } else {
            Log.d(TAG, "Reminder disabled, skipping notification")
            return Result.success()
        }
    }
} 