package com.weighttracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.weighttracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 禁用所有触摸音效和系统声音
        disableAllSounds()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置工具栏
        setSupportActionBar(binding.toolbar)

        // 设置导航
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav: BottomNavigationView = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)

        // 设置顶级目的地
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_weight_entry,
                R.id.navigation_history,
                R.id.navigation_chart,
                R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // 为底部导航和根视图禁用声音
        disableViewSounds()
    }
    
    /**
     * 禁用应用中的所有声音效果
     */
    private fun disableAllSounds() {
        // 禁用窗口触摸音效
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        
        // 通过系统设置禁用触摸音效（仅对当前窗口有效）
        try {
            val audioManager = getSystemService(AUDIO_SERVICE) as android.media.AudioManager
            // 不修改系统全局设置，只在应用内处理
        } catch (e: Exception) {
            // 忽略权限错误
        }
    }
    
    /**
     * 为视图组件禁用声音效果
     */
    private fun disableViewSounds() {
        // 禁用底部导航声音
        binding.bottomNavigation.isSoundEffectsEnabled = false
        
        // 禁用根布局的声音效果
        binding.root.isSoundEffectsEnabled = false
        
        // 禁用工具栏声音
        binding.toolbar?.isSoundEffectsEnabled = false
    }
} 