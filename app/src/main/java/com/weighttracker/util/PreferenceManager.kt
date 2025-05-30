package com.weighttracker.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    
    private val preferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "WeightTrackerPrefs"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
    }

    var isReminderEnabled: Boolean
        get() = preferences.getBoolean(KEY_REMINDER_ENABLED, false)
        set(value) = preferences.edit().putBoolean(KEY_REMINDER_ENABLED, value).apply()

    var reminderHour: Int
        get() = preferences.getInt(KEY_REMINDER_HOUR, 8)
        set(value) = preferences.edit().putInt(KEY_REMINDER_HOUR, value).apply()

    var reminderMinute: Int
        get() = preferences.getInt(KEY_REMINDER_MINUTE, 0)
        set(value) = preferences.edit().putInt(KEY_REMINDER_MINUTE, value).apply()
} 