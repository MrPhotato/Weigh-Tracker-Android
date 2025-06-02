package com.weighttracker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

object AutoStartHelper {

    /**
     * 检查是否可能需要自启动权限（基于厂商判断）
     */
    fun isAutoStartRequired(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when (manufacturer) {
            "xiaomi", "huawei", "oppo", "vivo", "oneplus", "meizu", "letv", "honor" -> true
            else -> false
        }
    }

    /**
     * 获取厂商名称
     */
    fun getManufacturerName(): String {
        return when (Build.MANUFACTURER.lowercase()) {
            "xiaomi" -> "小米"
            "huawei" -> "华为"
            "oppo" -> "OPPO"
            "vivo" -> "Vivo"
            "oneplus" -> "一加"
            "meizu" -> "魅族"
            "letv" -> "乐视"
            "honor" -> "荣耀"
            "samsung" -> "三星"
            else -> Build.MANUFACTURER
        }
    }

    /**
     * 显示自启动设置指导对话框
     */
    fun showAutoStartGuide(fragment: Fragment) {
        if (!isAutoStartRequired()) {
            return // 不需要自启动设置的厂商
        }

        val manufacturerName = getManufacturerName()
        val guide = getAutoStartGuide()
        
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("开启自启动权限")
            .setMessage("为了确保定时提醒在手机重启后能正常工作，建议开启自启动权限：\n\n$guide\n\n注意：不同版本的系统界面可能略有差异")
            .setPositiveButton("去设置") { _, _ ->
                openAutoStartSettings(fragment)
            }
            .setNegativeButton("稍后设置", null)
            .show()
    }

    /**
     * 获取厂商特定的自启动设置指南
     */
    private fun getAutoStartGuide(): String {
        return when (Build.MANUFACTURER.lowercase()) {
            "xiaomi" -> """
                MIUI系统：
                设置 → 应用设置 → 应用管理 → 体重追踪器 → 其他权限 → 自启动 → 开启
                
                或者：
                安全中心 → 应用管理 → 自启动管理 → 体重追踪器 → 开启
            """.trimIndent()
            
            "huawei" -> """
                EMUI/HarmonyOS系统：
                设置 → 应用 → 应用启动管理 → 体重追踪器 → 手动管理
                → 开启 自启动、关联启动、后台活动
            """.trimIndent()
            
            "oppo" -> """
                ColorOS系统：
                设置 → 电池 → 应用耗电管理 → 体重追踪器 → 允许自启动
                
                或者：
                手机管家 → 权限隐私 → 自启动管理 → 体重追踪器 → 开启
            """.trimIndent()
            
            "vivo" -> """
                FuntouchOS系统：
                i管家 → 应用管理 → 自启动 → 体重追踪器 → 开启
                
                或者：
                设置 → 电池 → 后台应用管理 → 体重追踪器 → 允许后台高耗电
            """.trimIndent()
            
            "oneplus" -> """
                OxygenOS系统：
                设置 → 电池 → 电池优化 → 体重追踪器 → 不优化
                
                或者：
                设置 → 应用管理 → 体重追踪器 → 电池 → 电池优化 → 不优化
            """.trimIndent()
            
            "meizu" -> """
                Flyme系统：
                手机管家 → 权限管理 → 后台管理 → 体重追踪器 → 允许后台运行
            """.trimIndent()
            
            else -> "请在系统设置中查找「自启动管理」或「应用启动管理」相关选项"
        }
    }

    /**
     * 尝试打开自启动设置页面
     */
    private fun openAutoStartSettings(fragment: Fragment) {
        val context = fragment.requireContext()
        val manufacturer = Build.MANUFACTURER.lowercase()
        
        try {
            val intent = when (manufacturer) {
                "xiaomi" -> {
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
                    }
                }
                "huawei", "honor" -> {
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                        )
                    }
                }
                "oppo" -> {
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                        )
                    }
                }
                "vivo" -> {
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                        )
                    }
                }
                "oneplus" -> {
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.oneplus.security",
                            "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                        )
                    }
                }
                else -> null
            }
            
            if (intent != null) {
                fragment.startActivity(intent)
            } else {
                // 如果没有特定的Intent，尝试打开应用详情页
                openAppDetailsSettings(fragment)
            }
        } catch (e: Exception) {
            // 如果特定设置页面打开失败，尝试应用详情页
            openAppDetailsSettings(fragment)
        }
    }

    /**
     * 打开应用详情设置页面
     */
    private fun openAppDetailsSettings(fragment: Fragment) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${fragment.requireContext().packageName}")
            }
            fragment.startActivity(intent)
        } catch (e: Exception) {
            // 最后的备选方案：打开应用管理页面
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                fragment.startActivity(intent)
            } catch (e2: Exception) {
                showManualGuideDialog(fragment)
            }
        }
    }

    /**
     * 显示手动设置指南对话框
     */
    private fun showManualGuideDialog(fragment: Fragment) {
        val guide = getAutoStartGuide()
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("手动设置指南")
            .setMessage("请按以下步骤手动设置：\n\n$guide")
            .setPositiveButton("我知道了", null)
            .show()
    }

    /**
     * 保存自启动引导显示状态
     */
    fun saveAutoStartGuideShown(context: Context) {
        val prefs = context.getSharedPreferences("AutoStartGuide", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("guide_shown", true).apply()
    }

    /**
     * 检查是否应该显示自启动引导
     */
    fun shouldShowAutoStartGuide(context: Context): Boolean {
        if (!isAutoStartRequired()) return false
        
        val prefs = context.getSharedPreferences("AutoStartGuide", Context.MODE_PRIVATE)
        return !prefs.getBoolean("guide_shown", false)
    }
} 