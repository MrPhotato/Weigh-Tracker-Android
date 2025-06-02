package com.weighttracker.notification

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"

    fun scheduleNotification(context: Context, hour: Int, minute: Int) {
        // 取消之前的任务
        cancelNotification(context)
        
        // 计算到设定时间的延迟
        val delay = calculateInitialDelay(hour, minute)
        
        Log.d(TAG, "Scheduling notification with WorkManager, delay: ${delay / 1000 / 60} minutes")

        // 创建周期性工作请求
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            1, TimeUnit.DAYS // 每天重复
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .build()
            )
            .addTag(NotificationWorker.WORK_NAME)
            .build()

        // 调度任务
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                NotificationWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        
        Log.d(TAG, "Notification scheduled successfully with WorkManager")
    }

    fun cancelNotification(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(NotificationWorker.WORK_NAME)
        Log.d(TAG, "Notification cancelled")
    }

    /**
     * 计算到设定时间的延迟毫秒数
     */
    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // 如果设置的时间已经过去，则设置为明天
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        return calendar.timeInMillis - System.currentTimeMillis()
    }

    /**
     * 检查是否有精确闹钟权限 - WorkManager版本不需要此权限
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return true // WorkManager不需要精确闹钟权限
    }
} 