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
import com.weighttracker.util.AutoStartHelper
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
            // 通知权限被授予，检查其他优化设置
            checkAllOptimizationsAndEnableReminder()
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
        // 从设置页面返回后刷新状态
        updateOptimizationStatus()
        if (checkNotificationPermission()) {
            checkAllOptimizationsAndEnableReminder()
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
        updateOptimizationStatus()
    }

    private fun setupViews() {
        // 设置提醒开关状态（检查通知权限）
        binding.switchReminder.isChecked = preferenceManager.isReminderEnabled && checkNotificationPermission()
        
        // 显示当前提醒时间
        updateTimeDisplay()
        
        // 设置优化卡片的可见性
        updateOptimizationCardVisibility()
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

        // 通知优化功能点击事件
        binding.layoutNotificationPermission.setOnClickListener {
            if (!checkNotificationPermission()) {
                requestNotificationPermissionIfNeeded()
            } else {
                openAppNotificationSettings()
            }
        }

        binding.layoutBatteryOptimization.setOnClickListener {
            BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(this)
        }

        binding.layoutAutoStart.setOnClickListener {
            if (AutoStartHelper.isAutoStartRequired()) {
                AutoStartHelper.showAutoStartGuide(this)
                AutoStartHelper.saveAutoStartGuideShown(requireContext())
            } else {
                Toast.makeText(requireContext(), "您的设备不需要额外的自启动设置", Toast.LENGTH_SHORT).show()
            }
        }

        binding.layoutTestNotification.setOnClickListener {
            testNotification()
        }
    }

    /**
     * 更新优化卡片的可见性
     */
    private fun updateOptimizationCardVisibility() {
        // 当提醒功能开启时显示优化卡片
        binding.cardNotificationOptimization.visibility = 
            if (preferenceManager.isReminderEnabled) View.VISIBLE else View.GONE
    }

    /**
     * 更新所有优化功能的状态显示
     */
    private fun updateOptimizationStatus() {
        updateNotificationPermissionStatus()
        updateBatteryOptimizationStatus()
        updateAutoStartStatus()
    }

    /**
     * 更新通知权限状态
     */
    private fun updateNotificationPermissionStatus() {
        val hasPermission = checkNotificationPermission()
        
        if (hasPermission) {
            binding.ivNotificationStatus.setImageResource(R.drawable.ic_check_circle)
            binding.ivNotificationStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.chart_line))
            binding.tvNotificationStatus.text = "已开启"
            binding.tvNotificationStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.chart_line))
        } else {
            binding.ivNotificationStatus.setImageResource(R.drawable.ic_warning)
            binding.ivNotificationStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.error_red))
            binding.tvNotificationStatus.text = "需要开启"
            binding.tvNotificationStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red))
        }
    }

    /**
     * 更新电池优化状态
     */
    private fun updateBatteryOptimizationStatus() {
        val isOptimized = !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(requireContext())
        
        if (!isOptimized) {
            binding.ivBatteryStatus.setImageResource(R.drawable.ic_check_circle)
            binding.ivBatteryStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.chart_line))
            binding.tvBatteryStatus.text = "已关闭电池优化"
            binding.tvBatteryStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.chart_line))
        } else {
            binding.ivBatteryStatus.setImageResource(R.drawable.ic_battery)
            binding.ivBatteryStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.accent_orange))
            binding.tvBatteryStatus.text = "建议关闭电池优化"
            binding.tvBatteryStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_orange))
        }
    }

    /**
     * 更新自启动状态
     */
    private fun updateAutoStartStatus() {
        if (AutoStartHelper.isAutoStartRequired()) {
            val hasShownGuide = !AutoStartHelper.shouldShowAutoStartGuide(requireContext())
            
            if (hasShownGuide) {
                binding.ivAutoStartStatus.setImageResource(R.drawable.ic_check_circle)
                binding.ivAutoStartStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.chart_line))
                binding.tvAutoStartStatus.text = "已引导设置"
                binding.tvAutoStartStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.chart_line))
            } else {
                binding.ivAutoStartStatus.setImageResource(R.drawable.ic_restart)
                binding.ivAutoStartStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_blue))
                binding.tvAutoStartStatus.text = "建议开启自启动权限"
                binding.tvAutoStartStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))
            }
        } else {
            binding.ivAutoStartStatus.setImageResource(R.drawable.ic_check_circle)
            binding.ivAutoStartStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.chart_line))
            binding.tvAutoStartStatus.text = "无需额外设置"
            binding.tvAutoStartStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.chart_line))
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
     * 检查所有优化并启用提醒
     */
    private fun checkAllOptimizationsAndEnableReminder() {
        enableReminder()
        
        // 检查是否需要显示优化建议
        val needsBatteryOptimization = BatteryOptimizationHelper.shouldShowBatteryOptimizationDialog(requireContext())
        val needsAutoStartGuide = AutoStartHelper.shouldShowAutoStartGuide(requireContext())
        
        if (needsBatteryOptimization || needsAutoStartGuide) {
            showOptimizationRecommendations(needsBatteryOptimization, needsAutoStartGuide)
        }
    }

    /**
     * 显示优化建议
     */
    private fun showOptimizationRecommendations(needsBattery: Boolean, needsAutoStart: Boolean) {
        val recommendations = buildString {
            append("提醒已启用！为了确保通知的可靠性，建议完成以下设置：\n\n")
            
            if (needsBattery) {
                append("🔋 关闭电池优化\n")
            }
            
            if (needsAutoStart) {
                append("🔄 开启自启动权限 (${AutoStartHelper.getManufacturerName()})\n")
            }
            
            append("\n您可以在下方的「通知优化设置」中完成这些配置。")
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("通知优化建议")
            .setMessage(recommendations)
            .setPositiveButton("我知道了", null)
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
        
        Toast.makeText(requireContext(), "提醒已启用", Toast.LENGTH_SHORT).show()
        
        // 更新UI状态
        updateOptimizationCardVisibility()
        updateOptimizationStatus()
    }

    /**
     * 禁用提醒功能
     */
    private fun disableReminder() {
        preferenceManager.isReminderEnabled = false
        NotificationScheduler.cancelNotification(requireContext())
        Toast.makeText(requireContext(), "提醒已关闭", Toast.LENGTH_SHORT).show()
        
        // 更新UI状态
        updateOptimizationCardVisibility()
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
        if (!checkNotificationPermission()) {
            Toast.makeText(requireContext(), "请先开启通知权限", Toast.LENGTH_SHORT).show()
            return
        }
        
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
            // 已有权限，检查其他优化并启用提醒
            checkAllOptimizationsAndEnableReminder()
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
        // 页面恢复时重新检查所有状态
        if (preferenceManager.isReminderEnabled && !checkNotificationPermission()) {
            binding.switchReminder.isChecked = false
            preferenceManager.isReminderEnabled = false
        }
        
        updateOptimizationStatus()
        updateOptimizationCardVisibility()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 