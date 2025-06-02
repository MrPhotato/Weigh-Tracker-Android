package com.weighttracker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

object BatteryOptimizationHelper {

    /**
     * 检查应用是否被电池优化白名单保护
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Android 6.0以下没有电池优化
        }
    }

    /**
     * 请求用户将应用加入电池优化白名单
     */
    fun requestIgnoreBatteryOptimizations(fragment: Fragment) {
        val context = fragment.requireContext()
        
        if (isIgnoringBatteryOptimizations(context)) {
            return // 已经在白名单中
        }

        showBatteryOptimizationDialog(fragment)
    }

    private fun showBatteryOptimizationDialog(fragment: Fragment) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("提高通知可靠性")
            .setMessage("为了确保定时提醒功能正常工作，建议将应用加入电池优化白名单。这样可以防止系统过度限制后台通知。")
            .setPositiveButton("去设置") { _, _ ->
                openBatteryOptimizationSettings(fragment)
            }
            .setNegativeButton("稍后") { _, _ ->
                // 用户选择稍后，可以在下次启动时再提醒
            }
            .setNeutralButton("不再提醒") { _, _ ->
                // 保存用户选择，不再提醒
                saveBatteryOptimizationChoice(fragment.requireContext(), false)
            }
            .show()
    }

    private fun openBatteryOptimizationSettings(fragment: Fragment) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${fragment.requireContext().packageName}")
                }
                fragment.startActivity(intent)
            }
        } catch (e: Exception) {
            // 如果直接跳转失败，尝试打开应用设置页面
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${fragment.requireContext().packageName}")
                }
                fragment.startActivity(intent)
            } catch (e2: Exception) {
                // 最后尝试打开通用电池优化设置
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    fragment.startActivity(intent)
                } catch (e3: Exception) {
                    // 如果都失败了，提示用户手动设置
                    showManualSettingsDialog(fragment)
                }
            }
        }
    }

    private fun showManualSettingsDialog(fragment: Fragment) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("手动设置指南")
            .setMessage("请手动进入系统设置 > 应用管理 > 体重追踪器 > 电池优化，选择「不优化」")
            .setPositiveButton("确定", null)
            .show()
    }

    private fun saveBatteryOptimizationChoice(context: Context, showAgain: Boolean) {
        val prefs = context.getSharedPreferences("BatteryOptimization", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("show_battery_optimization_dialog", showAgain).apply()
    }

    fun shouldShowBatteryOptimizationDialog(context: Context): Boolean {
        val prefs = context.getSharedPreferences("BatteryOptimization", Context.MODE_PRIVATE)
        return prefs.getBoolean("show_battery_optimization_dialog", true) && 
               !isIgnoringBatteryOptimizations(context)
    }

    /**
     * 获取厂商特定的电池优化设置指南
     */
    fun getManufacturerSpecificGuide(): String {
        return when (Build.MANUFACTURER.lowercase()) {
            "xiaomi" -> "MIUI: 设置 > 应用设置 > 应用管理 > 体重追踪器 > 省电策略 > 无限制"
            "huawei" -> "EMUI: 设置 > 应用 > 应用启动管理 > 体重追踪器 > 手动管理（开启自启动、关联启动、后台活动）"
            "oppo" -> "ColorOS: 设置 > 电池 > 应用耗电管理 > 体重追踪器 > 允许后台运行"
            "vivo" -> "FuntouchOS: 设置 > 电池 > 后台应用管理 > 体重追踪器 > 允许后台高耗电"
            "oneplus" -> "OxygenOS: 设置 > 电池 > 电池优化 > 体重追踪器 > 不优化"
            "samsung" -> "OneUI: 设置 > 应用程序 > 体重追踪器 > 电池 > 优化电池使用情况 > 关闭"
            else -> "请在系统设置中查找「电池优化」或「应用启动管理」相关选项"
        }
    }
} 