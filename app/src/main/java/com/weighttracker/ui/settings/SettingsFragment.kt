package com.weighttracker.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.weighttracker.R
import com.weighttracker.databinding.FragmentSettingsBinding
import com.weighttracker.notification.NotificationReceiver
import com.weighttracker.notification.NotificationScheduler
import com.weighttracker.util.PreferenceManager
import com.weighttracker.util.BatteryOptimizationHelper
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager

    // 通知权限请求启动器
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 通知权限被授予，检查电池优化设置
            checkBatteryOptimizationAndEnableReminder()
        } else {
            // 权限被拒绝，显示说明并重置开关
            showPermissionDeniedDialog()
            binding.switchReminder.isChecked = false
        }
    }

    // 系统设置页面启动器
    private val appSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { 
        // 从设置页面返回后检查权限状态
        if (checkNotificationPermission()) {
            checkBatteryOptimizationAndEnableReminder()
        } else {
            binding.switchReminder.isChecked = false
            Toast.makeText(requireContext(), "需要通知权限才能启用提醒功能", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        
        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // 设置提醒开关状态（检查通知权限）
        binding.switchReminder.isChecked = preferenceManager.isReminderEnabled && checkNotificationPermission()
        
        // 显示当前提醒时间
        updateTimeDisplay()
    }

    private fun setupClickListeners() {
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 用户想要启用提醒，先检查权限
                requestNotificationPermissionIfNeeded()
            } else {
                // 用户关闭提醒
                disableReminder()
            }
        }

        binding.layoutTimePicker.setOnClickListener {
            showTimePicker()
        }
        
        // 长按时间设置区域可以测试通知
        binding.layoutTimePicker.setOnLongClickListener {
            if (checkNotificationPermission()) {
                testNotification()
                true
            } else {
                Toast.makeText(requireContext(), "请先开启通知权限", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    /**
     * 检查通知权限
     */
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 需要 POST_NOTIFICATIONS 权限
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 13 以下检查通知是否被禁用
            NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
        }
    }

    /**
     * 检查电池优化并启用提醒
     */
    private fun checkBatteryOptimizationAndEnableReminder() {
        if (BatteryOptimizationHelper.shouldShowBatteryOptimizationDialog(requireContext())) {
            // 显示电池优化建议对话框
            showBatteryOptimizationRecommendation()
        } else {
            // 直接启用提醒
            enableReminder()
        }
    }

    /**
     * 显示电池优化建议
     */
    private fun showBatteryOptimizationRecommendation() {
        val manufacturerGuide = BatteryOptimizationHelper.getManufacturerSpecificGuide()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("优化通知设置")
            .setMessage("为了确保定时提醒正常工作，建议进行以下设置：\n\n1. 允许通知权限 ✓\n2. 关闭电池优化\n3. 允许后台运行\n\n厂商特定设置：\n$manufacturerGuide")
            .setPositiveButton("去设置") { _, _ ->
                BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(this)
                enableReminder()
            }
            .setNegativeButton("直接启用") { _, _ ->
                enableReminder()
            }
            .setNeutralButton("了解更多") { _, _ ->
                showDetailedOptimizationGuide()
            }
            .show()
    }

    /**
     * 显示详细的优化指南
     */
    private fun showDetailedOptimizationGuide() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("为什么需要这些设置？")
            .setMessage("""
                🔋 电池优化：
                系统可能会限制应用在后台运行，导致通知无法准时发送
                
                📱 厂商系统：
                不同厂商的Android系统有额外的后台限制策略
                
                ⏰ WorkManager：
                本应用使用Google推荐的WorkManager技术，但仍可能被某些激进的电池管理策略影响
                
                ✅ 解决方案：
                将应用加入电池优化白名单可以最大程度保证通知的可靠性
            """.trimIndent())
            .setPositiveButton("去设置") { _, _ ->
                BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(this)
                enableReminder()
            }
            .setNegativeButton("直接启用") { _, _ ->
                enableReminder()
            }
            .show()
    }

    /**
     * 启用提醒功能
     */
    private fun enableReminder() {
        preferenceManager.isReminderEnabled = true
        NotificationScheduler.scheduleNotification(
            requireContext(),
            preferenceManager.reminderHour,
            preferenceManager.reminderMinute
        )
        
        val batteryOptimized = !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(requireContext())
        val message = if (batteryOptimized) {
            "提醒已启用（建议关闭电池优化以提高可靠性）"
        } else {
            "提醒已启用"
        }
        
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 禁用提醒功能
     */
    private fun disableReminder() {
        preferenceManager.isReminderEnabled = false
        NotificationScheduler.cancelNotification(requireContext())
        Toast.makeText(requireContext(), "提醒已关闭", Toast.LENGTH_SHORT).show()
    }

    /**
     * 显示权限被拒绝的对话框
     */
    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("需要通知权限")
            .setMessage("为了向您发送体重记录提醒，应用需要通知权限。请在设置中允许通知权限。")
            .setPositiveButton("去设置") { _, _ ->
                openAppNotificationSettings()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 显示通知设置对话框（Android 13以下）
     */
    private fun showNotificationSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("开启通知权限")
            .setMessage("为了向您发送体重记录提醒，需要开启通知权限。")
            .setPositiveButton("去设置") { _, _ ->
                openAppNotificationSettings()
            }
            .setNegativeButton("取消") { _, _ ->
                binding.switchReminder.isChecked = false
            }
            .show()
    }

    /**
     * 打开应用的通知设置页面
     */
    private fun openAppNotificationSettings() {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
            }
        }
        appSettingsLauncher.launch(intent)
    }

    private fun showTimePicker() {
        val currentHour = preferenceManager.reminderHour
        val currentMinute = preferenceManager.reminderMinute

        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                preferenceManager.reminderHour = hour
                preferenceManager.reminderMinute = minute
                updateTimeDisplay()
                
                // 如果提醒已启用，重新设置时间
                if (preferenceManager.isReminderEnabled) {
                    NotificationScheduler.scheduleNotification(
                        requireContext(),
                        hour,
                        minute
                    )
                }
            },
            currentHour,
            currentMinute,
            true
        ).show()
    }

    private fun updateTimeDisplay() {
        val hour = preferenceManager.reminderHour
        val minute = preferenceManager.reminderMinute
        binding.tvReminderTime.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    /**
     * 测试通知功能
     */
    private fun testNotification() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("测试通知")
            .setMessage("立即发送一条测试通知？")
            .setPositiveButton("发送") { _, _ ->
                val intent = Intent(requireContext(), NotificationReceiver::class.java)
                requireContext().sendBroadcast(intent)
                Toast.makeText(requireContext(), "测试通知已发送", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 请求通知权限（如果需要）
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (checkNotificationPermission()) {
            // 已有权限，检查电池优化并启用提醒
            checkBatteryOptimizationAndEnableReminder()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ 请求权限
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Android 13 以下，引导用户到设置页面
                showNotificationSettingsDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 页面恢复时重新检查通知权限状态
        if (preferenceManager.isReminderEnabled && !checkNotificationPermission()) {
            binding.switchReminder.isChecked = false
            preferenceManager.isReminderEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 